package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.ui.theme.AgroGreenPrimary
import com.agroalert.araucania.ui.theme.CardBackground
import com.agroalert.araucania.ui.theme.InacapBlue
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: AgroAlertViewModel,
    onCerrarSesion: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showDialogSeguridad by rememberSaveable { mutableStateOf(false) }

    val usuarioActual by viewModel.usuarioActual.collectAsState()

    val tabs = listOf(
        Triple("Monitoreo", Icons.Default.Sensors, "Lecturas"),
        Triple("Control Riego", Icons.Default.Tune, "Actuador"),
        Triple("Dispositivos", Icons.Default.Devices, "Red IoT")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = "AgroAlert Araucanía",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val rolTexto = if (usuarioActual?.rol == com.agroalert.araucania.data.RolUsuario.ADMINISTRADOR) 
                            "🔑 Admin: ${usuarioActual?.nombre ?: "Sebastián Torres"}" 
                            else "👤 Operador: ${usuarioActual?.nombre ?: "Operador de Terreno"}"
                        Text(
                            text = rolTexto,
                            fontSize = 11.sp,
                            color = CardBackground.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    // Botón de Información de Ciberseguridad ISO para la evaluación docente
                    IconButton(onClick = { showDialogSeguridad = true }) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Auditoría de Seguridad ISO",
                            tint = CardBackground
                        )
                    }

                    // Botón de Cerrar Sesión
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = CardBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroGreenPrimary,
                    titleContentColor = CardBackground,
                    actionIconContentColor = CardBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground
            ) {
                tabs.forEachIndexed { index, (titulo, icono, _) ->
                    NavigationBarItem(
                        selected = (selectedTab == index),
                        onClick = { selectedTab = index },
                        icon = { Icon(icono, contentDescription = titulo) },
                        label = { Text(titulo, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = InacapBlue,
                            selectedTextColor = InacapBlue,
                            indicatorColor = AgroGreenPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MonitoreoTab(viewModel = viewModel)
                1 -> ActivadorTab(viewModel = viewModel)
                2 -> MultiDispositivoTab(viewModel = viewModel)
            }
        }
    }

    // Modal de Ciberseguridad ISO 27000 / ISO 27400
    if (showDialogSeguridad) {
        AlertDialog(
            onDismissRequest = { showDialogSeguridad = false },
            title = {
                Text("Ciberseguridad IoT (ISO 27000 / ISO 27400)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Text("• Cifrado en Reposo: EncryptedSharedPreferences (AES-256 GCM en Android Keystore).", fontSize = 12.sp)
                    Text("• Control de Acceso RBAC: Segregación estricta entre Administrador y Operador de Terreno.", fontSize = 12.sp)
                    Text("• Protección de Claves: Regex robusta y penalización de 60 s ante 3 intentos fallidos.", fontSize = 12.sp)
                    Text("• Enlace Wi-Fi: Tráfico forzado TLS 1.3 con usesCleartextTraffic='false'.", fontSize = 12.sp)
                    Text("• Lazo Industrial OT: Fail-Safe autónomo en ESP32 independiente de la app móvil.", fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialogSeguridad = false }) {
                    Text("ENTENDIDO")
                }
            }
        )
    }
}
