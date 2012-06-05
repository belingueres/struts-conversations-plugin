package com.hexaid.struts2.common;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public final class ConversationUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConversationUtils.class);
	
	private ConversationUtils() {
		// do not instantiate
	}

	public static Conversation getCurrentConversation(final HttpServletRequest request) {
		Conversation conversation = (Conversation) 
				request.getAttribute(Conversation.CURRENT_CONVERSATION_KEY);
		if (conversation == null) {
			final String conversationId = request.getParameter(Conversation.CONVERSATION_ID_PARAM);
			if (StringUtils.isNotEmpty(conversationId)) {
				final HttpSession session = request.getSession();
				@SuppressWarnings("unchecked")
				final Map<String, Conversation> conversationMap = 
						(Map<String, Conversation>) session.getAttribute(Conversation.CONVERSATIONS_MAP_KEY);
				if (conversationMap != null) {
					conversation = conversationMap.get(conversationId);
				}
			}
		}
		return conversation;
	}
	
	public static Conversation getCurrentConversation() {
		final HttpServletRequest request = ServletActionContext.getRequest();
		return ConversationUtils.getCurrentConversation(request);
	}

	public static void changeMaxInactiveInterval(final int maxInactiveInterval) {
		final Conversation currentConversation = getCurrentConversation();
		if (currentConversation != null) {
			LOG.debug(
					"Changed the maxInactiveInterval to {} seconds to conversation id [{}]",
					maxInactiveInterval, currentConversation.getId());
			currentConversation.setMaxInactiveInterval(maxInactiveInterval);
		}
	}

}
