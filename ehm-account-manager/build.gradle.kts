plugins {
    kotlin("jvm")
}

baseProject()

dependencies {
    compile(http4k("core"))
    compile(http4k("format-jackson"))
    compile(http4k("server-netty"))
}

