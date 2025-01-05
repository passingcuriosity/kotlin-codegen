package com.passingcuriosity.codegen.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.outerType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.passingcuriosity.codedev.annotations.Context
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class ContextSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
): SymbolProcessor {
    override fun process(
        resolver: Resolver,
    ): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(Context::class.qualifiedName!!)
            .forEach { symbol ->
                symbol.accept(
                    visitor = ContextSymbolVisitor(),
                    data = Unit,
                )
            }

        return listOf()
    }

    override fun finish() {
        super.finish()
    }

    override fun onError() {
        super.onError()
    }

    private fun generateSchemaBuilder(
        annotatedClass: KSClassDeclaration,
    ) {
        val generatedMethod =
            FunSpec
                .builder("getSchema")
                .addOriginatingKSFile(annotatedClass.containingFile!!)
                .receiver(annotatedClass.toClassName())
                .returns(String::class)
                .addStatement("""return "Hello World" """)
                .build()

        val generatedFile =
            FileSpec
                .builder(
                    packageName = annotatedClass.packageName.asString(),
                    fileName = annotatedClass.simpleName.asString().plus("Schema"),
                )
                .addFunction(generatedMethod)
                .build()

        generatedFile.writeTo(
            codeGenerator = codeGenerator,
            aggregating = true, // TODO: No idea what this means?
            originatingKSFiles = listOfNotNull(annotatedClass.containingFile),
        )
    }


    @OptIn(KspExperimental::class)
    private fun generateContextExpression(
        names: List<String>,
        ksType: KSType,
    ): String? {
        val nullable =
            if (ksType.isMarkedNullable) {
                "?"
            } else {
                ""
            }

        val typeName = ksType.toTypeName()

        val expr = when {
            ksType.declaration.isAnnotationPresent(Context::class) ->
                "${nullable}.toContextMap()"
            typeName == LIST -> {
                val subexpression = generateContextExpression(
                    listOf("it"),
                    ksType.arguments.first().type!!.resolve(),
                )
                "${nullable}.map { " + subexpression + " }"
            }
            typeName == MAP -> {
                val (key, value) = ksType.arguments.map { it.type?.resolve()!! }
                if (key.toClassName() != STRING) {
                    logger.error(
                        "Map keys must be String, not $key",
                    )
                }
                val subexpression =
                    generateContextExpression(
                        listOf("value"),
                        value,
                    )
                "${nullable}.mapValues { (_, value) -> $subexpression }"
            }
            else -> {
                logger.warn(
                    "Unable to process type: ${ksType.toTypeName()}",
                )
                return null
            }
        }

        return names.joinToString(".") + nullable + expr
    }

    @OptIn(KspExperimental::class)
    private fun generateContextBuilder(
        annotatedClass: KSClassDeclaration,
    ) {
        val contextType = map.parameterizedBy(
            ClassName("kotlin", "String"),
            ClassName("kotlin", "Any").copy(nullable = true),
        )
        val generatedMethod =
            FunSpec
                .builder("toContextMap")
                .addOriginatingKSFile(annotatedClass.containingFile!!)
                .receiver(annotatedClass.toClassName())
                .returns(contextType)
                .addCode(
                    CodeBlock
                        .builder()
                        .apply {
                            addStatement("val result = mapOf<String, Any?>(")
                            indent()
                            annotatedClass
                                .getAllProperties()
                                .forEach {
                                    val name = it.simpleName.asString()
                                    val type = it.type.resolve()
                                    when (val expr = generateContextExpression(
                                        listOf("this", name),
                                        type
                                    )) {
                                        null ->
                                            logger.info("Skipping $name")
                                        else ->
                                            addStatement(
                                                "%S to %L,",
                                                name,
                                                expr
                                            )
                                    }
                                }
                            unindent()
                            addStatement(")")
                            addStatement("return result")
                        }
                        .build()
                )
                .build()

        val generatedFile =
            FileSpec
                .builder(
                    packageName = annotatedClass.packageName.asString(),
                    fileName = annotatedClass.simpleName.asString().plus("Context"),
                )
                .addFunction(generatedMethod)
                .build()

        generatedFile.writeTo(
            codeGenerator = codeGenerator,
            aggregating = true, // TODO: No idea what this means?
            originatingKSFiles = listOfNotNull(annotatedClass.containingFile),
        )
    }

    private inner class ContextSymbolVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {
            if (classDeclaration.modifiers.none() { it == Modifier.DATA }) {
                logger.error(
                    message = "@Context can only be applied to data classes.",
                    symbol = classDeclaration,
                )
            }

            generateContextBuilder(classDeclaration)

//            generateSchemaBuilder(classDeclaration)
        }
    }

    companion object {
        val map = ClassName("kotlin.collections", "Map")
    }
}