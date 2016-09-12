package com.evolveum.midpoint.eclipse.runtime.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;

public enum ObjectTypes {
	
	OBJECT("object", "objects", "ObjectType", "Object (abstract type)", null, null, null),
    FOCUS("focus", "focus", "FocusType", "Focus (abstract type)", null, null, OBJECT),
    ABSTRACT_ROLE("abstractRole", "abstractRoles", "AbstractRoleType", "Abstract role (abstract type)", null, "displayName", FOCUS),
    CONNECTOR("connector", "connectors", "ConnectorType", "Connector", "connectorType", "displayName", OBJECT),
    CONNECTOR_HOST("connectorHost", "connectorHosts", "ConnectorHostType", "Connector host", null, null, OBJECT),
    GENERIC_OBJECT("genericObject", "genericObjects", "GenericObjectType", "Generic object", null, null, OBJECT),
    RESOURCE("resource", "resources", "ResourceType", "Resource", null, null, OBJECT),
    USER("user", "users", "UserType", "User", "employeeType", "fullName", FOCUS),
    OBJECT_TEMPLATE("objectTemplate", "objectTemplates", "ObjectTemplateType", "Object template", null, null, OBJECT),
    SYSTEM_CONFIGURATION("systemConfiguration", "systemConfigurations", "SystemConfigurationType", "System configuration", null, null, OBJECT),
    TASK("task", "tasks", "TaskType", "Task", "category", null, OBJECT),
    SHADOW("shadow", "shadows", "ShadowType", "Shadow", null, null, OBJECT),
    ROLE("role", "roles", "RoleType", "Role", "roleType", "displayName", ABSTRACT_ROLE),
    PASSWORD_POLICY("valuePolicy", "valuePolicies", "ValuePolicyType", "Value policy", null, null, OBJECT),
    NODE("node", "nodes", "NodeType", "Node", null, null, OBJECT),
    ORG("org", "orgs", "OrgType", "Organization", "orgType", "displayName", ABSTRACT_ROLE),
    REPORT("report", "reports", "ReportType", "Report", null, null, OBJECT),
    REPORT_OUTPUT("reportOutput", "reportOutputs", "ReportOutputType", "Report output", null, null, OBJECT),
    SECURITY_POLICY("securityPolicy", "securityPolicies", "SecurityPolicyType", "Security policy", null, null, OBJECT),
    LOOKUP_TABLE("lookupTable", "lookupTables", "LookupTableType", "Lookup table", null, null, OBJECT),
    ACCESS_CERTIFICATION_DEFINITION("accessCertificationDefinition", "accessCertificationDefinitions", "AccessCertificationDefinitionType", "Access certification definition", null, null, OBJECT),
    ACCESS_CERTIFICATION_CAMPAIGN("accessCertificationCampaign", "accessCertificationCampaigns", "AccessCertificationCampaignType", "Access certification campaign", null, null, OBJECT),
    SEQUENCE("sequence", "sequences", "SequenceType", "Sequence", null, null, OBJECT),
    SERVICE("service", "services", "ServiceType", "Service", "serviceType", "displayName", ABSTRACT_ROLE)
    ;
	
	private String elementName;
	private String restType;
	private String typeName;
	private String displayName;
	private String subTypeElement;
	private String displayNameElement;
	private ObjectTypes superType;
	
	ObjectTypes(String elementName, String restType, String typeName, String displayName, String subTypeElement, String displayNameElement, ObjectTypes superType) {
		this.elementName = elementName;
		this.restType = restType;
		this.typeName = typeName;
		this.displayName = displayName;
		this.subTypeElement = subTypeElement;
		this.displayNameElement = displayNameElement;
		this.superType = superType;
	}
	
    public String getElementName() {
		return elementName;
	}

	public String getRestType() {
		return restType;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public String getSubTypeElement() {
		return subTypeElement;
	}
	
	public String getDisplayNameElement() {
		return displayNameElement;
	}

	public boolean isConcrete() {
		for (ObjectTypes t : values()) {
			if (t.superType == this) {
				return false;
			}
		}
		return true;
	}

	public static String getRestTypeForElementName(String elementName) {
        for (ObjectTypes type : values()) {
            if (type.elementName.equals(elementName)) {
                return type.restType;
            }
        }
        return null;
    }

	public static ObjectTypes findByAny(String w) {
		for (ObjectTypes t : values()) {
			if (t.elementName.equalsIgnoreCase(w) || t.restType.equalsIgnoreCase(w) || t.typeName.equalsIgnoreCase(w)) {
				return t;
			}
		}
		return null;
	}

	public static ObjectTypes findByElementName(String w) {
		for (ObjectTypes t : values()) {
			if (t.elementName.equalsIgnoreCase(w)) {
				return t;
			}
		}
		return null;
	}

	public static ObjectTypes findByXsiType(String localPart) {
		for (ObjectTypes type : values()) {
            if (type.typeName.equals(localPart)) {
                return type;
            }
        }
        return null;	
    }

	public static List<ObjectTypes> getConcreteTypes() {
		List<ObjectTypes> rv = new ArrayList<>();
		for (ObjectTypes t : values()) {
			if (t.isConcrete()) {
				rv.add(t);
			}
		}
		return rv;
	}

	public QName getTypeQName() {
		return new QName(Constants.COMMON_NS, getTypeName());
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public ObjectTypes getSuperType() {
		return superType;
	}

	public boolean isAssignableFrom(ObjectTypes type) {
		while (type != null) {
			if (type == this) {
				return true;
			}
			type = type.superType;
		}
		return false;
	}

	public static Comparator<ObjectTypes> getDisplayNameComparator() {
		return new Comparator<ObjectTypes>() {

			@Override
			public int compare(ObjectTypes o1, ObjectTypes o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		};
	}

}
