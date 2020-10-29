import org.jfrog.gradle.plugin.artifactory.dsl.*

plugins {
    kotlin("jvm") version "1.4.10"
    id("idea")
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("com.jfrog.artifactory") version "4.18.0"
}

group = "cloud.openerrands"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://openerrands.jfrog.io/artifactory/OpenErrands/")
    }
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

artifactory {
    setContextUrl("https://openerrands.jfrog.io/artifactory/OpenErrands/")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<DoubleDelegateWrapper> {
            setProperty("repoKey", "OpenErrandsLocal")
            setProperty("username", "openerrandsci")
            setProperty("password", System.getenv("ARTIFACTORY_CI_PASSWORD") ?: "nopassword")
        })
    })
}




