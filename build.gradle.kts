plugins {
    kotlin("jvm") version "2.1.10"
    antlr
    application
}

group = "lv426.compiler"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

val generatedAntlr = layout.buildDirectory.dir("generated/sources/antlr/main")

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener")
    outputDirectory = generatedAntlr.get().asFile
}

sourceSets {
    main {
        java {
            srcDir(generatedAntlr)
        }
    }
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

application {
    mainClass.set("lv426.compiler.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
