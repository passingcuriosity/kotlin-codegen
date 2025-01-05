package com.passingcuriosity.codegen.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ContextSymbolProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ContextSymbolProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
    }
}
