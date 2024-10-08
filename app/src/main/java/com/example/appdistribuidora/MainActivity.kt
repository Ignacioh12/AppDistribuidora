package com.example.appdistribuidora

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.appdistribuidora.ui.theme.AppDistribuidoraTheme
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns
import android.location.Location
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.navigation.NavHostController

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance() // Inicializar FirebaseAuth
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance().reference // Inicializar Firebase Database

        // Solicitar permisos de ubicación
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    // Permiso concedido
                    Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    // Permiso concedido
                    Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Permiso denegado
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Solicitar permisos cuando se inicie la app
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            AppDistribuidoraTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "login") {
                    composable("login") {
                        SignInScreen(
                            onSignInClick = { email, password -> signInWithEmailPassword(email, password, navController) },
                            onRegisterClick = { navController.navigate("register") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterClick = { email, password -> registerUser(email, password, navController) },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }
                    composable("dashboard") {
                        DashboardScreen(onSaveLocationClick = { saveCurrentLocation() })
                    }
                }
            }
        }
    }

    // Función para iniciar sesión con correo y contraseña
    private fun signInWithEmailPassword(email: String, password: String, navController: NavHostController) {
        if (validateEmailPassword(email, password)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(
                            this, "Autenticación exitosa: ${user?.email}",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate("dashboard") // Redirige a Dashboard después de iniciar sesión
                    } else {
                        Toast.makeText(
                            this, "Error en la autenticación: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // Función para registrar usuario con correo y contraseña
    private fun registerUser(email: String, password: String, navController: NavHostController) {
        if (validateEmailPassword(email, password)) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(this, "Usuario registrado: ${user?.email}", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") // Redirige a la pantalla de inicio de sesión
                    } else {
                        Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Guardar la ubicación actual en Firebase Realtime Database
    private fun saveCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val locationData = mapOf(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude
                )
                database.child("locations").push().setValue(locationData)
                Toast.makeText(this, "Ubicación guardada en Firebase", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validación de email y contraseña
    private fun validateEmailPassword(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un correo.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una contraseña.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

@Composable
fun SignInScreen(onSignInClick: (String, String) -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally // Corrección aquí
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onSignInClick(email, password) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Iniciar Sesión")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { onRegisterClick() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Registrar")
            }
        }
    }
}

@Composable
fun RegisterScreen(onRegisterClick: (String, String) -> Unit, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally // Corrección aquí
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onRegisterClick(email, password) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Registrar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onBackToLogin() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Volver a Iniciar Sesión")
        }
    }
}

@Composable
fun DashboardScreen(onSaveLocationClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally // Corrección aquí
    ) {
        Text(text = "Bienvenido al Dashboard")
        Button(onClick = onSaveLocationClick) {
            Text("Guardar Ubicación")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    AppDistribuidoraTheme {
        SignInScreen(
            onSignInClick = { _, _ -> },
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    AppDistribuidoraTheme {
        RegisterScreen(
            onRegisterClick = { _, _ -> },
            onBackToLogin = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    AppDistribuidoraTheme {
        DashboardScreen(onSaveLocationClick = {})
    }
}
