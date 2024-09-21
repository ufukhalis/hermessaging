import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.github.ufukhalis.hermessaging.core.model.MessageRequest
import io.github.ufukhalis.hermessaging.core.model.MessageResult
import io.github.ufukhalis.hermessaging.core.properties.HermesProperties
import io.github.ufukhalis.hermessaging.core.publisher.PublisherClient
import model.RabbitMqPublisherRequest
import properties.RabbitMqProperties
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

class RabbitMqPublisherClient(
    hermesProperties: HermesProperties<Nothing, Nothing>
) : PublisherClient<Nothing, String, String> {

    private val connectionFactory: ConnectionFactory
    private val connection: Connection
    private val channel: Channel

    init {
        val rabbitMqProperties = hermesProperties as RabbitMqProperties
        connectionFactory = ConnectionFactory().apply { host = rabbitMqProperties.host }
        connection = connectionFactory.newConnection()
        channel = connection.createChannel()
    }

    override fun publish(messageRequest: MessageRequest<Nothing, String>): CompletableFuture<MessageResult<String>> {
        val callable = {
            val rabbitMqMessageRequest = messageRequest as RabbitMqPublisherRequest

            channel.basicPublish(
                rabbitMqMessageRequest.exchange,
                rabbitMqMessageRequest.queue,
                rabbitMqMessageRequest.properties,
                rabbitMqMessageRequest.content.toByteArray(StandardCharsets.UTF_8)
            )
        }
        return CompletableFuture.supplyAsync {
            callable.invoke()
            MessageResult.Success("Message published") as MessageResult<String>
        }.exceptionally {
            MessageResult.Failure(it) as MessageResult<String>
        }
    }

}
