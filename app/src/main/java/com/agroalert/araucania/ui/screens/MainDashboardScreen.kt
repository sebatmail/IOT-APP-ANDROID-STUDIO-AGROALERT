package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.ui.theme.AgroGreenPrimary
import com.agroalert.araucania.ui.theme.CardBackground
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: AgroAlertViewModel,
    onCerrarSesion: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val usuarioActual by viewModel.usuarioActual.collectAsState()

    // 4 Pestañas exactas al mockup original del usuario
    val tabs = listOf(
        Pair("INICIO", Icons.Default.Home),
        Pair("CLIMA", Icons.Default.Thermostat),
        Pair("ALERTAS", Icons.Default.Notifications),
        Pair("CONFIGURACIÓN", Icons.Default.Settings)
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
                tabs.forEachIndexed { index, (titulo, icono) ->
                    NavigationBarItem(
                        selected = (selectedTab == index),
                        onClick = { selectedTab = index },
                        icon = { Icon(icono, contentDescription = titulo) },
                        label = {
                            Text(
                                text = titulo,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (selectedTab == index) AgroGreenPrimary else Color(0xFF6B7280)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgroGreenPrimary,
                            selectedTextColor = AgroGreenPrimary,
                            unselectedIconColor = Color(0xFF6B7280),
                            unselectedTextColor = Color(0xFF6B7280),
                            indicatorColor = AgroGreenPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> InicioTab(viewModel = viewModel)
                1 -> ClimaTab(viewModel = viewModel)
                2 -> AlertasTab(viewModel = viewModel)
                3 -> ConfiguracionTab(viewModel = viewModel, onCerrarSesion = onCerrarSesion)
            }
        }
    }
}
