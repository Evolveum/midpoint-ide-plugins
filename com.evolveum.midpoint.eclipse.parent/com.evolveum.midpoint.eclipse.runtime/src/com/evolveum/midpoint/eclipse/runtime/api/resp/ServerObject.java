package com.evolveum.midpoint.eclipse.runtime.api.resp;

import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;

// TODO find better name
public class ServerObject {
	private String oid;
	private String name;
	private ObjectTypes type;
	private List<String> subtypes;
	private String displayName;
	private String xml;
	
	public ServerObject(String oid, String name, ObjectTypes type, List<String> subtypes, String displayName, String xml) {
		super();
		this.oid = oid;
		this.name = name;
		this.type = type;
		this.subtypes = subtypes;
		this.displayName = displayName;
		this.xml = xml;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ObjectTypes getType() {
		return type;
	}
	public void setType(ObjectTypes type) {
		this.type = type;
	}
	public List<String> getSubtypes() {
		return subtypes;
	}
	public void setSubtypes(List<String> subtypes) {
		this.subtypes = subtypes;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
}
