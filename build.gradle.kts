import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    base
    kotlin("jvm") version "1.3.0" apply false
}

allprojects {
    group = "fr.qsh"
    version = "1.0"

    repositories {
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}


subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}