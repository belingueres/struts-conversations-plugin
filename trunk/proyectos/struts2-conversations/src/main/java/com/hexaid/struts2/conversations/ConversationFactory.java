package com.hexaid.struts2.conversations;

import java.io.Serializable;

/**
 * @author Gabriel Belingueres
 *
 */
public interface ConversationFactory extends Serializable {
	
	/**
	 * Obtain a new synthetic Id for a conversation.
	 * @return the String with the newly generated id.
	 */
	String getNextConversationId();

	/**
	 * Create a new conversation with synthetic id
	 * @return the newly created conversation
	 */
	Conversation createConversation();

	/**
	 * Create a new conversation with a natural id.
	 * Note that the id is supplied to the conversation in
	 * a later phase of processing.
	 * @return the newly created conversation with a 
	 * natural id.
	 */
	Conversation createNaturalConversation();
}
