package com.focus3d.pgame.protocal;
/**
 * *
 * @author lihaijun
 *
 */
public class GameMessage {
	
	private int type;//1-新建组 2-加入组，3-群消息
	private String sessionId;
	private int status;
	private String groupId;
	private int userCount;//用户数
	private String body;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getUserCount() {
		return userCount;
	}
	
	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
	
	@Override
	public String toString() {
		return "sessionId" + getSessionId() + ", type:" + getType() + ", groupId:" + getGroupId() + ",userCount:" + getUserCount() + ", body:" + body;
	}
}
