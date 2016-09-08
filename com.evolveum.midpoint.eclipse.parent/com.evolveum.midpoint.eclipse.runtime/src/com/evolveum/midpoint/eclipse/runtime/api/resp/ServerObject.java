package com.evolveum.midpoint.eclipse.runtime.api.resp;

import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;

// TODO find better name
public class ServerObject {
	private String oid;
	private String name;
	private ObjectTypes type;
	private String xml;
	public ServerObject(String oid, String name, ObjectTypes type, String xml) {
		super();
		this.oid = oid;
		this.name = name;
		this.type = type;
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
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	

}
