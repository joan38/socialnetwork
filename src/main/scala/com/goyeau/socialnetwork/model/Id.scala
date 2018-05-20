package com.goyeau.socialnetwork.model

import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}

case class Id[Resource](value: String) extends AnyVal
object Id {
  implicit def encoder[T]: Encoder[Id[T]] = Encoder[String].contramap(_.value)
  implicit def decoder[T]: Decoder[Id[T]] = Decoder[String].map(Id[T])
  implicit def keyEncoder[T] : KeyEncoder[Id[T]] = KeyEncoder[String].contramap(_.value)
  implicit def keyDecoder[T] : KeyDecoder[Id[T]] = KeyDecoder[String].map(Id[T])
}
