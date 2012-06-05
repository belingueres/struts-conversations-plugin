package com.hexaid.struts2.conversations.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestActionMessages {

	private ActionMessages actionMessages;

	@Before
	public void setUp() throws Exception {
		actionMessages = new ActionMessages();
	}

	@After
	public void tearDown() throws Exception {
		actionMessages = null;
	}

	@Test
	public void testHasNotActionMessages() {
		assertFalse(actionMessages.hasActionMessages());
	}

	@Test
	public void testHasNotActionMessages2() {
		Collection<String> messages = new ArrayList<String>();
		actionMessages.setActionMessages(messages);
		assertFalse(actionMessages.hasActionMessages());
	}

	@Test
	public void testHasActionMessages() {
		Collection<String> messages = new ArrayList<String>();
		messages.add("hello");
		actionMessages.setActionMessages(messages);
		assertTrue(actionMessages.hasActionMessages());
		assertArrayEquals(messages.toArray(), actionMessages.getActionMessages().toArray());
	}

}
