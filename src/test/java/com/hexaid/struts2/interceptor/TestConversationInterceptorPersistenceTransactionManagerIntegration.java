package com.hexaid.struts2.interceptor;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.junit.Config;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Gabriel Belingueres
 */
@Config(file = { "struts-plugin.xml", "struts-persistence.xml" })
public class TestConversationInterceptorPersistenceTransactionManagerIntegration extends
        ConversationBaseTestCase {

    /**
     * Test ending normally a conversation, with commit without errors
     */
    @Test
    @Config(actionName = "testWithBeginAnnotation")
    public void testEndConversationWithEndAnnotation() {
        try {
            String result = actionProxy.execute();

            assertEquals(ActionSupport.SUCCESS, result);

            TestActionWithBeginAnnotation action = 
                    (TestActionWithBeginAnnotation) actionProxy.getAction();
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
            createActionProxy(null, "endActionWithMessage", null);

            String resultEnd = actionProxy.execute();
            assertEquals(ActionSupport.SUCCESS, resultEnd);

            Map<String, Conversation> allConversations2 = getAllConversationsMap();
            assertNotNull(allConversations2);

            Conversation conversation2 = allConversations2.get(conversationId);
            assertNotNull(conversation2);

            TestActionWithBeginAnnotation action2 = 
                    (TestActionWithBeginAnnotation) actionProxy.getAction();

            assertActionMessage("a message", conversation2, action2);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("The action execution failed");
        }
    }

    /**
     * Test ending normally a conversation, with commit failed
     */
    @Test
    @Config(actionName = "testWithBeginAnnotation")
    public void testEndConversationWithEndAnnotationCommitException() {
        try {
            String result = actionProxy.execute();

            assertEquals(ActionSupport.SUCCESS, result);

            TestActionWithBeginAnnotation action = 
                    (TestActionWithBeginAnnotation) actionProxy.getAction();
            assertTrue(action.wasExecuted);

            Map<String, Conversation> allConversations = getAllConversationsMap();
            assertNotNull(allConversations);

            String conversationId = "1";
            Conversation conversation = allConversations.get(conversationId);
            assertNotNull(conversation);
            assertFalse(conversation.isEnded());
            assertFalse(conversation.isMarkedForDeletion());

            // Important: before second request, change the conversationId to
            // allow
            // for testing different PersistenceTransactionManager scenarios
            conversationId = "2";
            changeConversationId("1", conversationId);

            // second request, ending the conversation
            request = new MockHttpServletRequest();
            request.setSession(session);
            request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);

            response = new MockHttpServletResponse();
            createActionProxy(null, "endActionWithMessage", null);

            // commit fails with an exception!
            String resultEnd = actionProxy.execute();
            assertEquals("exception", resultEnd);

            Map<String, Conversation> allConversations2 = getAllConversationsMap();
            assertNotNull(allConversations2);

            Conversation conversation2 = allConversations2.get(conversationId);
            assertNotNull(conversation2);

            TestActionWithBeginAnnotation action2 = 
                    (TestActionWithBeginAnnotation) actionProxy.getAction();

            assertActionMessage("a message", conversation2, action2);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("The action execution failed");
        }
    }

}
