package com.hexaid.struts2.common;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public class ConversationUtilsTest {

	private MockHttpServletRequest request;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		request = new MockHttpServletRequest();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		request = null;
	}

	/**
	 * Test when nothing is loaded
	 */
	@Test
	public void testGetCurrentConversationWithNothing() {
		Conversation currentConversation = ConversationUtils.getCurrentConversation(request);
		assertNull(currentConversation);
	}

	/**
	 * Test when a current conversation is in request scope
	 */
	@Test
	public void testGetCurrentConversationInRequest() {
		Conversation conversation = new Conversation();
		request.setAttribute(Conversation.CURRENT_CONVERSATION_KEY, conversation);
		
		Conversation currentConversation = ConversationUtils.getCurrentConversation(request);
		
		assertEquals(conversation, currentConversation);
	}

	/**
	 * Test when a the conversation id parameter is present, and is not in session
	 */
	@Test
	public void testGetCurrentConversationWithConversationIdAndNoConversationPresent() {
		String conversationId = "1";
		request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		
		Conversation currentConversation = ConversationUtils.getCurrentConversation(request);
		
		assertNull(currentConversation);
	}

	/**
	 * Test when a the conversation id parameter is present, and the map with all 
	 * conversation doesn't have it
	 */
	@Test
	public void testGetCurrentConversationWithConversationIdNotInAllConversationsMap() {
		String conversationId = "1";
		request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, new HashMap<String,Conversation>());
		
		request.setSession(session);
		
		Conversation currentConversation = ConversationUtils.getCurrentConversation(request);
		
		assertNull(currentConversation);
	}

	/**
	 * Test when a the conversation id parameter is present, and the map with all 
	 * conversation has it
	 */
	@Test
	public void testGetCurrentConversationWithConversationIdInAllConversationsMap() {
		String conversationId = "1";
		request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		
		Conversation conversation = new Conversation(conversationId);
		
		HashMap<String, Conversation> conversationsMap = new HashMap<String,Conversation>();
		conversationsMap.put(conversationId, conversation);

		MockHttpSession session = new MockHttpSession();
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		
		request.setSession(session);
		
		Conversation currentConversation = ConversationUtils.getCurrentConversation(request);
		
		assertEquals(conversation, currentConversation);
	}

}
