// Copyright © FunctionalKotlin.com 2017. All rights reserved.

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