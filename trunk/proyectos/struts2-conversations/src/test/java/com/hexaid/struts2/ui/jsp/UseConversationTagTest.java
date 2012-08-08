package com.hexaid.struts2.ui.jsp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockPageContext;

import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public class UseConversationTagTest {

	private MockHttpSession session;
	private MockHttpServletRequest request;
	private MockPageContext pageContext;
	private UseConversationTag tag;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		session = new MockHttpSession();

		request = new MockHttpServletRequest();
		request.setSession(session);

		pageContext = new MockPageContext(null, request);

		tag = new UseConversationTag();
		tag.setJspContext(pageContext);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		tag = null;
		pageContext = null;
		request = null;
		session = null;
	}

	/**
	 * Test tag when conversation exists with some values in it.
	 */
	@Test
	public void testDoTagConversationExists() {
		String conversationId = "1";
		
		Conversation conversation = new Conversation();
		conversation.setId(conversationId);
		
		conversation.getMap().put("aString", "hello");
		Object anObject = new Object();
		conversation.getMap().put("anObject", anObject);

		HashMap<String, Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);

		request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		
		try {
			tag.doTag();

			// test result
			assertEquals("hello", pageContext.findAttribute("aString"));
			assertEquals(anObject, pageContext.findAttribute("anObject"));
		} catch (JspException e) {
			e.printStackTrace();
			fail("test failed!");
		} catch (IOException e) {
			e.printStackTrace();
			fail("test failed!");
		}
	}


	/**
	 * Test tag when conversation exists with some values in it, but the 
	 * conversation Id is not present.
	 */
	@Test
	public void testDoTagConversationExistsNoConversationIdParam() {
		String conversationId = "1";
		
		Conversation conversation = new Conversation();
		conversation.setId(conversationId);
		
		conversation.getMap().put("aString", "hello");
		Object anObject = new Object();
		conversation.getMap().put("anObject", anObject);

		HashMap<String, Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);

		//NO PARAMETER request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);

		try {
			tag.doTag();

			// test result
			assertNull(pageContext.findAttribute("aString"));
			assertNull(pageContext.findAttribute("anObject"));
		} catch (JspException e) {
			e.printStackTrace();
			fail("test failed!");
		} catch (IOException e) {
			e.printStackTrace();
			fail("test failed!");
		}
	}

	/**
	 * Test tag when conversation exists with some values in it, the 
	 * conversation Id is not present, BUT the "current Conversation"
	 * attribute is present
	 */
	@Test
	public void testDoTagConversationExistsNoConversationIdParamWithCurrentConversation() {
		String conversationId = "1";
		
		Conversation conversation = new Conversation();
		conversation.setId(conversationId);
		
		conversation.getMap().put("aString", "hello");
		Object anObject = new Object();
		conversation.getMap().put("anObject", anObject);

		HashMap<String, Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);

		//NO PARAMETER request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		
		// the current conversation is setted
		request.setAttribute(Conversation.CURRENT_CONVERSATION_KEY, conversation);

		try {
			tag.doTag();

			// test result
			assertEquals("hello", pageContext.findAttribute("aString"));
			assertEquals(anObject, pageContext.findAttribute("anObject"));
		} catch (JspException e) {
			e.printStackTrace();
			fail("test failed!");
		} catch (IOException e) {
			e.printStackTrace();
			fail("test failed!");
		}
	}

	/**
	 * Test tag when conversation not exists
	 */
	@Test
	public void testDoTagConversationNotExists() {
		HashMap<String, Conversation> conversationsMap = new HashMap<String, Conversation>();

		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);

		// any conversation id
		request.addParameter(Conversation.CONVERSATION_ID_PARAM, "blabla");

		try {
			tag.doTag();

			// test result

			List<String> names = new ArrayList<String>();
			for(@SuppressWarnings("rawtypes")
			Enumeration iter = pageContext.getAttributeNamesInScope(PageContext.PAGE_SCOPE);iter.hasMoreElements();) {
				String element = (String) iter.nextElement();
				names.add(element);
			}
			assertArrayEquals(new String[] {}, names.toArray());
		} catch (JspException e) {
			e.printStackTrace();
			fail("test failed!");
		} catch (IOException e) {
			e.printStackTrace();
			fail("test failed!");
		}
	}
	
	/**
	 * Test tag when conversation not exists and no conversationID parameter is present
	 */
	@Test
	public void testDoTagConversationNotExistsNoParam() {
		HashMap<String, Conversation> conversationsMap = new HashMap<String, Conversation>();

		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);

		// NO conversation id
		// request.addParameter(Conversation.CONVERSATION_ID_PARAM, "blabla");

		try {
			tag.doTag();

			// test result

			List<String> names = new ArrayList<String>();
			for(@SuppressWarnings("rawtypes")
			Enumeration iter = pageContext.getAttributeNamesInScope(PageContext.PAGE_SCOPE);iter.hasMoreElements();) {
				String element = (String) iter.nextElement();
				names.add(element);
			}
			assertArrayEquals(new String[] {}, names.toArray());
		} catch (JspException e) {
			e.printStackTrace();
			fail("test failed!");
		} catch (IOException e) {
			e.printStackTrace();
			fail("test failed!");
		}
	}

	/**
	 * Test tag when conversation exists with some value, but a previos pageContext variable is
	 * SHADOWED by the conversation data
	 */
	@Test
	public void testDoTagConversationExistsWithShadowedData() {
		String conversationId = "1";
		
		Conversation conversation = new Conversation();
		conversation.setId(conversationId);
		
		String shadowedKey = "aString";
		conversation.getMap().put(shadowedKey, "hello");
		Object anObject = new Object();
		conversation.getMap().put("anObject", anObject);

		HashMap<String, Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		
		session.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);

		// conversation id
		request.addParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		
		// a previously setted pageContext value
		pageContext.setAttribute(shadowedKey, "WILL BE SHADOWED!!");

		try {
			tag.doTag();

			// test result
			assertEquals("hello", pageContext.findAttribute(shadowedKey));
			assertEquals(anObject, pageContext.findAttribute("anObject"));
		} catch (JspException e) {
			e.printStackTrace();
			fail("test failed!");
		} catch (IOException e) {
			e.printStackTrace();
			fail("test failed!");
		}
	}
}
