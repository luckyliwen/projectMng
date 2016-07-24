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
	
	//As now team may change dynamically, so we need get the team dynamically
		public String getTeamDonationDynamically() {
			getEntityManager();

			List<TeamDonation> tdList = new ArrayList<TeamDonation>();
			
			// get the team id list:
			String teamQueryStr = "select t.teamId from Team t where t.teamId <> 0"; 
			Query query = em.createQuery(teamQueryStr);
			List<Object> teamIds = query.getResultList();
			
			for (Object idObj : teamIds) {
				long teamId = ((Long)idObj).longValue();
				
				//then by the id try to get the register id belong to same team
				String regQueryStr = "select r.userId from Registration r where r.teamId = " + teamId;
				Query regQuery = em.createQuery(regQueryStr);
				List<Object> regIds = regQuery.getResultList();
				
				//then create the query for same team 
				StringBuffer querySb = new StringBuffer(300);
				querySb.append("select sum(d.amount) from Donation d "); 
				
				if (regIds.isEmpty())
					continue;
				
				querySb.append(" where ");
				int idx = 0;
				for (Object regIdObj: regIds) {
					if (idx>0) {
						querySb.append( " or ");
					}
					
					querySb.append( "d.donatoryId =\"");
					querySb.append(regIdObj);
					querySb.append("\" " );
					
					idx++;
				}
				
				//add to result list
				Query donationQuery = em.createQuery(querySb.toString());
				Object sumObj = donationQuery.getSingleResult();
				if (sumObj != null) {
					long  sum = (Long)sumObj;
					if ( sum != 0) {
						tdList.add( new TeamDonation(teamId, (Long)sum));
					}
				}
			}
			
			//then sort it 
			Collections.sort(tdList, new AmountComparator());
			
			//then create the json string
			StringBuffer sb = new StringBuffer(2000);
			sb.append("[");
			
			int idx = 0;
			for (TeamDonation td: tdList) {
				if (idx > 0) {
					sb.append(",");
				}
				
				sb.append(td.toJson());
				idx++;
			}
			sb.append("]");
			
			return sb.toString();
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
	
	
	@EdmFunctionImport(name = "TestEmail", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean TestEmail(@EdmFunctionImportParameter(name = "To") String to, 
			@EdmFunctionImportParameter(name = "Subject") String subject,
			@EdmFunctionImportParameter(name = "Body") String body) throws ODataException{
		EmailMng email = new EmailMng();
		try {
			return email.sendEmail(to, subject, body);
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
