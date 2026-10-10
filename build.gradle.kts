plugins {
    kotlin("jvm") version "2.3.20"
    id("net.neoforged.moddev") version "2.0.148"
}
group = "net.ciyuan.lynxtags"
version = "1.0.0"
repositories { mavenCentral() }
kotlin { jvmToolchain(25) }
neoForge {
    version = "26.1.2.115"
    runs { create("client") { client() } }
    mods { if(!providers.gradleProperty("plainClient").isPresent) create("lynxtags") { sourceSet(sourceSets.main.get()) } }
}
dependencies {
    implementation(kotlin("stdlib")); jarJar(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test { useJUnitPlatform() }
tasks.jar { archiveBaseName.set("LynxTags"); from(listOf("LICENSE.md", "NOTICE.md")) { into("META-INF") } }
val verification by sourceSets.creating { compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output }
val plainVerification by sourceSets.creating { compileClasspath += sourceSets.main.get().compileClasspath }
if(providers.gradleProperty("plainClient").isPresent) {
    neoForge.mods.create("lynxplain_verification") { sourceSet(plainVerification) }
    neoForge.runs.named("client") {
        programArguments.addAll("--quickPlayMultiplayer", "127.0.0.1:25579")
        systemProperty("lynxplain.requests",providers.gradleProperty("requestClient").orElse("false").get())
    }
}
if(providers.gradleProperty("verifyClient").isPresent) {
    neoForge.mods.create("lynxtags_verification") { sourceSet(verification) }
    neoForge.runs.named("client") {
        programArguments.addAll("--quickPlayMultiplayer", "127.0.0.1:25579")
        systemProperty("lynxtags.visual",providers.gradleProperty("visualName").orElse("false").get())
    }
}
tasks.register<Zip>("sourceArchive") {
    archiveFileName.set("LynxTags-${project.version}-sources.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(projectDir) { include("src/**", "docs/**", "gradle/wrapper/**", "*.gradle.kts", "gradle.properties", "gradlew.ps1", ".gitignore", ".gitattributes", "README.md", "LICENSE.md", "NOTICE.md") }
}
