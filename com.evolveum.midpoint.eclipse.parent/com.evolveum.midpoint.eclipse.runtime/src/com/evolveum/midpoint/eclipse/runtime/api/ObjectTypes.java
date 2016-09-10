package com.evolveum.midpoint.eclipse.runtime.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;

public enum ObjectTypes {
	
	OBJECT("object", "objects", "ObjectType", "Object (abstract type)", null),
    FOCUS("focus", "focus", "FocusType", "Focus (abstract type)", OBJECT),
    ABSTRACT_ROLE("abstractRole", "abstractRoles", "AbstractRoleType", "Abstract role (abstract type)", FOCUS),
    CONNECTOR("connector", "connectors", "ConnectorType", "Connector", OBJECT),
    CONNECTOR_HOST("connectorHost", "connectorHosts", "ConnectorHostType", "Connector host", OBJECT),
    GENERIC_OBJECT("genericObject", "genericObjects", "GenericObjectType", "Generic object", OBJECT),
    RESOURCE("resource", "resources", "ResourceType", "Resource", OBJECT),
    USER("user", "users", "UserType", "User", FOCUS),
    OBJECT_TEMPLATE("objectTemplate", "objectTemplates", "ObjectTemplateType", "Object template", OBJECT),
    SYSTEM_CONFIGURATION("systemConfiguration", "systemConfigurations", "SystemConfigurationType", "System configuration", OBJECT),
    TASK("task", "tasks", "TaskType", "Task", OBJECT),
    SHADOW("shadow", "shadows", "ShadowType", "Shadow", OBJECT),
    ROLE("role", "roles", "RoleType", "Role", ABSTRACT_ROLE),
    PASSWORD_POLICY("valuePolicy", "valuePolicies", "ValuePolicyType", "Value policy", OBJECT),
    NODE("node", "nodes", "NodeType", "Node", OBJECT),
    ORG("org", "orgs", "OrgType", "Organization", ABSTRACT_ROLE),
    REPORT("report", "reports", "ReportType", "Report", OBJECT),
    REPORT_OUTPUT("reportOutput", "reportOutputs", "ReportOutputType", "Report output", OBJECT),
    SECURITY_POLICY("securityPolicy", "securityPolicies", "SecurityPolicyType", "Security policy", OBJECT),
    LOOKUP_TABLE("lookupTable", "lookupTables", "LookupTableType", "Lookup table", OBJECT),
    ACCESS_CERTIFICATION_DEFINITION("accessCertificationDefinition", "accessCertificationDefinitions", "AccessCertificationDefinitionType", "Access certification definition", OBJECT),
    ACCESS_CERTIFICATION_CAMPAIGN("accessCertificationCampaign", "accessCertificationCampaigns", "AccessCertificationCampaignType", "Access certification campaign", OBJECT),
    SEQUENCE("sequence", "sequences", "SequenceType", "Sequence", OBJECT),
    SERVICE("service", "services", "ServiceType", "Service", ABSTRACT_ROLE)
    ;
	
	private String elementName;
	private String restType;
	private String typeName;
	private String displayName;
	private ObjectTypes superType;
	
	ObjectTypes(String elementName, String restType, String typeName, String displayName, ObjectTypes superType) {
		this.elementName = elementName;
		this.restType = restType;
		this.typeName = typeName;
		this.displayName = displayName;
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
