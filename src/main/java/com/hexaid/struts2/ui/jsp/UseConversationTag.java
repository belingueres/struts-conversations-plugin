package com.hexaid.struts2.ui.jsp;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hexaid.struts2.common.ConversationUtils;
import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public class UseConversationTag extends SimpleTagSupport {
	
	final static private Logger LOG = LoggerFactory.getLogger(UseConversationTag.class); 

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 */
	@Override
	public void doTag() throws JspException, IOException {
		final PageContext jspContext = (PageContext) getJspContext();

		final HttpServletRequest request = (HttpServletRequest) jspContext.getRequest();
		final Conversation conversation = ConversationUtils.getCurrentConversation(request);
		
		if (conversation != null) {
			putConversationAttributesInPageContext(jspContext, conversation);
		}
	}

	private void putConversationAttributesInPageContext(final PageContext jspContext,
														final Conversation conversation) {
		final Map<String, Object> internalConversationMap = conversation.getMap();
		for(final Entry<String, Object> entry : internalConversationMap.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			// si encuentra un valor con el mismo nombre emite un WARNING
			if (jspContext.findAttribute(key) != null) {
				LOG.warn(
						"Page attribute '{}' in current page will be shadowed by same variable name from conversation id=[{}]", 
						key, conversation.getId());
			}
			
			LOG.debug("adding to pageContext {}={}", key, value);

			jspContext.setAttribute(key, value, PageContext.PAGE_SCOPE);
		}
	}

}
