package com.hexaid.struts2.components;

import java.util.Map;

import org.apache.struts2.components.ServletUrlRenderer;
import org.apache.struts2.components.UrlProvider;

import com.hexaid.struts2.common.ConversationUtils;
import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public class ServletConversationUrlRenderer extends ServletUrlRenderer {
	
	private static final String CONVERSATION_PROPAGATION_PARAM = "conversationPropagation";

	@Override
	public void beforeRenderUrl(UrlProvider urlComponent) {
		final String conversationPropagation = urlComponent.findString(CONVERSATION_PROPAGATION_PARAM);
		if (conversationPropagation != null && "join".equalsIgnoreCase(conversationPropagation)) {
			final Conversation currentConversation = ConversationUtils.getCurrentConversation();
			if (currentConversation != null) {
				@SuppressWarnings("unchecked")
				Map<String, String> parameters = urlComponent.getParameters();
				parameters.put(Conversation.CONVERSATION_ID_PARAM, currentConversation.getId());
			}
			// do not include the "conversationPropagation" parameter in the final URL
			// a little bit disturbing it is not in the final URL, but since it is only
			// a control parameter it is OK
			//parameters.remove(CONVERSATION_PROPAGATION_PARAM);
		}
		super.beforeRenderUrl(urlComponent);
	}

}
