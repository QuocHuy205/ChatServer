plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "vku.chatapp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("vku.chatapp.chatappserver")
    mainClass.set("vku.chatapp.chatappserver.HelloApplication")
}

javafx {
    version = "17.0.14"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // MySQL Driver
    implementation("mysql:mysql-connector-java:8.0.33")

    // Connection Pool
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Email Service
    implementation("com.sun.mail:javax.mail:1.6.2")

    // Password Hashing
    implementation("org.mindrot:jbcrypt:0.4")

    // JSON Processing
    implementation("com.google.code.gson:gson:2.10.1")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}
