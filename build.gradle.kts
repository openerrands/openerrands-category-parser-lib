plugins {
    id("idea")
    id("java-library")
    id("jacoco")
    kotlin("jvm") version "1.4.10"
}

group = "cloud.openerrands"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    kotlin("stdlib-jdk8")
    implementation("com.fasterxml.woodstox:woodstox-core:6.2.3")
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("org.apache.opennlp:opennlp-tools:1.9.3")
    testImplementation("org.testng:testng:7.3.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    test {
        useTestNG()
        finalizedBy(jacocoTestReport)
    }
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.isEnabled = true
        }
    }
}
