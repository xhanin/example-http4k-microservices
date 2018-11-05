import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

import org.gradle.kotlin.dsl.*

object Versions {
    val junit = "5.3.1"
    val strikt = "0.16.2"
    val http4k = "3.94.0"
    val mockk = "1.8.9"
}

fun http4k(module:String) = "org.http4k:http4k-${module}:${Versions.http4k}"

/**
 * Configures the current project with base dependencies
 */
fun Project.baseProject() {
    dependencies {
        "compile"(kotlin("stdlib-jdk8"))

        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
        "testImplementation"("io.strikt:strikt-core:${Versions.strikt}")
        "testImplementation"("io.mockk:mockk:${Versions.mockk}")

        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    }

    tasks.getByPath("test").doFirst({
        with(this as Test) {
            useJUnitPlatform()
        }
    })

}
