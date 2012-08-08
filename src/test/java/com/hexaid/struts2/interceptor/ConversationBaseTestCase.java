package com.hexaid.struts2.interceptor;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.conversations.impl.ActionMessages;
import com.hexaid.struts2.junit.StrutsBaseTestCase;
import com.opensymphony.xwork2.ValidationAware;

/**
 * @author Gabriel Belingueres
 */
public class ConversationBaseTestCase extends StrutsBaseTestCase {

    public void changeConversationId(String fromId, String toId) {
        Map<String, Conversation> allConversationsMap = getAllConversationsMap();
        Conversation conversation = allConversationsMap.get(fromId);
        conversation.setId(toId);
        allConversationsMap.remove(fromId);
        allConversationsMap.put(toId, conversation);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Conversation> getAllConversationsMap() {
        return (Map<String, Conversation>) session.getAttribute(Conversation.CONVERSATIONS_MAP_KEY);
    }

    public void assertActionMessage(String expected,
                                    Conversation conversation,
                                    ValidationAware action) {
        ActionMessages actionMessages = 
                (ActionMessages) conversation.getMap().get(Conversation.CURRENT_CONVERSATION_MESSAGES_KEY);
        assertTrue(isStringPresent(expected, actionMessages.getActionMessages()));

        assertTrue(isStringPresent(expected, action.getActionMessages()));
    }

    public boolean isStringPresent(String expected, Collection<String> collection) {
        for (String message : collection) {
            if (expected.equals(message)) {
                return true;
            }
        }
        return false;
    }

}
