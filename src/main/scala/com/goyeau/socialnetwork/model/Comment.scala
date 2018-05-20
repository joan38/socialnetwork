package com.goyeau.socialnetwork.model

import java.time.Instant

import com.goyeau.socialnetwork.Record

case class Comment(id: Id[Comment],
                   postId: Id[Post],
                   updatedOn: Instant,
                   author: Id[User],
                   text: String,
                   deleted: Boolean)

object Comment {
  implicit val record: Record[Id[Post], Comment] = new Record[Id[Post], Comment] {
    val topic = "comments"
    def key(comment: Comment): Id[Post] = comment.postId
    def timestamp(comment: Comment): Long = comment.updatedOn.toEpochMilli
  }
}
