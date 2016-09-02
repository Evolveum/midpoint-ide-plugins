package com.evolveum.midpoint.eclipse.runtime.api;

public class ConnectionParameters {
	private String url;
	private String login;
	private String password;
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
	
	public ConnectionParameters(String url, String login, String password) {
		super();
		this.url = url;
		this.login = login;
		this.password = password;
	}
	
	@Override
	public String toString() {
		return "ConnectionParameters [url=" + url + ", login=" + login + "]";
	}

}
