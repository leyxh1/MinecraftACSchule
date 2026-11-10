plugins {
    java
}

repositories {
    mavenCentral()
    // Hier liegt die Paper-API
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // compileOnly: Paper stellt die API zur Laufzeit selbst bereit
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}

java {
    // Aktuelles Paper braucht Java 25
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    // Trägt die Version aus gradle.properties automatisch in plugin.yml ein
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}
