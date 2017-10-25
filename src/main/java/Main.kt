// Copyright © FunctionalKotlin.com 2017. All rights reserved.

data class User(val name: String, val password: String)

enum class UserError {
    USERNAME_OUT_OF_BOUNDS,
    PASSWORD_TOO_SHORT
}

fun validateName(name: String): Result<String, UserError> =
    name.takeIf { name.isNotEmpty() && name.length <= 15 }
        ?.let(::Success)
        ?: Failure(UserError.USERNAME_OUT_OF_BOUNDS)

fun validatePassword(password: String): Result<String, UserError> =
    password.takeIf { password.length >= 10 }
        ?.let(::Success)
        ?: Failure(UserError.PASSWORD_TOO_SHORT)

fun createUser(name: String, password: String): Result<User, UserError> {
    val validateName = validateName(name)

    if (validateName is Failure)
        return validateName

    val validatePassword = validatePassword(password)

    if (validatePassword is Failure)
        return validatePassword

    return User(name, password).let(::Success)
}

fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks")
        .map { it.name }
        .ifSuccess(::println)
}