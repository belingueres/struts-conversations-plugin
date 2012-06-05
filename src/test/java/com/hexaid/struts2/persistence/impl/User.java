package com.hexaid.struts2.persistence.impl;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User class for Testing JPA related code
 * 
 * @author Gabriel Belingueres
 *
 */
@Entity
@Table(name="User")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;

	public User() {
	}

	public User(String username) {
		this.username = username;
	}

	@Id
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "User(" + username + ")";
	}
}
