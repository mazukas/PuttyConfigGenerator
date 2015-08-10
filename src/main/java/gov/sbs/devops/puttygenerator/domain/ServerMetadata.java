package gov.sbs.devops.puttygenerator.domain;

public class ServerMetadata {
	
	private String host;
	private String name;
	private int port;
	private String comments;
	
	public ServerMetadata(String host, String name, int port, String comments) {
		this.host = host;
		this.name = name;
		this.port = port;
		this.comments = comments;
	}
	
	public ServerMetadata() {
		
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nHOST : " + host);
		sb.append("\nNAME : " + name);
		sb.append("\nPORT : " + port);
		sb.append("\nCOMMENTS : " + comments);
		
		
		return sb.toString();
	}
}
