plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.hilt.android)
	alias(libs.plugins.kotlinxSerialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.grgit)
}

android {
	namespace = Constants.APP_ID
	compileSdk = Constants.TARGET_SDK

	androidResources {
		generateLocaleConfig = true
	}

	// reproducibility
	dependenciesInfo {
		// Disables dependency metadata when building APKs.
		includeInApk = false
		// Disables dependency metadata when building Android App Bundles.
		includeInBundle = false
	}

	defaultConfig {
		applicationId = Constants.APP_ID
		minSdk = Constants.MIN_SDK
		targetSdk = Constants.TARGET_SDK
		versionCode = Constants.VERSION_CODE
		versionName = Constants.VERSION_NAME

		ksp { arg("room.schemaLocation", "$projectDir/schemas") }

		sourceSets {
			getByName("debug").assets.srcDirs(files("$projectDir/schemas")) // Room
		}

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables { useSupportLibrary = true }
	}

	signingConfigs {
		create(Constants.RELEASE) {
			storeFile = getStoreFile()
			storePassword = getSigningProperty(Constants.STORE_PASS_VAR)
			keyAlias = getSigningProperty(Constants.KEY_ALIAS_VAR)
			keyPassword = getSigningProperty(Constants.KEY_PASS_VAR)
		}
	}

	buildTypes {
		// don't strip
		packaging.jniLibs.keepDebugSymbols.addAll(
			listOf("libwg-go.so", "libwg-quick.so", "libwg.so"),
		)

		release {
			isDebuggable = false
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
			applicationIdSuffix = ".pro"
			signingConfig = signingConfigs.getByName(Constants.RELEASE)
			resValue("string", "provider", "\"${Constants.APP_NAME}.provider\"")
		}
		debug {
			applicationIdSuffix = ".debug"
			versionNameSuffix = "-debug"
			isDebuggable = true
			resValue("string", "app_name", "PiVPN - Debug")
			resValue("string", "provider", "\"${Constants.APP_NAME}.provider.debug\"")
		}

		applicationVariants.all {
			val variant = this
			variant.outputs
				.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
				.forEach { output ->
					val outputFileName =
						"${Constants.APP_NAME}-${variant.flavorName}-" +
							"${variant.buildType.name}-${variant.versionName}.apk"
					output.outputFileName = outputFileName
				}
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
		isCoreLibraryDesugaringEnabled = true
	}
	kotlinOptions { jvmTarget = Constants.JVM_TARGET }
	buildFeatures {
		compose = true
		buildConfig = true
	}
	packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)

	// helpers for implementing LifecycleOwner in a Service
	implementation(libs.androidx.lifecycle.service)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)

	// test
	testImplementation(libs.junit)
	testImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test)
	androidTestImplementation(libs.androidx.room.testing)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.manifest)

	// tunnel
	implementation(libs.tunnel)
	implementation(libs.amneziawg.android)
	coreLibraryDesugaring(libs.desugar.jdk.libs)

	// logging
	implementation(libs.timber)

	// compose navigation
	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.hilt.navigation.compose)

	// hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.android.compiler)
	ksp(libs.androidx.hilt.compiler)

	// accompanist
	implementation(libs.accompanist.permissions)
	implementation(libs.accompanist.drawablepainter)

	// storage
	implementation(libs.androidx.room.runtime)
	ksp(libs.androidx.room.compiler)
	implementation(libs.androidx.room.ktx)
	implementation(libs.androidx.datastore.preferences)

	// lifecycle
	implementation(libs.lifecycle.runtime.compose)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.process)

	// icons
	implementation(libs.material.icons.extended)
	// serialization
	implementation(libs.kotlinx.serialization.json)

	// barcode scanning
	implementation(libs.zxing.android.embedded)

	// bio
	implementation(libs.androidx.biometric.ktx)

	// shortcuts
	implementation(libs.androidx.core)

	// splash
	implementation(libs.androidx.core.splashscreen)

	// worker
	implementation(libs.androidx.work.runtime)
	implementation(libs.androidx.hilt.work)
}
