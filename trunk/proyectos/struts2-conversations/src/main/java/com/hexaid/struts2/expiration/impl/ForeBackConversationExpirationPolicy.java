package com.hexaid.struts2.expiration.impl;

import com.hexaid.struts2.conversations.Conversation;
import com.opensymphony.xwork2.inject.Inject;

/**
 * @author Gabriel Belingueres
 *
 */
public class ForeBackConversationExpirationPolicy extends AbstractConversationExpirationPolicy {

	private static final long serialVersionUID = 1L;

	@Inject
	public ForeBackConversationExpirationPolicy(@Inject(value = "com.hexaid.struts2.conversation.expiration.maxInactiveInterval", required = false) String maxInactiveIntervalStr) {
		super(maxInactiveIntervalStr);
	}

	public boolean isExpired(	final Conversation conversation,
								final Conversation requestedConversation,
								final long currentTime) {
		final boolean isBackground = isBackgroundConversation(requestedConversation, conversation);
		if (isBackground && maxInactiveInterval > 0) {
			return (conversation.getLastAccessTime() + (maxInactiveInterval*1000)) < currentTime;
		}
		else {
			// the conversation parameter will not expire in this opportunity
			return false;
		}
	}

	protected boolean isBackgroundConversation(	final Conversation requestedConversation, final Conversation conversation) {
		return requestedConversation != null
				&& !requestedConversation.isEnded()
				&& conversation != requestedConversation;
	}

}
