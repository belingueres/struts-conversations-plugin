package com.hexaid.struts2.interceptor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.junit.Config;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Gabriel Belingueres
 *
 */
@Config(file={"struts-plugin.xml", "struts-fixedtimeexpiration.xml"})
public class TestConversationInterceptorFixedTimeConversationExpirationPolicyIntegration
		extends ConversationBaseTestCase {

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
	 * Test access an expired conversation
	 */
	@Test
	@Config(actionName="beginConvWithExpiration")
	public void testConversationExpired() {
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
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "mandatoryAction", null);
			
			String result2 = actionProxy.execute();
			assertEquals(ConversationInterceptor.DEFAULT_CONVERSATION_NOT_FOUND_RESULT, result2);
			
			// should not found
			Conversation conversation2 = getAllConversationsMap().get(conversationId);
			assertNull(conversation2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

}
