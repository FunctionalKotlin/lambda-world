// Copyright © FunctionalKotlin.com 2017. All rights reserved.

import Validators.IsValidName
import Validators.IsValidPassword

data class User(val name: String, val password: String)

enum class UserError {
    USERNAME_OUT_OF_BOUNDS,
    PASSWORD_TOO_SHORT
}

typealias Validator<A, E> = (A) -> Result<A, E>

object Validators {
    val IsValidName: Validator<String, UserError> = {
        it.takeIf { it.isNotEmpty() && it.length <= 15 }
            ?.let(::Success)
            ?: Failure(UserError.USERNAME_OUT_OF_BOUNDS)
    }

    val IsValidPassword: Validator<String, UserError> = {
        it.takeIf { it.length >= 10 }
            ?.let(::Success)
            ?: Failure(UserError.PASSWORD_TOO_SHORT)
    }
}

fun createUser(name: String, password: String): Result<User, UserError> {
    val validateName = IsValidName(name)

    if (validateName is Failure)
        return validateName

    val validatePassword = IsValidPassword(password)

    if (validatePassword is Failure)
        return validatePassword

    return User(name, password).let(::Success)
}

fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks")
        .map { it.name }
        .ifSuccess(::println)
}