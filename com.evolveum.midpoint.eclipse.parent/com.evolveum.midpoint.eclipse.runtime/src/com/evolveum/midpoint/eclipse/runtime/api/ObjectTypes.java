package com.evolveum.midpoint.eclipse.runtime.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;

public enum ObjectTypes {
	
	// TODO implement AssignmentHolderType correctly here - i.e. as part of the inheritance hierarchy 
	// But be careful: it is known only since midPoint 4.0
	
	OBJECT("object", "objects", "ObjectType", "Object (abstract type)", null, null, null, false),
	ASSIGNMENT_HOLDER("assignmentHolder", "assignmentHolders", "AssignmentHolderType", "Assignment holder (abstract type)", null, null, OBJECT, false),
    FOCUS("focus", "focus", "FocusType", "Focus (abstract type)", null, null, OBJECT, false),
    ABSTRACT_ROLE("abstractRole", "abstractRoles", "AbstractRoleType", "Abstract role (abstract type)", null, "displayName", FOCUS, false),
    CONNECTOR("connector", "connectors", "ConnectorType", "Connector", "connectorType", "displayName", OBJECT, true),
    CONNECTOR_HOST("connectorHost", "connectorHosts", "ConnectorHostType", "Connector host", null, null, OBJECT, true),
    GENERIC_OBJECT("genericObject", "genericObjects", "GenericObjectType", "Generic object", null, null, OBJECT, true),
    RESOURCE("resource", "resources", "ResourceType", "Resource", null, null, OBJECT, true),
    USER("user", "users", "UserType", "User", "employeeType", "fullName", FOCUS, true),
    OBJECT_TEMPLATE("objectTemplate", "objectTemplates", "ObjectTemplateType", "Object template", null, null, OBJECT, true),
    SYSTEM_CONFIGURATION("systemConfiguration", "systemConfigurations", "SystemConfigurationType", "System configuration", null, null, OBJECT, true),
    TASK("task", "tasks", "TaskType", "Task", "category", null, OBJECT, true),
    SHADOW("shadow", "shadows", "ShadowType", "Shadow", null, null, OBJECT, true),
    ROLE("role", "roles", "RoleType", "Role", "roleType", "displayName", ABSTRACT_ROLE, true),
    PASSWORD_POLICY("valuePolicy", "valuePolicies", "ValuePolicyType", "Value policy", null, null, OBJECT, true),
    NODE("node", "nodes", "NodeType", "Node", null, null, OBJECT, true),
    FORM("form", "forms", "FormType", "Form", null, null, OBJECT, true),
    ORG("org", "orgs", "OrgType", "Organization", "orgType", "displayName", ABSTRACT_ROLE, true),
    REPORT("report", "reports", "ReportType", "Report", null, null, OBJECT, true),
    REPORT_OUTPUT("reportOutput", "reportOutputs", "ReportOutputType", "Report output", null, null, OBJECT, true),
    SECURITY_POLICY("securityPolicy", "securityPolicies", "SecurityPolicyType", "Security policy", null, null, OBJECT, true),
    LOOKUP_TABLE("lookupTable", "lookupTables", "LookupTableType", "Lookup table", null, null, OBJECT, true),
    ACCESS_CERTIFICATION_DEFINITION("accessCertificationDefinition", "accessCertificationDefinitions", "AccessCertificationDefinitionType", "Access certification definition", null, null, OBJECT, true),
    ACCESS_CERTIFICATION_CAMPAIGN("accessCertificationCampaign", "accessCertificationCampaigns", "AccessCertificationCampaignType", "Access certification campaign", null, null, OBJECT, true),
    SEQUENCE("sequence", "sequences", "SequenceType", "Sequence", null, null, OBJECT, true),
    SERVICE("service", "services", "ServiceType", "Service", "serviceType", "displayName", ABSTRACT_ROLE, true),
    FUNCTION_LIBRARY("functionLibrary", "functionLibraries", "FunctionLibraryType", "Function library", null, null, OBJECT, true),
    ARCHETYPE("archetype", "archetypes", "ArchetypeType", "Archetype", null, null, ABSTRACT_ROLE, true),
    CASE("case", "cases", "CaseType", "Case", null, null, OBJECT, true),
    OBJECT_COLLECTION("objectCollection", "objectCollections", "ObjectCollectionType", "Object collection", null, null, OBJECT, true),
    DASHBOARD("dashboard", "dashboards", "DashboardType", "Dashboard", null, null, OBJECT, true)
    ;
	
	private String elementName;
	private String restType;
	private String typeName;
	private String displayName;
	private String subTypeElement;
	private String displayNameElement;
	private ObjectTypes superType;
	private boolean concrete;
	
	ObjectTypes(String elementName, String restType, String typeName, String displayName, String subTypeElement, String displayNameElement, ObjectTypes superType, boolean concrete) {
		this.elementName = elementName;
		this.restType = restType;
		this.typeName = typeName;
		this.displayName = displayName;
		this.subTypeElement = subTypeElement;
		this.displayNameElement = displayNameElement;
		this.superType = superType;
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
	
	public String getSubTypeElement() {
		return subTypeElement;
	}
	
	public String getDisplayNameElement() {
		return displayNameElement;
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

	public static ObjectTypes commonSuperType(ObjectTypes t1, ObjectTypes t2) {
		if (t1 == null) {
			return t2;
		} 
		if (t2 == null) {
			return t1;
		}
		List<ObjectTypes> path1 = pathFromRoot(t1);
		List<ObjectTypes> path2 = pathFromRoot(t2);
		int i = 0;
		while(i < path1.size() && i < path2.size() && path1.get(i) == path2.get(i)) {
			i++;
		}
		ObjectTypes rv = path1.get(i-1);
		System.out.println("commonSuperType(" + t1 + ", " + t2 + ") returns " + rv + "; path1=" + path1 + ", path2=" + path2 + ", i=" + i);
		return rv;
	}

	private static List<ObjectTypes> pathFromRoot(ObjectTypes t) {
		List<ObjectTypes> path = new ArrayList<>();
		do {
			path.add(t);
			t = t.superType;
		} while (t != null);
		Collections.reverse(path);
		return path;
	}

}
