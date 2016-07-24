package com.sap.csr.odata;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.olingo.odata2.api.processor.ODataContext;

import com.sap.csr.model.UserInfo;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.User;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;


public class UserMng implements ServiceConstant{
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	private static String getAttribute(User user, String name)  {
		try {
			String value = user.getAttribute(name);
			if (value != null)
				return value;
			else 
				return "";
		} catch (UnsupportedUserAttributeException e) {
			return "";
		}
	}
	
	public static String getCurrentUserAttributes() throws UnsupportedUserAttributeException{
		StringBuffer sb = new StringBuffer();
		UserProvider userProvider;
		try {
			userProvider = UserManagementAccessor.getUserProvider();
			User user = userProvider.getCurrentUser();
			Set<String>  attrs = user.listAttributes();
			for (String attr : attrs) {
				String value = user.getAttribute(attr);
				sb.append(attr + ":"+ value + "  ");
			}
			return sb.toString();
			
		} catch (PersistenceException e) {
			return Util.logException(" getCurrentUserInfo ", e);
		}
	}
	
	public static UserInfo getCurrentUserInfo(){
		UserProvider userProvider;
		try {
			userProvider = UserManagementAccessor.getUserProvider();
			User user = userProvider.getCurrentUser();
			if (user == null) {
				UserInfo tmpUser = new UserInfo();
				tmpUser.setEmail("null@sap.com");
				return tmpUser;
			}
			
			String userId = getAttribute(user, ATTR_USER_ID);
			String email = getAttribute(user, ATTR_EMAIL);
			String 	firstName = getAttribute(user, ATTR_FIRST_NAME);
			String lastName = getAttribute(user, ATTR_LAST_NAME);
			
			UserInfo userInfo = new UserInfo(userId,email, firstName, lastName);
			Set<String> roles = user.getRoles();
			if (roles.contains("Admin")) {
				userInfo.setAdmin(true);
			}
			return userInfo;
			
		} catch (PersistenceException e) {
			Util.logException(" getCurrentUserInfo ", e);
			
			//e.printStackTrace();
			UserInfo user = new UserInfo();
			user.setEmail("unknown@sap.com");
			return user;
		}
		
	}
	
//	private HttpSession getHttpSession() {
//		HttpServletRequest req = (HttpServletRequest) odataSingleProcessor.getContext().getParameter(
//				ODataContext.HTTP_SERVLET_REQUEST_OBJECT);
//		return req.getSession();
//	}
	/**
	 * Get the current assigned user: need get from the HTTP Session
	 * @return
	 */
//	public CPUserInfo getAssignedUserInfo() throws Exception{
//		HttpSession session = getHttpSession();
//		Object userInfo = session.getAttribute(CURRENT_ASSIGNED_BACKEND_USER_ATTRIBUTE_NAME); 
//		if ( userInfo != null) {
//			return (CPUserInfo)userInfo;
//		}
//		return null;
//	}
}
