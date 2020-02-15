package com.github.turansky.kfc.gradle.plugin

import com.github.turansky.kfc.gradle.plugin.GradleProperty.GROUP
import com.github.turansky.kfc.gradle.plugin.GradleProperty.VERSION
import com.github.turansky.kfc.gradle.plugin.JvmTarget.JVM_1_8
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import java.io.File

private const val GRADLE_PLUGIN_PREFIX = "gradle.plugin."

open class PluginPublishExtension {
    var gradlePluginPrefix: Boolean = false
    var versionFiles: List<File> = emptyList()
}

class PluginPublishPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val extension = extensions.create<PluginPublishExtension>("pluginPublish")

        tasks {
            withType<KotlinJvmCompile> {
                kotlinOptions {
                    jvmTarget = JVM_1_8
                    allWarningsAsErrors = true
                }
            }

            named<Jar>("jar") {
                into("META-INF") {
                    from("$projectDir/LICENSE.md")
                }
            }

            register("preparePublish") {
                doLast {
                    changeVersion(Version::toRelease, extension.versionFiles)
                    if (extension.gradlePluginPrefix) {
                        changeGroup(addPrefix = false)
                    }
                }
            }

            register("prepareDevelopment") {
                doLast {
                    changeVersion(Version::toNextSnapshot, extension.versionFiles)
                    if (extension.gradlePluginPrefix) {
                        changeGroup(addPrefix = true)
                    }
                }
            }
        }
    }
}

private fun Project.changeGroup(addPrefix: Boolean) {
    var group = group.toString()
    group = if (addPrefix) {
        "$GRADLE_PLUGIN_PREFIX$group"
    } else {
        group.removePrefix(GRADLE_PLUGIN_PREFIX)
    }

    setGradleProperty(GROUP, group)
}

private fun Project.changeVersion(
    change: (Version) -> Version,
    versionFiles: List<File>
) {
    val oldVersion = currentVersion.toString()
    val newVersion = change(currentVersion).toString()

    setGradleProperty(VERSION, newVersion)
    version = newVersion

    for (file in versionFiles) {
        file.writeText(file.readText().replace(oldVersion, newVersion))
    }
}
