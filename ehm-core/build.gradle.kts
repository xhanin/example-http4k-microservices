plugins {
    kotlin("jvm")
}

baseProject()

dependencies {
    compile(project(":ehm-account-manager"))

    compile(http4k("core"))
    compile(http4k("format-jackson"))
    compile(http4k("server-netty"))
}

