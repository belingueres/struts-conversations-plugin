package com.hexaid.struts2.result;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.dispatcher.ServletRedirectResult;

import com.hexaid.struts2.common.ConversationUtils;
import com.hexaid.struts2.conversations.Conversation;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * @author Gabriel Belingueres
 *
 */
public class ServletRedirectConversationResult extends ServletRedirectResult {

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
	private String maxInactiveInterval;
	
	public ServletRedirectConversationResult() {
	}

	public ServletRedirectConversationResult(String location) {
		super(location);
	}

	public ServletRedirectConversationResult(String location, String anchor) {
		super(location, anchor);
	}

	public ServletRedirectConversationResult(String location, String anchor, String maxInactiveInterval) {
		super(location, anchor);
		this.maxInactiveInterval = maxInactiveInterval;
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

		int timeout = Integer.parseInt(maxInactiveInterval);
		if (timeout > 0) {
			ConversationUtils.changeMaxInactiveInterval(timeout);
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
		newList.add("maxInactiveInterval");
		return newList;
	}

	public String getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(String maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

}
