import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target

plugins {
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.22.0"
    kotlin("multiplatform") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.diffplug.gradle.spotless") version "4.4.0"
    id("com.moowork.node") version "1.3.1"
    id("org.jetbrains.dokka") version "1.4.20"
    id("com.android.library")
    id("kotlin-android-extensions")
}

repositories {
    jcenter()
    google()
    maven("https://kotlin.bintray.com/kotlinx")
}

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    }
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.20")
}

group = "com.adamratzman"
version = "3.2.15"

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

android {
    compileSdkVersion(30)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/*.md")
    }
    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(30)
        compileSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    testOptions {
        @Suppress("UNCHECKED_CAST")
        this.unitTests.all(closureOf<Test> {
            this.useJUnitPlatform()
        } as groovy.lang.Closure<Test>)
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.setSrcDirs(listOf("src/androidMain/kotlin"))
            res.setSrcDirs(listOf("src/androidMain/res"))
        }
        getByName("test") {
            java.setSrcDirs(listOf("src/androidTest/kotlin"))
            res.setSrcDirs(listOf("src/androidTest/res"))
        }
    }
}

val dokkaJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Docs"
    classifier = "javadoc"
    from(tasks.dokkaHtml)
}

kotlin {
    explicitApiWarning()

    android {
        mavenPublication {
            artifactId = "spotify-api-kotlin-android"
            setupPom(artifactId)
        }

        publishLibraryVariants("debug", "release")

        publishLibraryVariantsGroupedByFlavor = true
    }

    jvm {
        mavenPublication {
            artifactId = "spotify-api-kotlin"
            setupPom(artifactId)
        }
    }

    js(BOTH) {
        mavenPublication {
            artifactId = "spotify-api-kotlin-js"
            setupPom(artifactId)
        }

        browser {
            dceTask {
                keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
            }

            webpackTask {
                output.libraryTarget = Target.UMD
            }

            testTask {
                enabled = false
            }
        }

        nodejs()
    }

    targets {
        sourceSets {
            val coroutineVersion = "1.4.2"
            val serializationVersion = "1.0.1"
            val spekVersion = "2.0.15"
            val ktorVersion = "1.4.1"

            val commonMain by getting {
                dependencies {
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                    implementation("io.ktor:ktor-client-core:$ktorVersion")
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                    implementation("org.spekframework.spek2:spek-dsl-metadata:$spekVersion")
                }
            }

            val jvmMain by getting {
                repositories {
                    mavenCentral()
                    jcenter()
                }

                dependencies {
                    implementation("io.ktor:ktor-client-cio:$ktorVersion")
                }
            }

            val jvmTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(kotlin("test-junit"))
                    implementation("org.junit.jupiter:junit-jupiter:5.6.2")
                    implementation("com.sparkjava:spark-core:2.9.3")
                    implementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
                    runtimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
                    runtimeOnly(kotlin("reflect"))
                }
            }

            val jsMain by getting {
                dependencies {
                    implementation(npm("text-encoding", "0.7.0"))
                    implementation("io.ktor:ktor-client-js:$ktorVersion")
                    implementation(npm("abort-controller", "3.0.0"))
                    implementation(npm("node-fetch", "2.6.0"))
                }
            }

            val jsTest by getting {
                dependencies {
                    implementation(kotlin("test-js"))
                    implementation("org.spekframework.spek2:spek-dsl-js:$spekVersion")
                }
            }

            val androidMain by getting {
                repositories {
                    mavenCentral()
                    jcenter()
                }

                dependencies {
                    implementation("net.sourceforge.streamsupport:android-retrofuture:1.7.2")
                    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                    implementation("io.coil-kt:coil:1.1.0")
                }
            }

            val androidTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(kotlin("test-junit"))
                    implementation("org.junit.jupiter:junit-jupiter:5.6.2")
                    implementation("com.sparkjava:spark-core:2.9.3")
                    implementation("org.mockito:mockito-core:3.3.3")
                    implementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
                    runtimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
                    runtimeOnly(kotlin("reflect"))
                }
            }

            all {
                languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            }
        }
    }
}

publishing {
    publications {
        val kotlinMultiplatform by getting(MavenPublication::class) {
            artifactId = "spotify-api-kotlin-core"
            setupPom(artifactId)
        }

        val metadata by getting(MavenPublication::class) {
            artifactId = "spotify-api-kotlin-metadata"
            setupPom(artifactId)
        }
    }

    repositories {
        maven {
            name = "nexus"
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                val nexusUsername: String? by project.extra
                val nexusPassword: String? by project.extra
                username = nexusUsername
                password = nexusPassword
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.keyId")
            && project.hasProperty("signing.password")
            && project.hasProperty("signing.secretKeyRingFile")) {
        sign(publishing.publications)
    }
}

tasks {
    val dokkaHtml by getting(DokkaTask::class) {
        outputDirectory.set(projectDir.resolve("docs"))

        dokkaSourceSets {
            val js by creating {
                sourceLink {
                    localDirectory.set(file("src"))
                    remoteUrl.set(uri("https://github.com/adamint/spotify-web-api-kotlin/tree/master/").toURL())
                    remoteLineSuffix.set("#L")
                }
            }
            val jvm by creating {
                sourceLink {
                    localDirectory.set(file("src"))
                    remoteUrl.set(uri("https://github.com/adamint/spotify-web-api-kotlin/tree/master/").toURL())
                    remoteLineSuffix.set("#L")

                }
            }

            register("common") {
                sourceLink {
                    localDirectory.set(file("src"))
                    remoteUrl.set(uri("https://github.com/adamint/spotify-web-api-kotlin/tree/master/").toURL())
                    remoteLineSuffix.set("#L")
                }
            }

            register("global") {
                sourceLink {
                    localDirectory.set(file("src"))
                    remoteUrl.set(uri("https://github.com/adamint/spotify-web-api-kotlin/tree/master/").toURL())
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }

    spotless {
        kotlin {
            target("**/*.kt")
            licenseHeader("/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2020; Original author: Adam Ratzman */")
            ktlint()
        }
    }

    nexusStaging {
        packageGroup = "com.adamratzman"
    }


    getByName<Test>("jvmTest") {
        useJUnitPlatform()
    }

    val publishJvm by registering(Task::class) {
        dependsOn.add(check)
        dependsOn.add(dokkaHtml)
        dependsOn.add("publishJvmPublicationToNexusRepository")
    }

}


fun MavenPublication.setupPom(publicationName: String) {
    artifact(dokkaJar.get())

    pom {
        name.set(publicationName)
        description.set("A Kotlin wrapper for the Spotify Web API.")
        url.set("https://github.com/adamint/spotify-web-api-kotlin")
        inceptionYear.set("2018")
        scm {
            url.set("https://github.com/adamint/spotify-web-api-kotlin")
            connection.set("scm:https://github.com/adamint/spotify-web-api-kotlin.git")
            developerConnection.set("scm:git://github.com/adamint/spotify-web-api-kotlin.git")
        }
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("adamratzman")
                name.set("Adam Ratzman")
                email.set("adam@adamratzman.com")
            }
        }
    }
}
