// Copyright © FunctionalKotlin.com 2017. All rights reserved.

data class Organization(val name: String)

data class User(val name: String, val organizationId: Int)

fun getOrganizationById(id: Int): Option<Organization> = Just(Organization("Functional Kotlin"))

fun getUserById(id: Int): Option<User> = Just(User("alex", 1))

fun main(args: Array<String>) {
    val option: Option<Option<Organization>> = getUserById(42)
        .map { user -> getOrganizationById(user.organizationId) }
}