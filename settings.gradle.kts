// Name des Projekts (bestimmt auch den Namen der .jar-Datei)
rootProject.name = "SchoolAntiCheat"

// Dieses Plugin lädt bei Bedarf automatisch das passende JDK herunter (Toolchain)
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
