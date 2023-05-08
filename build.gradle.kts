import io.gitlab.arturbosch.detekt.Detekt
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.13.3"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.0.0"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.23.0-RC3"
}

val androidStudioPath: String by project
val androidStudioPathMacOS: String by project
// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project

val platformCompilerVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
    mavenCentral()
    google()
}
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(pluginName_)
    localPath.set(if (Os.isFamily(Os.FAMILY_WINDOWS)) androidStudioPath else androidStudioPathMacOS)
    downloadSources.set(platformDownloadSources.toBoolean())
    updateSinceUntilBuild.set(true)
    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set( provider { pluginVersion } )
    groups.set( provider { emptyList() } )
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true
}

project.gradle.startParameter.excludedTaskNames.add("buildSearchableOptions")
tasks {
    // Set the compatibility versions to 11
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = "11"
        }
    }
    patchPluginXml {
        version.set(provider { pluginVersion } )
        sinceBuild.set(provider { pluginSinceBuild } )
        pluginDescription.set(provider {
            File("./README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n")
                .run { markdownToHTML(this) }
        })
        // Get the latest available change notes from the changelog file
        changeNotes.set( provider {
            changelog.render(
                org.jetbrains.changelog.Changelog.OutputType.HTML)
        } )
    }

    instrumentCode {
        compilerVersion.set(provider { platformCompilerVersion })
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(provider { System.getenv("PUBLISH_TOKEN") })
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
        channels.set(provider {
            pluginVersion.split('-')
                .getOrElse(1) { "default" }
                .split('.')
        } )
    }
}

tasks.withType(Detekt::class).configureEach {
    reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
    }
}