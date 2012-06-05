package com.hexaid.struts2.conversations.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.conversations.ConversationFactory;
import com.opensymphony.xwork2.inject.Inject;

/**
 * @author Gabriel Belingueres
 * 
 */
public class DefaultConversationFactory implements ConversationFactory {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultConversationFactory.class);

	final private AtomicInteger conversationIdCounter = new AtomicInteger();
	
	/**
	 * Injected value
	 */
	private int maxInactiveIntervalConstant;

	@Override
	public String getNextConversationId() {
		return Integer.toString(conversationIdCounter.incrementAndGet());
	}

	@Override
	public Conversation createConversation() {
		final String id = getNextConversationId();
		LOG.debug("Creating new conversation id [{}]", id);
		final Conversation conversation = new Conversation(id);
		conversation.setMaxInactiveInterval(maxInactiveIntervalConstant);
		return conversation;
	}

	@Override
	public Conversation createNaturalConversation() {
		LOG.debug("Creating new natural conversation");
		final Conversation conversation = new Conversation(null, System.currentTimeMillis(), true);
		conversation.setMaxInactiveInterval(maxInactiveIntervalConstant);
		return conversation;
	}

	public int getMaxInactiveIntervalConstant() {
		return maxInactiveIntervalConstant;
	}

	@Inject(value = "com.hexaid.struts2.conversation.expiration.maxInactiveInterval")
	public void setMaxInactiveIntervalConstant(String maxInactiveIntervalConstantStr) {
		this.maxInactiveIntervalConstant = Integer.parseInt(maxInactiveIntervalConstantStr);
	}

}
