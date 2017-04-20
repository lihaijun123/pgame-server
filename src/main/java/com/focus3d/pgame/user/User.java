package com.focus3d.pgame.user;

import io.netty.channel.Channel;

import com.focus3d.pgame.constant.UserType;

/**
 * 
 * *
 * @author lihaijun
 *
 */
public class User {
	private int type = UserType.GROUP_MEMBER.getType();//默认是连接者
	private String groupId;
	private Channel channel;
	
	public User(int type, String groupId, Channel channel){
		this.type = type;
		this.channel = channel;
		this.groupId = groupId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	@Override
	public String toString() {
		return "type:" + getType() + ",groupId:" + getGroupId() + ", channelId:" + channel.id();
	}
}
