plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "me.billbai.compiler.kwacc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("me.billbai.compiler.kwacc.MainKt")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}