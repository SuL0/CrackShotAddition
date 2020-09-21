plugins {
    kotlin("jvm") version "1.4.10"
    id("kr.entree.spigradle") version "2.2.3"
}


group = "kr.sul"
version = "1.0-SNAPSHOT"

tasks.compileJava.get().options.encoding = "UTF-8"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("com.destroystokyo.paper", "paper-api", "1.12.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc", "spigot", "1.12.2-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol", "ProtocolLib", "4.5.1")
    compileOnly("org.projectlombok", "lombok", "1.18.12")
    compileOnly(files("C:/Users/PHR/Desktop/PluginStorage/CrackShot_SuL.jar"))
    compileOnly(files("C:/Users/PHR/Desktop/PluginStorage/ServerCore_SuL.jar"))
    compileOnly(files("C:/Users/PHR/Desktop/PluginStorage/CustomEntity_SuL.jar"))
    compileOnly(files("C:/Users/PHR/Desktop/PluginStorage/Dependencies/item-nbt-api-plugin-2.5.0.jar"))
}


spigot {
    authors = listOf("SuL")
    version = "1.12"
    depends = listOf("ProtocolLib", "ServerCore", "CustomEntity", "NBTAPI")
    commands {
        create("csa") {
            description = "Command for test"
            permission = "op.op"
        }
    }
}


val shade = configurations.create("shade")
shade.extendsFrom(configurations.implementation.get())

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        archiveFileName.set("${project.name}_SuL.jar")
        destinationDirectory.set(file("C:/Users/PHR/Desktop/PluginStorage"))
        from(
                shade.map {
                    if (it.isDirectory)
                        it
                    else
                        zipTree(it)
                }
        )
    }
}