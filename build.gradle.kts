plugins {
    kotlin("jvm") version "1.9.23"
    id("java")
}

group = "me.window"
version = "1.1"

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
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.projecttl:InventoryGUI-api:4.5.1")
    compileOnly("net.luckperms:api:5.4")
}

var targetJavaVersion = 17
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
