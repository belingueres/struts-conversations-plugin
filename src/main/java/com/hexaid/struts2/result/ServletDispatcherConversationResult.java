package com.hexaid.struts2.result;

import org.apache.struts2.dispatcher.ServletDispatcherResult;

import com.hexaid.struts2.common.ConversationUtils;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * @author Gabriel Belingueres
 *
 */
public class ServletDispatcherConversationResult extends
		ServletDispatcherResult {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Result parameter that changes the conversation timeout to the specified
	 * value (measured in Seconds). (If the expiration policy is not "perview"
	 * the parameter will be ignored).
	 */
	private int maxInactiveInterval;

	public ServletDispatcherConversationResult() {
		super();
	}

	public ServletDispatcherConversationResult(String location) {
		super(location);
	}

	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		if (maxInactiveInterval > 0) {
			ConversationUtils.changeMaxInactiveInterval(maxInactiveInterval);
		}

		super.execute(invocation);
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

}
