// Copyright © FunctionalKotlin.com 2017. All rights reserved.

data class User(val name: String, val password: String)

enum class UserError {
    USERNAME_OUT_OF_BOUNDS,
    PASSWORD_TOO_SHORT
}

fun validateName(name: String): Boolean =
    name.isNotEmpty() && name.length <= 15

fun validatePassword(password: String): Boolean =
    password.length >= 10

fun createUser(name: String, password: String): Result<User, UserError> {
    if (!validateName(name))
        return Failure(UserError.USERNAME_OUT_OF_BOUNDS)

    if (!validatePassword(password))
        return Failure(UserError.PASSWORD_TOO_SHORT)

    return User(name, password).let(::Success)
}

fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks")
        .map { it.name }
        .ifSuccess(::println)
}