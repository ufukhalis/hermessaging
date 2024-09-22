package io.github.ufukhalis.hermessaging.kafka

import io.github.ufukhalis.hermessaging.core.model.MessageResult
import io.github.ufukhalis.hermessaging.kafka.model.KafkaAsyncSubscriberRequest
import io.github.ufukhalis.hermessaging.kafka.model.KafkaSubscriberRequest
import io.github.ufukhalis.hermessaging.kafka.properties.KafkaSubscriberProperties
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.concurrent.TimeUnit

class KafkaSubscriberClientTest : KafkaContainerSpec() {

    private lateinit var properties: KafkaSubscriberProperties<String, String>
    private lateinit var kafkaSubscriberClient: KafkaSubscriberClient<String, String>

    init {
        beforeSpec {
            super.beforeSpec(this)
            properties = KafkaSubscriberProperties(
                brokers = listOf(bootstrapServers),
                groupId = "group-id-1",
                offset = "earliest",
                keyDeserializer = StringDeserializer(),
                valueDeserializer = StringDeserializer()
            )
            kafkaSubscriberClient = KafkaSubscriberClient(properties)
        }

        afterSpec {
            super.afterSpec(this)
            kafkaSubscriberClient.close()
        }

        should("not consume any events") {
            val request = KafkaSubscriberRequest<String, String>(
                destinations = listOf("topic-1"),
                timeout = Duration.ofSeconds(3)
            )

            val result = kafkaSubscriberClient.subscribe(request)
            result::class shouldBe MessageResult.Success::class

            val records = (result as MessageResult.Success).result
            records.shouldBeEmpty()
        }

        should("consume events") {
            testProducer.send(
                ProducerRecord("topic-2", "key", "value")
            )

            val request = KafkaSubscriberRequest<String, String>(
                destinations = listOf("topic-2"),
                timeout = Duration.ofSeconds(3)
            )

            val result = kafkaSubscriberClient.subscribe(request)
            result::class shouldBe MessageResult.Success::class

            val records = (result as MessageResult.Success).result
            records.shouldNotBeEmpty()
            records.count() shouldBe 1
            records.first().key() shouldBe "key"
            records.first().value() shouldBe "value"
        }

        should("consume events async") {
            testProducer.send(
                ProducerRecord("topic-3", "key", "value")
            )

            val callbackResults = mutableListOf<ConsumerRecord<String, String>>()
            val callback = { result: MessageResult<ConsumerRecord<String, String>> ->
                if (result is MessageResult.Success) {
                    callbackResults.add(result.result)
                }
            }

            val request = KafkaAsyncSubscriberRequest(
                destinations = listOf("topic-3"),
                timeout = Duration.ofSeconds(3),
                callback = callback
            )

            kafkaSubscriberClient.subscribeAsync(request)

            TimeUnit.SECONDS.sleep(5)

            callbackResults.map { it.value() } shouldContain "value"
        }
    }
}
