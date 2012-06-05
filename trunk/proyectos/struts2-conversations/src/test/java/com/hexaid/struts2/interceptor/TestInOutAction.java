package com.hexaid.struts2.interceptor;

import com.hexaid.struts2.annotations.In;
import com.hexaid.struts2.annotations.Out;
import com.opensymphony.xwork2.ActionSupport;

public class TestInOutAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	@In @Out
	private String msg;
	
	@Override
	public String execute() throws Exception {
		msg += "-Out";
		return SUCCESS;
	}
	
	public String executeAgain() throws Exception {
		msg += "-Out2";
		return SUCCESS;
	}

}
