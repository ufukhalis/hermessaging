package io.github.ufukhalis.hermessaging.core.subscriber

import io.github.ufukhalis.hermessaging.core.model.MessageAsyncRequest
import io.github.ufukhalis.hermessaging.core.model.MessageRequest
import io.github.ufukhalis.hermessaging.core.model.MessageResult

interface SubscriberClient<K, V, R> {

    fun subscribe(messageRequest: MessageRequest<K, V>): MessageResult<R>

    fun subscribeAsync(messageRequest: MessageAsyncRequest<K, V>)
}

