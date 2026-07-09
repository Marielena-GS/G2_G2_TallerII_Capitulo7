plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val jomlVersion = "1.10.8"
val jogampVersion = "2.3.2"
val flatlafVersion = "3.6"

dependencies {
    // OpenGL bindings
    implementation("org.jogamp.jogl:jogl-all:$jogampVersion")
    implementation("org.jogamp.gluegen:gluegen-rt:$jogampVersion")
    runtimeOnly("org.jogamp.jogl:jogl-all:$jogampVersion:natives-windows-amd64")
    runtimeOnly("org.jogamp.gluegen:gluegen-rt:$jogampVersion:natives-windows-amd64")

    // Look and Feel
    implementation("com.formdev:flatlaf:$flatlafVersion")

    // Math library
    implementation("org.joml:joml:$jomlVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.programacion.Main")
}

tasks.test {
    useJUnitPlatform()
}