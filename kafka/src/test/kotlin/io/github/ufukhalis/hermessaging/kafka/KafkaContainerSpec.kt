package io.github.ufukhalis.hermessaging.kafka

import io.kotest.core.spec.style.ShouldSpec
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.testcontainers.kafka.KafkaContainer

open class KafkaContainerSpec : ShouldSpec() {
    private val kafkaContainer = KafkaContainer("apache/kafka-native:3.8.0")
    lateinit var bootstrapServers: String

    lateinit var testProducer: KafkaProducer<String, String>

    init {
        beforeSpec {
            kafkaContainer.start()
            bootstrapServers = kafkaContainer.bootstrapServers
            testProducer = KafkaProducer<String, String>(
                mapOf(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name
                )
            )
        }

        afterSpec {
            kafkaContainer.stop()
        }
    }
}


