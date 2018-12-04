plugins {
    java
//    application
}

group = "gwsl"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    compile("com.beust:jcommander:1.72")
//    compile("com.google.code.gson:gson:2.8.5")
    testImplementation("junit:junit:4.12")
    compileOnly("org.projectlombok:lombok:1.16.20")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

//application {
//    applicationName = project.name
//    group = "gwsl"
//    mainClassName = "gwsl.srpgstudio.extractor.Application"
//}


tasks.withType<Jar> {
    baseName = project.name
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "gwsl.srpgstudio.extractor.Application"
    }
//    configurations["compileClasspath"].forEach { file: File ->
//        from(zipTree(file.absoluteFile))
//    }
    from(configurations.compile.map { if (it.isDirectory) it else zipTree(it) })
}