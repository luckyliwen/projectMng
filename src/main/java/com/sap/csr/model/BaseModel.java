package com.sap.csr.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import com.sap.csr.odata.JpaEntityManagerFactory;

public class BaseModel {
	transient protected  	 EntityManager em = null;

	public BaseModel() {
		super();
	}
	
	public void getEntityManager() {
		if (em != null)
			return ;
		
		try {
			InitialContext ctx = new InitialContext();
			//TenantContext tenantctx = (TenantContext) ctx.lookup(HCP_TENANTCONEXT_PATH);
			//String tenantId = tenantctx.getTenant().getId();
			Map<String, Object> properties = new HashMap<String, Object>();
			//properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenantId);
//			EntityManager
			em = JpaEntityManagerFactory.getEntityManagerFactory().createEntityManager(properties);

		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
