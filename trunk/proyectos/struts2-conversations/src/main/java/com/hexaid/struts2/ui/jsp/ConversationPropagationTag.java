package com.hexaid.struts2.ui.jsp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.hexaid.struts2.common.ConversationUtils;
import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public class ConversationPropagationTag extends SimpleTagSupport {
	
	private String mode;
	
	@Override
	public void doTag() throws JspException, IOException {
		if (Conversation.CONVERSATION_PROPAGATION_JOIN.equalsIgnoreCase(mode)) {
			final PageContext jspContext = (PageContext) getJspContext();
	
			final HttpServletRequest request = (HttpServletRequest) jspContext.getRequest();
			final Conversation conversation = ConversationUtils.getCurrentConversation(request);
			
			if (conversation != null) {
				jspContext.getOut()
					.append("<input type=\"hidden\" name=\"")
					.append(Conversation.CONVERSATION_ID_PARAM)
					.append("\" value=\"")
					.append(conversation.getId())
					.append("\"/>\n");
			}
		}
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
