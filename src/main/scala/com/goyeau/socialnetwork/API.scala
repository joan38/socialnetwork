package com.goyeau.socialnetwork

import com.goyeau.kafka.streams.circe.CirceSerdes._
import com.goyeau.socialnetwork.model._
import io.circe.generic.auto._
import io.circe.java8.time._
import java.net.URI
import java.time.Instant
import java.util.Properties

import scala.concurrent.Await
import scala.concurrent.duration._

import org.apache.kafka.clients.producer.ProducerConfig

object API extends App {
  println("Starting producer")

  val config = new Properties()
  config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)

  // Send a user
  val userProducer = Producer[User](config)

  val user = User(
    Id[User]("user0"),
    Instant.now(),
    URI.create("https://some-uri"),
    "Test",
    verified = false,
    deleted = false
  )
  Await.result(userProducer.send(user), 1.minute)

  userProducer.close()

  // Send a post
  val postProducer = Producer[Post](config)

  val post = Post(
    Id[Post]("post0"),
    Instant.now(),
    user.id,
    "Some text",
    URI.create("https://some-uri"),
    deleted = false
  )
  Await.result(postProducer.send(post), 1.minute)

  postProducer.close()
}
