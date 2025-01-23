import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.20"
    id("java")
    id("com.gradleup.shadow") version "8.3.2"
}

group = "me.window"
version = "1.3.2"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("net.projecttl:InventoryGUI-api:4.6.0")
    compileOnly("net.luckperms:api:5.4")
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

var targetJavaVersion = 21
val javaVersion = JavaVersion.toVersion(targetJavaVersion)
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlin { // Extension for easy setup
    jvmToolchain(targetJavaVersion) // Target version of generated JVM bytecode. See 7️⃣
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveClassifier.set("shadowless")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    dependencies {
        include {
            it.moduleGroup == "org.bstats"
        }
    }
    relocate("org.bstats", "me.window.bstats")
}