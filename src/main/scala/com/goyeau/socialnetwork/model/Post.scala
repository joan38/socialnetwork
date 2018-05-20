package com.goyeau.socialnetwork.model

import java.net.URI
import java.time.Instant

import com.goyeau.socialnetwork.Record

case class Post(id: Id[Post], updatedOn: Instant, author: Id[User], text: String, image: URI, deleted: Boolean)

object Post {
  implicit val record: Record[Id[Post], Post] = new Record[Id[Post], Post] {
    val topic = "posts"
    def key(post: Post): Id[Post] = post.id
    def timestamp(post: Post): Long = post.updatedOn.toEpochMilli
  }
}
