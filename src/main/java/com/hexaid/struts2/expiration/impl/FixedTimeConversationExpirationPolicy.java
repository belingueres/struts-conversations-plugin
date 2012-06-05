package com.hexaid.struts2.expiration.impl;

import com.hexaid.struts2.conversations.Conversation;
import com.opensymphony.xwork2.inject.Inject;

/**
 * @author Gabriel Belingueres
 *
 */
public class FixedTimeConversationExpirationPolicy extends AbstractConversationExpirationPolicy {
	
	private static final long serialVersionUID = 1L;

	@Inject
	public FixedTimeConversationExpirationPolicy(	@Inject(value = "com.hexaid.struts2.conversation.expiration.maxInactiveInterval", required = false) String maxInactiveIntervalStr) {
		super(maxInactiveIntervalStr);
	}

	@Override
	public boolean isExpired(	final Conversation conversation,
								final Conversation requestedConversation,
								final long currentTime) {
		if (maxInactiveInterval > 0) {
			return (conversation.getLastAccessTime() + (maxInactiveInterval*1000)) < currentTime;
		}
		else {
			// never expires => expires only when the Session expires
			return false;
		}
	}
}
