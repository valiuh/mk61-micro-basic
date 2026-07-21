import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.vanniktechMavenPublish)
}

group = "io.github.valiuh.mk61"
version = providers.gradleProperty("VERSION_NAME").get()

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MiniBasic"
            isStatic = true
        }
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    androidLibrary {
        namespace = "com.valiukh.microbasic"
        compileSdk = 36
        minSdk = 24

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.mk61.virtual.machine)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = com.vanniktech.maven.publish.JavadocJar.Empty(),
            sourcesJar = true,
            androidVariantsToPublish = listOf("release"),
        )
    )

    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("MK61 Micro Basic")
        description.set("A BASIC-inspired programming language for the Elektronika MK-61 virtual machine.")
        url.set("https://github.com/valiuh/mk61-micro-basic")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("valiukh")
                name.set("Anton Valiukh")
            }
        }

        scm {
            connection.set("scm:git:https://github.com/valiuh/mk61-micro-basic.git")
            developerConnection.set("scm:git:ssh://git@github.com/valiuh/mk61-micro-basic.git")
            url.set("https://github.com/valiuh/mk61-micro-basic")
        }
    }
}

tasks.register("assertPublishSecrets") {
    group = "verification"
    description = "Fails when required Maven Central/signing secrets are missing."

    doLast {
        val required = listOf(
            "ORG_GRADLE_PROJECT_mavenCentralUsername",
            "ORG_GRADLE_PROJECT_mavenCentralPassword",
            "ORG_GRADLE_PROJECT_signingInMemoryKey",
            "ORG_GRADLE_PROJECT_signingInMemoryKeyId",
            "ORG_GRADLE_PROJECT_signingInMemoryKeyPassword",
        )

        val missing = required.filter { System.getenv(it).isNullOrBlank() }
        check(missing.isEmpty()) {
            "Missing required publish secrets: ${missing.joinToString()}"
        }
    }
}
