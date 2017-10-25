// Copyright © FunctionalKotlin.com 2017. All rights reserved.

data class User(val name: String, val password: String)

fun validateName(name: String): Boolean =
    name.isNotEmpty() && name.length <= 15

fun validatePassword(password: String): Boolean =
    password.length >= 10

fun createUser(name: String, password: String): User? =
    User(name, password).takeIf {
        validateName(name) && validatePassword(password)
    }

fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks")
        ?.let(::println)
}