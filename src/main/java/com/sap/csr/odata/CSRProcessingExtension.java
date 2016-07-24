package com.sap.csr.odata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;

public class CSRProcessingExtension implements JPAEdmExtension {

	public void extendJPAEdmSchema(JPAEdmSchemaView schemaView) {
	
		Schema schema = schemaView.getEdmSchema();
        for (EntityType t : schema.getEntityTypes()) {
            if (t.getName().equals("Attachment")) {
                //t.setHasStream(true);
                
                //try to remove the content property for performance
                List<Property> props =  t.getProperties();
                for (Property prop : props) {
                	if (prop.getName().equals("Content")){
                		props.remove(prop);
                		break;
                	}
                }
            }
        }
	}

	public void extendWithOperation(JPAEdmSchemaView arg0) {
		arg0.registerOperations(CSRProcessor.class, null);
	}

	public InputStream getJPAEdmMappingModelStream() {
		return null;
	}

	private ComplexType getComplexType() {
		ComplexType complexType = new ComplexType();

		List<Property> properties = new ArrayList<Property>();
		SimpleProperty property = new SimpleProperty();

		property.setName("firstName");
		property.setType(EdmSimpleTypeKind.String);
		properties.add(property);

		property = new SimpleProperty();
		property.setName("lastName");
		property.setType(EdmSimpleTypeKind.String);
		properties.add(property);

		property = new SimpleProperty();
		property.setName("email");
		property.setType(EdmSimpleTypeKind.String);
		properties.add(property);

		complexType.setName("UserInfo");
		complexType.setProperties(properties);
		return complexType;
	}
}
