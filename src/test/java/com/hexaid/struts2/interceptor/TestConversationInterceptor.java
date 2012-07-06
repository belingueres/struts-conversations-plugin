package com.hexaid.struts2.interceptor;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.hexaid.struts2.common.ConversationAttributeType;
import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.conversations.impl.ActionMessages;
import com.hexaid.struts2.junit.Config;
import com.hexaid.struts2.junit.StrutsBaseTestCase;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.interceptor.PreResultListener;

@Config(file={"struts-plugin.xml", "struts-conversations.xml"})
public class TestConversationInterceptor extends StrutsBaseTestCase {
	
	/**
	 * Tests that non conversation-aware actions are at least reached and executed
	 * by the interceptor.
	 */
	@Test
	@Config(actionName="test")
	public void testNormalActionReached() {
		try {
			actionProxy.execute();
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
		
		TestConversationAction action = (TestConversationAction) actionProxy.getAction();
		assertEquals(5, action.getNumber());
	}

	/**
	 * Test that a normal action can be recognized as conversation-aware if its execute
	 * method is annotated with @Begin 
	 */
	@Test
	@Config(actionName="testWithBeginAnnotation")
	public void testActionWithConversationUsingBeginAnnotation() {
		try {
			actionProxy.execute();
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
		
		TestActionWithBeginAnnotation action = (TestActionWithBeginAnnotation) actionProxy.getAction();
		assertTrue(action.wasExecuted);
		assertFalse(action.initExecuted);
	}
	
	/**
	 * Test that a normal action can be recognized as conversation-aware if its execute
	 * method is annotated with @Begin , and calls an specific initilization method
	 */
	@Test
	@Config(actionName="testWithBeginAnnotationInitMethod")
	public void testActionWithConversationUsingBeginAnnotationAndInitMethod() {
		try {
			actionProxy.execute();
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
		
		TestActionWithBeginAnnotation action = (TestActionWithBeginAnnotation) actionProxy.getAction();
		assertTrue(action.wasExecuted);
		assertTrue(action.initExecuted);
	}

	/**
	 * Test that a normal action can be recognized as conversation-aware if its execute
	 * method is annotated with @Begin , and calls an specific initilization method that
	 * does NOT EXIST => should throw a NoSuchMethodException
	 * @throws Exception 
	 */
	@Test(expected=NoSuchMethodException.class)
	@Config(actionName="testWithBeginAnnotationInitMethodNoExist")
	public void testActionWithConversationUsingBeginAnnotationAndInitMethodNotExist() throws Exception {
		actionProxy.execute();
	}
	
	/**
	 * Test execution of an action with a non existent mandatory conversation id
	 */
	@Test
	@Config(actionName="mandatoryAction")
	public void testConversationNoExists() {
		request.addParameter(Conversation.CONVERSATION_ID_PARAM, "blablabla");

		try {
			String result = actionProxy.execute();
			assertEquals(ConversationInterceptor.DEFAULT_CONVERSATION_NOT_FOUND_RESULT, result);
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

    /**
     * Test execution of an action with a non existent mandatory conversation id
     * but using a custom "conversationNotFoundResult" value
     */
    @Test
    @Config(actionName="mandatoryActionCustomResult")
    public void testConversationNoExistsWithCustomConversationNotFoundResult() {
        request.addParameter(Conversation.CONVERSATION_ID_PARAM, "blablabla");

        try {
            String result = actionProxy.execute();
            assertEquals("custom-noconversation-result", result);
        } catch (Exception e) {
            e.printStackTrace();
            fail("The action execution failed");
        }
    }

	/**
	 * Test overriding the defaultConversationAttr interceptor parameter from struts.xml
	 */
	@Test
	public void testOverrideInterceptorParam() {
		try {
			String result = actionProxy.execute();

			assertEquals(ActionSupport.SUCCESS, result);

			for(final InterceptorMapping im : actionProxy.getConfig().getInterceptors()) {
				if (im.getInterceptor() instanceof ConversationInterceptor) {
					final ConversationInterceptor interceptor = (ConversationInterceptor) im.getInterceptor();
					assertEquals(ConversationAttributeType.NONE, interceptor.getDefaultConversationAttr());
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test NOT overriding the defaultConversationAttr interceptor parameter from struts.xml
	 */
	@Test
	@Config(actionName="test")
	public void testUseDefaultConversationAttribute() {
		try {
			String result = actionProxy.execute();

			assertEquals(ActionSupport.SUCCESS, result);

			for(final InterceptorMapping im : actionProxy.getConfig().getInterceptors()) {
				if (im.getInterceptor() instanceof ConversationInterceptor) {
					final ConversationInterceptor interceptor = (ConversationInterceptor) im.getInterceptor();
					assertEquals(ConversationAttributeType.REQUIRED, interceptor.getDefaultConversationAttr());
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test ending a conversation with @End(beforeRedirect=true)
	 */
	@Test
	@Config(actionName="testWithBeginAnnotation")
	public void testEndConversationWithEndAnnotationBeforeRedirect() {
		try {
			String result = actionProxy.execute();

			assertEquals(ActionSupport.SUCCESS, result);

			TestActionWithBeginAnnotation action = (TestActionWithBeginAnnotation) actionProxy.getAction();
			assertTrue(action.wasExecuted);
			
			Map<String, Conversation> allConversations = getAllConversationsMap();
			assertNotNull(allConversations);

			String conversationId = "1";
			Conversation conversation = allConversations.get(conversationId);
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			assertFalse(conversation.isMarkedForDeletion());
			
			// second request, ending the conversation
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "endActionSuccess", null);
			
			String resultEnd = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, resultEnd);

			Map<String, Conversation> allConversations2 = getAllConversationsMap();
			assertNotNull(allConversations2);
			
			Conversation conversation2 = allConversations2.get(conversationId);
			assertNull(conversation2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test ending a conversation with @End(beforeRedirect=true) but method not returns SUCCESS
	 */
	@Test
	@Config(actionName="testWithBeginAnnotation")
	public void testEndConversationWithEndAnnotationBeforeRedirectNotSuccess() {
		try {
			String result = actionProxy.execute();

			assertEquals(ActionSupport.SUCCESS, result);

			TestActionWithBeginAnnotation action = (TestActionWithBeginAnnotation) actionProxy.getAction();
			assertTrue(action.wasExecuted);
			
			Map<String, Conversation> allConversations = getAllConversationsMap();
			assertNotNull(allConversations);

			String conversationId = "1";
			Conversation conversation = allConversations.get(conversationId);
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			assertFalse(conversation.isMarkedForDeletion());
			
			// second request, ending the conversation
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "endActionInput", null);
			
			String resultEnd = actionProxy.execute();
			assertEquals(ActionSupport.INPUT, resultEnd);

			Map<String, Conversation> allConversations2 = getAllConversationsMap();
			assertNotNull(allConversations2);
			
			Conversation conversation2 = allConversations2.get(conversationId);
			assertNotNull(conversation2);
			assertFalse(conversation2.isMarkedForDeletion());
			assertFalse(conversation2.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test ending a conversation with @End
	 */
	@Test
	@Config(actionName="testWithBeginAnnotation")
	public void testEndConversationWithEndAnnotationAfterRedirect() {
		try {
			String result = actionProxy.execute();

			assertEquals(ActionSupport.SUCCESS, result);

			TestActionWithBeginAnnotation action = (TestActionWithBeginAnnotation) actionProxy.getAction();
			assertTrue(action.wasExecuted);
			
			Map<String, Conversation> allConversations = getAllConversationsMap();
			assertNotNull(allConversations);

			String conversationId = "1";
			Conversation conversation = allConversations.get(conversationId);
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			assertFalse(conversation.isMarkedForDeletion());
			
			// second request, ending the conversation
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "endActionSuccessAfter", null);
			
			String result2 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result2);

			Map<String, Conversation> allConversations2 = getAllConversationsMap();
			assertNotNull(allConversations2);
			
			Conversation conversation2 = allConversations2.get(conversationId);
			assertNotNull(conversation2);
			assertFalse(conversation2.isMarkedForDeletion());
			assertTrue(conversation2.isEnded());
			
			// third request, the conversation is not found (but available if accessed directly by a JSP page)
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "mandatoryAction", null);
			
			String result3 = actionProxy.execute();
			assertEquals(ConversationInterceptor.DEFAULT_CONVERSATION_NOT_FOUND_RESULT, result3);
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test ending a conversation with @End, but the action throws an exception
	 */
	@Test
	@Config(actionName="testWithBeginAnnotation")
	public void testEndConversationWithEndAnnotationException() {
		try {
			String result = actionProxy.execute();

			assertEquals(ActionSupport.SUCCESS, result);

			TestActionWithBeginAnnotation action = (TestActionWithBeginAnnotation) actionProxy.getAction();
			assertTrue(action.wasExecuted);
			
			Map<String, Conversation> allConversations = getAllConversationsMap();
			assertNotNull(allConversations);

			String conversationId = "1";
			Conversation conversation = allConversations.get(conversationId);
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			assertFalse(conversation.isMarkedForDeletion());
			
			// second request, ending the conversation
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "endActionWithException", null);
			
			String result2 = actionProxy.execute();
			assertEquals(ActionSupport.ERROR, result2);

			Map<String, Conversation> allConversations2 = getAllConversationsMap();
			assertNotNull(allConversations2);
			
			Conversation conversation2 = allConversations2.get(conversationId);
			assertNotNull(conversation2);
			assertFalse(conversation2.isMarkedForDeletion());
			assertFalse(conversation2.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}
	
	/**
	 * Test that an ended conversation with Action messages survives a redirect
	 */
	@Test
	@Config(actionName="testWithBeginAnnotation")
	public void testEndAnnotationWithMessagesSurviveRedirect() {
		try {
			String result = actionProxy.execute();

			assertEquals(ActionSupport.SUCCESS, result);

			TestActionWithBeginAnnotation action = (TestActionWithBeginAnnotation) actionProxy.getAction();
			assertTrue(action.wasExecuted);
			
			Map<String, Conversation> allConversations = getAllConversationsMap();
			assertNotNull(allConversations);

			final String conversationId = "1";
			Conversation conversation = allConversations.get(conversationId);
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
			assertFalse(conversation.isMarkedForDeletion());
			
			// second request, ending the conversation
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
			
			response = new MockHttpServletResponse();
			createActionProxy(null, "endActionWithMessage", null);
			
			String result2 = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result2);

			Map<String, Conversation> allConversations2 = getAllConversationsMap();
			assertNotNull(allConversations2);
			
			Conversation conversation2 = allConversations2.get(conversationId);
			assertNotNull(conversation2);
			
			ActionMessages messages = (ActionMessages) conversation2.getMap().get(Conversation.CURRENT_CONVERSATION_MESSAGES_KEY);
			assertNotNull(messages);
			assertTrue(messages.hasActionMessages());
			assertArrayEquals(new Object[] {"a message"}, messages.getActionMessages().toArray());

			assertTrue(conversation2.isEnded());
			assertFalse(conversation2.isMarkedForDeletion());
			
			// third request, where the action messages must survive
			request = new MockHttpServletRequest();
			request.setSession(session);
			request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);

			response = new MockHttpServletResponse();
			createActionProxy(null, "executeNoConversation", null);

			// adds a pre result listener to check for the action messages BEFORE the request completes
			actionProxy.getInvocation().addPreResultListener(new PreResultListener() {
				@Override
				public void beforeResult(ActionInvocation invocation, String resultCode) {
					assertEquals(ActionSupport.SUCCESS, resultCode);

					Map<String, Conversation> allConversations3 = getAllConversationsMap();
					assertNotNull(allConversations3);

					Conversation conversation3 = allConversations3.get(conversationId);
					assertNotNull(conversation3);

					ActionMessages messagesAfterNewRequest = (ActionMessages) conversation3.getMap().get(Conversation.CURRENT_CONVERSATION_MESSAGES_KEY);
					assertNotNull(messagesAfterNewRequest);
					assertTrue(messagesAfterNewRequest.hasActionMessages());
					assertArrayEquals(new Object[] {"a message"}, messagesAfterNewRequest.getActionMessages().toArray());
				}
			});

			// executes the action
			actionProxy.execute();
			
			// conversation is removed
			assertNull(getAllConversationsMap().get(conversationId));
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}
	
	/**
	 * Test that a conversation with an action with @End(commitResult="commit") is
	 * ended if result is "commit"
	 */
	@Test
	public void testEndAnnotationWithCommitResult() {
		try {
			String result = actionProxy.execute();
			assertEquals("commit", result);

			final String conversationId = "1";
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertTrue(conversation.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}

	/**
	 * Test that a conversation with an action with @End(endResult="commit") is
	 * NOT ended if result is NOT "commit"
	 */
	@Test
	public void testEndAnnotationWithCommitResultNotEnded() {
		try {
			String result = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result);

			final String conversationId = "1";
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}
	
	/**
	 * Test the creation of a conversation with natural id
	 */
	@Test
	public void testConversationWithFixedNaturalId() {
		try {
			String result = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result);
			
			TestNaturalConversationAction action = (TestNaturalConversationAction) actionProxy.getAction();
			
			assertEquals(8, action.getId());

			// the conversation id is the defined by the action
			final String conversationId = Integer.toString(action.getId());
			
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}
	
	/**
	 * Test the creation of a conversation with natural id which exists only after
	 * executing the conversation's initialization method
	 */
	@Test
	public void testConversationWithNaturalIdFromInitializationMethod() {
		try {
			String result = actionProxy.execute();
			assertEquals(ActionSupport.SUCCESS, result);
			
			TestNaturalConversationAction action = (TestNaturalConversationAction) actionProxy.getAction();
			
			assertEquals("Gabrielito", action.getPerson().getFirstname());

			// the conversation id is the defined by the OGNL expression "person.firstname"
			final String conversationId = action.getPerson().getFirstname();
			
			Conversation conversation = getAllConversationsMap().get(conversationId);
			
			assertNotNull(conversation);
			assertFalse(conversation.isEnded());
		} catch (Exception e) {
			e.printStackTrace();
			fail("The action execution failed");
		}
	}
	
	/**
	 * Test that if the OGNL expression with the natural id resolves to null
	 * then an IllegalArgumentException is thrown.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testConversationWithNaturalIdNull() throws Exception {
		actionProxy.execute();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Conversation> getAllConversationsMap() {
		return (Map<String, Conversation>) session.getAttribute(Conversation.CONVERSATIONS_MAP_KEY);
	}
}
