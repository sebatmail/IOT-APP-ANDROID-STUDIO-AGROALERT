package com.agroalert.araucania

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agroalert.araucania.data.SecureStorageManager
import com.agroalert.araucania.ui.screens.LoginScreen
import com.agroalert.araucania.ui.screens.MainDashboardScreen
import com.agroalert.araucania.ui.theme.AgroAlertTheme
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AgroAlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val secureStorage = SecureStorageManager(applicationContext)
        val usuario = secureStorage.obtenerUsuario()
        val estaLogueado = usuario != null
        if (usuario != null) {
            viewModel.setUsuario(usuario)
        }

        setContent {
            AgroAlertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AgroAlertNavGraph(
                        viewModel = viewModel,
                        inicioDestino = if (estaLogueado) "dashboard" else "login",
                        onLogout = {
                            secureStorage.cerrarSesion()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AgroAlertNavGraph(
    viewModel: AgroAlertViewModel,
    inicioDestino: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = inicioDestino
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            MainDashboardScreen(
                viewModel = viewModel,
                onCerrarSesion = {
                    onLogout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}
