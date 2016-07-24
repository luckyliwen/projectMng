package com.sap.csr.odata;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.eclipse.persistence.config.EntityManagerProperties;

import com.sap.cloud.account.TenantContext;

public class ServiceFactory extends ODataJPAServiceFactory implements ServiceConstant {

	@Override
	public ODataJPAContext initializeODataJPAContext()
			throws ODataJPARuntimeException {
		ODataJPAContext oDataJPAContext = this.getODataJPAContext();
		OnJPAWriteContent onDBWriteContent = new OnDBWriteContent();
		
		try {
			//InitialContext ctx = new InitialContext();
			//TenantContext tenantctx = (TenantContext) ctx.lookup(ServiceConstant.HCP_TENANTCONEXT_PATH);
//			String tenantId = tenantctx.getTenant().getId();
			//String tenantId = tenantctx.getTenantId();
			
			EntityManagerFactory emf = JpaEntityManagerFactory
					.getEntityManagerFactory();
			Map<String, Object> properties = new HashMap<String, Object>();
			//properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenantId);
			
			EntityManager em = emf.createEntityManager(properties);
			oDataJPAContext.setEntityManagerFactory(emf);
			
			oDataJPAContext.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
			oDataJPAContext.setEntityManager(em);
			
			setOnWriteJPAContent(onDBWriteContent); 
			
			oDataJPAContext.setJPAEdmExtension((JPAEdmExtension) new CSRProcessingExtension());
			return oDataJPAContext;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
