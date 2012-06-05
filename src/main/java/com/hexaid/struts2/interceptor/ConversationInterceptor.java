package com.hexaid.struts2.interceptor;

import static com.hexaid.struts2.common.ConversationAttributeType.MANDATORY;
import static com.hexaid.struts2.common.ConversationAttributeType.NONE;
import static com.hexaid.struts2.common.ConversationAttributeType.REQUIRED;
import static com.hexaid.struts2.common.ConversationAttributeType.REQUIRES_NEW;
import static com.hexaid.struts2.common.ConversationAttributeType.SUPPORTS;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.hexaid.struts2.annotations.Begin;
import com.hexaid.struts2.annotations.ConversationControl;
import com.hexaid.struts2.annotations.End;
import com.hexaid.struts2.common.ConversationAttributeType;
import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.conversations.ConversationFactory;
import com.hexaid.struts2.conversations.impl.ActionMessages;
import com.hexaid.struts2.expiration.ConversationExpirationPolicy;
import com.hexaid.struts2.persistence.PersistenceTransactionManager;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;
import com.opensymphony.xwork2.util.ValueStack;

public class ConversationInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(ConversationInterceptor.class);

	public static final String CONVERSATION_CONTROL_PARAM = "conversationControl";

	public static final String CONVERSATION_NOT_FOUND = "conversation_not_found";

	private ConversationFactory conversationFactory;
	
	private transient PersistenceTransactionManager persistenceTransactionManager;
	
	private ConversationExpirationPolicy expirationPolicyManager;
	
	private Container container;

	/**
	 * Interceptor parameter:
	 * if no default control specified for this interceptor use REQUIRED
	 */
	private ConversationAttributeType defaultConversationAttr = REQUIRED;
	
	/**
	 * Interceptor parameter:
	 * Specifies the PersistenceTransactionManager to be used. Default is "default".
	 */
	private String persistence = Container.DEFAULT_NAME;
	
	/**
	 * Injected constant
	 * Specifies the conversation expiration policy.
	 */
	private String expirationPolicy = Container.DEFAULT_NAME;
	
	/**
	 * Injected constant
	 * Specifies the ConversationFactory to use. Default is "default".
	 */
	private String conversationFactoryStr = Container.DEFAULT_NAME;
	
	@Override
	public String intercept(final ActionInvocation invocation) throws Exception {
		final Object action = invocation.getAction();
		final HttpServletRequest request = ServletActionContext.getRequest();

		final String methodName = resolveMethodName(invocation);
		
		final Method actionMethod = getActionMethod(action, methodName);

		final String conversationId = request.getParameter(Conversation.CONVERSATION_ID_PARAM);

		final ConversationAttributeType conversationAttr = getConversationAttribute(action, actionMethod, conversationId);

		final boolean isActionWithConversationSupport = (conversationAttr != NONE);
		
		if (!isActionWithConversationSupport) {
			// process without conversation support
			LOG.debug("Invoking action method {}() without support for conversation", methodName);
			return invocation.invoke();
		}

		LOG.debug(
				"Invoking action method {}() with support for conversation. ConversationAttributeType: {}",
				methodName, conversationAttr);
		
		final HttpSession session = request.getSession();
		
		// there are no 2 requests on the same session which can operate from now on
		// and since the invocation.invoke() is inside the synchronized block too,
		// the succeeding interceptors will be synchronized too (ie bijection and others)
		// TODO: Implement something like Spring's WebUtils and HttpSessionMutexListener
		synchronized(session) {

			// Is there a ALL_CONVERSATIONS_MAP Map in the session? If not put one
			// there.
			final ConcurrentMap<String, Conversation> allConversationsMap = createAllConversationsMapIfNecessary(session);
	
			// remove expired conversations
			expirationPolicyManager.checkExpirations(allConversationsMap, conversationId, System.currentTimeMillis());
			
			final Begin beginAnnotation = actionMethod.getAnnotation(Begin.class);
			final boolean hasNaturalIdExpression = 
					beginAnnotation != null && !beginAnnotation.naturalIdExpression().isEmpty();
					
			final Conversation conversation = 
					conversationAttr.process(conversationFactory, allConversationsMap, conversationId, hasNaturalIdExpression);
	
			if (conversationAttr == MANDATORY && (conversation == null || conversation.isEnded())) {
				// was mandatory and conversation was not found =>
				// result CONVERSATION_NOT_FOUND without calling the action
				return CONVERSATION_NOT_FOUND;
			}

			try {
				// if it is new => initialize conversation and put it in the map
				if (conversation != null && conversation.isNew()) {
					persistenceTransactionManager.conversationStarting(conversation);
	
					initializeNewConversation(action, beginAnnotation, conversation, invocation.getStack());
					
					// save the conversation
					allConversationsMap.put(conversation.getId(), conversation);
				}
				else if (conversation != null) {
					// not a new conversation
					if (!conversation.isEnded()) {
						// update last accessed time when is not ended (and when not new)
						conversation.updateLastAccessTime();
					}
					persistenceTransactionManager.conversationResumed(conversation);
				}
				
				// sets the current conversation in request scope to allow custom tags to find
				// it when it just begins (because no conversationId parameter is present)
				request.setAttribute(Conversation.CURRENT_CONVERSATION_KEY, conversation);
				
				if (action instanceof ValidationAware && conversation != null) {
					// set action messages inside the conversation, if any, but ONLY
					// if the conversation is NOT ended, meaning that after ending the
					// conversation it can not longer CHANGE its contents (which is 
					// very sound too)
					invocation.addPreResultListener(new PreResultListener() {
						@Override
						public void beforeResult(ActionInvocation invocation, String resultCode) {
							saveActionMessages((ValidationAware) action, conversation);
						}
					});
				}
				
				// Invoke the next interceptor in the interceptor stack.
				final String result = invocation.invoke();
		
				// After action invocation
				if (conversation != null) {
					conversation.setNew(false);

					endConversation(actionMethod, conversation, result);
				}
		
				return result;
			}
			catch(Exception e) {
				persistenceTransactionManager.exceptionThrown(conversation, e);
				throw e;
			}
			finally {
				removeMarkedForDeletionConversations(allConversationsMap);
				
				// forces update on clustered environments??
				session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, allConversationsMap);
			}
		} // end synchronized block on the HttpSession object
	}

	private void initializeNewConversation(final Object action,
											final Begin beginAnnotation,
											final Conversation conversation,
											final ValueStack valueStack)
			throws Exception {
		final String initializationMethod = beginAnnotation.initialization();
		if (!initializationMethod.isEmpty()) {
			final Method initMethod = getActionMethod(action, initializationMethod);
			if (initMethod != null) {
				// the initialization method was found
				initMethod.invoke(action, new Object[]{});
			}
		}
		
		final String naturalIdExpression = beginAnnotation.naturalIdExpression();
		if (!naturalIdExpression.isEmpty()) {
			// a natural conversation => initialize its id
			final String naturalId = valueStack.findString(naturalIdExpression);
			if (StringUtils.isEmpty(naturalId)) {
				throw new IllegalArgumentException(
						"Illegal natural conversation Id [" + naturalId
								+ "] using expression ["
								+ naturalIdExpression
								+ "]");
			}
			LOG.debug(
					"Assigning natural conversation Id [{}] to conversation {} using expression [{}]", 
					new Object[] {naturalId, conversation, naturalIdExpression});
			conversation.setId(naturalId);
		}
	}

	private ConcurrentMap<String, Conversation> createAllConversationsMapIfNecessary(final HttpSession session) {
		@SuppressWarnings("unchecked")
		ConcurrentMap<String, Conversation> allConversationsMap = 
				(ConcurrentMap<String, Conversation>) session.getAttribute(Conversation.CONVERSATIONS_MAP_KEY);
		if (allConversationsMap == null) {
			allConversationsMap = new ConcurrentHashMap<String, Conversation>();
			session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, allConversationsMap);
		}
		return allConversationsMap;
	}

	private void saveActionMessages(final ValidationAware action, final Conversation conversation) {
		ActionMessages messages = 
				(ActionMessages) conversation.getMap().get(Conversation.CURRENT_CONVERSATION_MESSAGES_KEY);
		if (conversation.isEnded()) {
			if (messages.hasActionMessages()) {
				// copy messages to current action BEFORE rendering the page
				for(final String msg : messages.getActionMessages()) {
					action.addActionMessage(msg);
				}
			}
		}
		else {
			// if NOT ended
			if  (messages == null) {
				// salva este objeto que tiene SOLO mensajes (para que no se "cuelen" datos de un request previo)
				messages = new ActionMessages();
			}
			
			// whether or not they have messages (so previous messages stored in conversation are deleted)
			messages.setActionMessages(action.getActionMessages());
			
			LOG.debug("Putting action messages {} in conversation id [{}]", messages, conversation.getId());
			
			conversation.getMap().put(Conversation.CURRENT_CONVERSATION_MESSAGES_KEY, messages);
		}
	}

	private void removeMarkedForDeletionConversations(final Map<String, Conversation> allConversationsMap) {
		for(final Conversation conversation : allConversationsMap.values()) {
			if (conversation.isMarkedForDeletion()) {
				LOG.debug("Removing conversation id [{}] from allConversationsMap", conversation.getId());
				allConversationsMap.remove(conversation.getId());
			}
		}
	}

	private void endConversation(final Method actionMethod,
									final Conversation conversation,
									final String result) {
		if (conversation.isEnded()) {
			LOG.debug("The Ended conversation id [{}] has received a new request", conversation.getId());
			conversation.newRequestReceived();
		}
		else {
			// has an @End annotation?
			final End endAnnotation = actionMethod.getAnnotation(End.class);
			
			if (endAnnotation != null) {
				if (endAnnotation.endResult().equals(result)) {
					// may throw an exception on COMMIT of the transaction
					persistenceTransactionManager.conversationEnding(conversation, endAnnotation.commit());
	
					// end the conversation
					LOG.debug("The conversation id [{}] has Ended ({})", conversation.getId(), endAnnotation);
					conversation.end(endAnnotation.beforeRedirect());
				}
			}
			else {
				persistenceTransactionManager.conversationPaused(conversation);
			}
		}
	}
	
	private ConversationAttributeType getConversationAttribute(	final Object action,
																final Method actionMethod,
																final String conversationId) {
		// si tiene anotación @Begin, @End or @ConversationControl
		final Begin begin = actionMethod.getAnnotation(Begin.class);
		final End end = actionMethod.getAnnotation(End.class);
		final ConversationControl conversationControl = actionMethod.getAnnotation(ConversationControl.class);
		
		if (begin != null && conversationControl != null) {
			// @Begin and @ConversationControl, with or without @End
			final ConversationAttributeType conversationAttribute = conversationControl.value();
			if (conversationAttribute == NONE || conversationAttribute == SUPPORTS || conversationAttribute == MANDATORY) {
				final String message = 
						MessageFormatter.format("The action method {}() has declared the @Begin with @ConversationControl({})", 
								actionMethod.getName(), conversationAttribute.toString()).getMessage();
				throw new IllegalStateException(message);
			}
			
			return conversationAttribute;
		}
		else if (begin != null && end != null && conversationControl == null) {
			// @Begin, @End without @ConversationControl
			return REQUIRES_NEW;
		}
		else if (begin == null && end != null && conversationControl != null) {
			// @End and @ConversationControl, without @Begin
			final ConversationAttributeType conversationAttribute = conversationControl.value();
			if (conversationAttribute != MANDATORY) {
				final String message = 
						MessageFormatter.format("The action method {}() has declared the @End annotation, but with @ConversationControl({})", 
								actionMethod.getName(), conversationAttribute.toString()).getMessage();
				throw new IllegalStateException(message);
			}
			
			return MANDATORY;
		}
		else if (begin != null && end == null && conversationControl == null) {
			// @Begin only
			return REQUIRED;
		}
		else if (begin == null && end != null && conversationControl == null) {
			// @End only
			return MANDATORY;
		}
		else if (begin == null && end == null && conversationControl != null) {
			// @ConversationControl only
			return conversationControl.value();
		}
		else if (!StringUtils.isEmpty(conversationId)) {
			// conversationId parameter is present ONLY => assume SUPPORTS
			return SUPPORTS;
		}
		
		// anything else is NONE
		return NONE;
	}

	private Method getActionMethod(final Object action, String methodName) throws NoSuchMethodException {
		LOG.debug("clase: " + action.getClass().getName() + "  metodo: " + methodName);
		return action.getClass().getMethod(methodName, new Class<?>[]{});
	}

	private static String resolveMethodName(final ActionInvocation invocation) {
		String method = invocation.getProxy().getMethod();
		if (method == null) {
			return "execute";
		}
		return method; 
	}

	public ConversationAttributeType getDefaultConversationAttr() {
		return defaultConversationAttr;
	}

	public void setDefaultConversationAttr(ConversationAttributeType defaultConversationAttr) {
		this.defaultConversationAttr = defaultConversationAttr;
	}

	@Override
	public void init() {
		LOG.info("=== Initializing ConversationInterceptor ===");
		conversationFactory = container.getInstance(ConversationFactory.class, conversationFactoryStr);
		LOG.info(
				"Configured Conversation Factory \"{}\" with class: {}", conversationFactoryStr, conversationFactory.getClass().getName());

		persistenceTransactionManager = container.getInstance(PersistenceTransactionManager.class, persistence);
		LOG.info(
				"Configured persistence transaction manager \"{}\" with class: {}",
				persistence, persistenceTransactionManager.getClass().getName());
		persistenceTransactionManager.init();

		expirationPolicyManager = container.getInstance(ConversationExpirationPolicy.class, expirationPolicy);
		LOG.info(
				"Configured conversation expiration policy \"{}\" with class: {}",
				expirationPolicy, expirationPolicyManager.getClass().getName());
	}

	@Override
	public void destroy() {
		conversationFactory = null;
		persistenceTransactionManager = null;
		expirationPolicyManager = null;
		container = null;
	}

	public ConversationFactory getConversationFactory() {
		return conversationFactory;
	}

	public void setConversationFactory(ConversationFactory conversationFactory) {
		this.conversationFactory = conversationFactory;
	}

	public PersistenceTransactionManager getPersistenceTransactionManager() {
		return persistenceTransactionManager;
	}

	public void setPersistenceTransactionManager(	PersistenceTransactionManager persistenceTransactionManager) {
		this.persistenceTransactionManager = persistenceTransactionManager;
	}

	public String getPersistence() {
		return persistence;
	}

	public void setPersistence(String persistence) {
		this.persistence = persistence;
		LOG.debug("set Persistence to: {}", persistence);
	}
	
	@Inject
	public void setContainer(Container container) {
		this.container = container;
	}

	public String getExpirationPolicy() {
		return expirationPolicy;
	}

	@Inject(value="com.hexaid.struts2.conversation.expiration.policy")
	public void setExpirationPolicy(String expirationPolicy) {
		this.expirationPolicy = expirationPolicy;
	}

	public ConversationExpirationPolicy getExpirationPolicyManager() {
		return expirationPolicyManager;
	}

	public void setExpirationPolicyManager(ConversationExpirationPolicy expirationPolicyManager) {
		this.expirationPolicyManager = expirationPolicyManager;
	}

	public String getConversationFactoryStr() {
		return conversationFactoryStr;
	}

	@Inject(value="com.hexaid.struts2.conversation.factory")
	public void setConversationFactoryStr(String conversationFactoryStr) {
		this.conversationFactoryStr = conversationFactoryStr;
	}

}
