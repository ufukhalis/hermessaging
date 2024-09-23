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
        require(hermesProperties is RabbitMqProperties)

        connectionFactory = ConnectionFactory().apply { host = hermesProperties.host }
        connection = connectionFactory.newConnection()
        channel = connection.createChannel()
    }

    override fun publish(messageRequest: MessageRequest<Nothing, String>): CompletableFuture<MessageResult<String>> {
        require(messageRequest is RabbitMqPublisherRequest)

        val callable = {
            channel.basicPublish(
                messageRequest.exchange,
                messageRequest.queue,
                messageRequest.properties,
                messageRequest.content.toByteArray(StandardCharsets.UTF_8)
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
