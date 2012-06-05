package com.hexaid.struts2.interceptor;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.junit.Config;
import com.hexaid.struts2.junit.StrutsBaseTestCase;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Gabriel Belingueres
 *
 */
@Config(file={"struts-plugin.xml", "struts-forebackexpiration.xml"})
public class TestConversationInterceptorForeBackConversationExpirationPolicyIntegration
		extends StrutsBaseTestCase {

	/**
	 * Test access an expired conversation
	 */
	@Test
	@Config(actionName="beginConvWithExpiration")
	public void testConversationLastAccessedTimeIsUpdated() {
		try {
			String result = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result);

			final String conversationId = "1";
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertTrue(conversation.getLastAccessTime() > 0);
			assertFalse(conversation.isEnded());
			
			long lastAccessTimeFirstRequest = conversation.getLastAccessTime();
			
			// wait some time
			Thread.sleep(100);
			
			// second request
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "mandatoryAction", null);
			
			String result2 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result2);
			
			// should not found
			Conversation conversation2 = getAllConversationsMap().get(conversationId);
			assertNotNull(conversation2);

			long lastAccessTimeSecondRequest = conversation2.getLastAccessTime();
			
			assertTrue(lastAccessTimeSecondRequest > lastAccessTimeFirstRequest);
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test access to a Foreground conversation when it should be expired if it were a 
	 * background one.
	 */
	@Test
	@Config(actionName="beginConvWithExpiration")
	public void testForegroundConversationNotExpired() {
		try {
			String result = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result);

			final String conversationId = "1";
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			
			// now FORCE the conversation to expire, by setting the lastAccessTime to 2 
			// minutes before
			conversation.setLastAccessTime(conversation.getLastAccessTime() - (120*1000));
			
			// second request
			request = new MockHttpServletRequest();
			request.setSession(session);

			// make it the foreground conversation
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "mandatoryAction", null);
			
			String result2 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result2);
			
			// should be found
			Conversation conversation2 = getAllConversationsMap().get(conversationId);
			assertNotNull(conversation2);
			assertFalse(conversation2.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test access to a Foreground conversation when it should be expired if it were a 
	 * background one, and test that expired background conversation is removed.
	 */
	@Test
	@Config(actionName="beginConvWithExpiration")
	public void testForegroundConversationNotExpiredButBackgroundExpired() {
		try {
			// first request, creating conversation id=1
			String result = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result);

			final String conversationId = "1";
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			
			// second request, creating conversation id=2
			request = new MockHttpServletRequest();
			request.setSession(session);

			createActionProxy(null, "beginConvWithExpiration", null);
			String result2 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result2);

			final String conversationId2 = "2";
			Conversation conversation2 = getAllConversationsMap().get(conversationId2);
			
			assertNotNull(conversation2);
			assertFalse(conversation2.isEnded());
			
			// now FORCE BOTH conversations to expire, by setting the lastAccessTime to 2 
			// minutes before
			conversation.setLastAccessTime(conversation.getLastAccessTime() - (120*1000));
			conversation2.setLastAccessTime(conversation.getLastAccessTime() - (120*1000));
			
			// third request
			request = new MockHttpServletRequest();
			request.setSession(session);

			// make it the foreground conversation
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "mandatoryAction", null);
			
			String result3 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result3);

			// only the foreground should remain
			assertEquals(1, getAllConversationsMap().size());
			
			// should be found
			Conversation conversation3 = getAllConversationsMap().get(conversationId);
			assertNotNull(conversation3);
			assertFalse(conversation3.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}
	
	/**
	 * Test that if I make a request with a non existent conversation, then the expiration
	 * of other conversation is NOT run
	 */
	@Test
	@Config(actionName="beginConvWithExpiration")
	public void testNotExpiredForegroundAfterRequestingAnExpiredConversation() {
		try {
			// first request, creating conversation id=1
			String result = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result);

			final String conversationId = "1";
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			
			// second request, creating conversation id=2
			request = new MockHttpServletRequest();
			request.setSession(session);

			createActionProxy(null, "beginConvWithExpiration", null);
			String result2 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result2);

			final String conversationId2 = "2";
			Conversation conversation2 = getAllConversationsMap().get(conversationId2);
			
			assertNotNull(conversation2);
			assertFalse(conversation2.isEnded());
			
			// now FORCE BOTH conversations to expire, by setting the lastAccessTime to 2 
			// minutes before
			conversation.setLastAccessTime(conversation.getLastAccessTime() - (120*1000));
			conversation2.setLastAccessTime(conversation2.getLastAccessTime() - (120*1000));
			
			// third request
			request = new MockHttpServletRequest();
			request.setSession(session);

			// make it the foreground conversation
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "mandatoryAction", null);
			
			String result3 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result3);

			// only the foreground should remain
			assertEquals(1, getAllConversationsMap().size());
			
			// should be found
			Conversation conversation3 = getAllConversationsMap().get(conversationId);
			assertNotNull(conversation3);
			assertFalse(conversation3.isEnded());
			
			// fourth request

			// now FORCE valid conversation to expire, by setting the lastAccessTime to 2 
			// minutes before
			conversation.setLastAccessTime(conversation.getLastAccessTime() - (120*1000));

			request = new MockHttpServletRequest();
			request.setSession(session);
			
			// make the expired conversation the foreground conversation
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId2);

			response = new MockHttpServletResponse();
			createActionProxy(null, "mandatoryAction", null);

			String result4 = actionProxy.execute();
			assertEquals(ConversationInterceptor.CONVERSATION_NOT_FOUND, result4);

			// the other conversation should remain, since the requested foreground 
			// conversation did not exist
			assertEquals(1, getAllConversationsMap().size());
			
			Conversation conversation4 = getAllConversationsMap().get(conversationId);
			assertNotNull(conversation4);
			assertFalse(conversation4.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}


	@SuppressWarnings("unchecked")
	private Map<String, Conversation> getAllConversationsMap() {
		return (Map<String, Conversation>) session.getAttribute(Conversation.CONVERSATIONS_MAP_KEY);
	}

}
