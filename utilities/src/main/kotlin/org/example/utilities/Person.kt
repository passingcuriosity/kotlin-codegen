package org.example.utilities

import com.passingcuriosity.codedev.annotations.Context

@Context
data class Person(
    val name: Name,
    val age: Int,
    val children: List<Person>?,
) {
    companion object {
        val empty = Person(Name.empty, 0, null)

        val wot = empty.toContextMap()
    }
}