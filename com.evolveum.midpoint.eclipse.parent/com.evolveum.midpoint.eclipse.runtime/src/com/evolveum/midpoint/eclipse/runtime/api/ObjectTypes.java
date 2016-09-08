package com.evolveum.midpoint.eclipse.runtime.api;

import java.util.ArrayList;
import java.util.List;

public enum ObjectTypes {
	
    CONNECTOR("connector", "connectors", "ConnectorType", true),
    CONNECTOR_HOST("connectorHost", "connectorHosts", "ConnectorHostType", true),
    GENERIC_OBJECT("genericObject", "genericObjects", "GenericObjectType", true),
    RESOURCE("resource", "resources", "ResourceType", true),
    USER("user", "users", "UserType", true),
    OBJECT_TEMPLATE("objectTemplate", "objectTemplates", "ObjectTemplateType", true),
    SYSTEM_CONFIGURATION("systemConfiguration", "systemConfigurations", "SystemConfigurationType", true),
    TASK("task", "tasks", "TaskType", true),
    SHADOW("shadow", "shadows", "ShadowType", true),
    ROLE("role", "roles", "RoleType", true),
    PASSWORD_POLICY("valuePolicy", "valuePolicies", "ValuePolicyType", true),
    NODE("node", "nodes", "NodeType", true),
    ORG("org", "orgs", "OrgType", true),
    ABSTRACT_ROLE("abstractRole", "abstractRoles", "AbstractRoleType", false),
    FOCUS("focus", "focus", "FocusType", false),
    REPORT("report", "reports", "ReportType", true),
    REPORT_OUTPUT("reportOutput", "reportOutputs", "ReportOutputType", true),
    SECURITY_POLICY("securityPolicy", "securityPolicies", "SecurityPolicyType", true),
    LOOKUP_TABLE("lookupTable", "lookupTables", "LookupTableType", true),
    ACCESS_CERTIFICATION_DEFINITION("accessCertificationDefinition", "accessCertificationDefinitions", "AccessCertificationDefinitionType", true),
    ACCESS_CERTIFICATION_CAMPAIGN("accessCertificationCampaign", "accessCertificationCampaigns", "AccessCertificationCampaignType", true),
    SEQUENCE("sequence", "sequences", "SequenceType", true),
    SERVICE("service", "services", "ServiceType", true),
    OBJECT("object", "objects", "ObjectType", false);
	
	private String elementName;
	private String restType;
	private String typeName;
	private boolean concrete;
	
	ObjectTypes(String elementName, String restType, String typeName, boolean concrete) {
		this.elementName = elementName;
		this.restType = restType;
		this.typeName = typeName;
		this.concrete = concrete;
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
		return concrete;
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

	public static String getElementNameForXsiType(String localPart) {
		for (ObjectTypes type : values()) {
            if (type.typeName.equals(localPart)) {
                return type.elementName;
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

}
