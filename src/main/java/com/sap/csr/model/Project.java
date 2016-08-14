package com.sap.csr.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import javax.activation.DataSource;

import com.sap.csr.odata.EmailMessage;
import com.sap.csr.odata.EmailMng;
import com.sap.csr.odata.ICalendar;
import com.sap.csr.odata.JsonUtility;
import com.sap.csr.odata.ServiceConstant;
import com.sap.csr.odata.UserMng;

//the project management information, mainly for project general information and form information
@Entity(name="Project")
@NamedQueries({ 
	@NamedQuery(name=ServiceConstant.PROJECT_BY_ID, query="select P from Project p where p.projectId=:projectId"),
})

public class Project implements Serializable {
	@Id  @GeneratedValue
    private long projectId;
	
	private String owner = "";  //auto set to the people who create the project
	private String ownerEmail = "";
	
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

	//startDate, startTime, endDate, endTime, now just use one json format to store in order to easy adjust the format 
	private String  eventStartDateTime, eventEndDateTime, 
		regStartDateTime, regEndDateTime;
	
	private String location, status;
	
	//for approve and email
	private boolean needApprove = false;  
	private boolean needEmailNotification = true; //now all will send email as need save in calendar
	 
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
	
	//sub-project information
	private long registrationLimit = 0;  //register limit for registration:  -1 means sub-project, 0 means no limiation, >0 means normal limiation
	@Column(name = "subProjectInfo", length = 4000)
	String subProjectInfo;  //complex information
	String subProjectTitle; //title show as label for select sub-project

	transient List<Map<String, Object>> subProjectList;
//	@Column(name = "subProjectLimit", length = 1000)
//	String subProjectLimit;
	
	//!!following content need adjust
	public String createICalendarContent() {
		StringBuffer sb = new StringBuffer(100);
		sb.append("Welcome to join the event of " + title);
		return sb.toString();
	}
	
	public void sendFullRegistrationNotify(Registration reg) {
		//first create the subject, as it can be used as in body 
		String subject = "The registration of ";
		subject += title;
		if (reg.getSubProject()!= null && reg.getSubPrject().length()>0) {
			subject += " -- ";
			subject += reg.getSubPrject();
		}
		subject += " has reach the maximum number now!";
		
		StringBuffer sb = new StringBuffer(50);
		sb.append("Hello\r\n\t");
		sb.append(subject);
		sb.append("\r\nBest Regards");
		
		EmailMessage msg = new EmailMessage(ownerEmail, subject, sb.toString());
		EmailMng.sendEmail(msg);
	}
	
	/**
	 * 
	 * @param reg
	 * @param promoteFlag: true means promote one from waiting list successful
	 */
	public void sendCancelNotify(Registration reg, boolean promoteFlag) {
		//first create the subject, as it can be used as in body 
		String subject = "Registration of ";
		subject += title;
		if (reg.getSubProject()!= null && reg.getSubPrject().length()>0) {
			subject += " -- ";
			subject += reg.getSubPrject();
		}
		subject += " from ";
		subject += reg.getUserName();
		subject += " (ID:";
		subject += reg.getUserId();
		subject += ") ";
		
		subject += " has been canceled";
		
		StringBuffer sb = new StringBuffer(50);
		sb.append("Hello\r\n\t");
		sb.append(subject);
		if (promoteFlag) {
			sb.append("\tAnd it promoted one from the waiting list successfully");
		}
		sb.append("\r\nBest Regards");
		
		EmailMessage msg = new EmailMessage(ownerEmail, subject, sb.toString());
		EmailMng.sendEmail(msg);
	}
	
	/**
	 * Get the real value by the template name, now support Title, UserName
	 * @param name
	 * @param reg
	 * @return
	 */
	public String getTemplateValue(String name, Registration reg) {
		if (name.equals("Title")) {
			return title;
		} else {
			return reg.getTemplateValue(name);
		}
	}
	
	public String getEmailSubject(boolean success,Registration reg) {
		String subject;
		if (success) {
			subject = emailApproveSubject;
		} else {
			subject = emailRejectSubject;
		}
		return createEmailTemplateContent(subject, reg);
	}
	
	
	public String createEmailTemplateContent(String templateStr, Registration reg) {
		String template = templateStr;
		//??project owner need ensure the template have content
		if (template == null)
			return "";
		
		StringBuffer sb = new StringBuffer(200);
		//just replace all the ${ to corresponding value 
		while (true) {
			int pos = template.indexOf("${");
			if (pos == -1) {
				sb.append(template);
				break;
			} else {
				//find the next },  and 
				int endPos = template.indexOf("}", pos);
				if (endPos == -1) {
					sb.append(template);
					break;
				} else {
					//append previous part,  use value from Registration to replace, for the remain part then continue
					String matchName = template.substring(pos+2, endPos);
					
					sb.append(template.substring(0, pos));
					
					sb.append(  getTemplateValue(matchName, reg));
					
					template = template.substring(endPos+1);
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param reg
	 * @param success: true means approve/submit, otherwise means rejected
	 * 
	 *  * @return
	 */
	public String createEmailSubject(boolean success, Registration reg) {
		String subject = success ?  emailApproveSubject : emailRejectSubject;
		return createEmailTemplateContent(subject, reg);
	}
	
	
	/**
	 * 
	 * @param success : true means submitted or approved, just get the template
	 * @return
	 */
	public String createEmailBody(boolean success, Registration reg) {
		if (!needEmailNotification)
			return null;
		
//		EmailApproveSubject : "Congratulations, your registration ${Title} has been submitted successful",
//		EmailApproveBody: "Hello ${UserName},\r\n" +
//				"\tYour registration ${Title} has been submitted.",
//		EmailSignature: "Best Regards,\r\nSAP Labs China"
		//now only support ${Title}  ${UserName}
		String body = null;
		if ( success) {
			body = emailApproveBody;	
		} else {
			body = emailRejectBody;
		}
		String bodyPart = createEmailTemplateContent(body, reg);
		return bodyPart + "\r\n" +  emailSignature;
	}
	
	public String  createICalendarContent(String subProject) throws Exception{
		ICalendar cal = new ICalendar();
		
		//depend on whether need get the sub-project or not, it will get different start/end time
		if ( subProject == null || subProject.length()==0) {
			cal.setDuration(  eventStartDateTime, eventEndDateTime);
			cal.setLocation(location);
			cal.setTitle(title);
			cal.setSubject("Event of " + title);
		} else {
			Map<String, Object> map = getSubProjectMap(subProject);
			if ( map != null) {
				String start = eventStartDateTime,  end = eventEndDateTime;
				String subStart = (String)map.get("startDateTime");
				if ( subStart != null && subStart.length()>0) {
					start = subStart;
				}
				String subEnd = (String)map.get("endDateTime");
				if ( subEnd != null && subEnd.length()>0) {
					end = subEnd;
				}
				cal.setDuration(  start, end);
				cal.setTitle(title);
				cal.setSubject("Event of " + title + " " + (String)map.get("info"));
				
				// location will be both project part and sub-project part
				String subLocation = (String)map.get("location");
				if ( subLocation!=null && subLocation.length()>0 ) {
					if ( location != null) {
						cal.setLocation(location + " " + subLocation);
					} else {
						cal.setLocation(subLocation);
					}
				}
			} 
		}
		
		
		//for the description of the iCalendar
		cal.setDescription( createICalendarContent());
		return cal.createStringContent();
	}
	
	public Map<String, Object> getSubProjectMap(String subProject) {
		if ( subProject == null || subProject.length()==0)
			return null;
		
		if (subProjectList == null) {
			try {
				subProjectList = JsonUtility.readMapArrayFromString(subProjectInfo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
		for (Map<String, Object> map : subProjectList) {
			String info = (String) map.get("info");
			if (info.equals(subProject)) {
				return map;
			}
		}
		return null;
	}
	
	/**
	 * Get the limit of the sub-project, 0 means no limitation
	 * @param subProject
	 * @return
	 */
	public long getLimitForSubProject(String subProject) throws Exception {
		if (subProjectList == null) {
			subProjectList = JsonUtility.readMapArrayFromString(subProjectInfo);
		}
		for (Map<String, Object> map : subProjectList) {
			String info = (String) map.get("info");
			if (info.equals(subProject)) {
				String limit = (String) map.get("limit");
				if (limit == null || limit.length() == 0)
					return 0;
				else
					return Long.parseLong(limit);
			}
		}
		return 0;
		/*	String []aInfo = subProjectInfo.split(";");
		String []aLimit = subProjectLimit.split(";");
		if ( aInfo.length != aLimit.length)
			throw new Exception("Project content wrong: subProjectInfo not consiste with subProjectLimit");
		
		int i=0;
		for ( String info: aInfo) {
			if ( subProject.equals(info)) {
				if ( aLimit[i].length() ==0)
					return 0;
				else 
					return Long.parseLong(aLimit[i]);
			}
			i++;
		}
		
		return 0;*/
	}
	
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
	public final boolean isNeedApprove() {
		return needApprove;
	}
	/**
	 * @param needAprrove the needAprrove to set
	 */
	public final void setNeedApprove(boolean needApprove) {
		this.needApprove = needApprove;
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
	 * @return the owner
	 */
	public final String getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public final void setOwner(String owner) {
		this.owner = owner;
	}
	
	@PrePersist
	public void onPrePersist() {
		UserInfo userInfo = UserMng.getCurrentUserInfo();
		String userId = userInfo.getUserId();
		modifiedTime = new Date();
		setOwner(userId);
		setOwnerEmail(userInfo.getEmail());
	}
	
	@PreUpdate
	public void onPreUpdate() {
		modifiedTime = new Date();
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
		if ( ! user.isAdmin() && !user.getName().equals(owner)) {
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
	/**
	 * @return the subProjectInfo
	 */
	public final String getSubProjectInfo() {
		return subProjectInfo;
	}
	/**
	 * @param subProjectInfo the subProjectInfo to set
	 */
	public final void setSubProjectInfo(String subProjectInfo) {
		this.subProjectInfo = subProjectInfo;
	}
//	/**
//	 * @return the subProjectLimit
//	 */
//	public final String getSubProjectLimit() {
//		return subProjectLimit;
//	}
//	/**
//	 * @param subProjectLimit the subProjectLimit to set
//	 */
//	public final void setSubProjectLimit(String subProjectLimit) {
//		this.subProjectLimit = subProjectLimit;
//	}
	/**
	 * @return the registrationLimit
	 */
	public final long getRegistrationLimit() {
		return registrationLimit;
	}
	/**
	 * @param registrationLimit the registrationLimit to set
	 */
	public final void setRegistrationLimit(long registrationLimit) {
		this.registrationLimit = registrationLimit;
	}

	
	/**
	 * @return the location
	 */
	public final String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public final void setLocation(String location) {
		this.location = location;
	}

	

	/**
	 * @return the subProjectTitle
	 */
	public final String getSubProjectTitle() {
		return subProjectTitle;
	}

	/**
	 * @param subProjectTitle the subProjectTitle to set
	 */
	public final void setSubProjectTitle(String subProjectTitle) {
		this.subProjectTitle = subProjectTitle;
	}

	/**
	 * @return the status
	 */
	public final String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public final void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the eventStartDateTime
	 */
	public final String getEventStartDateTime() {
		return eventStartDateTime;
	}

	/**
	 * @param eventStartDateTime the eventStartDateTime to set
	 */
	public final void setEventStartDateTime(String eventStartDateTime) {
		this.eventStartDateTime = eventStartDateTime;
	}

	/**
	 * @return the eventEndDateTime
	 */
	public final String getEventEndDateTime() {
		return eventEndDateTime;
	}

	/**
	 * @param eventEndDateTime the eventEndDateTime to set
	 */
	public final void setEventEndDateTime(String eventEndDateTime) {
		this.eventEndDateTime = eventEndDateTime;
	}

	/**
	 * @return the regStartDateTime
	 */
	public final String getRegStartDateTime() {
		return regStartDateTime;
	}

	/**
	 * @param regStartDateTime the regStartDateTime to set
	 */
	public final void setRegStartDateTime(String regStartDateTime) {
		this.regStartDateTime = regStartDateTime;
	}

	/**
	 * @return the regEndDateTime
	 */
	public final String getRegEndDateTime() {
		return regEndDateTime;
	}

	/**
	 * @param regEndDateTime the regEndDateTime to set
	 */
	public final void setRegEndDateTime(String regEndDateTime) {
		this.regEndDateTime = regEndDateTime;
	}

	/**
	 * @return the ownerEmail
	 */
	public final String getOwnerEmail() {
		return ownerEmail;
	}

	/**
	 * @param ownerEmail the ownerEmail to set
	 */
	public final void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
		
}

