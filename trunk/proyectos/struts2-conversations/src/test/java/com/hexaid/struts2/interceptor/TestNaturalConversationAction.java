package com.hexaid.struts2.interceptor;

import com.hexaid.struts2.annotations.Begin;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Gabriel Belingueres
 *
 */
public class TestNaturalConversationAction extends ActionSupport {
	
	private static final long serialVersionUID = 1L;

	// the natural conversation id
	private int id = 8;
	
	private Person person;

	@Override
	@Begin(naturalIdExpression="id")
	public String execute() throws Exception {
		return SUCCESS;
	}

	@Begin(initialization="loadPerson", naturalIdExpression="person.firstname")
	public String executeWithInit() {
		return SUCCESS;
	}

	@Begin(naturalIdExpression="person.firstname")
	public String executeWithNaturalIdNull() {
		return SUCCESS;
	}

	public void loadPerson() {
		person = new Person();
		person.setFirstname("Gabrielito");
	}
	
	public int getId() {
		return id;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	static class Person {
		private String firstname;

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}
	}

}
