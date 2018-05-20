package com.goyeau

import java.net.URI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.lightbend.kafka.scala.streams.{KStreamS, StreamsBuilderS}
import io.circe.{Decoder, Encoder}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.streams.Consumed
import org.apache.kafka.streams.kstream.Produced

package object socialnetwork {

  implicit def encoderUri: Encoder[URI] = Encoder[String].contramap(_.toString)
  implicit def decoderUri: Decoder[URI] = Decoder[String].map(URI.create)

  implicit class KafkaProducerOps[K, V](kafkaProducer: KafkaProducer[K, V]) {
    def send(value: V)(implicit record: Record[K, V]): Future[RecordMetadata] = Future {
      kafkaProducer.send(new ProducerRecord(record.topic, null, record.timestamp(value), record.key(value), value)).get()
    }
  }

  implicit class StreamsBuilderSOps(streamsBuilder: StreamsBuilderS) {
    def streamFromRecord[V] = new StreamBuilder[V]

    class StreamBuilder[V] {
      def apply[K]()(implicit record: Record[K, V], consumed: Consumed[K, V]): KStreamS[K, V] =
        streamsBuilder.stream[K, V](record.topic)
    }
  }

  implicit class KStreamSOps[K, V](stream: KStreamS[K, V]) {
    def toTopic(implicit record: Record[K, V], produced: Produced[K, V]) = stream.to(record.topic)
  }
}
