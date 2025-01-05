package org.example.utilities

import com.passingcuriosity.codedev.annotations.Context

@Context
data class Name(
    val givenNames: List<String>,
    val familyNames: List<String>,
) {
    companion object {
        val empty = Name(emptyList(), emptyList())
    }
}

