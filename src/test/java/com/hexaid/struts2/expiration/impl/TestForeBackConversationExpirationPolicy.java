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
public class TestForeBackConversationExpirationPolicy {
	
	private ForeBackConversationExpirationPolicy policy;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		policy = new ForeBackConversationExpirationPolicy("0");
		// default: no parameter
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		policy = null;
	}

	/**
	 * when there is no conversationId request parameter, is not
	 * a background conversation
	 */
	@Test
	public void testIsBackgroundConversationWithNoConversationId() {
		Conversation conversation = new Conversation();
		conversation.setId("any");
		assertTrue(policy.isBackgroundConversation(conversation, null));
	}

	/**
	 * when there is conversationId request parameter, AND it is the same 
	 * conversation => is Foreground
	 */
	@Test
	public void testIsForegroundConversationWithSameConversationId() {
		String conversationId = "the same";
		Conversation conversation = new Conversation();
		conversation.setId(conversationId);
		assertFalse(policy.isBackgroundConversation(conversation, conversation));
	}

	/**
	 * when there is conversationId request parameter, but it is not the same 
	 * conversation => is Background
	 */
	@Test
	public void testIsBackgroundConversationWithDifferentConversationId() {
		String conversationId = "some conversationId";
		Conversation requestedConversation = new Conversation(conversationId);
		
		Conversation conversation = new Conversation();
		conversation.setId("a different conversationId");
		assertTrue(policy.isBackgroundConversation(requestedConversation, conversation));
	}

	/**
	 * Test when there is no foreground conversation, then there are not any background
	 * conversation, and no conversation should expire
	 */
	@Test
	public void testCheckExpirationsAllBackground() {
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

		// no request parameter
		String conversationId = null;
		
		policy.checkExpirations(allConversationsMap, conversationId, currentTime);
		
		assertEquals(3, allConversationsMap.size());
	}

	/**
	 * Test when there is one foreground conversation, then it should not expire
	 */
	@Test
	public void testCheckExpirationsOneInForeground() {
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

		// request parameter
		String conversationId = "1";
		
		policy.checkExpirations(allConversationsMap, conversationId, currentTime);
		
		assertEquals(2, allConversationsMap.size());
		
		Conversation foreConversation = allConversationsMap.get(conversationId);
		assertNotNull(foreConversation);
		assertFalse(foreConversation.isEnded());
	}

	/**
	 * Test that even if conversations are expired by time, if the current request
	 * has no foreground conversation, then none of them should expire
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

		// no request parameter
		String conversationId = null;

		policy.checkExpirations(allConversationsMap, conversationId, currentTime);
		
		assertEquals(3, allConversationsMap.size());
	}

	/**
	 * Test that if the foreground conversation is not ended, then all expired
	 * background conversations are removed
	 */
	@Test
	public void testCheckExpirationsWithEndedConversationsAndForegroundNotEnded() {
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

		// no request parameter
		String conversationId = "1";

		policy.checkExpirations(allConversationsMap, conversationId, currentTime);
		
		assertEquals(2, allConversationsMap.size());

		Conversation foreConversation = allConversationsMap.get(conversationId);
		assertNotNull(foreConversation);
		assertFalse(foreConversation.isEnded());
	}

	/**
	 * Test that if the foreground conversation is ended, then all expired
	 * background conversations are NOT removed
	 */
	@Test
	public void testCheckExpirationsWithEndedConversationsAndForegroundEnded() {
		// expiration time 5 seconds
		policy.setMaxInactiveInterval(5);

		ConcurrentHashMap<String, Conversation> allConversationsMap = new ConcurrentHashMap<String, Conversation>();
		
		long creationTime = 1000L;

		Conversation c1 = new Conversation("1", creationTime);
		// ended
		c1.end(true);
		
		// created 5 seconds after the first
		Conversation c2 = new Conversation("2", creationTime + (5*1000));

		// created 10 seconds after the first
		Conversation c3 = new Conversation("3", creationTime + (10*1000));
		
		allConversationsMap.put("1", c1);
		allConversationsMap.put("2", c2);
		allConversationsMap.put("3", c3);

		// 12 seconds after the first conversation
		long currentTime = creationTime + (12*1000);

		// no request parameter
		String conversationId = "1";

		policy.checkExpirations(allConversationsMap, conversationId, currentTime);
		
		assertEquals(3, allConversationsMap.size());

		Conversation foreConversation = allConversationsMap.get(conversationId);
		assertNotNull(foreConversation);
		assertTrue(foreConversation.isEnded());
	}

	@Test
	public void testNeverExpiredWhenMaxInactiveTimeNonPositive() {
		long creationTime = 1000;
		Conversation conversation = new Conversation("1", creationTime);
		
		// no request parameter
		Conversation requestedConversation = null;

		assertFalse(policy.isExpired(conversation, requestedConversation, 1001));
		assertFalse(policy.isExpired(conversation, requestedConversation, 10000000));
		assertFalse(policy.isExpired(conversation, requestedConversation, 100000000000L));
		assertFalse(policy.isExpired(conversation, requestedConversation, 10000000000000000L));
	}

	@Test
	public void testNeverExpiredWhenMaxInactiveTimeNonPositiveAndForeground() {
		long creationTime = 1000;
		Conversation conversation = new Conversation("1", creationTime);
		
		// request parameter
		Conversation requestedConversation = conversation;

		assertFalse(policy.isExpired(conversation, requestedConversation, 1001));
		assertFalse(policy.isExpired(conversation, requestedConversation, 10000000));
		assertFalse(policy.isExpired(conversation, requestedConversation, 100000000000L));
		assertFalse(policy.isExpired(conversation, requestedConversation, 10000000000000000L));
	}

	/**
	 * Test that no conversation should be expired if there is no foreground
	 * requested conversation.
	 */
	@Test
	public void testExpiredAfterMaxInactiveInterval() {
		// expiration time 60 seconds
		policy.setMaxInactiveInterval(60);
		
		long creationTime = System.currentTimeMillis();
		Conversation conversation = new Conversation("1", creationTime);

		// no request parameter
		Conversation requestedConversation = null;

		// after 59 seconds
		long newAccessTime = creationTime + (59 * 1000);
		assertFalse(policy.isExpired(conversation, requestedConversation, newAccessTime));
		
		// after 60 seconds
		long newAccessTime2 = creationTime + (60 * 1000);
		assertFalse(policy.isExpired(conversation, requestedConversation, newAccessTime2));

		// after 61 seconds
		long newAccessTime3 = creationTime + (61 * 1000);
		assertFalse(policy.isExpired(conversation, requestedConversation, newAccessTime3));
	}

	@Test
	public void testNotExpiredAfterMaxInactiveIntervalAndForeground() {
		// expiration time 60 seconds
		policy.setMaxInactiveInterval(60);
		
		long creationTime = System.currentTimeMillis();
		Conversation conversation = new Conversation("1", creationTime);

		// request parameter
		Conversation requestedConversation = conversation;

		// after 59 seconds
		long newAccessTime = creationTime + (59 * 1000);
		assertFalse(policy.isExpired(conversation, requestedConversation, newAccessTime));
		
		// after 60 seconds
		long newAccessTime2 = creationTime + (60 * 1000);
		assertFalse(policy.isExpired(conversation, requestedConversation, newAccessTime2));

		// after 61 seconds
		long newAccessTime3 = creationTime + (61 * 1000);
		assertFalse(policy.isExpired(conversation, requestedConversation, newAccessTime3));
	}
	
}
