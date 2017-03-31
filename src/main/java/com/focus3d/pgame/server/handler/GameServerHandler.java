package com.focus3d.pgame.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import com.focus3d.pgame.constant.MessageType;
import com.focus3d.pgame.protocal.GameMessage;
/**
 * *
 * @author lihaijun
 *
 */
public class GameServerHandler extends SimpleChannelInboundHandler<Object> {
	
	private static final Logger log = Logger.getLogger(GameServerHandler.class.getName());
	private String serverIp;
	private int serverPort;
	private WebSocketServerHandshaker handshaker;
	//用户组
	public static Map<String, List<Channel>> userGroup = new ConcurrentHashMap<String, List<Channel>>();
	
	public GameServerHandler(String serverIp, int serverPort) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof FullHttpRequest){
			handleHttpRequest(ctx, (FullHttpRequest)msg);
		} else if(msg instanceof WebSocketFrame){
			handleWebSocketFrame(ctx, (WebSocketFrame)msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		log.info("client id:" + channel.id() + " join in");
	}
	/**
	 * 
	 * *
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		ChannelId id = channel.id();
		for(Map.Entry<String, List<Channel>> g : userGroup.entrySet()){
			List<Channel> c = g.getValue();
			for (Channel ch : c) {
				if(ch.id().toString().equals(id.toString())){
					c.remove(ch);
					log.info("client id:" + id + " leave from group-" + g.getKey());
					log.info("group size:" + c.size());
					break;
				}
			}
			/*if(c.isEmpty()){
				userGroup.remove(g.getKey());
			}*/
		}
		log.info("client id:" + id + " close");
		ctx.close();
	}
	/**
	 * *
	 * @param ctx
	 * @param req
	 * @throws Exception
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		// 如果HTTP解码失败，返回HHTP异常
		if(!req.decoderResult().isSuccess()  || (!"websocket".equals(req.headers().get("Upgrade")))){
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		// 构造握手响应返回，本机测试
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + serverIp + ":" + serverPort + "/websocket", null, false);
		handshaker = wsFactory.newHandshaker(req);
		if(handshaker == null){
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}
	/**
	 * *
	 * @param ctx
	 * @param frame
	 */
	private void handleWebSocketFrame(final ChannelHandlerContext ctx, WebSocketFrame frame) {
		// 判断是否是关闭链路的指令
		Channel channel = ctx.channel();
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(channel, (CloseWebSocketFrame) frame.retain());
			return;
		}
		// 判断是否是Ping消息
		if (frame instanceof PingWebSocketFrame) {
			channel.write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		//本例程仅支持文本消息，不支持二进制消息
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}
		// 返回应答消息
		String request = ((TextWebSocketFrame) frame).text();
		String response = "";
		try {
			GameMessage msg = (GameMessage)JSONObject.toBean(JSONObject.fromObject(request), GameMessage.class);
			int type = msg.getType();
			String groupId = msg.getGroupId();
			if(type == MessageType.CREATE_GROUP.getCode()){
				boolean hCreateGroup = false;
				for(Map.Entry<String, List<Channel>> g : userGroup.entrySet()){
					List<Channel> c = g.getValue();
					for (Channel ch : c) {
						if(channel.id().toString().equals(ch.id().toString())){
							response = createResponseMessage(MessageType.CREATE_GROUP, g.getKey(), c.size(), "已经获取过识别码,不可以重复获取");
							log.info("client id:" + ch.id().toString() + " have create group, groupId:" + g.getKey());
							hCreateGroup = true;
							break;
						}
					}
					if(hCreateGroup){
						break;
					}
				}
				if(!hCreateGroup){
					String time = String.valueOf(System.currentTimeMillis());
					groupId = time.substring(time.length() - 8, time.length());
					userGroup.put(groupId, new ArrayList<Channel>());
					List<Channel> userList = userGroup.get(groupId);
					userList.add(channel);
					response = createResponseMessage(MessageType.CREATE_GROUP, groupId, userList.size(), "创建一个组");
				}
				channel.writeAndFlush(new TextWebSocketFrame(response));
				
			} else if(type == MessageType.JOIN_GROUP.getCode()){
				List<Channel> userList = userGroup.get(groupId);
				if(userList == null || userList.isEmpty()){
					response = createResponseMessage(MessageType.JOIN_GROUP, groupId, 0, "用户组不存在");
				} else {
					boolean hJoinGroup = false;
					for (Channel ch : userList) {
						if(channel.id().toString().equals(ch.id().toString())){
							hJoinGroup = true;
							break;
						}
					}
					if(!hJoinGroup){
						userList.add(channel);
						log.info("client id:" + channel.id() + " join group:" + groupId + ", group size:" + userList.size());
						response = createResponseMessage(MessageType.JOIN_GROUP, groupId, userList.size(), "加入成功");
					} else {
						response = createResponseMessage(MessageType.JOIN_GROUP, groupId, userList.size(), "已经加入");
					}
				}
				channel.writeAndFlush(new TextWebSocketFrame(response));
				
			} else if(type == MessageType.GROUP_MESSAGE.getCode()){
				List<Channel> userList = userGroup.get(groupId);
				if(userList != null && !userList.isEmpty()){
					for (Channel ch : userList) {
						if(!channel.id().toString().equals(ch.id().toString())){
							//response = createResponseMessage(MessageType.GROUP_MESSAGE, groupId, request);
							msg.setStatus(0);
							msg.setUserCount(userList.size());
							ch.writeAndFlush(new TextWebSocketFrame(JSONObject.fromObject(msg).toString()));
						}
					}
				}
			} else {
				response = createResponseMessage(MessageType.CREATE_GROUP, "", 0, "");
				channel.writeAndFlush(new TextWebSocketFrame(response));
			}
		} catch (Exception e) {
			response = createResponseMessage(MessageType.CREATE_GROUP, "", 0, "");
			channel.writeAndFlush(new TextWebSocketFrame(response));
		}
		if (log.isLoggable(java.util.logging.Level.FINE)) {
			log.fine(String.format("%s received %s", channel, request));
		}
	}
	/**
	 * *
	 * @param type
	 * @param groupId
	 * @param body
	 * @return
	 */
	private String createResponseMessage(MessageType type, String groupId, int userCount, String body){
		GameMessage message = new GameMessage();
		message.setType(type.getCode());
		message.setGroupId(groupId);
		message.setBody(body);
		message.setUserCount(userCount);
		return JSONObject.fromObject(message).toString();
	}
	/**
	 * *
	 * @param ctx
	 * @param req
	 * @param res
	 */
	@SuppressWarnings("deprecation")
	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		 // 返回应答给客户端
		if(res.getStatus().code() != 200){
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}
		// 如果是非Keep-Alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if(!HttpUtil.isKeepAlive(req) || res.status().code() != 200){
			 f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
