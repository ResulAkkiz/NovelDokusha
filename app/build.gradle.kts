import java.util.Properties


plugins {
	id("com.android.application")
	id("kotlin-android")
	id("kotlin-kapt")
}

android {
	
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	
	compileSdkVersion(30)
	buildToolsVersion = "30.0.2"
	
	defaultConfig {
		applicationId = "my.noveldokusha"
		minSdkVersion(26)
		targetSdkVersion(30)
		versionCode = 1
		versionName = "1.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	
	signingConfigs {
		create("release") {
			val properties = Properties().apply {
				load(file("../local.properties").inputStream())
			}
			storeFile = file(properties.getProperty("storeFile"))
			storePassword = properties.getProperty("storePassword")
			keyAlias = properties.getProperty("keyAlias")
			keyPassword = properties.getProperty("keyPassword")
		}
	}
	
	buildTypes {
		
		all {
			signingConfig = signingConfigs["release"]
		}
		
		named("release") {
			postprocessing {
				proguardFile("proguard-rules.pro")
				isRemoveUnusedCode = true
				isObfuscate = false
				isOptimizeCode = true
				isRemoveUnusedResources = true
			}
		}
	}
	
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_1_8.toString()
	}
	
	buildFeatures {
		viewBinding = true
	}
}

dependencies {
	
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
	
	implementation("androidx.appcompat:appcompat:1.3.0")
	
	// Room components
	implementation("androidx.room:room-runtime:2.3.0")
	implementation("androidx.room:room-ktx:2.3.0")
	kapt("androidx.room:room-compiler:2.3.0")
	androidTestImplementation("androidx.room:room-testing:2.3.0")
	
	// Lifecycle components
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
	implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
	implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
	
	// UI
	implementation("androidx.constraintlayout:constraintlayout:2.0.4")
	implementation("com.google.android.material:material:1.4.0")
	
	implementation("com.google.code.gson:gson:2.8.6")
	
	implementation("androidx.recyclerview:recyclerview:1.2.1")
	implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
	
	
	implementation(fileTree("libs") { include("*.jar") })
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.10")
	implementation("androidx.core:core-ktx:1.6.0")
	implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
	implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
	implementation("org.jsoup:jsoup:1.13.1")
	
	implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.4.21")
	
	implementation("com.afollestad.material-dialogs:core:3.2.1")
	
	// Glide
	implementation("com.github.bumptech.glide:glide:4.12.0")
	kapt("com.github.bumptech.glide:compiler:4.12.0")
	
	implementation("com.chimbori.crux:crux:3.0.1")
	implementation("net.dankito.readability4j:readability4j:1.0.6")
	
	implementation("com.l4digital.fastscroll:fastscroll:2.0.1")
}