package com.sap.csr.model;

import java.util.Date;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceException;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Query;

import com.sap.csr.odata.ServiceConstant;
import com.sap.csr.odata.Util;  

@Entity(name = "Registration")
@NamedQueries({
@NamedQuery(name = ServiceConstant.REGISTRATION_BY_PROJECT_AND_USERID, 
	query = "select r from Registration r where r.projectId=:projectId and r.userId = :userId"),
@NamedQuery(name = ServiceConstant.REGISTRATION_FROM_WAITLIST,
	query="select r from Registration r where r.projectId = :projectId and r.status=com.sap.csr.model.Status.Waiting order by r.submittedTime"),
@NamedQuery(name = ServiceConstant.REGISTRATION_FROM_WAITLIST_SUBPROJECT,
query="select r from Registration r where r.projectId = :projectId and r.subProject=:subProject and r.status=com.sap.csr.model.Status.Waiting  order by r.submittedTime"),
})

public class Registration extends BaseModel implements ServiceConstant {
	transient Logger logger  = LoggerFactory.getLogger(Registration.class); 
	
	@Id
	@GeneratedValue
	private long registerId;

	@Id
	private long projectId;

	@Id
	@Column(name = "UERID", length = 10)
	private String userId; // employee id, get from IdP
	private String userName; // employee name, get from IdP
	private String subProject; // needed when it has sub-project limitation

	// for approve, calcel, reject
	// private String status ;
	@Enumerated(EnumType.STRING)
	private Status status;
	private String rejectReason;
	private String cancelReason;
	private int entriesCount; // multiple

	// some common information
	private String idOrPassort; // 身份证 ID or passport
	private String gender; // Male or Female
	private String phone;
	private String email;
	private int age;
	private String birthdate;
	private String name; // employee name or kid's name,
	private String nationality;
	private String department;
	private String tshirtSize;
	private String location;
	private String club;

	@Temporal(TemporalType.TIMESTAMP)
	private Date submittedTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedTime;

	// some extra attribute can set by user
	private String attr0, attr1, attr2, attr3, attr4, attr5, attr6, attr7, attr8, attr9;
	private String attr10, attr11, attr12, attr13, attr14, attr15, attr16, attr17, attr18, attr19;

	// now max support 5 attachment, here just store the file name so user know
	// upload which file, for the project setup it need
	// define the attachment type
	private String fileName0, fileName1, fileName2, fileName3, fileName4;

	public enum ActionFlag {
		Submit, Approve, Cancel, Others
	};

	// For the approve/submit we need check whether it exceed the limitation, so
	// we need know whether it is for the "Submit"/"Approve" action
	// or the normal update trigger by administrator, we use this flag to
	// identify it
	@Enumerated(EnumType.STRING)
	@Access(AccessType.FIELD)
	private ActionFlag actionFlag = ActionFlag.Others;

	// !!used to check the limitation of the registered project
	transient Project project;
	transient long registrationLimit; // the max limitation

	public Registration() {
		super();
	}

	public static Registration createNewRegistration(UserInfo userInfo) {
		Registration reg = new Registration();
		reg.setEmail(userInfo.getEmail());
		reg.setUserName(userInfo.getName());
		reg.setUserId(userInfo.getUserId());
		reg.setStatus(Status.New);
		return reg;
	}

	public void setSubmitModifyTime() {
		// here should ensure it only update once
		// if ( status.equals("Submitted")) {
		if (status == Status.Submitted) {
			if (submittedTime == null)
				submittedTime = new Date();
		}

		modifiedTime = new Date();
	}

	/**
	 * @return the modifiedTime
	 * @throws Exception
	 */
	@PreUpdate
	public void onPreUpdate() throws Exception {
		setSubmitModifyTime();
		checkLimitation();
		//reset flag to void affect next time check
		setActionFlag( ActionFlag.Others);
	}

	/**
	 * Get the Project from JPA by the projectId
	 */
	public void getProject() {
		if (project == null) {
			getEntityManager();
	
			TypedQuery query = em.createNamedQuery(PROJECT_BY_ID, Project.class);
			query.setParameter("projectId", projectId);
	
			project = (Project) query.getSingleResult();
		}
	}

	/**
	 * Only care about the registrationLimit, not considerate the needApprove
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean needCheckLimitation() throws Exception {
		getProject();

		long regLimit = project.getRegistrationLimit();
		if (regLimit == 0)
			return false;
		else if (regLimit == -1) {
			// sub-project, need check that
			registrationLimit = project.getLimitForSubProject(subProject);
			return registrationLimit > 0;
		} else {
			registrationLimit = project.getRegistrationLimit();
			return true;
		}
	}

	// ??need check whether need synchronozed
	public boolean isExceedLimitation() {
		getEntityManager();
		if (project.isNeedApprove()) {
			if (status != Status.Approved)
				return false;
		} else {
			if (status != Status.Submitted)
				return false;
		}

		Query query;
		Status checkStatus = project.isNeedApprove() ? Status.Approved : Status.Submitted;
		
		if (project.getRegistrationLimit() == -1) {
			// subproject
			String queryStr = "select count(r) from Registration r where r.projectId = :projectId and r.status=:status and r.subProject=:subProject";
			query = em.createQuery(queryStr);
			query.setParameter("subProject", subProject);
		} else {
			String queryStr = "select count(r) from Registration r where r.projectId = :projectId and r.status=:status ";
			query = em.createQuery(queryStr);
		}
		query.setParameter("status", checkStatus);
		query.setParameter("projectId", projectId);
		Long count = (Long) query.getSingleResult();

		if (count >= registrationLimit)
			return true;
		else
			return false;
	}

	@PreRemove
	public void onPreRemove() {
		// need update the entriesCount, so later can easy know the entries
		// which create by some user for one project
		getEntityManager();

		Query query = em.createNamedQuery(REGISTRATION_BY_PROJECT_AND_USERID, Registration.class);
		query.setParameter("userId", getUserId());
		query.setParameter("projectId", getProjectId());

		List<Registration> result = query.getResultList();
		if (result.size() > 1) {
			// for all existing entry, need update the entry count
			int newCount = result.size() - 1;

			// only need set for others as this entry will be delete now
			em.getTransaction().begin();
			for (Registration reg : result) {
				if (reg.getRegisterId() != getRegisterId())
					reg.setEntriesCount(newCount);
			}
			em.getTransaction().commit();
		}
		em.close();
	}
	
	/**
	 * Promote the head from registration waiting list if it is not empty 
	 */
	public void tryPromoteOneFromWaitingList() {
		getProject();
		
		if (project.isNeedApprove())
			return;

		TypedQuery<Registration> query;
		getEntityManager();

		if (project.getRegistrationLimit() == -1) {
			query = em.createNamedQuery(REGISTRATION_FROM_WAITLIST_SUBPROJECT,  Registration.class);
			query.setParameter("subProject", subProject);
		} else {
			query = em.createNamedQuery(REGISTRATION_FROM_WAITLIST,  Registration.class);
		}
		query.setParameter("projectId", projectId);
		query.setMaxResults(1);

		try {
			Registration reg = query.getSingleResult();
			// found one, directly change status,  not found, means waiting list is empty
			if (reg != null) {
				em.getTransaction().begin();
				//as it will trigger the PreUpdate, so here need set the flag to ignore the check
				reg.setActionFlag(ActionFlag.Others);
				reg.setStatus(Status.Submitted);
				// ??check send out email
				em.getTransaction().commit();
				em.close();
			}
		} catch (PersistenceException e) {
			//normal case, no need log
		}

	}

	public void checkLimitation() throws Exception {
		if (actionFlag == ActionFlag.Others) {
			return;
		} else if ( actionFlag == ActionFlag.Cancel ) {
			tryPromoteOneFromWaitingList();
			return;
		}

		// first get the project information, then from that to check
		if (!needCheckLimitation())
			return;

		// for cancel, need auto promote
		if (isExceedLimitation()) {
			if (project.isNeedApprove()) {
				// by throw exception, so the approve know exceed
				throw new Exception("Exceed registration limitation " + registrationLimit);
			} else {
				// change status to Waiting
				setStatus(Status.Waiting);
			}
		}
	}

	@PrePersist
	public void onPrePersist() throws Exception {
		Util.debug("^^onPrePersist {}", toString());

		setSubmitModifyTime();

		// need update the entriesCount, so later can easy know the entries
		// which create by some user for one project
		getEntityManager();

		Query query = em.createNamedQuery(REGISTRATION_BY_PROJECT_AND_USERID, Registration.class);
		query.setParameter("userId", getUserId());
		query.setParameter("projectId", getProjectId());

		List<Registration> result = query.getResultList();
		if (result.isEmpty()) {
			// only one
			setEntriesCount(1);
		} else {
			// for all existing entry, need update the entry count
			int newCount = 1 + result.size();
			setEntriesCount(newCount);

			em.getTransaction().begin();
			for (Registration reg : result) {
				//reset the acton flag to pass the limitation check
				reg.setActionFlag(ActionFlag.Others);
				reg.setEntriesCount(newCount);
			}
			em.getTransaction().commit();
		}
		em.close();

		if (status == Status.Submitted) {
			checkLimitation();
		}
		
		//clear flag to void affect next time logic
		setActionFlag( ActionFlag.Others);
	}

	/**
	 * @return the nationality
	 */
	public final String getNationality() {
		return nationality;
	}

	/**
	 * @param nationality
	 *            the nationality to set
	 */
	public final void setNationality(String nationality) {
		this.nationality = nationality;
	}

	/**
	 * @return the club
	 */
	public final String getClub() {
		return club;
	}

	/**
	 * @param club
	 *            the club to set
	 */
	public final void setClub(String club) {
		this.club = club;
	}

	/**
	 * @return the phone
	 */
	public final String getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 *            the phone to set
	 */
	public final void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the email
	 */
	public final String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public final void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the tshirtSize
	 */
	public final String getTshirtSize() {
		return tshirtSize;
	}

	/**
	 * @param tshirtSize
	 *            the tshirtSize to set
	 */
	public final void setTshirtSize(String tshirtSize) {
		this.tshirtSize = tshirtSize;
	}

	/**
	 * @return the submittedTime
	 */
	public final Date getSubmittedTime() {
		return submittedTime;
	}

	/**
	 * @param submittedTime
	 *            the submittedTime to set
	 */
	public final void setSubmittedTime(Date submittedTime) {
		this.submittedTime = submittedTime;
	}

	/**
	 * @return the modifiedTime
	 */
	public final Date getModifiedTime() {
		return modifiedTime;
	}

	/**
	 * @param modifiedTime
	 *            the modifiedTime to set
	 */
	public final void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	/**
	 * @return the rejectReason
	 */
	public final String getRejectReason() {
		return rejectReason;
	}

	/**
	 * @param rejectReason
	 *            the rejectReason to set
	 */
	public final void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}

	/**
	 * @return the cancelReason
	 */
	public final String getCancelReason() {
		return cancelReason;
	}

	/**
	 * @param cancelReason
	 *            the cancelReason to set
	 */
	public final void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	/**
	 * @return the age
	 */
	public final int getAge() {
		return age;
	}

	/**
	 * @param age
	 *            the age to set
	 */
	public final void setAge(int age) {
		this.age = age;
	}

	/**
	 * @return the department
	 */
	public final String getDepartment() {
		return department;
	}

	/**
	 * @param department
	 *            the department to set
	 */
	public final void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * @return the location
	 */
	public final String getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public final void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the registerId
	 */
	public final long getRegisterId() {
		return registerId;
	}

	/**
	 * @param registerId
	 *            the registerId to set
	 */
	public final void setRegisterId(long registerId) {
		this.registerId = registerId;
	}

	/**
	 * @return the projectId
	 */
	public final long getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public final void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the sapUserId
	 */
	public final String getUserId() {
		return userId;
	}

	/**
	 * @param sapUserId
	 *            the sapUserId to set
	 */
	public final void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the sapName
	 */
	public final String getUserName() {
		return userName;
	}

	/**
	 * @return the idOrPassort
	 */
	public final String getIdOrPassort() {
		return idOrPassort;
	}

	/**
	 * @param idOrPassort
	 *            the idOrPassort to set
	 */
	public final void setIdOrPassort(String idOrPassort) {
		this.idOrPassort = idOrPassort;
	}

	/**
	 * @return the gender
	 */
	public final String getGender() {
		return gender;
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public final void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the attr0
	 */
	public final String getAttr0() {
		return attr0;
	}

	/**
	 * @param attr0
	 *            the attr0 to set
	 */
	public final void setAttr0(String attr0) {
		this.attr0 = attr0;
	}

	/**
	 * @return the attr1
	 */
	public final String getAttr1() {
		return attr1;
	}

	/**
	 * @param attr1
	 *            the attr1 to set
	 */
	public final void setAttr1(String attr1) {
		this.attr1 = attr1;
	}

	/**
	 * @return the attr2
	 */
	public final String getAttr2() {
		return attr2;
	}

	/**
	 * @param attr2
	 *            the attr2 to set
	 */
	public final void setAttr2(String attr2) {
		this.attr2 = attr2;
	}

	/**
	 * @return the attr3
	 */
	public final String getAttr3() {
		return attr3;
	}

	/**
	 * @param attr3
	 *            the attr3 to set
	 */
	public final void setAttr3(String attr3) {
		this.attr3 = attr3;
	}

	/**
	 * @return the attr4
	 */
	public final String getAttr4() {
		return attr4;
	}

	/**
	 * @param attr4
	 *            the attr4 to set
	 */
	public final void setAttr4(String attr4) {
		this.attr4 = attr4;
	}

	/**
	 * @return the attr5
	 */
	public final String getAttr5() {
		return attr5;
	}

	/**
	 * @param attr5
	 *            the attr5 to set
	 */
	public final void setAttr5(String attr5) {
		this.attr5 = attr5;
	}

	/**
	 * @return the attr6
	 */
	public final String getAttr6() {
		return attr6;
	}

	/**
	 * @param attr6
	 *            the attr6 to set
	 */
	public final void setAttr6(String attr6) {
		this.attr6 = attr6;
	}

	/**
	 * @return the attr7
	 */
	public final String getAttr7() {
		return attr7;
	}

	/**
	 * @param attr7
	 *            the attr7 to set
	 */
	public final void setAttr7(String attr7) {
		this.attr7 = attr7;
	}

	/**
	 * @return the attr8
	 */
	public final String getAttr8() {
		return attr8;
	}

	/**
	 * @param attr8
	 *            the attr8 to set
	 */
	public final void setAttr8(String attr8) {
		this.attr8 = attr8;
	}

	/**
	 * @return the attr9
	 */
	public final String getAttr9() {
		return attr9;
	}

	/**
	 * @param attr9
	 *            the attr9 to set
	 */
	public final void setAttr9(String attr9) {
		this.attr9 = attr9;
	}

	/**
	 * @return the fileName0
	 */
	public final String getFileName0() {
		return fileName0;
	}

	/**
	 * @param fileName0
	 *            the fileName0 to set
	 */
	public final void setFileName0(String fileName0) {
		this.fileName0 = fileName0;
	}

	/**
	 * @return the fileName1
	 */
	public final String getFileName1() {
		return fileName1;
	}

	/**
	 * @param fileName1
	 *            the fileName1 to set
	 */
	public final void setFileName1(String fileName1) {
		this.fileName1 = fileName1;
	}

	/**
	 * @return the fileName2
	 */
	public final String getFileName2() {
		return fileName2;
	}

	/**
	 * @param fileName2
	 *            the fileName2 to set
	 */
	public final void setFileName2(String fileName2) {
		this.fileName2 = fileName2;
	}

	/**
	 * @return the fileName3
	 */
	public final String getFileName3() {
		return fileName3;
	}

	/**
	 * @param fileName3
	 *            the fileName3 to set
	 */
	public final void setFileName3(String fileName3) {
		this.fileName3 = fileName3;
	}

	/**
	 * @return the fileName4
	 */
	public final String getFileName4() {
		return fileName4;
	}

	/**
	 * @param fileName4
	 *            the fileName4 to set
	 */
	public final void setFileName4(String fileName4) {
		this.fileName4 = fileName4;
	}

	/**
	 * @return the attr10
	 */
	public final String getAttr10() {
		return attr10;
	}

	/**
	 * @param attr10
	 *            the attr10 to set
	 */
	public final void setAttr10(String attr10) {
		this.attr10 = attr10;
	}

	/**
	 * @return the attr11
	 */
	public final String getAttr11() {
		return attr11;
	}

	/**
	 * @param attr11
	 *            the attr11 to set
	 */
	public final void setAttr11(String attr11) {
		this.attr11 = attr11;
	}

	/**
	 * @return the attr12
	 */
	public final String getAttr12() {
		return attr12;
	}

	/**
	 * @param attr12
	 *            the attr12 to set
	 */
	public final void setAttr12(String attr12) {
		this.attr12 = attr12;
	}

	/**
	 * @return the attr13
	 */
	public final String getAttr13() {
		return attr13;
	}

	/**
	 * @param attr13
	 *            the attr13 to set
	 */
	public final void setAttr13(String attr13) {
		this.attr13 = attr13;
	}

	/**
	 * @return the attr14
	 */
	public final String getAttr14() {
		return attr14;
	}

	/**
	 * @param attr14
	 *            the attr14 to set
	 */
	public final void setAttr14(String attr14) {
		this.attr14 = attr14;
	}

	/**
	 * @return the attr15
	 */
	public final String getAttr15() {
		return attr15;
	}

	/**
	 * @param attr15
	 *            the attr15 to set
	 */
	public final void setAttr15(String attr15) {
		this.attr15 = attr15;
	}

	/**
	 * @return the attr16
	 */
	public final String getAttr16() {
		return attr16;
	}

	/**
	 * @param attr16
	 *            the attr16 to set
	 */
	public final void setAttr16(String attr16) {
		this.attr16 = attr16;
	}

	/**
	 * @return the attr17
	 */
	public final String getAttr17() {
		return attr17;
	}

	/**
	 * @param attr17
	 *            the attr17 to set
	 */
	public final void setAttr17(String attr17) {
		this.attr17 = attr17;
	}

	/**
	 * @return the attr18
	 */
	public final String getAttr18() {
		return attr18;
	}

	/**
	 * @param attr18
	 *            the attr18 to set
	 */
	public final void setAttr18(String attr18) {
		this.attr18 = attr18;
	}

	/**
	 * @return the attr19
	 */
	public final String getAttr19() {
		return attr19;
	}

	/**
	 * @param attr19
	 *            the attr19 to set
	 */
	public final void setAttr19(String attr19) {
		this.attr19 = attr19;
	}

	/**
	 * @return the birthdate
	 */
	public final String getBirthdate() {
		return birthdate;
	}

	/**
	 * @param birthdate
	 *            the birthdate to set
	 */
	public final void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	/**
	 * @return the entriesCount
	 */
	public final int getEntriesCount() {
		return entriesCount;
	}

	/**
	 * @param entriesCount
	 *            the entriesCount to set
	 */
	public final void setEntriesCount(int entriesCount) {
		this.entriesCount = entriesCount;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(200);
		sb.append("ProjectId:");
		sb.append(getProjectId());
		sb.append(" RegisterId:");
		sb.append(getRegisterId());
		sb.append(" idOrPassord:");
		sb.append(getIdOrPassort());
		return sb.toString();
	}

	/**
	 * @return the subPrject
	 */
	public final String getSubPrject() {
		return subProject;
	}

	/**
	 * @param subPrject
	 *            the subPrject to set
	 */
	public final void setSubPrject(String subProject) {
		this.subProject = subProject;
	}

	/**
	 * @return the subProject
	 */
	public final String getSubProject() {
		return subProject;
	}

	/**
	 * @param subProject
	 *            the subProject to set
	 */
	public final void setSubProject(String subProject) {
		this.subProject = subProject;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public final void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the status
	 */
	public final Status getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public final void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the actionFlag
	 */
	public final ActionFlag getActionFlag() {
		return actionFlag;
	}

	/**
	 * @param actionFlag
	 *            the actionFlag to set
	 */
	public final void setActionFlag(ActionFlag actionFlag) {
		this.actionFlag = actionFlag;
	}
}
