package com.evolveum.midpoint.eclipse.runtime.api;

public enum ObjectTypes {
	
    CONNECTOR("connector", "connectors"),
    CONNECTOR_HOST("connectorHost", "connectorHosts"),
    GENERIC_OBJECT("genericObject", "genericObjects"),
    RESOURCE("resource", "resources"),
    USER("user", "users"),
    OBJECT_TEMPLATE("objectTemplate", "objectTemplates"),
    SYSTEM_CONFIGURATION("systemConfiguration", "systemConfigurations"),
    TASK("task", "tasks"),
    SHADOW("shadow", "shadows"),
    ROLE("role", "roles"),
    PASSWORD_POLICY("valuePolicy", "valuePolicies"),
    NODE("node", "nodes"),
    ORG("org", "orgs"),
    ABSTRACT_ROLE("abstractRole", "abstractRoles"),
    FOCUS("focus", "focus"),
    REPORT("report", "reports"),
    REPORT_OUTPUT("reportOutput", "reportOutputs"),
    SECURITY_POLICY("securityPolicy", "securityPolicies"),
    LOOKUP_TABLE("lookupTable", "lookupTables"),
    ACCESS_CERTIFICATION_DEFINITION("accessCertificationDefinition", "accessCertificationDefinitions"),
    ACCESS_CERTIFICATION_CAMPAIGN("accessCertificationCampaig", "accessCertificationCampaigns"),
    SEQUENCE("sequence", "sequences"),
    SERVICE("service", "services"),
    OBJECT("object", "objects");
	
	private String elementName;
	private String restType;
	
	ObjectTypes(String elementName, String restType) {
		this.elementName = elementName;
		this.restType = restType;
	}
	
    public String getElementName() {
		return elementName;
	}

	public String getRestType() {
		return restType;
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
			if (t.elementName.equalsIgnoreCase(w) || t.restType.equalsIgnoreCase(w)) {
				return t;
			}
		}
		return null;
	}

}
