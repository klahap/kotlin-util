import org.jetbrains.dokka.DokkaConfiguration.Visibility
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    id("com.vanniktech.maven.publish")
}

repositories {
    mavenCentral()
}

val githubUser = "goquati"
val githubProject = "kotlin-util"
val groupStr = "io.github.$githubUser"
val versionStr = System.getenv("GIT_TAG_VERSION") ?: "1.0-SNAPSHOT"

enum class SupProjects(val projectName: String) {
    KOTLIN_UTIL("kotlin-util"),
    KOTLIN_UTIL_COROUTINE("kotlin-util-coroutine"),
    KOTLIN_UTIL_CSV("kotlin-util-csv"),
}

tasks.matching { it.name.startsWith("publish") }.configureEach {
    enabled = false // disable for root project
}

subprojects {
    val projectType = SupProjects.values().singleOrNull { it.projectName == name }
        ?: throw NotImplementedError("no description defined for $name")

    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "com.vanniktech.maven.publish")

    repositories {
        mavenCentral()
    }

    group = groupStr
    version = versionStr

    configurations.all {
        resolutionStrategy {
            val kotlinxCoroutineVersion = rootProject.ext["kotlinxCoroutineVersion"]
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutineVersion")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$kotlinxCoroutineVersion")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-bom-jvm:$kotlinxCoroutineVersion")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxCoroutineVersion")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8-jvm:$kotlinxCoroutineVersion")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutineVersion")

            val kotlinVersion = rootProject.ext["kotlinVersion"]
            force("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
            force("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion")
            force("org.jetbrains.kotlin:kotlin-dom-api-compat:$kotlinVersion")

            force("org.jetbrains:annotations:23.0.0")
            force("org.jetbrains.kotlinx:atomicfu:0.25.0")
            force("io.kotest:kotest-assertions-core:5.9.0")

            failOnVersionConflict()
        }
    }

    kover {
        reports {
            verify {
                CoverageUnit.values().forEach { covUnit ->
                    rule("minimal ${covUnit.name.lowercase()} coverage rate") {
                        minBound(100, coverageUnits = covUnit)
                    }
                }
            }
        }
    }

    val artifactId = project.name
    val descriptionStr = when (projectType) {
        SupProjects.KOTLIN_UTIL -> "Enhanced Kotlin util functions for simpler code."
        SupProjects.KOTLIN_UTIL_COROUTINE -> "Enhanced Kotlin coroutine util functions for simpler code."
        SupProjects.KOTLIN_UTIL_CSV -> "A Kotlin library for type-safe CSV writing with coroutine support."
    }
    mavenPublishing {
        coordinates(
            groupId = project.group as String,
            artifactId = artifactId,
            version = project.version as String
        )
        pom {
            name = artifactId
            description = descriptionStr
            url = "https://github.com/$githubUser/$githubProject"
            licenses {
                license {
                    name = "MIT License"
                    url = "https://github.com/$githubUser/$githubProject/blob/main/LICENSE"
                }
            }
            developers {
                developer {
                    id = githubUser
                    name = githubUser
                    url = "https://github.com/$githubUser"
                }
            }
            scm {
                url = "https://github.com/${githubUser}/${githubProject}"
                connection = "scm:git:https://github.com/${githubUser}/${githubProject}.git"
                developerConnection = "scm:git:git@github.com:${githubUser}/${githubProject}.git"
            }
        }
        publishToMavenCentral(
            SonatypeHost.CENTRAL_PORTAL,
            automaticRelease = true,
        )
        signAllPublications()
    }
}

tasks.dokkaHtml {
    moduleName.set(groupStr)
    moduleVersion = versionStr
    dokkaSourceSets {
        register("commonMainSet") {
            displayName.set("common")
            sourceRoots.from(
                rootDir.resolve("kotlin-util/src/commonMain/kotlin"),
                rootDir.resolve("kotlin-util-coroutine/src/commonMain/kotlin"),
                rootDir.resolve("kotlin-util-csv/src/commonMain/kotlin"),
            )
        }
        register("jvmMainSet") {
            displayName.set("jvm")
            sourceRoots.from(
                rootDir.resolve("kotlin-util/src/jvmMain/kotlin"),
                rootDir.resolve("kotlin-utill-coroutine/src/jvmMain/kotlin"),
            )
        }
        configureEach {
            documentedVisibilities.set(setOf(Visibility.PUBLIC, Visibility.PROTECTED))
            includeNonPublic = false
            skipEmptyPackages = true
            suppressGeneratedFiles = false
        }
    }
}
