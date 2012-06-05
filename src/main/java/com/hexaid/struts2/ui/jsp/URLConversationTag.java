package com.hexaid.struts2.ui.jsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hexaid.struts2.common.ConversationUtils;
import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public class URLConversationTag extends org.apache.struts2.views.jsp.URLTag {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(URLConversationTag.class);
	
	/**
	 * defaults to "join"
	 */
	private String conversationPropagation = Conversation.CONVERSATION_PROPAGATION_JOIN;
	
	@Override
	protected void populateParams() {
		super.populateParams();
		
		final String convPropagValue = findString(conversationPropagation);
		
		if (Conversation.CONVERSATION_PROPAGATION_JOIN.equalsIgnoreCase(convPropagValue)) {
			final Conversation currentConversation = ConversationUtils.getCurrentConversation();
			if (currentConversation != null) {
				getComponent().addParameter(Conversation.CONVERSATION_ID_PARAM, currentConversation.getId());
			}
		}
		else if (!Conversation.CONVERSATION_PROPAGATION_NONE.equalsIgnoreCase(convPropagValue)) {
			LOG.warn("Unrecognized conversationPropagation attribute value '{}' in URL tag", convPropagValue);
		}
	}

	public void setConversationPropagation(String conversationPropagation) {
		this.conversationPropagation = conversationPropagation;
	}

}
