package io.github.ufukhalis.hermessaging.kafka.properties

import io.github.ufukhalis.hermessaging.core.properties.HermesProperties
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

data class KafkaPublisherProperties<K, V> (
    val brokers: List<String>,
    val keySerializer: Serializer<K>,
    val valueSerializer: Serializer<V>,
    override val additionalConfig: Map<String, Any> = emptyMap()
): HermesProperties<K, V>(additionalConfig)

data class KafkaSubscriberProperties<K, V>(
    val brokers: List<String>,
    val groupId: String,
    val offset: String,
    val keyDeserializer: Deserializer<K>,
    val valueDeserializer: Deserializer<V>,
    override val additionalConfig: Map<String, Any> = emptyMap()
): HermesProperties<K, V>(additionalConfig)
