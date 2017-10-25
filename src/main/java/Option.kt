// Copyright © FunctionalKotlin.com 2017. All rights reserved.

sealed class Option<out A>

object None : Option<Nothing>()
data class Just<out A>(val value: A) : Option<A>()

fun <A, B> Option<A>.map(transform: (A) -> B): Option<B> = when(this) {
    is Just -> Just(transform(this.value))
    is None -> None
}

fun <A> Option<A>.ifPresent(continuation: (A) -> Unit) {
    if (this is Just) continuation(value)
}

fun <A> Option<Option<A>>.flatten(): Option<A> = when(this) {
    is Just -> value
    is None -> None
}

fun <A, B> Option<A>.mapAndFlatten(transform: (A) -> Option<B>): Option<B> =
    map(transform).flatten()