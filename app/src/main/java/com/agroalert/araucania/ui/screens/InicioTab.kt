package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.R
import com.agroalert.araucania.data.RolUsuario
import com.agroalert.araucania.ui.components.GaugeTemperatura
import com.agroalert.araucania.ui.theme.*
import com.agroalert.araucania.viewmodel.AgroAlertViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InicioTab(viewModel: AgroAlertViewModel) {
    val telemetria by viewModel.telemetria.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val isSwitchLoading by viewModel.isSwitchLoading.collectAsState()

    val esAdmin = usuarioActual?.rol == RolUsuario.ADMINISTRADOR
    var showDialogRestringido by remember { mutableStateOf(false) }

    var horaActual by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Santiago")
        }
        while (true) {
            horaActual = sdf.format(Date())
            kotlinx.coroutines.delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Visor de Cámara Huerto - Vista Nocturna (Exacto al Mockup)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.vista_huerto_aspersores),
                contentDescription = "Cámara Huerto Nocturna",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradiente y barra inferior de estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.72f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CAMARA HUERTO - VISTA NOCTURNA",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = horaActual,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Velocímetro / Arco de Temperatura Central
        GaugeTemperatura(
            temperatura = telemetria.temperatura,
            umbralHelada = telemetria.umbralHelada,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Botones de Control de Riego (Estilo Mockup)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Botón 1: Riego Automático (Verde con Switch)
            val esAutomatico = telemetria.modoOperacion == "AUTOMATICO"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (esAutomatico) listOf(Color(0xFF388E3C), Color(0xFF1B5E20))
                                     else listOf(Color(0xFF6B7280), Color(0xFF374151))
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (esAutomatico) Color(0xFF81C784) else Color(0xFF9CA3AF),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (esAdmin) {
                            val nuevo = if (esAutomatico) "MANUAL" else "AUTOMATICO"
                            viewModel.cambiarModo(nuevo)
                        } else {
                            showDialogRestringido = true
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Switch visual simulado
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (esAutomatico) Color(0xFF81C784) else Color(0xFF4B5563))
                            .padding(2.dp),
                        contentAlignment = if (esAutomatico) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = if (esAutomatico) "RIEGO AUTOMÁTICO: ENCENDIDO" else "RIEGO AUTOMÁTICO: APAGADO",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Botón 2: Activar Riego Manual (Rojo)
            val bombaEncendida = telemetria.bombaFisicaActiva
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (bombaEncendida) listOf(Color(0xFFD32F2F), Color(0xFF880E4F))
                                     else listOf(Color(0xFFC62828), Color(0xFFB71C1C))
                        )
                    )
                    .border(width = 1.dp, color = Color(0xFFEF9A9A), shape = RoundedCornerShape(16.dp))
                    .clickable {
                        if (esAdmin) {
                            if (telemetria.modoOperacion != "MANUAL") {
                                viewModel.cambiarModo("MANUAL")
                            }
                            viewModel.toggleBombaManual(!bombaEncendida)
                        } else {
                            showDialogRestringido = true
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSwitchLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (bombaEncendida) "DESACTIVAR RIEGO MANUAL" else "ACTIVAR RIEGO MANUAL",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Píldora de estado inferior
        Text(
            text = "ESP32 Wi-Fi en Huerto Freire • Latencia: ${telemetria.latenciaMs} ms",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }

    if (showDialogRestringido) {
        AlertDialog(
            onDismissRequest = { showDialogRestringido = false },
            title = { Text("Acceso Restringido (RBAC)") },
            text = { Text("El perfil Operador solo cuenta con permisos de supervisión. Para accionar la motobomba o cambiar el modo inicie sesión como Administrador.") },
            confirmButton = {
                TextButton(onClick = { showDialogRestringido = false }) { Text("ENTENDIDO") }
            }
        )
    }
}
