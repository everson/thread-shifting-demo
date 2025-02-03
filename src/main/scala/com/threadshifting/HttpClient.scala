package com.threadshifting

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import scalaz.\/-
import scalaz.concurrent.Task
import scalaz.syntax.either._

class HttpClient extends StrictLogging {
  private val vertx = Vertx.vertx()
  private val client = WebClient.create(vertx)

  def forwardRequestTask(body: String): Task[String] = {
    logger.info("HttpClient.forwardRequest")
    Task.async[String] { callback =>
      logger.info("HttpClient.forwardRequest.async")
      client.postAbs("https://httpbin.org/anything")
        .putHeader("Content-Type", "application/json")
        .as(BodyCodec.string())
        .sendBuffer(Buffer.buffer(body))
        .onSuccess { response =>
          logger.info("HttpClient.forwardRequest.async.sendBuffer.onSuccess")
          Thread.sleep(100)
          callback(response.body().right)
        }
        .onFailure { exception =>
          logger.error("Failed to forward request", exception)
          callback(exception.left)
        }
    }.flatMap { str: String =>
      logger.info("HttpClient.forwardRequestTask.flatMap")
      Thread.sleep(100)

      Task.async[String] { callback =>
        Thread.sleep(100)
        logger.info("HttpClient.forwardRequestTask.async")
        callback(\/-(str))
      }
    }
  }

  def forwardRequestIO(body: String): IO[String] = {
    logger.info("HttpClient.forwardRequest")
    IO.async_[String] { callback =>
      logger.info("HttpClient.forwardRequest.async")
      client.postAbs("https://httpbin.org/anything")
        .putHeader("Content-Type", "application/json")
        .as(BodyCodec.string())
        .sendBuffer(Buffer.buffer(body))
        .onSuccess { response =>
          logger.info("HttpClient.forwardRequest.async.sendBuffer.onSuccess")
          callback(Right(response.body()))
        }
        .onFailure { exception =>
          logger.error("Failed to forward request", exception)
          callback(Left(exception))
        }
    }
  }
}
