package properties

import io.github.ufukhalis.hermessaging.core.properties.HermesProperties

data class RabbitMqProperties(
    val host: String,
) : HermesProperties<Nothing, Nothing>(emptyMap())
