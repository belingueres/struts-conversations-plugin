package com.hexaid.struts2.interceptor;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.junit.Config;
import com.hexaid.struts2.junit.StrutsBaseTestCase;

/**
 * @author Gabriel Belingueres
 *
 */
@Config(file={"struts-plugin.xml", "struts.xml"})
public class TestBijectionInterceptorComplete extends StrutsBaseTestCase {

	/**
	 * Test bijection with annotations in property
	 */
	@Test
	@Config(actionName="test")
	public void testInterceptAtInAtOutAtConversationScopeConversation() throws Exception {
		// prepare a new empty conversation
		String conversationId = "1234";
		Conversation conversation = new Conversation(conversationId);
		conversation.getMap().put("msg", "In");
		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);

		actionProxy.execute();

		String outjected = (String) conversation.getMap().get("msg");
		
		assertEquals("El valor outectado es incorrecto", "In-Out", outjected);

		// nuevo request con el mismo id de conversación
		createActionProxy(null, "testAgain", "executeAgain");

		actionProxy.execute();

		String outjected2 = (String) conversation.getMap().get("msg");
		
		assertEquals("El valor outectado es incorrecto", "In-Out-Out2", outjected2);
	}

	/**
	 * Test bijection with annotations in getter/setters
	 */
	@Test
	@Config(actionName="testMethods")
	public void testInterceptAtInAtOutAtConversationScopeConversationWithMethods() throws Exception {
		// prepare a new empty conversation
		String conversationId = "1234";
		Conversation conversation = new Conversation(conversationId);
		conversation.getMap().put("msg", "In");
		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);

		actionProxy.execute();

		String outjected = (String) conversation.getMap().get("msg");
		
		assertEquals("El valor outectado es incorrecto", "In-Out", outjected);

		// nuevo request con el mismo id de conversación
		createActionProxy(null, "testAgainMethods", "executeAgain");

		actionProxy.execute();

		String outjected2 = (String) conversation.getMap().get("msg");
		
		assertEquals("El valor outectado es incorrecto", "In-Out-Out2", outjected2);
	}

}
