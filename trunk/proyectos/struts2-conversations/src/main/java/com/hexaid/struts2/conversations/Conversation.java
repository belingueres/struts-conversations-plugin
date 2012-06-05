package com.hexaid.struts2.conversations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase central del plugin, que representa el objeto Conversación con el los
 * datos salvados en el contexto.
 * 
 * @author Gabriel Belingueres
 * @version 1.0
 */
public class Conversation implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String CONVERSATIONS_MAP_KEY = "com.hexaid.struts2.conversations.conversationsMap";
	public static final String CONVERSATION_ID_PARAM = "conversationId";
	public static final String CURRENT_CONVERSATION_KEY = "com.hexaid.struts2.conversations.currentConversation";
	public static final String CURRENT_CONVERSATION_MESSAGES_KEY = "com.hexaid.struts2.conversations.currentConversation.messages";
	
	public static final String CONVERSATION_PROPAGATION_JOIN = "join";
	public static final String CONVERSATION_PROPAGATION_NONE = "none";

	private String id;
	private Map<String, Object> map;
	private boolean isANewConversation;
	private boolean ended;
	
	/**
	 * The conversation timeout
	 */
	private int maxInactiveInterval;
	
	/**
	 * the last time the conversation was accessed (for timeout evaluation)
	 */
	private long lastAccessTime;
	
	/**
	 * maximum quantity of requests the conversation can be accessed after it was ended
	 * defaults to 1 (that is, like a flash scope)  
	 */
	private int maxRequestCountAfterEnded = 1;
	private int requestCountAfterEnded = 0;
	
	/**
	 * True if the conversation has a natural Id. False otherwise.
	 * This is an immutable field.
	 */
	final private boolean naturalId;

	public Conversation() {
		this(null);
	}

	public Conversation(final String id) {
		this(id, System.currentTimeMillis());
	}

	public Conversation(final String id, final long creationTime) {
		this(id, creationTime, false);
	}
	
	public Conversation(final String id, final long creationTime, final boolean naturalId) {
		this.id = id;
		this.map = new HashMap<String, Object>();
		this.isANewConversation = true;
		this.ended = false;
		this.lastAccessTime = creationTime;
		this.naturalId = naturalId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public void setNew(boolean isNew) {
		this.isANewConversation = isNew;
	}

	/**
	 * Don't use this method directly! Use {@link #end(boolean)} instead.
	 * @param ended true or false
	 */
	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public String getId() {
		return id;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public boolean isNew() {
		return isANewConversation;
	}

	public boolean isEnded() {
		return ended;
	}

	public void end(final boolean beforeRedirect) {
		this.ended = true;
		if (beforeRedirect) {
			// forces not to survive a single redirect
			maxRequestCountAfterEnded = 0;
		}
	}

	public boolean isMarkedForDeletion() {
		// > for safety....
		return requestCountAfterEnded >= maxRequestCountAfterEnded;
	}

	public int getMaxRequestCountAfterEnded() {
		return maxRequestCountAfterEnded;
	}

	public void setMaxRequestCountAfterEnded(int maxRequestCountAfterEnded) {
		this.maxRequestCountAfterEnded = maxRequestCountAfterEnded;
	}
	
	public void newRequestReceived() {
		if (ended) {
			++requestCountAfterEnded;
		}
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public void updateLastAccessTime() {
		this.lastAccessTime = System.currentTimeMillis();
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public boolean isNaturalId() {
		return naturalId;
	}

}
