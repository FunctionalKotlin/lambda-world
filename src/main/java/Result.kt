// Copyright © FunctionalKotlin.com 2017. All rights reserved.

sealed class Result<out A, out E>

data class Success<out A>(val value: A): Result<A, Nothing>()

data class Failure<out E>(val error: E): Result<Nothing, E>()

fun <A, E, B> Result<A, E>.map(transform: (A) -> B): Result<B, E> =
    when(this) {
        is Success -> Success(transform(this.value))
        is Failure -> this
    }

fun <A> Result<A, *>.ifSuccess(continuation: (A) -> Unit) {
    if (this is Success) continuation(value)
}

operator infix fun <T, E>
    Validator<T, E>.plus(validator: Validator<T, E>): Validator<T, E> = {

    val result = this(it)

    when (result) {
        is Success -> validator(it)
        is Failure -> result
    }
}