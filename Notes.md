# Lambda World - Presenter notes

## Kotlin Introduction

### Exercise 1

```kotlin
sealed class Option<out A>

object None : Option<Nothing>()
data class Just<out A>(val value: A) : Option<A>()

fun eventFromPersistence(date: LocalDate): Option<Event> {
    return Just(Event("Lambda World"))
}

fun main(args: Array<String>) {
    val event = eventFromPersistence(LocalDate.of(2017, 10, 26))

    when(event) {
        is Just -> println("The event is ${event.value}")
        is None -> println("There is no event on that date")
    }
}
```

### Exercise 2

```kotlin
data class Person(val name: String, val age: Int)

fun groupByAgeSorted(people: List<Person>): Map<Int, List<Person>> =
    people.groupBy {
        person -> person.age
    }.mapValues { entry ->
        entry.value.sortedBy {
            person -> person.name
        }
    }
    
fun main(args: Array<String>) {
    val people = listOf(
            Person("John", 20),
            Person("Kim", 25),
            Person("Dan", 20),
            Person("Rob", 30),
            Person("Dom", 25),
            Person("Tim", 20),
            Person("Jess", 20)
    )

    val result = groupByAgeSorted(people)
    println(result)
}
```

## Functors

### Nullables

#### Step 1

```kotlin
data class User(val name: String, val password: String)

fun validateName(name: String): Boolean =
    name.isNotEmpty() && name.length <= 15

fun validatePassword(password: String): Boolean =
    password.length >= 10
```

#### Step 2

```kotlin
fun createUser(name: String, password: String): User? =
    User(name, password).takeIf {
        validateName(name) && validatePassword(password)
    }
```

#### Step 3

```kotlin
fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks")
        ?.let(::println)
}
```

### Option

#### Step 1

```kotlin
sealed class Option<out A>

object None : Option<Nothing>()
data class Just<out A>(val value: A) : Option<A>()
```

#### Step 2

```kotlin
fun createUser(name: String, password: String): Option<User> =
    User(name, password).takeIf {
        validateName(name) && validatePassword(password)
    }?.let(::Just)
    ?: None
```

#### Step 3

```kotlin
fun main(args: Array<String>) {
    val option = createUser("Antonio", "functionalrocks")

    when(option) {
        is Just -> println("We have a user: ${option.value}")
        is None -> println("Something went wrong")
    }
}
```

#### Step 4

```kotlin
fun <A, B> Option<A>.map(transform: (A) -> B): Option<B> = when(this) {
    is Just -> Just(transform(this.value))
    is None -> None
}

val name = createUser("Antonio", "functionalrocks")
    .map { it.name }

println(name)
```

#### Step 5

```kotlin
fun <A> Option<A>.ifPresent(continuation: (A) -> Unit) {
    if (this is Just) continuation(value)
}

createUser("Antonio", "functionalrocks")
    .map { it.name }
    .ifPresent(::println)
```

### Result

#### Step 1

```kotlin
sealed class Result<out A, out E>

data class Success<out A>(val value: A): Result<A, Nothing>()

data class Failure<out E>(val error: E): Result<Nothing, E>()

fun <A, E, B> Result<A, E>.map(transform: (A) -> B): Result<B, E> =
    when(this) {
        is Success -> Success(transform(this.value))
        is Failure -> this
    }
```

#### Step 2

```kotlin
enum class UserError {
    USERNAME_OUT_OF_BOUNDS,
    PASSWORD_TOO_SHORT
}

fun createUser(name: String, password: String): Result<User, UserError> {
    if (!validateName(name))
        return Failure(UserError.USERNAME_OUT_OF_BOUNDS)

    if (!validatePassword(password))
        return Failure(UserError.PASSWORD_TOO_SHORT)

    return User(name, password).let(::Success)
}
```

#### Step 3

```kotlin
fun <A> Result<A, *>.ifSuccess(continuation: (A) -> Unit) {
    if (this is Success) continuation(value)
}

createUser("Antonio", "functionalrocks")
    .map { it.name }
    .ifSuccess(::println)
```

## Semigroup && Monoid

### Validators with Result

#### Step 1

```kotlin
fun validateName(name: String): Result<String, UserError> =
    name.takeIf { name.isNotEmpty() && name.length <= 15 }
        ?.let(::Success)
        ?: Failure(UserError.USERNAME_OUT_OF_BOUNDS)

fun validatePassword(password: String): Result<String, UserError> =
    password.takeIf { password.length >= 10 }
        ?.let(::Success)
        ?: Failure(UserError.USERNAME_OUT_OF_BOUNDS)
```

#### Step 2

```kotlin
fun createUser(name: String, password: String): Result<User, UserError> {
    val validateName = validateName(name)

    if (validateName is Failure)
        return validateName

    val validatePassword = validatePassword(password)

    if (validatePassword is Failure)
        return validatePassword

    return User(name, password).let(::Success)
}
```

### Typealias Validator

#### Step 1

```kotlin
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
```

#### Step 2

```kotlin
fun createUser(name: String, password: String): Result<User, UserError> {
    val validateName = IsValidName(name)

    if (validateName is Failure)
        return validateName

    val validatePassword = IsValidPassword(password)

    if (validatePassword is Failure)
        return validatePassword

    return User(name, password).let(::Success)
}
```

### AND Combinator

#### Step 1

```kotlin
typealias Validator<T, E> = (T) -> Result<T, E>

operator infix fun <T, E> Validator<T, E>.plus(validator: Validator<T, E>): Validator<T, E> = {
    val result = this(it)

    when (result) {
        is Success -> validator(it)
        is Failure -> result
    }
}
```

#### Step 2

```kotlin
fun createUser(name: String, password: String): Result<User, UserError> =
    User(name, password).let(IsValidName + IsValidPassword)
```

#### Step 3

```kotlin
val IsValidName: Validator<User, UserError> = {
    it.takeIf { it.name.isNotEmpty() && it.name.length <= 15 }
        ?.let(::Success)
        ?: Failure(UserError.USERNAME_OUT_OF_BOUNDS)
}

val IsValidPassword: Validator<User, UserError> = {
    it.takeIf { it.password.length >= 10 }
        ?.let(::Success)
        ?: Failure(UserError.PASSWORD_TOO_SHORT)
}
```

### Build generic validate function [ONLY IF WE HAVE TIME]

#### Step 1

```kotlin
fun <A> validate(with: (A) -> Boolean): (A) -> A? = { it.takeIf(with) }

fun <A, E> ((A) -> A?).orElseFail(with: E): Validator<A, E> = {
    this(it)?.let(::Success) ?: Failure(with)
}
```

#### Step 2

```kotlin
object Validators {
    val IsValidName: Validator<User, UserError> =
            validate<User>(with = { it.name.isNotEmpty() && it.name.length < 15 })
                    .orElseFail(with = UserError.USERNAME_OUT_OF_BOUNDS)

    val IsValidPassword: Validator<User, UserError> =
            validate<User>(with = { it.password.length >= 10 })
                    .orElseFail(with = UserError.PASSWORD_TOO_SHORT)
}
```

### Monoid

#### Step 1

```kotlin
fun <A, E> allOf(vararg validators: Validator<A, E>): Validator<A, E> =
    validators.reduce { acc, validator -> acc + validator }  
```

#### Step 2

```kotlin
fun createUser(name: String, password: String): Result<User, UserError> =
        User(name, password).let(allOf(Name, Password))
```

#### Step 3
```kotlin
fun <A, E> allOf(vararg validators: Validator<A, E>): Validator<A, E> =
    validators.fold(::Success) { acc, validator -> acc + validator }
```

## Monads

#### Step 1

```kotlin
data class Organization(val name: String)

data class User(val name: String, val organizationId: Int)

fun getOrganizationById(id: Int): Option<Organization> = 
    Just(Organization("Functional Kotlin"))

fun getUserById(id: Int): Option<User> = Just(User("alex", 1))

fun main(args: Array<String>) {
    getUserById(42)
        .map { user -> getOrganizationById(user.organizationId) }
}
```

#### Step 2

```kotlin
fun <A> Option<Option<A>>.flatten(): Option<A> = when(this) {
    is Just -> value
    is None -> None
}

val option: Option<Organization> = getUserById(42)
    .map { user -> getOrganizationById(user.organizationId) }
    .flatten()
```

#### Step 3

```kotlin
fun <A, B> Option<A>.mapAndFlatten(transform: (A) -> Option<B>): Option<B> =
    map(transform).flatten()
    
    val option: Option<Organization> = getUserById(42)
    .mapAndFlatten { user -> getOrganizationById(user.organizationId) }
```

#### Step 4

```kotlin
fun <A, B> Option<A>.mapAndFlatten(transform: (A) -> Option<B>): Option<B> {
    val option = map(transform)
    
    return when(option) {
        is Just -> option.value
        is None -> None
    }
}
```

#### Step 5

```kotlin
fun <A, B> Option<A>.mapFlatten(transform: (A) -> Option<B>): Option<B>
```

#### Step 6

```kotlin
fun <A, B> Option<A>.mapFlat(transform: (A) -> Option<B>): Option<B>
```

#### Step 7

```kotlin
fun <A, B> Option<A>.flatMap(transform: (A) -> Option<B>): Option<B>
```

### Result as a monad

#### Step 1

```kotlin
data class Organization(val name: String)
```

#### Step 2

```kotlin
enum class UserError {
    USERNAME_OUT_OF_BOUNDS,
    PASSWORD_TOO_SHORT,
    MISSING_ORGANIZATION
}
```

#### Step 3

```kotlin
fun getOrganization(id: Int): Result<Organization, UserError> =
    Success(Organization("Lambda World"))
```

#### Step 4

```kotlin
fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks", 1)
        .map { getOrganization(it.organizationId) }
        .ifSuccess(::println)
}
```

#### Step 5

```kotlin
fun <A, E, B> Result<A, E>.flatMap(
    transform: (A) -> Result<B, E>): Result<B, E> =

    when(this) {
        is Success -> transform(this.value)
        is Failure -> this
    }
```

#### Step 6

```kotlin
fun main(args: Array<String>) {
    createUser("Antonio", "functionalrocks", 1)
        .flatMap { getOrganization(it.organizationId) }
        .ifSuccess(::println)
}
```