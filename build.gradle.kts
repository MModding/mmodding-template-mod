import com.mmodding.gradle.api.EnvironmentTarget

plugins {
	id("maven-publish")
	alias(libs.plugins.fabric.loom)
	alias(libs.plugins.mod.publish)
	alias(libs.plugins.mmodding.gradle)
}

version = "${project.properties["mod_version"]}+${libs.versions.minecraft.get()}"
group = project.properties["maven_group"] as String

base {
	archivesName = project.properties["archives_base_name"] as String
}

repositories {
	mavenCentral()
	maven {
		name = "MModding"
		url = uri("https://maven.mmodding.com/releases")
	}
}

dependencies {
	minecraft(libs.minecraft)
	implementation(libs.fabric.loader)

	implementation(libs.mmodding)
	implementation(libs.fabric.api)
}

fabricApi {
	configureDataGeneration {
		client = true
	}
}

mmodding {
	configureFabricModJson {
		name = project.properties["mod_name"] as String
		namespace = "template"
		icon = "assets/template/icon.png"
		license = "CC0 1.0 Universal"
		addAuthor("Project Author")
		withContact {
			homepage = "https://example.com"
            sources = "https://github.com/MModding/mmodding-template-mod"
            issues = "https://github.com/MModding/mmodding-template-mod/issues"
		}
		environment = EnvironmentTarget.ANY
		withEntrypoints {
			init("com.example.template.TemplateMod")
			custom("fabric-datagen", "com.example.template.TemplateModDataGenerator")
		}
		addMixin("template.mixins.json")
		withDependencies {
			javaVersion = ">=" + libs.versions.java.get()
			minecraftVersion = "~" + libs.versions.minecraft.get()
            fabricLoaderVersion = ">=" + libs.versions.fabric.loader.get()
            fabricApiVersion = ">=" + libs.versions.fabric.api.get()
			mmoddingLibraryVersion = ">=" + libs.versions.mmodding.get() + "-"
		}
	}
	loomModRegistration()
}

tasks.named<ProcessResources>("processResources") {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType(JavaCompile::class) {
	options.encoding = "UTF-8"
	// Minecraft 26.1 upwards uses Java 25.
	options.release = libs.versions.java.get().toInt()
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

setOf(tasks.named<Jar>("jar").get(), tasks.named<Jar>("sourcesJar").get()).forEach { jarTask ->
	jarTask.inputs.property("archivesName", project.name)

	for (licenseFilePath in setOf("LICENSE", "LICENSE.md")) {
		jarTask.from(licenseFilePath) {
			rename { "${licenseFilePath}_${project.name}"}
		}
	}
}

// Configuring Maven Publications
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}

fun extractSupportedVersions() : List<String> {
	var mcVer = libs.versions.minecraft.get()
	if (mcVer.contains("snapshot")) {
		return listOf(mcVer)
	}
	else {
		// published artifacts on these versions should also cover the proper Minecraft release correctly
		if (mcVer.contains("-pre")) mcVer = mcVer.split("-pre").first()
		if (mcVer.contains("-rc")) mcVer = mcVer.split("-rc").first()
		val versionComponents = mcVer.split(".")
		if (versionComponents.size == 2) {
			return listOf(mcVer)
		}
		else {
			val lastComponent = versionComponents.last().toInt()
			val versionBase = versionComponents.subList(0, 1).joinToString(".")
			val versions = mutableListOf(versionBase)
			for (i in 1..lastComponent) {
				versions.add("$versionBase.$i")
			}
			return versions
		}
	}
}

// Configures the mod publication
publishMods {
	if (providers.environmentVariable("CHANGELOG").isPresent) {
		changelog.set(providers.environmentVariable("CHANGELOG").get())

		val title = providers.environmentVariable("TITLE").get()
		if (title.contains("alpha")) {
			type.set(ALPHA)
		}
		else if (title.contains("beta")) {
			type.set(BETA)
		}
		else {
			type.set(STABLE)
		}

		displayName = "${properties["mod_name"]} $title"

		file.set(tasks.named<Jar>("jar").get().archiveFile)
		additionalFiles.from(tasks.named<Jar>("sourcesJar").get().archiveFile)

		modLoaders.add("fabric")
		modLoaders.add("quilt")

		modrinth {
			projectId = project.properties["modrinth_project"] as String
			accessToken = providers.environmentVariable("MODRINTH_TOKEN").get()

			minecraftVersions = extractSupportedVersions()

			projectDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText
		}

		curseforge {
			projectId = project.properties["curseforge_project"] as String
			accessToken = providers.environmentVariable("CURSEFORGE_TOKEN").get()

			javaVersions.add(JavaVersion.entries.first { v -> v.name == "VERSION_" + libs.versions.java.get() })

			minecraftVersions = extractSupportedVersions()

			client = true
			server = true

			changelogType = "markdown"
		}
	}
}