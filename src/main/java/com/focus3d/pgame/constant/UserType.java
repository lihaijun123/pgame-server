package com.focus3d.pgame.constant;
/**
 * 用户类型
 * *
 * @author lihaijun
 *
 */
public enum UserType {
	GROUP_MEMBER(0),
	GROUP_CREATER(1);
	int type;
	UserType(int type){
		this.type = type;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
