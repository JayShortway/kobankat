plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    compileOnly(libs.arturbosch.detekt.api)

    testImplementation(libs.arturbosch.detekt.test)
    testImplementation(libs.kotlin.test.junit)
}
