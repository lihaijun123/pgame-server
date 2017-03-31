package com.focus3d.pgame.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import com.focus3d.pgame.server.handler.GameServerHandler;

/**
 * websocket server
 * *
 * @author lihaijun
 *
 */
public class GameServer {
	
	public static final String SERVER_IP = "172.17.13.77";
	public static final int SERVER_PORT = 8080;
	/**
	 * *
	 * @param port
	 * @throws Exception
	 */
	public void run(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("http-codec", new HttpServerCodec());
					pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
					pipeline.addLast("http-chunked", new ChunkedWriteHandler());
					pipeline.addLast("handler", new GameServerHandler(SERVER_IP, SERVER_PORT));				
				}
			});
			Channel ch = b.bind(port).sync().channel();
			System.out.println("web socket server started at port " + port + ".");
			System.out.println("open your browser and navigate to http://" + SERVER_IP + ":" + port + "/");
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
        new GameServer().run(SERVER_PORT);
    }
}
