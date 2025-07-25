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

repositories { mavenCentral() }

dependencies {
    implementation(libs.springBootStarter)
    implementation(libs.kotlinReflect)
    implementation(libs.ktor)
    developmentOnly(libs.springBootDevtools)
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.kotlinJunit)
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
