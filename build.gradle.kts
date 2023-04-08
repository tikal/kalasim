import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    `maven-publish`
    signing

    // see https://kotlinlang.slack.com/archives/C4W52CFEZ/p1641056747134600
    id("org.jetbrains.kotlin.jupyter.api") version "0.11.0-187" // "0.11.0-45"

    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "com.github.holgerbrandl"
//version = "0.8.100"
version = "0.9-SNAPSHOT"


repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("org.apache.commons:commons-math3:3.6.1")
    // note updated postponed because of regression errors
    api("io.insert-koin:koin-core:3.1.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20")

    api("com.github.holgerbrandl:jsonbuilder:0.10")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    //  api("io.github.microutils:kotlin-logging:1.12.5")
//    api("org.slf4j:slf4j-simple:1.7.32")

    implementation("com.google.code.gson:gson:2.10.1")

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")


    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.20")
    testImplementation("io.kotest:kotest-assertions-core:5.5.5")

    // **TODO** move to api to require users to pull it in if needed
    implementation("com.github.holgerbrandl:krangl:0.18.4") // must needed for kravis
    implementation("com.github.holgerbrandl:kdfutils:1.2-SNAPSHOT")
    testImplementation("com.github.holgerbrandl:kdfutils:1.2-SNAPSHOT")

    compileOnly("com.github.holgerbrandl:kravis:0.9.95")
    testImplementation("com.github.holgerbrandl:kravis:0.9.95")

    compileOnly("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.3.0")
    testImplementation("org.jetbrains.lets-plot:lets-plot-batik:3.1.0")
    //    testImplementation("org.jetbrains.lets-plot:lets-plot-jfx:1.5.4")

    //experimental dependencies  use for experimentation
    testImplementation("com.thoughtworks.xstream:xstream:1.4.20")

    //https://youtrack.jetbrains.com/issue/KT-44197

    testImplementation(kotlin("script-runtime"))
//    implementation(kotlin("script-runtime"))
}

// see https://youtrack.jetbrains.com/issue/KT-52735
val compileKotlin: KotlinCompile by tasks


compileKotlin.kotlinOptions.freeCompilerArgs += "-Xallow-any-scripts-in-source-roots"


// to set bytecode version to 11 we need to do 2 things (note: this requires the usage projects to do the same)
//compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
//
//kotlin { // Extension to make an easy setup
//    jvmToolchain(11) // Target version of generated JVM bytecode
//}



//https://github.com/Kotlin/kotlin-jupyter/blob/master/docs/libraries.md
tasks.processJupyterApiResources {
    libraryProducers = listOf("org.kalasim.analysis.NotebookIntegration")
}

//https://gist.github.com/domnikl/c19c7385927a7bef7217aa036a71d807
val jar by tasks.getting(Jar::class) {
    manifest {
//        attributes["Main-Class"] = "com.example.MainKt"
        attributes["Implementation-Title"] = "kalasim"
        attributes["Implementation-Version"] = project.version
    }
}

//
//subprojects {
//    java.sourceCompatibility = JavaVersion.VERSION_1_8
//    java.targetCompatibility = JavaVersion.VERSION_1_8
//}

//application {
//    mainClassName = "MainKt"
//}

//bintray kts example https://gist.github.com/s1monw1/9bb3d817f31e22462ebdd1a567d8e78a

java {
    withJavadocJar()
    withSourcesJar()
}


// disabled because docs examples were moved back into tests
//java {
//    sourceSets["test"].java {
//        srcDir("docs/userguide/examples/kotlin")
//    }
//}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
//          artifact sourcesJar { classifier "sources" }
//          artifact javadocJar

            pom {
                url.set("https://www.kalasim.org")
                name.set("kalasim")
                description.set("kalasim is a process-oriented discrete event simulation engine")

                scm {
                    connection.set("scm:git:github.com/holgerbrandl/kalasim.git")
                    url.set("https://github.com/holgerbrandl/kalasim.git")
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://raw.githubusercontent.com/holgerbrandl/kalasim/master/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("holgerbrandl")
                        name.set("Holger Brandl")
                        email.set("holgerbrandl@gmail.com")
                    }
                }
            }
        }
    }
}


nexusPublishing {
//    packageGroup.set("com.github.holgerbrandl.kalasim")

    repositories {
        sonatype {
//            print("staging id is ${project.properties["sonatypeStagingProfileId"]}")
            stagingProfileId.set(project.properties["sonatypeStagingProfileId"] as String?)

//            nexusUrl.set(uri("https://oss.sonatype.org/"))
//            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))

            username.set(project.properties["ossrhUsername"] as String?) // defaults to project.properties["myNexusUsername"]
            password.set(project.properties["ossrhPassword"] as String?) // defaults to project.properties["myNexusPassword"]
        }
    }
}


signing {
    sign(publishing.publications["maven"])
}

fun findProperty(s: String) = project.findProperty(s) as String?


//val compileKotlin: KotlinCompile by tasks
//compileKotlin.kotlinOptions {
//    freeCompilerArgs = listOf("-Xinline-classes")
//}
