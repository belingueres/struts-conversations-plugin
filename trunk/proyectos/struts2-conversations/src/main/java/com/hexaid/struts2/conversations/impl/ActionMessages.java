package com.hexaid.struts2.conversations.impl;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Gabriel Belingueres
 *
 */
public class ActionMessages implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Collection<String> messages;

	public void setActionMessages(Collection<String> messages) {
		this.messages = messages;
	}

	public Collection<String> getActionMessages() {
		return messages;
	}

	public boolean hasActionMessages() {
		return messages != null && !messages.isEmpty();
	}
	
	public String toString() {
		return "ActionMessages:" + messages.toString();
	}

}
