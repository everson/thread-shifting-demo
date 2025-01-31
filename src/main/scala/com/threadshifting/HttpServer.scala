package com.threadshifting

import com.typesafe.scalalogging.StrictLogging
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelInitializer, ChannelOption, EventLoopGroup}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}

class HttpServer extends StrictLogging {
  private val bossGroup: EventLoopGroup = new NioEventLoopGroup(1)
  private val workerGroup: EventLoopGroup = new NioEventLoopGroup()
  private val client = new HttpClient()

  def start(): Unit = {
    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new ChannelInitializer[SocketChannel]() {
          @throws[Exception]
          override def initChannel(ch: SocketChannel): Unit = {
            ch.pipeline().addLast(new HttpServerCodec())
            ch.pipeline().addLast(new HttpObjectAggregator(65536))
            ch.pipeline().addLast(new HttpServerHandler(client))
          }
        })
        .option(ChannelOption.SO_BACKLOG, Integer.valueOf(128))
        .childOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)

      val f: ChannelFuture = b.bind(8080).sync()
      logger.info("HTTP server started on port 8080")
      f.channel().closeFuture().sync()
    } finally {
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
    }
  }
}