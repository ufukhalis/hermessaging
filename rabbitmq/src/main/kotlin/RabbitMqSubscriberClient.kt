import com.rabbitmq.client.*
import io.github.ufukhalis.hermessaging.core.model.MessageRequest
import io.github.ufukhalis.hermessaging.core.model.MessageResult
import io.github.ufukhalis.hermessaging.core.properties.HermesProperties
import io.github.ufukhalis.hermessaging.core.subscriber.SubscriberClient
import model.RabbitMqAsyncSubscriberRequest
import model.RabbitMqSubscriberRequest
import properties.RabbitMqProperties
import java.util.concurrent.Executors


class RabbitMqSubscriberClient(
    hermesProperties: HermesProperties<Nothing, Nothing>
) : SubscriberClient<Nothing, String, GetResponse> {

    private val executor = Executors.newSingleThreadExecutor()

    private val connectionFactory: ConnectionFactory
    private val connection: Connection
    private val channel: Channel

    init {
        val rabbitMqProperties = hermesProperties as RabbitMqProperties
        connectionFactory = ConnectionFactory().apply {
            host = rabbitMqProperties.host

        }
        connection = connectionFactory.newConnection()
        channel = connection.createChannel()
    }

    override fun subscribe(messageRequest: MessageRequest<Nothing, String>): MessageResult<GetResponse> {
        val rabbitMqRequest = messageRequest as RabbitMqSubscriberRequest
        try {
            val response = channel.basicGet(rabbitMqRequest.queue, rabbitMqRequest.autoAck)
            return MessageResult.Success(response)
        } catch (ex: Exception) {
            return MessageResult.Failure(ex)
        }
    }

    fun GetResponse.ack(multiple: Boolean = false) =
        channel.basicAck(this.envelope.deliveryTag, multiple)

    fun Envelope.ack(multiple: Boolean = false) = channel.basicAck(this.deliveryTag, multiple)

    override fun subscribeAsync(messageRequest: MessageRequest<Nothing, String>) {
        executor.submit {
            val rabbitMqRequest = messageRequest as RabbitMqAsyncSubscriberRequest
            channel.basicConsume(rabbitMqRequest.queue, rabbitMqRequest.autoAck, rabbitMqRequest.callback)
        }
    }

    fun declareQueue(
        queueName: String,
        durable: Boolean = false,
        exclusive: Boolean = false,
        autoDelete: Boolean = false,
        args: Map<String, Any> = emptyMap()
    ): AMQP.Queue.DeclareOk {
        return channel.queueDeclare(queueName, durable, exclusive, autoDelete, args)
    }
}
