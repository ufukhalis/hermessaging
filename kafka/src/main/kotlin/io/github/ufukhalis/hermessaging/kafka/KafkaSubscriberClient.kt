package io.github.ufukhalis.hermessaging.kafka

import io.github.ufukhalis.hermessaging.core.model.MessageRequest
import io.github.ufukhalis.hermessaging.core.model.MessageResult
import io.github.ufukhalis.hermessaging.core.properties.HermesProperties
import io.github.ufukhalis.hermessaging.core.subscriber.SubscriberClient
import io.github.ufukhalis.hermessaging.kafka.model.KafkaAsyncSubscriberRequest
import io.github.ufukhalis.hermessaging.kafka.model.KafkaSubscriberRequest
import io.github.ufukhalis.hermessaging.kafka.properties.KafkaSubscriberProperties
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.concurrent.Executors

class KafkaSubscriberClient<K, V>(
    hermesProperties: HermesProperties<K, V>
) : SubscriberClient<K, V, ConsumerRecords<K, V>>, AutoCloseable {

    private val executor = Executors.newSingleThreadExecutor()

    private val kafkaConsumer: KafkaConsumer<K, V>

    init {
        val properties = hermesProperties as KafkaSubscriberProperties<K, V>
        val keyDeserializer = properties.keyDeserializer
        val valueDeserializer = properties.valueDeserializer

        val combinedProperties = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to properties.brokers.joinToString(),
            ConsumerConfig.GROUP_ID_CONFIG to properties.groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to properties.offset,

            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to keyDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to valueDeserializer::class.java.name,
        ) + properties.additionalConfig

        kafkaConsumer = KafkaConsumer(combinedProperties)
    }


    override fun subscribe(messageRequest: MessageRequest<K, V>): MessageResult<ConsumerRecords<K, V>> {
        val kafkaRequest = messageRequest as KafkaSubscriberRequest
        kafkaConsumer.subscribe(kafkaRequest.destinations)
        try {
            val consumerRecords = kafkaConsumer.poll(kafkaRequest.timeout)
            return MessageResult.Success(consumerRecords)
        } catch (ex: Exception) {
            return MessageResult.Failure(ex)
        }
    }

    override fun subscribeAsync(messageRequest: MessageRequest<K, V>) {
        executor.submit {
            val kafkaRequest = messageRequest as KafkaAsyncSubscriberRequest
            kafkaConsumer.subscribe(kafkaRequest.destinations)
            while (true) {
                val consumerRecords = kafkaConsumer.poll(kafkaRequest.timeout)
                println("polling...$consumerRecords")
                for (record in consumerRecords) {
                    kafkaRequest.callback.invoke(MessageResult.Success(record))
                }
            }
        }
    }

    override fun close() {
        kafkaConsumer.close()
    }
}
