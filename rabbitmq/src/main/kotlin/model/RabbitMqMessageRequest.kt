package model

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Consumer
import io.github.ufukhalis.hermessaging.core.model.MessageAsyncRequest
import io.github.ufukhalis.hermessaging.core.model.MessageRequest

data class RabbitMqPublisherRequest(
    val queue: String,
    val exchange: String = "",
    val properties: BasicProperties? = null,
    val content: String
) : MessageRequest<Nothing, String>()

data class RabbitMqSubscriberRequest(
    val queue: String,
    val autoAck: Boolean = true
) : MessageRequest<Nothing, String>()

data class RabbitMqAsyncSubscriberRequest(
    val queue: String,
    val autoAck: Boolean = true,
    val callback: Consumer
) : MessageAsyncRequest<Nothing, String>()
