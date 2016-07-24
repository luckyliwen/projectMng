package com.sap.csr.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sap.csr.odata.ServiceConstant;
import com.sap.csr.odata.UserMng;

//the porject management informaiton, mainly for project general information and form information
@Entity(name="Project")
@NamedQueries({ 
	
})

public class Project implements Serializable {
	@Id  @GeneratedValue
    private long projectId;
	
	private String author="";  //auto set all who did change
	
	private String title = ""; //show information of the project, like '2016 Marathon'
	@Column(name = "DESCRIPITON", length = 2000)
	private String  description="";    //include time, venue
	private String  deadline = "";
	private boolean multipleEntry = true; //whether one user will have multiple entry,  like kid@sap, one parent can register multiple children
	private String link = "";  //
	
	//for display
	private boolean needAprrove = false;  
	private long maxNum = -1;  //maximun for number 
	private boolean allowCancel = false; 
	private boolean displayProjectInfoAtTop = true;
	
	@Column(name = "FORM", length = 20000)
	private String form;   //the json of the register form
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedTime;
	/**
	 * @return the projectId
	 */
	public final long getProjectId() {
		return projectId;
	}
	/**
	 * @param projectId the projectId to set
	 */
	public final void setProjectId(long projectId) {
		this.projectId = projectId;
	}
	/**
	 * @return the title
	 */
	public final String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public final void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the deadline
	 */
	public final String getDeadline() {
		return deadline;
	}
	/**
	 * @param deadline the deadline to set
	 */
	public final void setDeadline(String deadline) {
		this.deadline = deadline;
	}
	/**
	 * @return the multipleEntry
	 */
	public final boolean isMultipleEntry() {
		return multipleEntry;
	}
	/**
	 * @param multipleEntry the multipleEntry to set
	 */
	public final void setMultipleEntry(boolean multipleEntry) {
		this.multipleEntry = multipleEntry;
	}
	/**
	 * @return the link
	 */
	public final String getLink() {
		return link;
	}
	/**
	 * @param link the link to set
	 */
	public final void setLink(String link) {
		this.link = link;
	}
	/**
	 * @return the needAprrove
	 */
	public final boolean isNeedAprrove() {
		return needAprrove;
	}
	/**
	 * @param needAprrove the needAprrove to set
	 */
	public final void setNeedAprrove(boolean needAprrove) {
		this.needAprrove = needAprrove;
	}
	/**
	 * @return the maxNum
	 */
	public final long getMaxNum() {
		return maxNum;
	}
	/**
	 * @param maxNum the maxNum to set
	 */
	public final void setMaxNum(long maxNum) {
		this.maxNum = maxNum;
	}
	/**
	 * @return the allowCancel
	 */
	public final boolean isAllowCancel() {
		return allowCancel;
	}
	/**
	 * @param allowCancel the allowCancel to set
	 */
	public final void setAllowCancel(boolean allowCancel) {
		this.allowCancel = allowCancel;
	}
	/**
	 * @return the form
	 */
	public final String getForm() {
		return form;
	}
	/**
	 * @param form the form to set
	 */
	public final void setForm(String form) {
		this.form = form;
	}
	/**
	 * @return the displayProjectInfoAtTop
	 */
	public final boolean isDisplayProjectInfoAtTop() {
		return displayProjectInfoAtTop;
	}
	/**
	 * @param displayProjectInfoAtTop the displayProjectInfoAtTop to set
	 */
	public final void setDisplayProjectInfoAtTop(boolean displayProjectInfoAtTop) {
		this.displayProjectInfoAtTop = displayProjectInfoAtTop;
	}
	/**
	 * @return the author
	 */
	public final String getAuthor() {
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public final void setAuthor(String author) {
		this.author = author;
	}
	
	@PrePersist
	public void onPrePersist() {
		modifiedTime = new Date();
		addAuthor();
	}
	
	@PreUpdate
	public void onPreUpdate() {
		modifiedTime = new Date();
		addAuthor();
	}
	
	public void addAuthor() {
		UserInfo userInfo = UserMng.getCurrentUserInfo();
		String userId = userInfo.getUserId();
		int pos = author.indexOf(userId);
		if (pos == -1) {
			if (author.isEmpty())
				author = userId;
			else
				author = author + "," + userId;
		} 
	}
	/**
	 * @return the modifiedTime
	 */
	public final Date getModifiedTime() {
		return modifiedTime;
	}
	/**
	 * @param modifiedTime the modifiedTime to set
	 */
	public final void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	
		
}

