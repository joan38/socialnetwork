package com.goyeau.socialnetwork.model

import com.goyeau.socialnetwork.Record

case class DenormalisedPost(post: Post, author: User, interactions: DenormalisedPost.Interactions)

object DenormalisedPost {
  case class Interactions(likes: Set[Like], comments: Int)

  implicit val record = new Record[Id[Post], DenormalisedPost] {
    val topic = "denormalised-posts"
    def key(denormalisedPost: DenormalisedPost): Id[Post] = denormalisedPost.post.id
    def timestamp(denormalisedPost: DenormalisedPost): Long = denormalisedPost.post.updatedOn.toEpochMilli
  }
}
