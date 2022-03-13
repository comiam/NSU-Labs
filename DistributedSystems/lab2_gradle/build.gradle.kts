import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "me.maxim"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.1")

    implementation("com.sun.xml.bind:jaxb-xjc:2.4.0-b180830.0438")
    implementation("com.sun.xml.bind:jaxb-impl:2.4.0-b180830.0438")
    implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("javax.activation:activation:1.1.1")

    implementation("org.postgresql:postgresql:42.3.3")
    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-log4j12:1.7.36")

    implementation("com.lordcodes.turtle:turtle:0.5.0")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java"))
        }
        resources {
            setSrcDirs(listOf("src/main/resources"))
        }
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = "MainKt"
        }
        configurations["compileClasspath"].forEach {
            from(zipTree(it.absoluteFile))
        }
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}