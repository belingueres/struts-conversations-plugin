package com.hexaid.struts2.interceptor;

import com.hexaid.struts2.annotations.In;
import com.hexaid.struts2.annotations.Out;
import com.opensymphony.xwork2.ActionSupport;

public class TestInOutActionWithMethods extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
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

	@Out
	public String getMsg() {
		return msg;
	}

	@In
	public void setMsg(String msg) {
		this.msg = msg;
	}

}
