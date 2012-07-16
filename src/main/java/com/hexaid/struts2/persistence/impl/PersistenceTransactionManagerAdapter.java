package com.hexaid.struts2.persistence.impl;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.persistence.PersistenceTransactionManager;

/**
 * @author Gabriel Belingueres
 *
 */
public class PersistenceTransactionManagerAdapter implements
		PersistenceTransactionManager {

	@Override
	public void init() {
		// nothing
	}

	/* (non-Javadoc)
	 * @see com.hexaid.struts2.persistence.PersistenceTransactionManager#conversationStarting(com.hexaid.struts2.conversations.Conversation)
	 */
	@Override
	public void conversationStarting(Conversation conversation) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see com.hexaid.struts2.persistence.PersistenceTransactionManager#conversationPaused(com.hexaid.struts2.conversations.Conversation)
	 */
	@Override
	public void conversationPaused(Conversation conversation) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see com.hexaid.struts2.persistence.PersistenceTransactionManager#conversationResumed(com.hexaid.struts2.conversations.Conversation)
	 */
	@Override
	public void conversationResumed(Conversation conversation) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see com.hexaid.struts2.persistence.PersistenceTransactionManager#conversationEnding(com.hexaid.struts2.conversations.Conversation, boolean)
	 */
	@Override
	public void conversationEnding(Conversation conversation, boolean commit) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see com.hexaid.struts2.persistence.PersistenceTransactionManager#exceptionThrown(com.hexaid.struts2.conversations.Conversation, java.lang.Exception)
	 */
	@Override
	public void exceptionThrown(Conversation conversation, Exception exception) {
		// nothing
	}

}
