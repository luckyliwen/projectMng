package com.sap.csr.odata;

public interface ServiceConstant {
	
	public static final String FUNC_GET_MYEGISTRATION = "GetMyRegistration";
	public static final String FUNC_GET_USRERINFO = "GetUserInfo";
	
	public static final String ODATA_ISREGISTED = "IsRegisted";
	public static final String ODATA_APPROVE = "Approve";
	

	//entity type 
	public static final String ET_REGISTRATION = "Registration";
	public static final String ET_USERINFO = "UserInfo";
	public static final String ET_ATTACHMENT =  "Attachment";
	public static final String ET_PROJECT =  "Project";


	//entity sets 
	public static final String ES_REGISTRATION = "Registrations";
	public static final String ES_ATTACHMENT =  "Attachments";
	public static final String ES_USERINFO  =  "UserInfos";
	public static final String ES_PROJECT =  "Projects";
	
	//some query
	public static final String REGISTRATION_BY_PROJECT_AND_USERID="GetRegistrationByProjectAndUserId";
	public static final String ATTACHMENT_BY_ENTRY_AND_TYPE = "GetAttachmentByEntryAndType";
	public static final String ATTACHMENT_BY_ENTRY = "GetAttachmentByEntry";
	  
	
	public static final String EMAIL = "email";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String EMPLOYEE_ID = "employeeId";

	public static final String ROLE_ADMIN = "Admin";
	
	public static final String STATUS_NOT_APPROVED = "Not Approved";
	public static final String STATUS_APPORVED = "Approved";
	
	
	public static final String PERSISTENCE_UNIT_NAME = "CSR";
	public static final String HCP_TENANTCONEXT_PATH = "java:comp/env/TenantContext";
	
		
	//user mgn attribute
	public static final String ATTR_EMAIL = "email";
	public static final String ATTR_FIRST_NAME = "firstname";
	public static final String ATTR_LAST_NAME = "lastname";
	public static final String ATTR_USER_ID = "name";

	//email 
	public static final String FROM_ADDRESS = "hello.sap.csr@gmail.com";
	

}
