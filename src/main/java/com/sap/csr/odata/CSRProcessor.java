package com.sap.csr.odata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType.Type;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.account.TenantContext;
import com.sap.csr.model.Project;
import com.sap.csr.model.Registration;
import com.sap.csr.model.UserInfo;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

public class CSRProcessor implements ServiceConstant {
	final Logger logger = LoggerFactory.getLogger(CSRProcessor.class);
	
	private EntityManager em = null;
//	private UserInfo  currentUserInfo = null;  

	public CSRProcessor() throws NamingException, SQLException {
		
	}
	
	
	public class TeamDonation {
		public long teamId, amount;
		public TeamDonation(long teamId, long amount) {
			this.teamId = teamId;
			this.amount = amount;
		}
		
		public String toString() {
			return "teamId amount " + teamId + " " + amount;
		}
		public String toJson() {
			StringBuffer sb = new StringBuffer(60);
			sb.append("{\"TeamId\":");
			sb.append(String.valueOf(teamId));
			sb.append(", \"Amount\":");
			sb.append(String.valueOf(amount));
			sb.append("}");
			return sb.toString();
		}
	}
	
	public class AmountComparator implements Comparator<TeamDonation> {
		public int compare(TeamDonation td0, TeamDonation td1) {
			return Long.compare(td1.amount, td0.amount);
		}
	}
		
	
	private void getEntityManager() {
		
		try {
			InitialContext ctx = new InitialContext();
			//TenantContext tenantctx = (TenantContext) ctx.lookup(HCP_TENANTCONEXT_PATH);
			//String tenantId = tenantctx.getTenant().getId();
			Map<String, Object> properties = new HashMap<String, Object>();
			//properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenantId);
			em = JpaEntityManagerFactory.getEntityManagerFactory().createEntityManager(properties);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	//one time can get the register or create a new register which just return the user information
	//provide this function for performance, so no need wait to first get the UserId, then by the user id to retrieve
	@EdmFunctionImport(name = FUNC_GET_MYEGISTRATION, entitySet = ES_REGISTRATION,
			returnType = @ReturnType(type = Type.ENTITY, isCollection = true) )
	public List<Registration> GetMyRegistration(@EdmFunctionImportParameter(name = "projectId") String projectId) {
		Query query = null;
		UserInfo userInfo = UserMng.getCurrentUserInfo();

		getEntityManager();
		
		query = em.createNamedQuery(REGISTRATION_BY_PROJECT_AND_USERID);
		query.setParameter("userId", userInfo.getUserId());
		long prjId = Long.parseLong(projectId);
		query.setParameter("projectId", prjId);
		
		List<Registration> result = null;
		try {
			result = (List<Registration>)query.getResultList();
		} catch (Exception e) {
			logger.error("Call GetMyRegistration error", e);
		} finally {
			em.close();
		}

		if ( result.isEmpty() ) {
			//not register, just create a new one, so front-ent know the user id
			Registration reg = Registration.createNewRegistration(userInfo);
			result.add(reg);
		}
		
		return result;
 	}
	
	@EdmFunctionImport(name = FUNC_GET_USRERINFO, entitySet = ES_USERINFO,
			returnType = @ReturnType(type = Type.ENTITY, isCollection = false) )
	public UserInfo GetUserInfo() {
   	    return UserMng.getCurrentUserInfo();
	}
	
	
	@EdmFunctionImport(name = ODATA_ISREGISTED, 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean isRegistered(){
//		List<Registration> registrations = getMyRegistration();
//		return !registrations.isEmpty();
		return false;
	}
	
	@EdmFunctionImport(name = "DelTable", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean delTable(@EdmFunctionImportParameter(name = "Table") String table) {
		if ( !isAdmin()) {
			throw new Error("Only admin can do this");
		}
		
		getEntityManager();
		
		String []tables = {};
		String []allTables = {"Registration", "Donation", "Attachment", "Team"};
		if (table.equals("all")) {
			tables = allTables;
		} else {
			tables= table.split(",");
		}
		
		em.getTransaction().begin();
		for (String et : tables) {
			String queryStr = "delete from " + et;
			Query query = em.createQuery(queryStr);
			query.executeUpdate();
		}
		em.getTransaction().commit();
		
		return true;
	}
	
	
	/**
	 * 
	 * @param list
	 * @param names
	 * @param flags : whether need add the " 
	 * @return
	 */
	private String formatResultAsArray(List<Object[]> list, String[] names, boolean []flags ) {
		StringBuffer sb = new StringBuffer("[");
		int row = 0;
		for (Object[] objs: list) {
			if (row > 0) {
				sb.append(",{");
			} else {
				sb.append("{");
			}
			
			int i=0;
			for (String name : names ) {
				if (i==0) {
					sb.append("\"" + name +"\":");
				} else {
					sb.append(",\"" + name +"\":");
				}
				if ( flags[i]) {
					sb.append("\"" +  objs[i] + "\"");
				} else {
					sb.append(objs[i]);
				}
				i++;
			}
			sb.append("}");
			row ++;
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	
	/**
	 * first two param is id and name, combine togeter as name(Id)
	 * @param list
	 * @param names
	 * @param flags : whether need add the " 
	 * @return
	 */
	private String formatResultAsArray_IdName(List<Object[]> list, String[] names, boolean []flags ) {
		StringBuffer sb = new StringBuffer("[");
		int row = 0;
		for (Object[] objs: list) {
			if (row > 0) {
				sb.append(",{");
			} else {
				sb.append("{");
			}
			//here need two index
			int i=0, flagIdx=0;
			for (String name : names ) {
				if (i==0) {
					sb.append("\"" + name +"\":");
				} else {
					sb.append(",\"" + name +"\":");
				}
				
				Object value = objs[i];
				if (i==0) {
					value = objs[1] + " (" + objs[0] + ")";
					i = 1;
				}
				
				if ( flags[flagIdx]) {
					sb.append("\"" +  value + "\"");
				} else {
					sb.append( value);
				}
				
				i++;
				flagIdx++;
			}
			sb.append("}");
			row ++;
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param projectId
	 * @param subProject : true means need get status grouped by sub project
	 * @return
	 */
	@EdmFunctionImport(name = "GetStatusStatistics", returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public String GetStatusStatistics(@EdmFunctionImportParameter(name = "ProjectId") String projectId,
			@EdmFunctionImportParameter(name = "SubProject") String subProject) {
		getEntityManager();
		
		// then the registration by status:
		String queryStr ;
		if (  subProject.equals("true")) {
			 
			
//			queryStr = "select r.subProject, r.status, count(r) from Registration r group by r.subProject, r.status where r.projectId=:projectId";
//			Query query = em.createQuery(queryStr);
			Query query = em.createNamedQuery(REGISTRATION_COUNT_SUB_PROJECT);
			query.setParameter("projectId",  Long.parseLong(projectId));
			String[] regName = { "SubProject", "Status", "Count" };
			boolean[] regFlag = { true, true, false };
			
			return JsonUtility.formatResultAsArray(query.getResultList(), regName, regFlag);
		} else {
//			queryStr = "select r.status, count(r) from Registration r group by r.status where r.projectId=:projectId"; 
//			Query query = em.createQuery(queryStr);
			Query query = em.createNamedQuery(REGISTRATION_COUNT_ALL_PROJECT);
			query.setParameter("projectId", Long.parseLong(projectId));
			String[] regName = { "Status", "Count" };
			boolean[] regFlag = {true, false };
			
			return JsonUtility.formatResultAsArray(query.getResultList(), regName, regFlag);
		}
	}
	
	@EdmFunctionImport(name = "TestEmail", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean TestEmail(@EdmFunctionImportParameter(name = "To") String to, 
			@EdmFunctionImportParameter(name = "Subject") String subject,
			@EdmFunctionImportParameter(name = "Body") String body,
			@EdmFunctionImportParameter(name = "Attachment") String attachment
			) throws ODataException
	{
	
		try {
			EmailMessage msg = new EmailMessage(to, subject,body);
			if ( attachment.length()>0)
				msg.setAttachment("TestAttachment.txt", attachment);
			return EmailMng.sendEmail(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String error  = Util.logException("test email", e);
			
			//return false;
			throw new ODataException(error);
		}
	}
	
	@EdmFunctionImport(name = "IsAdmin", returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )	
	public boolean isAdmin(){
		UserInfo userInfo = UserMng.getCurrentUserInfo();
		return userInfo.isAdmin();
	}
	
}
