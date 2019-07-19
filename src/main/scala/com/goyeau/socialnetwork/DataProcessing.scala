package com.goyeau.socialnetwork

import com.goyeau.kafka.streams.circe.CirceSerdes._
import com.goyeau.socialnetwork.model._
import com.lightbend.kafka.scala.streams.ImplicitConversions._
import com.lightbend.kafka.scala.streams.StreamsBuilderS
import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe.syntax._
import java.util.Properties

import monocle.macros.syntax.lens._
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore

object DataProcessing extends App {
  println("Starting streams")

  val streamsBuilder = new StreamsBuilderS()
  val usersStream = streamsBuilder.streamFromRecord[User]()
  val postsStream = streamsBuilder.streamFromRecord[Post]()
  val commentsStream = streamsBuilder.streamFromRecord[Comment]()
  val likesStream = streamsBuilder.streamFromRecord[Like]()

  def materializer[K, V](implicit serdeK: Serde[K], serdeV: Serde[V]) =
    Materialized.`with`[K, V, KeyValueStore[Bytes, Array[Byte]]](serdeK, serdeV)

  val usersByKey = usersStream
    .groupByKey
    .reduce((first, second) => if (first.updatedOn.isAfter(second.updatedOn)) first else second)

  val postsByAuthor = postsStream
    .groupBy((_, post) => post.author)
    .aggregate(
      () => Map.empty[Id[Post], Post],
      (_, post: Post, posts: Map[Id[Post], Post]) =>
        if (posts.get(post.id).exists(_.updatedOn.isAfter(post.updatedOn))) posts
        else posts + (post.id -> post),
      materializer[Id[User], Map[Id[Post], Post]])
    .mapValues(_.values.toSet)

  val likesByKey = likesStream
    .groupByKey
    .aggregate(
      () => Set.empty[Like],
      (_, like: Like, likes: Set[Like]) => if (like.unliked) likes - like else likes + like,
      materializer[Id[Post], Set[Like]])

  val commentCountByKey = commentsStream
    .groupByKey
    .aggregate(
      () => Set.empty[Id[Comment]],
      (_, comment: Comment, commentIds: Set[Id[Comment]]) =>
        if (comment.deleted) commentIds - comment.id else commentIds + comment.id,
      materializer[Id[Post], Set[Id[Comment]]])
    .mapValues(_.size)

  postsByAuthor
    .join(usersByKey,
      (posts: Set[Post], author: User) =>
        posts.map(DenormalisedPost(_, author, DenormalisedPost.Interactions(Set.empty, 0))))
    .toStream
    .flatMapValues(identity)
    .groupBy((_, denormalisedPost) => denormalisedPost.post.id)
    .reduce((first, second) => if (first.post.updatedOn.isAfter(second.post.updatedOn)) first else second)
    .leftJoin(likesByKey,
      (denormalisedPost: DenormalisedPost, likes: Set[Like]) =>
        Option(likes).fold(denormalisedPost)(denormalisedPost.lens(_.interactions.likes).set(_)))
    .leftJoin(commentCountByKey,
      (denormalisedPost: DenormalisedPost, commentCount: Int) =>
        denormalisedPost.lens(_.interactions.comments).set(commentCount))
    .toStream
    .toTopic

  val config = new Properties()
  config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
  config.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream0")
  val streams = new KafkaStreams(streamsBuilder.build(), config)
  Runtime.getRuntime.addShutdownHook(new Thread(() => streams.close()))
  streams.start()
}
