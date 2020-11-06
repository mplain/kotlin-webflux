plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.spring") version "1.4.10"
    id("org.springframework.boot") version "2.3.5.RELEASE"
}

group = "ru.mplain"
version = "DEV-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_14

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.4.0-RC1"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.fu:spring-fu-kofu:0.4.2")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    { exclude(group = "org.junit.vintage", module = "junit-vintage-engine") }
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://repo.spring.io/milestone")
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