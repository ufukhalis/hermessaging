package io.github.ufukhalis.hermessaging.kafka

import io.github.ufukhalis.hermessaging.core.Util.toCompletableFuture
import io.github.ufukhalis.hermessaging.core.model.MessageRequest
import io.github.ufukhalis.hermessaging.core.model.MessageResult
import io.github.ufukhalis.hermessaging.core.properties.HermesProperties
import io.github.ufukhalis.hermessaging.core.publisher.PublisherClient
import io.github.ufukhalis.hermessaging.kafka.model.KafkaPublisherRequest
import io.github.ufukhalis.hermessaging.kafka.properties.KafkaPublisherProperties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.CompletableFuture

class KafkaPublisherClient<K, V>(
    hermesProperties: HermesProperties<K, V>
) : PublisherClient<K, V, RecordMetadata>, AutoCloseable {

    private val producer: KafkaProducer<K, V>

    init {
        val properties = hermesProperties as KafkaPublisherProperties<K, V>
        val keySerializer = properties.keySerializer
        val valueSerializer = properties.valueSerializer

        val combinedProperties = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to properties.brokers.joinToString(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to keySerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to valueSerializer::class.java.name,
        ) + properties.additionalConfig

        producer = KafkaProducer(combinedProperties, keySerializer, valueSerializer)
    }

    override fun publish(messageRequest: MessageRequest<K, V>): CompletableFuture<MessageResult<RecordMetadata>> {
        val kafkaPublisherRequest = messageRequest as KafkaPublisherRequest<K, V>

        val producerRecord = ProducerRecord(
            kafkaPublisherRequest.destination,
            kafkaPublisherRequest.key,
            kafkaPublisherRequest.content
        )

        return producer.send(producerRecord)
            .toCompletableFuture()
            .thenApply {
                MessageResult.Success(it) as MessageResult<RecordMetadata>
            }.exceptionally {
                MessageResult.Failure(it) as MessageResult<RecordMetadata>
            }
    }

    override fun close() {
        producer.close()
    }

}
