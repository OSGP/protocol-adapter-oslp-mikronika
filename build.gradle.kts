import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.spotless)
    alias(libs.plugins.gradleWrapperUpgrade)
    alias(libs.plugins.jacoco)
    alias(libs.plugins.jacocoReportAggregation)
}

group = "org.lfenergy.gxf"

version = System.getenv("GITHUB_REF_NAME")?.replace("/", "-")?.lowercase() ?: "develop"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "OSGP_protocol-adapter-oslp-mikronika")
        property("sonar.organization", "gxf")
    }
}

wrapperUpgrade {
    gradle {
        register("protocol-adapter-oslp-mikronika") {
            repo.set("OSGP/protocol-adapter-oslp-mikronika")
            baseBranch.set("main")
        }
    }
}

repositories {
    mavenCentral()
    maven {
        name = "GXFGithubPackages"
        url = uri("https://maven.pkg.github.com/osgp/*")
        credentials {
            username = project.findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    developmentOnly(libs.springBootDevtools)

    implementation(libs.flywayCore)
    implementation(libs.flywayPostgresql)
    implementation(libs.kotlinLoggingJvm)
    implementation(libs.kotlinReflect)
    implementation(libs.kotlinSerializationJson)
    implementation(libs.ktor)
    implementation(libs.micrometerRegistryPrometheus)
    implementation(libs.oslpMessageSigning)
    implementation(libs.postgresql)
    implementation(libs.protobufJavaUtil)
    implementation(libs.protobufKotlin)
    implementation(libs.springBootStarter)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterArtemis)
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.springBootStarterWeb)
    implementation(libs.protoDefinitions)

    testAndDevelopmentOnly(libs.springBootCompose)

    testImplementation(libs.assertJ)
    testImplementation(libs.kotlinJunit)
    testImplementation(libs.mockkJvm)
    testImplementation(libs.mockkSpring)
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.springBootTestcontainers)
    testImplementation(libs.testcontainersPostgresql)

    testRuntimeOnly(libs.junitLauncher)
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

tasks.withType<Test> { useJUnitPlatform() }

extensions.configure<SpotlessExtension> {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.6.0")

        licenseHeaderFile(file("./spotless/license-header-template.kt"))
    }
}

tasks.named<Jar>("bootJar") { archiveFileName.set("protocol-adapter-oslp-mikronika.jar") }
