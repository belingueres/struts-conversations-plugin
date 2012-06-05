package com.hexaid.struts2.conversations;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Gabriel Belingueres
 *
 */
public class TestConversation {

	/**
	 * Test method for {@link com.hexaid.struts2.conversations.Conversation#isNew()}.
	 */
	@Test
	public void testIsNew() {
		Conversation conv = new Conversation();
		assertTrue("The conversation is not new", conv.isNew());
	}

	/**
	 * Test method for {@link com.hexaid.struts2.conversations.Conversation#isNew()}.
	 */
	@Test
	public void testEnded() {
		Conversation conv = new Conversation();
		assertFalse("The conversation is ended", conv.isEnded());
	}

	@Test
	public void testEndedAndAccessedAgain() {
		Conversation conv = new Conversation();
		assertFalse("The conversation is ended", conv.isEnded());
		
		conv.end(false);
		
		assertTrue(conv.isEnded());
		assertFalse(conv.isMarkedForDeletion());
		
		// accessed again in a new request
		conv.newRequestReceived();
		assertTrue(conv.isEnded());
		assertTrue(conv.isMarkedForDeletion());
	}

}
