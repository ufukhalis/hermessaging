package io.github.ufukhalis.hermessaging.kafka.model

import io.github.ufukhalis.hermessaging.core.model.MessageRequest
import io.github.ufukhalis.hermessaging.core.model.MessageResult
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration

data class KafkaPublisherRequest<K, V>(
    val destination: String,
    val key: K,
    val content: V
) : MessageRequest<K, V>()

data class KafkaSubscriberRequest<K, V>(
    val destinations: List<String>,
    val timeout: Duration
) : MessageRequest<K, V>()

data class KafkaAsyncSubscriberRequest<K, V>(
    val destinations: List<String>,
    val timeout: Duration,
    val callback: (message: MessageResult<ConsumerRecord<K, V>>) -> Unit
) : MessageRequest<K, V>()
