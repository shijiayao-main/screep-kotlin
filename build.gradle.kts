import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDceDsl
import com.diffplug.gradle.spotless.SpotlessExtension
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

plugins {
    kotlin("multiplatform") version "2.0.0"
    kotlin("plugin.js-plain-objects") version "2.0.0"
    id("com.diffplug.spotless") version "6.11.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

val screepsUser: String? by project
val screepsPassword: String? by project
val screepsToken: String? by project
val screepsHost: String? by project
val screepsBranch: String? by project
val branch = screepsBranch ?: "default"
val host = screepsHost ?: "https://screeps.com"

val minifiedJsDirectory: String = File(buildDir, "minified-js").absolutePath

kotlin {
    js {
        compilations.all {
            compileTaskProvider.configure {
                destinationDirectory.file(minifiedJsDirectory)
                compilerOptions.freeCompilerArgs.addAll(
                    "-Xerror-tolerance-policy=SYNTAX",
                    "-Xir-minimized-member-names=false"
                )
            }
        }
        browser {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory.set(File(minifiedJsDirectory))
            }
            @OptIn(ExperimentalDceDsl::class)
            dceTask {
                keep(
                    "${project.name}.loop"
                )
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("io.github.exav:screeps-kotlin-types:2.1.0")
            }
        }
    }
}

fun String.encodeBase64() = Base64.getEncoder().encodeToString(this.toByteArray())

tasks.register("deploy") {
    group = "screeps"

    doFirst { // use doFirst to avoid running this code in configuration phase
        if (screepsToken == null && (screepsUser == null || screepsPassword == null)) {
            throw InvalidUserDataException("you need to supply either screepsUser and screepsPassword or screepsToken before you can upload code")
        }
        val minifiedCodeLocation = File(minifiedJsDirectory)
        if (!minifiedCodeLocation.isDirectory) {
            throw InvalidUserDataException("found no code to upload at ${minifiedCodeLocation.path}")
        }

        /*
        The screeps server expects us to upload our code in the following json format
        https://docs.screeps.com/commit.html#Using-direct-API-access
        {
            "branch":"<branch-name>"
            "modules": {
                "main":<main script as a string, must contain the "loop" function>
                "module1":<a module that is imported in the main script>
            }
        }
        The following code extracts the generated js code from the build folder and writes it to a string that has the
        correct format
         */

        val jsFiles = minifiedCodeLocation.listFiles { _, name -> name.endsWith(".js") }.orEmpty()
        val (mainModule, otherModules) = jsFiles.partition { it.nameWithoutExtension == project.name }
        val main = mainModule.firstOrNull()
            ?: throw IllegalStateException("Could not find js file corresponding to main module in ${minifiedCodeLocation.absolutePath}. Was looking for ${project.name}.js")
        val modules = mutableMapOf<String, String>()
        modules["main"] = main.readText()
        modules.putAll(otherModules.associate { it.nameWithoutExtension to it.readText() })
        val uploadContent = mapOf("branch" to branch, "modules" to modules)
        val uploadContentJson = groovy.json.JsonOutput.toJson(uploadContent)

        logger.lifecycle("Uploading ${jsFiles.count()} files to branch '$branch' on server $host")
        logger.debug("Request Body: $uploadContentJson")

        // upload using very old school HttpURLConnection as it is available in jdk < 9
        val url = URL("$host/api/user/code")
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        if (screepsToken != null) {
            connection.setRequestProperty("X-Token", screepsToken)
        } else {
            connection.setRequestProperty("Authorization", "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        }
        connection.outputStream.use {
            it.write(uploadContentJson.byteInputStream().readBytes())
        }

        val code = connection.responseCode
        val message = connection.responseMessage
        if (code in 200..299) {
            val body = connection.inputStream.bufferedReader().readText()
            logger.lifecycle("Upload done! $body")
        } else {
            val body = connection.errorStream.bufferedReader().readText()
            val shortMessage = "Upload failed! $code $message"

            logger.lifecycle(shortMessage)
            logger.lifecycle(body)
            logger.error(shortMessage)
            logger.error(body)
        }
        connection.disconnect()
    }
}

tasks.clean {
    delete("kotlin-js-store")
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("0.43.0")
    }
}

//subprojects {
//    project.afterEvaluate {
//        apply(plugin = "com.diffplug.spotless")
//
//        if (project.file("build.gradle").exists().not() && project.file("build.gradle.kts").exists().not()) {
//            return@afterEvaluate
//        }
//
//        configure<SpotlessExtension>() {
//            kotlin {
//                target("**/*.kt")
//                ktlint("0.43.0")
//            }
//            java {
//                target("**/*.java")
//                googleJavaFormat()
//                indentWithSpaces(2)
//                trimTrailingWhitespace()
//                removeUnusedImports()
//            }
//            kotlinGradle {
//                target("*.gradle.kts")
//                ktlint("0.43.0")
//            }
//        }
//    }
//}