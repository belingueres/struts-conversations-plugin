package com.hexaid.struts2.expiration.impl;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public class TestFixedTimeConversationExpirationPolicy {

	/**
	 * This is the conversationId request parameter, which is irrelevant for the
	 * FixedTimeConversationExpirationPolicy 
	 */
	private static final String CONVERSATION_ID_PARAM = "irrelevant id for FixedTimeConversationExpirationPolicy";

	/**
	 * This is the conversation requested as parameter, which is irrelevant for the
	 * FixedTimeConversationExpirationPolicy 
	 */
	private static final Conversation REQUESTED_CONVERSATION = new Conversation(CONVERSATION_ID_PARAM);

	
	private FixedTimeConversationExpirationPolicy policy;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		policy = new FixedTimeConversationExpirationPolicy("0");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		policy = null;
	}

	/**
	 * Test method for {@link com.hexaid.struts2.expiration.impl.FixedTimeConversationExpirationPolicy#checkExpirations(java.util.Map, long)}.
	 */
	@Test
	public void testCheckExpirations() {
		// expiration time 5 seconds
		policy.setMaxInactiveInterval(5);

		ConcurrentHashMap<String, Conversation> allConversationsMap = new ConcurrentHashMap<String, Conversation>();
		
		long creationTime = 1000L;

		Conversation c1 = new Conversation("1", creationTime);
		
		// created 5 seconds after the first
		Conversation c2 = new Conversation("2", creationTime + (5*1000));

		// created 10 seconds after the first
		Conversation c3 = new Conversation("3", creationTime + (10*1000));
		
		allConversationsMap.put("1", c1);
		allConversationsMap.put("2", c2);
		allConversationsMap.put("3", c3);

		// 12 seconds after the first conversation
		long currentTime = creationTime + (12*1000);
		
		policy.checkExpirations(allConversationsMap, CONVERSATION_ID_PARAM, currentTime);
		
		assertEquals(1, allConversationsMap.size());
		assertEquals(c3, allConversationsMap.values().iterator().next());
	}

	/**
	 * Test method for {@link com.hexaid.struts2.expiration.impl.FixedTimeConversationExpirationPolicy#checkExpirations(java.util.Map, long)}.
	 */
	@Test
	public void testCheckExpirationsWithEndedConversations() {
		// expiration time 5 seconds
		policy.setMaxInactiveInterval(5);

		ConcurrentHashMap<String, Conversation> allConversationsMap = new ConcurrentHashMap<String, Conversation>();
		
		long creationTime = 1000L;

		Conversation c1 = new Conversation("1", creationTime);
		c1.end(true);
		
		// created 5 seconds after the first
		Conversation c2 = new Conversation("2", creationTime + (5*1000));
		c2.end(false);

		// created 10 seconds after the first
		Conversation c3 = new Conversation("3", creationTime + (10*1000));
		c3.end(false);
		
		allConversationsMap.put("1", c1);
		allConversationsMap.put("2", c2);
		allConversationsMap.put("3", c3);

		// 12 seconds after the first conversation
		long currentTime = creationTime + (12*1000);
		
		policy.checkExpirations(allConversationsMap, CONVERSATION_ID_PARAM, currentTime);
		
		assertEquals(1, allConversationsMap.size());
		assertEquals(c3, allConversationsMap.values().iterator().next());
	}

	/**
	 * Test method for {@link com.hexaid.struts2.expiration.impl.FixedTimeConversationExpirationPolicy#isExpired(com.hexaid.struts2.conversations.Conversation, long)}.
	 */
	@Test
	public void testNeverExpiredWhenMaxInactiveTimeNonPositive() {
		long creationTime = 1000;
		Conversation conversation = new Conversation("1", creationTime);
		
		assertFalse(policy.isExpired(conversation, REQUESTED_CONVERSATION, 1001));
		assertFalse(policy.isExpired(conversation, REQUESTED_CONVERSATION, 10000000));
		assertFalse(policy.isExpired(conversation, REQUESTED_CONVERSATION, 100000000000L));
		assertFalse(policy.isExpired(conversation, REQUESTED_CONVERSATION, 10000000000000000L));
	}
	
	@Test
	public void testExpiredAfterMaxInactiveInterval() {
		// expiration time 60 seconds
		policy.setMaxInactiveInterval(60);
		
		long creationTime = System.currentTimeMillis();
		Conversation conversation = new Conversation("1", creationTime);
		
		// after 59 seconds
		long newAccessTime = creationTime + (59 * 1000);
		assertFalse(policy.isExpired(conversation, REQUESTED_CONVERSATION, newAccessTime));
		
		// after 60 seconds
		long newAccessTime2 = creationTime + (60 * 1000);
		assertFalse(policy.isExpired(conversation, REQUESTED_CONVERSATION, newAccessTime2));

		// after 61 seconds
		long newAccessTime3 = creationTime + (61 * 1000);
		assertTrue(policy.isExpired(conversation, REQUESTED_CONVERSATION, newAccessTime3));
	}

}
