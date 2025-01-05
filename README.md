Kotlin Code Generation
======================

This is a small sample project to experiment with annotation-driven code
generation in Kotlin.

TODO:

- [x] Setting up the project, Gradle plugins, etc.
- [x] Annotation processor to generate additional classes, extension methods, etc.
- [ ] Figure out how to write type-safe clients for the generated code. 

Sources
-------

A bunch of tutorials which mostly provide enough of a start that you can figure
out how to get the Gradle dependencies and plugins set-up. They all do almost
exactly the same thing but there are a few interesting differences in the
various examples: 

- https://medium.com/@luka.janjgava.6/code-generation-with-ksp-or-how-to-teach-kotlin-to-write-code-ae13a20c87f5
- https://www.dhiwise.com/post/how-to-build-your-first-kotlin-annotation-processor-with-ksp
- https://expertbeacon.com/supercharge-your-kotlin-development-with-ksp-for-code-generation/
- https://medium.com/google-developer-experts/ksp-for-code-generation-dfd2073a6635

My project was set up with `gradle init` and, as a result, uses a TOML version
catalog and some sort of new reference to things defined in the catalog. Of
course, this requires a different set of magical constants than we used in
those posts, so referring to the Gradle build for KSP was necessary:  

- https://github.com/google/ksp/blob/main/gradle-plugin/build.gradle.kts

There's some pretty OK official documentation for KSP: 

- https://kotlinlang.org/docs/ksp-quickstart.html

Kotlin Poet seems to be an OK way to generate Kotlin code: 

- https://square.github.io/kotlinpoet/
