package io.github.ufukhalis.hermessaging.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.ufukhalis.hermessaging.core.model.MessageResult
import io.github.ufukhalis.hermessaging.kafka.model.KafkaPublisherRequest
import io.github.ufukhalis.hermessaging.kafka.properties.KafkaPublisherProperties
import io.kotest.matchers.shouldBe
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer

class KafkaPublisherClientTest : KafkaContainerSpec() {
    private lateinit var properties: KafkaPublisherProperties<String, Event>
    private lateinit var publisherClient: KafkaPublisherClient<String, Event>

    init {
        beforeTest {
            properties = KafkaPublisherProperties(
                brokers = listOf(bootstrapServers),
                keySerializer = StringSerializer(),
                valueSerializer = EventSerializer(),
                additionalConfig = mapOf(
                    "auto.create.topics.enable" to true
                )
            )

            publisherClient = KafkaPublisherClient(properties)
        }

        afterTest {
            publisherClient.close()
        }

        should("produce kafka event") {
            val messageRequest = KafkaPublisherRequest(
                "topic-1",
                "key",
                Event(1, "event-name")
            )

            val result = publisherClient.publish(messageRequest).get()
            result::class shouldBe MessageResult.Success::class
        }
    }
}

data class Event(val id: Int, val name: String)

class EventSerializer : Serializer<Event> {
    override fun serialize(p0: String?, p1: Event?): ByteArray? {
        return p1?.let { jacksonObjectMapper().writeValueAsBytes(it) }
    }
}
