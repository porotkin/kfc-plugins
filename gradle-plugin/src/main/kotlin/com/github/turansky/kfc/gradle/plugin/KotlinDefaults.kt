package com.github.turansky.kfc.gradle.plugin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

private const val JS_COMPILER = "kotlin.js.compiler"
private const val BUILD_DISTRIBUTION = "kotlin.js.generate.executable.default"

private val STRICT_MODE = BooleanProperty("kfc.strict.mode", true)

internal fun Project.applyKotlinDefaults(both: Boolean) {
    if (both) {
        ext(JS_COMPILER, "both")
    }
    ext(BUILD_DISTRIBUTION, false)

    plugins.apply(SourceMapsPlugin::class)
    plugins.apply(WorkaroundPlugin::class)

    configureStrictMode()
    // disableTestsWithoutSources()

    extensions.create(
        NpmvDependencyExtension::class.java,
        "npmv",
        NpmvDependencyExtensionImpl::class.java,
        project
    )
}

private fun Project.configureStrictMode() {
    if (property(STRICT_MODE)) {
        tasks.configureEach<KotlinCompile<*>> {
            kotlinOptions.allWarningsAsErrors = true
        }
    }
}

private fun Project.disableTestsWithoutSources() {
    afterEvaluate {
        val sourceSetName = sequenceOf("jsTest", "test")
            .single { tasks.findByPath("${it}PackageJson") != null }

        tasks.named("${sourceSetName}PackageJson") {
            onlyIf {
                val kotlin = project.extensions.getByName<KotlinProjectExtension>("kotlin")
                val sourceDir = kotlin.sourceSets
                    .getByName(sourceSetName)
                    .kotlin.sourceDirectories
                    .singleOrNull()

                sourceDir?.exists() ?: true
            }
        }
    }
}
