package com.sap.csr.model;

import javax.persistence.Entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import com.sap.csr.odata.ServiceConstant;

@Entity(name="UserInfo")
public class UserInfo implements Serializable {
	
	@Id
    private String userId;
	
	private String firstName;
	private String lastName;
	private String email;
	private String name;
	private boolean admin=false;
	
	public UserInfo() {
		super();
	}
	
	public void setAdmin(boolean flag) {
		admin = flag;
	}
	public boolean isAdmin() {
		return admin;
	}
	
	public UserInfo(String userId, String email, String firstName, String lastName) {
		this.userId = userId;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getName() {
		return this.firstName + "," + this.lastName;
	}
	/**
	 * @return the userId
	 */
	public final String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public final void setUserId(String userId) {
		this.userId = userId;
	}
	public String toString() {
		return "Email:" + email + " userId:" + userId + " first: " + firstName + " last:" + lastName;
	}
}
