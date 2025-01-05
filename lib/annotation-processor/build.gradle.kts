plugins {
    id("buildlogic.kotlin-base-conventions")
    `java-library`
}

dependencies {
    implementation(project(":lib:annotations"))
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlin.poet)
    implementation(libs.kotlin.poet.ksp)
}
