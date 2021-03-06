package com.evolveum.midpoint.eclipse.runtime.api.req;

import org.apache.commons.lang.StringUtils;

public class ConnectionParameters {
	private String name;			// human-readable connection (server) name
	private String url;
	private String login;
	private String password;
	private boolean ignoreSslIssues;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public boolean isIgnoreSslIssues() {
		return ignoreSslIssues;
	}
	public void setIgnoreSslIssues(boolean ignoreSslIssues) {
		this.ignoreSslIssues = ignoreSslIssues;
	}
	public ConnectionParameters(String name, String url, String login, String password, boolean ignoreSslIssues) {
		this.name = name;
		this.url = url;
		this.login = login;
		this.password = password;
		this.ignoreSslIssues = ignoreSslIssues;
	}
	
	@Override
	public String toString() {
		return "ConnectionParameters [name=" + name + ", url=" + url + ", login=" + login + ", ignoreSslIssues=" + ignoreSslIssues + "]";
	}
	public String getDisplayName() {
		return StringUtils.isNotBlank(name) ? name : url;
	}

}
