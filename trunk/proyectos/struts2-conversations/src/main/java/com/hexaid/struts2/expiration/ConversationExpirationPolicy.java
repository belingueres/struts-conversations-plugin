package com.hexaid.struts2.expiration;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 * 
 */
public interface ConversationExpirationPolicy extends Serializable {

	public void checkExpirations(	final ConcurrentMap<String, Conversation> allConversationsMap,
									final String conversationId,
									final long currentTime);

	public boolean isExpired(	final Conversation conversation,
								final Conversation requestedConversation,
								final long currentTime);

}
