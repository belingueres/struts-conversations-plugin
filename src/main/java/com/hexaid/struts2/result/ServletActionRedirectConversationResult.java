package com.hexaid.struts2.result;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.dispatcher.ServletActionRedirectResult;

import com.hexaid.struts2.common.ConversationUtils;
import com.hexaid.struts2.conversations.Conversation;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * @author Gabriel Belingueres
 * 
 */
public class ServletActionRedirectConversationResult extends
		ServletActionRedirectResult {

	private static final long serialVersionUID = 1L;

	/**
	 * default propagation is "join"
	 */
	private String conversationPropagation = Conversation.CONVERSATION_PROPAGATION_JOIN;

	/**
	 * Result parameter that changes the conversation timeout to the specified
	 * value (measured in Seconds). (If the expiration policy is not "perview"
	 * the parameter will be ignored).
	 */
	private int maxInactiveInterval;

	public ServletActionRedirectConversationResult() {
		super();
	}

	public ServletActionRedirectConversationResult(String namespace, String actionName, String method, String anchor) {
		super(namespace, actionName, method, anchor);
	}

	public ServletActionRedirectConversationResult(String namespace, String actionName, String method) {
		super(namespace, actionName, method);
	}

	public ServletActionRedirectConversationResult(String actionName, String method) {
		super(actionName, method);
	}

	public ServletActionRedirectConversationResult(String actionName) {
		super(actionName);
	}

	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		if (Conversation.CONVERSATION_PROPAGATION_JOIN.equalsIgnoreCase(conversationPropagation)) {
			final Conversation currentConversation = ConversationUtils.getCurrentConversation();
			if (currentConversation != null) {
				addParameter(Conversation.CONVERSATION_ID_PARAM, currentConversation.getId());
			}
		}
		else if (!Conversation.CONVERSATION_PROPAGATION_NONE.equalsIgnoreCase(conversationPropagation)) {
			throw new IllegalArgumentException("Unrecognized conversationPropagation. Valid values are: join, none");
		}

		super.execute(invocation);

		if (getMaxInactiveInterval() > 0) {
			ConversationUtils.changeMaxInactiveInterval(getMaxInactiveInterval());
		}
	}

	public void setConversationPropagation(String conversationPropagation) {
		this.conversationPropagation = conversationPropagation;
	}

	@Override
	protected List<String> getProhibitedResultParams() {
		final List<String> prohibitedResultParams = super.getProhibitedResultParams();
		List<String> newList = new ArrayList<String>(prohibitedResultParams);
		newList.add("conversationPropagation");
		return newList;
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

}
