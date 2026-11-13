package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    val usuarioActual by viewModel.usuarioActual.collectAsState()

    val tabs = listOf(
        Triple("Monitoreo", Icons.Default.Sensors, "Lecturas"),
        Triple("Activador", Icons.Default.Tune, "Control"),
        Triple("Multi-App", Icons.Default.Devices, "Concurrencia"),
        Triple("Seguridad", Icons.Default.Shield, "ISO 27000")
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
                            "🔑 Admin: ${usuarioActual?.nombre ?: "Sebastián Torres (Admin)"}" 
                            else "👤 Operador: ${usuarioActual?.nombre ?: "Operador de Terreno"}"
                        Text(
                            text = rolTexto,
                            fontSize = 11.sp,
                            color = CardBackground.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
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
                3 -> SeguridadIsoTab()
            }
        }
    }
}
