package com.goyeau.socialnetwork.model

import java.time.Instant

import com.goyeau.socialnetwork.Record

case class Like(userId: Id[User], postId: Id[Post], updatedOn: Instant, unliked: Boolean)

object Like {
  implicit val record: Record[Id[Post], Like] = new Record[Id[Post], Like] {
    val topic = "likes"
    def key(like: Like): Id[Post] = like.postId
    def timestamp(like: Like): Long = like.updatedOn.toEpochMilli
  }
}
