@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.SonatypeHost
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.configurationcache.extensions.capitalized

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlin.cocoapods).apply(false)
    alias(libs.plugins.kotlinx.binaryCompatibilityValidator)
    alias(libs.plugins.adamko.dokkatoo.html)
    alias(libs.plugins.arturbosch.detekt).apply(false)
    alias(libs.plugins.vanniktech.mavenPublish).apply(false)
}

allprojects {
    group = "com.revenuecat.purchases"
    version = rootProject.libs.versions.revenuecat.kmp.get()

    plugins.withType<MavenPublishPlugin> {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
            signAllPublications()

            // We override the artifact ID of :paywalls for consistency with the other SDKs. We
            // could not name our Gradle module :ui, because this somehow conflicts with compose.ui
            // in the iosMain source set. We can retry this at a later time.
            val artifactIdSuffix = when (project.name) {
                "paywalls" -> "ui"
                else -> project.name
            }

            coordinates(
                groupId = group.toString(),
                artifactId = "purchases-kmp-$artifactIdSuffix",
                version = version.toString()
            )
            pom {
                name.set("purchases-kmp-(${project.name})")
                description.set("Mobile subscriptions in hours, not months.")
                inceptionYear.set("2024")
                url.set("https://github.com/RevenueCat/purchases-kmp")
                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("http://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("revenuecat")
                        name.set("RevenueCat, Inc.")
                        url.set("https://www.revenuecat.com/")
                    }
                }
                scm {
                    url.set("https://github.com/RevenueCat/purchases-kmp")
                    connection.set("scm:git:git://github.com/RevenueCat/purchases-kmp.git")
                    developerConnection.set("scm:git:ssh://git@github.com/RevenueCat/purchases-kmp.git")
                }
            }
        }

        // Register a Detekt task for all published modules.
        with(this@allprojects) {
            projectDir
                .resolve("src")
                .listFiles { child -> child.isDirectory }
                .orEmpty()
                .also { sourceDirectories ->
                    tasks.registerDetektTask(
                        taskName = "detektAll",
                        taskDescription = "Runs Detekt on all source sets.",
                        reportName = "all",
                        sourceDirs = files(sourceDirectories)
                    )

                    sourceDirectories.forEach { sourceDir ->
                        val sourceSet = sourceDir.name
                        tasks.registerDetektTask(
                            taskName = "detekt${sourceSet.capitalized()}",
                            taskDescription = "Runs Detekt on the $sourceSet source set.",
                            reportName = "$name${sourceSet.capitalized()}",
                            sourceDirs = files(sourceDir)
                        )
                    }
                }
        }
    }
}

apiValidation {
    ignoredProjects.addAll(listOf("apiTester", "composeApp"))

    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
}

dependencies {
    dokkatoo(projects.core)
    dokkatoo(projects.datetime)
    dokkatoo(projects.either)
    dokkatoo(projects.result)
}

private fun TaskContainer.registerDetektTask(
    taskName: String,
    taskDescription: String,
    reportName: String,
    sourceDirs: ConfigurableFileCollection,
) =
    register<Detekt>(taskName) {
        description = taskDescription
        setSource(sourceDirs.map { it.resolve("kotlin") })
        config = files("$rootDir/config/detekt/detekt.yml")
        reports {
            html.outputLocation = file("build/reports/detekt/$reportName.html")
            xml.outputLocation = file("build/reports/detekt/$reportName.xml")
        }
    }
