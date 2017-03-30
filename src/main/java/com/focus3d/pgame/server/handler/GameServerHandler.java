package com.focus3d.pgame.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
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

import java.util.logging.Logger;
/**
 * *
 * @author lihaijun
 *
 */
public class GameServerHandler extends SimpleChannelInboundHandler<Object> {
	
	private static final Logger logger = Logger.getLogger(GameServerHandler.class.getName());
	   
	private WebSocketServerHandshaker handshaker;
	
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
	/**
	 * *
	 * @param ctx
	 * @param req
	 * @throws Exception
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		// ���HTTP����ʧ�ܣ�����HHTP�쳣
		if(!req.decoderResult().isSuccess()  || (!"websocket".equals(req.headers().get("Upgrade")))){
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		// ����������Ӧ���أ���������
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket", null, false);
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
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// �ж��Ƿ��ǹر���·��ָ��
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		// �ж��Ƿ���Ping��Ϣ
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		//�����̽�֧���ı���Ϣ����֧�ֶ�������Ϣ
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}
		// ����Ӧ����Ϣ
		String request = ((TextWebSocketFrame) frame).text();
		if (logger.isLoggable(java.util.logging.Level.FINE)) {
			logger.fine(String.format("%s received %s", ctx.channel(), request));
		}
		ctx.channel().write(new TextWebSocketFrame(request + " , ��ӭʹ��Netty WebSocket��������ʱ�̣�" + new java.util.Date().toString()));
	}
	/**
	 * *
	 * @param ctx
	 * @param req
	 * @param res
	 */
	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		 // ����Ӧ����ͻ���
		if(res.getStatus().code() != 200){
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}
		// ����Ƿ�Keep-Alive���ر�����
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
