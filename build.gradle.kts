plugins {
    // Plugin de Android para la aplicación
    id("com.android.application") version "8.6.0" apply false

    // Plugin de Kotlin para Android
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // Plugin de Google Services (opcional, si usas Firebase)
    id("com.google.gms.google-services") version "4.3.15" apply false

    // Plugin de Hilt (opcional, si usas Hilt para inyección de dependencias)
    id("com.google.dagger.hilt.android") version "2.44" apply false

    // Plugin de SafeArgs (opcional, si usas SafeArgs con Navigation)
    id("androidx.navigation.safeargs.kotlin") version "2.5.3" apply false
}
