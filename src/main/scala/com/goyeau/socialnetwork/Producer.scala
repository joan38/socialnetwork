package com.goyeau.socialnetwork

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.Serializer

object Producer {
  def apply[V] = new ProducerBuilder[V]
  class ProducerBuilder[V] {
    def apply[K](config: Properties)(implicit record: Record[K, V],
      keySerializer: Serializer[K],
      valueSerializer: Serializer[V]): KafkaProducer[K, V] =
      new KafkaProducer(config, keySerializer, valueSerializer)
  }
}