package com.threadshifting

import com.typesafe.scalalogging.StrictLogging
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.util.concurrent.GenericFutureListener
import scalaz.{-\/, \/-}

class HttpServerHandler(client: HttpClient) extends SimpleChannelInboundHandler[FullHttpRequest] with StrictLogging {
  override def channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest): Unit = {
    val uri = request.uri()
    val body = request.content().toString(io.netty.util.CharsetUtil.UTF_8)

    logger.info("channelRead0")
    if (uri == "/scalaz-task") {
      handleScalazTask(body, ctx)
    } else if (uri == "/cats-io") {
      handleCatsIO(body, ctx)
    } else {
      val httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)
      ctx.writeAndFlush(httpResponse)
    }
  }

  private def handleScalazTask(requestBody: String, ctx: ChannelHandlerContext): Unit = {
    logger.info("handleScalazTask")
    client.forwardRequestTask(requestBody).unsafePerformAsync {
      case \/-(response) =>
        logger.info("handleScalazTask.unsafePerformAsync")
        writeResponse(ctx, response)

      case -\/(exception) => handleError(exception, ctx)
    }
  }

  private def handleCatsIO(requestBody: String, ctx: ChannelHandlerContext): Unit = {
    logger.info("handleCatsIO")
    import cats.effect.unsafe.implicits.global
    client.forwardRequestIO(requestBody).unsafeRunAsync {
      case Right(response) =>
        logger.info("handleCatsIO.unsafeRunAsync")
        writeResponse(ctx, response)

      case Left(exception) => handleError(exception, ctx)
    }
  }

  private def handleError(t: Throwable, ctx: ChannelHandlerContext): Unit = {
    logger.error("Failed to forward request", t)
    val httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
    ctx.writeAndFlush(httpResponse)
  }

  private def writeResponse(ctx: ChannelHandlerContext, response: String): Unit = {
    logger.info("HttpServerHandler.writeResponse")
    val responseContent = Unpooled.copiedBuffer(response, io.netty.util.CharsetUtil.UTF_8)
    val httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, responseContent)
    httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json")
    httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseContent.readableBytes())
    val completionListener = new GenericFutureListener[io.netty.util.concurrent.Future[Void]]() {
      def operationComplete(future: io.netty.util.concurrent.Future[Void]): Unit = {
        logger.info("HttpServerHandler.writeResponse.operationComplete")
      }
    }
    ctx.writeAndFlush(httpResponse, ctx.newPromise().addListener(completionListener))
  }

}