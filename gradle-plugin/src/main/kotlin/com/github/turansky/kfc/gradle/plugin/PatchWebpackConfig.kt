package com.github.turansky.kfc.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PatchWebpackConfig : DefaultTask() {
    @get:Input
    val patches: MutableMap<String, String> = mutableMapOf()

    @get:OutputDirectory
    val configDirectory: File
        get() = project.projectDir.resolve("webpack.config.d")

    fun patch(body: String) {
        val index = patches.size + 1
        patch("generated_$index", body)
    }

    fun patch(name: String, body: String) {
        if (patches.containsKey(name)) {
            patch(name + "_", body)
        } else {
            patches[name] = body
        }
    }

    @TaskAction
    private fun generatePatches() {
        for ((name, body) in patches) {
            generate(name, body)
        }
    }

    @TaskAction
    private fun generateResources() {
        val resources = project.relatedResources()
        if (resources.isEmpty()) {
            return
        }

        val paths = resources.joinToString(",\n") {
            it.toPathString()
        }

        // language=JavaScript
        val body = """
            config.resolve.modules.unshift(
                $paths
            )
        """.trimIndent()

        generate("resources", body)
    }

    private fun generate(name: String, body: String) {
        configDirectory
            .also { it.mkdirs() }
            .resolve("$name.js")
            .writeText(createPatch(body))
    }
}

private fun createPatch(body: String): String =
    """
        ;(function () {
        $body
        })()
    """.trimIndent()
