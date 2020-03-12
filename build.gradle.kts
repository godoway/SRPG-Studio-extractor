plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "gwsl"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.beust:jcommander:1.72")
//    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.apache.commons:commons-csv:1.6")
    testImplementation("junit:junit:4.12")
//    annotationProcessor("org.projectlombok:lombok:1.16.20")
//    compileOnly("org.projectlombok:lombok:1.16.20")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    applicationName = project.name
    group = "gwsl"
    mainClassName = "gwsl.srpgstudio.extractor.Application"
}

tasks {
    compileJava {
        options.encoding = "utf-8"
    }

    shadowJar {
        archiveClassifier.set("fat")
    }
}
