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
import javax.persistence.PreRemove;
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
	
	private String author="";  //auto set to the people who create the project
	@Column(name="administrator", length= 1000)
	private String administrator; //the people will have have permission to edit/mng project
	
	private String title = ""; //show information of the project, like '2016 Marathon'
	@Column(name = "DESCRIPITON", length = 2000)
	private String  description="";    //include time, venue
	private String  deadline = "";
	private boolean multipleEntry = true; //whether one user will have multiple entry,  like kid@sap, one parent can register multiple children
	private String link = "";  //
	private String formDownloadUrl;
	private boolean displayProjectInfoAtTop = true;
	
	@Column(name = "FORM", length = 20000)
	private String form;   //the json of the register form
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedTime;

	//for approve and email
	private boolean needAprrove = false;  
	private boolean needEmailNotification = false; //depend on need approve
	private long maxNum = 0;  //maximun for number 
	private boolean allowCancel = false; 
	
	//email notification 
	String emailApproveSubject;
	@Column(name = "emailApproveBody", length = 1000)
	String emailApproveBody;
	
	String emailRejectSubject;
	@Column(name = "emailRejectBody", length = 1000)
	String emailRejectBody;
	String emailSignature;
	
	//security
	boolean projectPublic = true;  //others can see the definition of the project
	String registrationSecurity ;  //all employ can see the registration
	
	//some extra attribute for extension
	String extraAttr0, extraAttr1, extraAttr2,extraAttr3;
	
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
		UserInfo userInfo = UserMng.getCurrentUserInfo();
		String userId = userInfo.getUserId();
		modifiedTime = new Date();
		setAuthor(userId);
	}
	
	@PreUpdate
	public void onPreUpdate() {
		modifiedTime = new Date();
//		addAuthor();
	}
	
	//??
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
	/**
	 * @return the administrator
	 */
	public final String getAdministrator() {
		return administrator;
	}
	/**
	 * @param administrator the administrator to set
	 */
	public final void setAdministrator(String administrator) {
		this.administrator = administrator;
	}
	/**
	 * @return the formDownloadUrl
	 */
	public final String getFormDownloadUrl() {
		return formDownloadUrl;
	}
	/**
	 * @param formDownloadUrl the formDownloadUrl to set
	 */
	public final void setFormDownloadUrl(String formDownloadUrl) {
		this.formDownloadUrl = formDownloadUrl;
	}
	/**
	 * @return the needEmailNotification
	 */
	public final boolean isNeedEmailNotification() {
		return needEmailNotification;
	}
	/**
	 * @param needEmailNotification the needEmailNotification to set
	 */
	public final void setNeedEmailNotification(boolean needEmailNotification) {
		this.needEmailNotification = needEmailNotification;
	}
	/**
	 * @return the emailApproveSubject
	 */
	public final String getEmailApproveSubject() {
		return emailApproveSubject;
	}
	/**
	 * @param emailApproveSubject the emailApproveSubject to set
	 */
	public final void setEmailApproveSubject(String emailApproveSubject) {
		this.emailApproveSubject = emailApproveSubject;
	}
	/**
	 * @return the emailApproveBody
	 */
	public final String getEmailApproveBody() {
		return emailApproveBody;
	}
	/**
	 * @param emailApproveBody the emailApproveBody to set
	 */
	public final void setEmailApproveBody(String emailApproveBody) {
		this.emailApproveBody = emailApproveBody;
	}
	/**
	 * @return the emailRejectSubject
	 */
	public final String getEmailRejectSubject() {
		return emailRejectSubject;
	}
	/**
	 * @param emailRejectSubject the emailRejectSubject to set
	 */
	public final void setEmailRejectSubject(String emailRejectSubject) {
		this.emailRejectSubject = emailRejectSubject;
	}
	/**
	 * @return the emailRejectBody
	 */
	public final String getEmailRejectBody() {
		return emailRejectBody;
	}
	/**
	 * @param emailRejectBody the emailRejectBody to set
	 */
	public final void setEmailRejectBody(String emailRejectBody) {
		this.emailRejectBody = emailRejectBody;
	}
	/**
	 * @return the extraAttr0
	 */
	public final String getExtraAttr0() {
		return extraAttr0;
	}
	/**
	 * @param extraAttr0 the extraAttr0 to set
	 */
	public final void setExtraAttr0(String extraAttr0) {
		this.extraAttr0 = extraAttr0;
	}
	/**
	 * @return the extraAttr1
	 */
	public final String getExtraAttr1() {
		return extraAttr1;
	}
	/**
	 * @param extraAttr1 the extraAttr1 to set
	 */
	public final void setExtraAttr1(String extraAttr1) {
		this.extraAttr1 = extraAttr1;
	}
	/**
	 * @return the extraAttr2
	 */
	public final String getExtraAttr2() {
		return extraAttr2;
	}
	/**
	 * @param extraAttr2 the extraAttr2 to set
	 */
	public final void setExtraAttr2(String extraAttr2) {
		this.extraAttr2 = extraAttr2;
	}
	/**
	 * @return the extraAttr3
	 */
	public final String getExtraAttr3() {
		return extraAttr3;
	}
	/**
	 * @param extraAttr3 the extraAttr3 to set
	 */
	public final void setExtraAttr3(String extraAttr3) {
		this.extraAttr3 = extraAttr3;
	}
	/**
	 * @return the projectPublic
	 */
	public final boolean isProjectPublic() {
		return projectPublic;
	}
	/**
	 * @param projectPublic the projectPublic to set
	 */
	public final void setProjectPublic(boolean projectPublic) {
		this.projectPublic = projectPublic;
	}
	
	
	//we try to add the permisson check here
	@PreRemove
	public void onPreRemove() throws Exception {
		UserInfo user = UserMng.getCurrentUserInfo();
		//only the admin or the owner can delete  the project
		if ( ! user.isAdmin() && !user.getName().equals(author)) {
			throw new Exception("Only the admin or project owner can delete the project");
		}
	}
	/**
	 * @return the emailSignature
	 */
	public final String getEmailSignature() {
		return emailSignature;
	}
	/**
	 * @param emailSignature the emailSignature to set
	 */
	public final void setEmailSignature(String emailSignature) {
		this.emailSignature = emailSignature;
	}
	/**
	 * @return the registrationSecurity
	 */
	public final String getRegistrationSecurity() {
		return registrationSecurity;
	}
	/**
	 * @param registrationSecurity the registrationSecurity to set
	 */
	public final void setRegistrationSecurity(String registrationSecurity) {
		this.registrationSecurity = registrationSecurity;
	}
		
}

