plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "4.4.1.3373"
	id("jacoco")
}

group = "ru.tw1.euchekavelo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.postgresql:postgresql")
	implementation("org.liquibase:liquibase-core")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("io.projectreactor:reactor-test")
	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok:1.18.30")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.739")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
	testImplementation("org.testcontainers:localstack:1.19.8")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-tracing-bridge-brave")
	implementation("io.zipkin.reporter2:zipkin-reporter-brave")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val integrationTest: SourceSet = sourceSets.create("integrationTest") {
	java {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		srcDir("src/integrationTest/java")
	}
	resources.srcDir("src/integrationTest/resources")
}

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTestTask = tasks.register<Test>("integrationTest") {
	group = "verification"
	description = "Runs the integration tests."

	useJUnitPlatform()
	testClassesDirs = integrationTest.output.classesDirs
	classpath = sourceSets["integrationTest"].runtimeClasspath

	shouldRunAfter("test")
}

tasks.named<ProcessResources>("processIntegrationTestResources") {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

sonar {
	properties {
		property("sonar.login", System.getenv("SONAR_TOKEN_ACCESS"))
		property("sonar.host.url", System.getenv("SONAR_HOST_URL"))
		property("sonar.coverage.exclusions", "src/main/java/ru/tw1/euchekavelo/postservice/model/**, " +
				"src/main/java/ru/tw1/euchekavelo/postservice/dto/**, " +
				"src/main/java/ru/tw1/euchekavelo/postservice/exception/**, " +
				"src/main/java/ru/tw1/euchekavelo/postservice/*"
		)
	}
}

tasks.test {
	finalizedBy(integrationTestTask, tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	reports {
		xml.required = true
	}
	dependsOn(tasks.test, integrationTestTask)
}
