package io.github.ufukhalis.hermessaging.kafka

import io.github.ufukhalis.hermessaging.core.model.MessageAsyncRequest
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class KafkaSubscriberClient<K, V>(
    hermesProperties: HermesProperties<K, V>
) : SubscriberClient<K, V, ConsumerRecords<K, V>>, AutoCloseable {

    private val executor = Executors.newSingleThreadExecutor()

    private val kafkaConsumer: KafkaConsumer<K, V>

    private var isRunning: AtomicBoolean = AtomicBoolean(true)

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

    override fun subscribeAsync(messageRequest: MessageAsyncRequest<K, V>) {
        executor.submit {
            isRunning.set(true)
            val kafkaRequest = messageRequest as KafkaAsyncSubscriberRequest
            kafkaConsumer.subscribe(kafkaRequest.destinations)
            while (isRunning.get()) {
                val consumerRecords = kafkaConsumer.poll(kafkaRequest.timeout)
                for (record in consumerRecords) {
                    kafkaRequest.callback.invoke(MessageResult.Success(record))
                }
            }
        }
    }

    override fun close() {
        isRunning.set(false)

        executor.shutdown()
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow()
        }

        kafkaConsumer.close()
    }
}
