package com.hexaid.struts2.common;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 * @since 1.0
 */
public enum ScopeType {
	REQUEST {
		@Override
		public void put(final String name, final Object value) {
			HttpServletRequest request = ServletActionContext.getRequest();
			request.setAttribute(name, value);
		}

		@Override
		public Object get(String name) {
			HttpServletRequest request = ServletActionContext.getRequest();
			return request.getAttribute(name);
		}
	},
	CONVERSATION {
		@Override
		public void put(String name, Object value) {
			final HttpServletRequest request = ServletActionContext.getRequest();
			final Conversation conversation = ConversationUtils.getCurrentConversation(request);
			if (conversation != null && !conversation.isEnded()) {
				conversation.getMap().put(name, value);
			}
		}

		@Override
		public Object get(String name) {
			final HttpServletRequest request = ServletActionContext.getRequest();
			final Conversation conversation = ConversationUtils.getCurrentConversation(request);
			if (conversation == null || conversation.isEnded()) {
				return null;
			}
			return conversation.getMap().get(name);
		}
	},
	SESSION {
		@Override
		public void put(String name, Object value) {
			ServletActionContext.getContext().getSession().put(name, value);
		}

		@Override
		public Object get(String name) {
			return ServletActionContext.getContext().getSession().get(name);
		}
	},
	APPLICATION {
		@Override
		public void put(String name, Object value) {
			ServletActionContext.getContext().getApplication().put(name, value);
		}

		@Override
		public Object get(String name) {
			return ServletActionContext.getContext().getApplication().get(name);
		}
	},
	COOKIE {
		@Override
		public void put(String name, Object value) {
			if (value == null) {
				return;
			}
			
			final HttpServletResponse response = ServletActionContext.getResponse();
			final Cookie cookie;
			if (value instanceof Cookie) {
				cookie = (Cookie) value;
			}
			else {
				cookie = new Cookie(name, value.toString());
			}
			response.addCookie(cookie);
		}

		@Override
		public Object get(String name) {
			final HttpServletRequest request = ServletActionContext.getRequest();
			final Cookie[] cookies = request.getCookies();
			for(int i=0; i < cookies.length; ++i) {
				if (cookies[i].getName().equals(name)) {
					return cookies[i];
				}
			}
			return null;
		}
	};

	/**
	 * Saves a value into the scope, identified by its name.
	 * @param name the name of the object to save.
	 * @param value the state to save into the scope.
	 */
	public abstract void put(final String name, final Object value);
	
	/**
	 * Returns the value associated with name parameter stored in the scope.
	 * @param name the name of the object to return.
	 * @return the value of the object stored in the scope.
	 */
	public abstract Object get(final String name);
}
