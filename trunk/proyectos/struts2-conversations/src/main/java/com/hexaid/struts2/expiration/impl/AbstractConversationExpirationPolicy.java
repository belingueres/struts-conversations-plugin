package com.hexaid.struts2.expiration.impl;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.expiration.ConversationExpirationPolicy;

/**
 * @author Gabriel Belingueres
 * 
 */
public abstract class AbstractConversationExpirationPolicy implements
		ConversationExpirationPolicy {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractConversationExpirationPolicy.class);

	/**
	 * Parameter:
	 * Time in seconds between requests before the framework will expire
	 * conversations.
	 * If non positive, it will NOT explicitly expire the conversations, 
	 * instead they will all timeout when the user Session does.
	 */
	protected int maxInactiveInterval = 0;
	
	protected AbstractConversationExpirationPolicy(final String maxInactiveIntervalStr) {
		this.maxInactiveInterval = Integer.parseInt(maxInactiveIntervalStr);
		LOG.info("maxInactiveInterval : {} seconds ({} minutes)",
				maxInactiveInterval, (maxInactiveInterval / 60));
	}

	@Override
	public void checkExpirations(	final ConcurrentMap<String, Conversation> allConversationsMap,
									final String conversationId,
									final long currentTime) {
		final Conversation requestedConversation = (conversationId == null) ? null : allConversationsMap.get(conversationId);
		for(final Entry<String, Conversation> entry : allConversationsMap.entrySet()) {
			final Conversation conversation = entry.getValue();
			if (isExpired(conversation, requestedConversation, currentTime)) {
				LOG.debug("Conversation id [{}] expired", conversation.getId());
				// thread-safe removal
				allConversationsMap.remove(conversation.getId(), conversation);
			}
		}
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

}
