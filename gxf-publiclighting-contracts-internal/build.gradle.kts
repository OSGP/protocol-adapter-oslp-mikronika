plugins {
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlinJvm)
}

group = "org.lfenergy.gxf"
description = "GXF public lighting contracts for internal use"
version = System.getenv("GITHUB_REF_NAME")?.replace("/", "-")?.lowercase() ?: "develop"

repositories { mavenCentral() }

dependencies { implementation(libs.protobufKotlin) }

kotlin { jvmToolchain(21) }

protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                register("kotlin")
            }
        }
    }
}
