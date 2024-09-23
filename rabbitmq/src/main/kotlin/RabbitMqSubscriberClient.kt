import com.rabbitmq.client.*
import io.github.ufukhalis.hermessaging.core.model.MessageAsyncRequest
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
        require(hermesProperties is RabbitMqProperties)

        connectionFactory = ConnectionFactory().apply {
            host = hermesProperties.host

        }
        connection = connectionFactory.newConnection()
        channel = connection.createChannel()
    }

    override fun subscribe(messageRequest: MessageRequest<Nothing, String>): MessageResult<GetResponse> {
        require(messageRequest is RabbitMqSubscriberRequest)

        try {
            val response = channel.basicGet(messageRequest.queue, messageRequest.autoAck)
            return MessageResult.Success(response)
        } catch (ex: Exception) {
            return MessageResult.Failure(ex)
        }
    }

    fun GetResponse.ack(multiple: Boolean = false) =
        channel.basicAck(this.envelope.deliveryTag, multiple)

    fun Envelope.ack(multiple: Boolean = false) = channel.basicAck(this.deliveryTag, multiple)

    override fun subscribeAsync(messageRequest: MessageAsyncRequest<Nothing, String>) {
        require(messageRequest is RabbitMqAsyncSubscriberRequest)

        executor.submit {
            channel.basicConsume(messageRequest.queue, messageRequest.autoAck, messageRequest.callback)
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
