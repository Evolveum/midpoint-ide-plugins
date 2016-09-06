package com.evolveum.midpoint.eclipse.runtime.api;

public enum ObjectTypes {
	
    CONNECTOR("connector", "connectors", "ConnectorType"),
    CONNECTOR_HOST("connectorHost", "connectorHosts", "ConnectorHostType"),
    GENERIC_OBJECT("genericObject", "genericObjects", "GenericObjectType"),
    RESOURCE("resource", "resources", "ResourceType"),
    USER("user", "users", "UserType"),
    OBJECT_TEMPLATE("objectTemplate", "objectTemplates", "ObjectTemplateType"),
    SYSTEM_CONFIGURATION("systemConfiguration", "systemConfigurations", "SystemConfigurationType"),
    TASK("task", "tasks", "TaskType"),
    SHADOW("shadow", "shadows", "ShadowType"),
    ROLE("role", "roles", "RoleType"),
    PASSWORD_POLICY("valuePolicy", "valuePolicies", "ValuePolicyType"),
    NODE("node", "nodes", "NodeType"),
    ORG("org", "orgs", "OrgType"),
    ABSTRACT_ROLE("abstractRole", "abstractRoles", "AbstractRoleType"),
    FOCUS("focus", "focus", "FocusType"),
    REPORT("report", "reports", "ReportType"),
    REPORT_OUTPUT("reportOutput", "reportOutputs", "ReportOutputType"),
    SECURITY_POLICY("securityPolicy", "securityPolicies", "SecurityPolicyType"),
    LOOKUP_TABLE("lookupTable", "lookupTables", "LookupTableType"),
    ACCESS_CERTIFICATION_DEFINITION("accessCertificationDefinition", "accessCertificationDefinitions", "AccessCertificationDefinitionType"),
    ACCESS_CERTIFICATION_CAMPAIGN("accessCertificationCampaig", "accessCertificationCampaigns", "AccessCertificationCampaignType"),
    SEQUENCE("sequence", "sequences", "SequenceType"),
    SERVICE("service", "services", "ServiceType"),
    OBJECT("object", "objects", "ObjectType");
	
	private String elementName;
	private String restType;
	private String typeName;
	
	ObjectTypes(String elementName, String restType, String typeName) {
		this.elementName = elementName;
		this.restType = restType;
		this.typeName = typeName;
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

	public static String getElementNameForXsiType(String localPart) {
		for (ObjectTypes type : values()) {
            if (type.typeName.equals(localPart)) {
                return type.elementName;
            }
        }
        return null;	
    }

}
