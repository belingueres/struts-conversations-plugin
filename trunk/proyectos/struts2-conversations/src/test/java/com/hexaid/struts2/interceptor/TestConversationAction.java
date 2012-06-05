package com.hexaid.struts2.interceptor;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Gabriel Belingueres
 *
 */
public class TestConversationAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	private int number = 0;

	@Override
	public String execute() throws Exception {
		setNumber(5);
		return SUCCESS;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

}
