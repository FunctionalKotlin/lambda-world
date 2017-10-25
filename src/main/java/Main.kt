// Copyright © FunctionalKotlin.com 2017. All rights reserved.

import Validators.IsValidName
import Validators.IsValidPassword

data class User(
    val name: String, val password: String, val organizationId: Int)

data class Organization(val name: String)

enum class UserError {
    USERNAME_OUT_OF_BOUNDS,
    PASSWORD_TOO_SHORT,
    MISSING_ORGANIZATION
}

typealias Validator<A, E> = (A) -> Result<A, E>

fun <A> validate(with: (A) -> Boolean): (A) -> A? = { it.takeIf(with) }

fun <A, E> ((A) -> A?).orElseFail(with: E): Validator<A, E> = {
    this(it)?.let(::Success) ?: Failure(with)
}

object Validators {
    val IsValidName: Validator<User, UserError> =
        validate<User>(with = { it.name.isNotEmpty() && it.name.length < 15 })
            .orElseFail(with = UserError.USERNAME_OUT_OF_BOUNDS)

    val IsValidPassword: Validator<User, UserError> =
        validate<User>(with = { it.password.length >= 10 })
            .orElseFail(with = UserError.PASSWORD_TOO_SHORT)
}

fun getOrganization(id: Int): Result<Organization, UserError> =
    Success(Organization("Lambda World"))

fun createUser(
    name: String, password: String, organizationId: Int):
        Result<User, UserError> =

    User(name, password, organizationId)
        .let(allOf(IsValidName, IsValidPassword))

fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks", 1)
        .map { getOrganization(it.organizationId) }
        .ifSuccess(::println)
}