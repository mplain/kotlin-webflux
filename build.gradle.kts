plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.spring") version "1.4.0"
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

group = "ru.mplain"
version = "2.0"
java.sourceCompatibility = JavaVersion.VERSION_14

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
//    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
//    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
//    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    { exclude(group = "org.junit.vintage", module = "junit-vintage-engine") }

}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "14"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    compileTestKotlin {
        kotlinOptions{
            jvmTarget = "14"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    test {
        useJUnitPlatform()
    }
}