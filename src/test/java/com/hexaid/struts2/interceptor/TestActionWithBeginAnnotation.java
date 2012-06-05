package com.hexaid.struts2.interceptor;

import java.sql.SQLException;

import com.hexaid.struts2.annotations.Begin;
import com.hexaid.struts2.annotations.ConversationControl;
import com.hexaid.struts2.annotations.End;
import com.hexaid.struts2.common.ConversationAttributeType;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Gabriel Belingueres
 * 
 */
public class TestActionWithBeginAnnotation extends ActionSupport {

	private static final long serialVersionUID = 1L;

	boolean wasExecuted = false;
	boolean initExecuted = false;

	@Begin
	@Override
	public String execute() throws Exception {
		wasExecuted = true;
		return SUCCESS;
	}
	
	@Begin(initialization="init")
	public String execWithInit() {
		wasExecuted = true;
		return SUCCESS;
	}
	
	public void init() {
		initExecuted = true;
	}

	@Begin(initialization="initMethodDontExist")
	public String execWithInitNoExist() {
		wasExecuted = true;
		return SUCCESS;
	}
	
	@End(beforeRedirect=true)
	public String endActionSuccess() {
		return SUCCESS;
	}

	@End(beforeRedirect=true)
	public String endActionInput() {
		return INPUT;
	}

	@End
	public String endActionSuccessAfter() {
		return SUCCESS;
	}

	@End
	public String endActionInputAfter() {
		return INPUT;
	}

	@End
	public String endActionWithException() throws SQLException {
		if ("a".equals("a"))
			throw new SQLException("test exception");
		return SUCCESS;
	}

	@End
	public String endActionWithMessage() {
		addActionMessage("a message");
		return SUCCESS;
	}

	@ConversationControl(ConversationAttributeType.MANDATORY)
	public String mandatoryAction() {
		return SUCCESS;
	}
	
	public String executeNoConversation() {
		return SUCCESS;
	}
	
	@Begin @End(endResult="commit")
	public String testEndAnnotationWithCommitResult() {
		return "commit";
	}

	@Begin @End(endResult="commit")
	public String testEndAnnotationWithCommitResultNotEnded() {
		return SUCCESS;
	}

}
