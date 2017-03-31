package com.focus3d.pgame.constant;
/**
 * *
 * @author lihaijun
 *
 */
public enum MessageType {
	
	CREATE_GROUP(1),
	JOIN_GROUP(2),
	GROUP_MESSAGE(3);
	
	int code;
	
	MessageType(int code){
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	
}
