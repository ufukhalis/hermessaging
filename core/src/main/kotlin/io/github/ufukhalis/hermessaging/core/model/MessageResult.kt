package io.github.ufukhalis.hermessaging.core.model

sealed class MessageResult<out T> {
    data class Success<T>(val result: T) : MessageResult<T>()
    data class Failure(val throwable: Throwable) : MessageResult<Nothing>()
}
