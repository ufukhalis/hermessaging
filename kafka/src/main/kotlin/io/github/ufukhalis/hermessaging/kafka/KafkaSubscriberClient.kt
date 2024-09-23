package io.github.ufukhalis.hermessaging.kafka

import io.github.ufukhalis.hermessaging.core.Util.withRetry
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
        require(hermesProperties is KafkaSubscriberProperties)

        val keyDeserializer = hermesProperties.keyDeserializer
        val valueDeserializer = hermesProperties.valueDeserializer

        val combinedProperties = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to hermesProperties.brokers.joinToString(),
            ConsumerConfig.GROUP_ID_CONFIG to hermesProperties.groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to hermesProperties.offset,

            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to keyDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to valueDeserializer::class.java.name,
        ) + hermesProperties.additionalConfig

        kafkaConsumer = KafkaConsumer(combinedProperties)
    }


    override fun subscribe(messageRequest: MessageRequest<K, V>): MessageResult<ConsumerRecords<K, V>> {
        require(messageRequest is KafkaSubscriberRequest)

        kafkaConsumer.subscribe(messageRequest.destinations)
        try {
            val consumerRecords = kafkaConsumer.poll(messageRequest.timeout)
            return MessageResult.Success(consumerRecords)
        } catch (ex: Exception) {
            return MessageResult.Failure(ex)
        }
    }

    override fun subscribeAsync(messageRequest: MessageAsyncRequest<K, V>) {
        require(messageRequest is KafkaAsyncSubscriberRequest)

        executor.submit {
            isRunning.set(true)
            kafkaConsumer.subscribe(messageRequest.destinations)
            while (isRunning.get()) {
                try {
                    withRetry(retryPolicy = messageRequest.retryPolicy) {
                        val consumerRecords = kafkaConsumer.poll(messageRequest.timeout)
                        for (record in consumerRecords) {
                            messageRequest.callback.invoke(MessageResult.Success(record))
                        }
                    }
                } catch (ex: Exception) {
                    messageRequest.callback.invoke(MessageResult.Failure(ex))
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
