plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.spring") version "1.4.20"
    id("org.springframework.boot") version "2.4.0"
}

group = "ru.mplain"
version = "DEV-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_14

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.4.0"))
    implementation(platform("org.testcontainers:testcontainers-bom:1.15.0"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kafka:reactor-kafka")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:mongodb")
}

repositories {
    mavenCentral()
    jcenter()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "14"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "14"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    test {
        useJUnitPlatform()
    }
}