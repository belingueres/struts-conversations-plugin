package com.hexaid.struts2.persistence;

import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public interface PersistenceTransactionManager {

	public void init();

	public void conversationStarting(final Conversation conversation);
	
	public void conversationPaused(final Conversation conversation);
	
	public void conversationResumed(final Conversation conversation);
	
	public void conversationEnding(final Conversation conversation, final boolean commit);

	public void conversationEnded(final Conversation conversation);
	
	public void exceptionThrown(final Conversation conversation, final Exception exception);

}
