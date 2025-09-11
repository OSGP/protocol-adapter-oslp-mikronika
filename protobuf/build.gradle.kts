plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(libs.protobufKotlin)
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

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

//sourceSets {
//    main {
//        java {
//            srcDirs += "build/generated/source/proto/main/java"
//        }
//    }
//}

