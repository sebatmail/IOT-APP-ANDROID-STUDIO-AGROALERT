package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.data.RolUsuario
import com.agroalert.araucania.ui.theme.*
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

@Composable
fun ClimaTab(viewModel: AgroAlertViewModel) {
    val telemetria by viewModel.telemetria.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()

    val esAdmin = usuarioActual?.rol == RolUsuario.ADMINISTRADOR

    var sliderValue by rememberSaveable { mutableFloatStateOf(telemetria.umbralHelada) }
    var showDialogPermisoRbac by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(telemetria.umbralHelada) {
        sliderValue = telemetria.umbralHelada
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Tarjetas de Sensores Agroclimáticos en Tiempo Real (DHT11)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Thermostat, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Temperatura", fontSize = 11.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${telemetria.temperatura} °C", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                    Text("Umbral: ${telemetria.umbralHelada} °C", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Humedad", fontSize = 11.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${telemetria.humedad} %", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = InacapBlue)
                    Text("Sensor DHT11 One-Wire", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        // Punto de Rocío y Estado
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Punto de Rocío", fontSize = 11.sp, color = TextSecondary)
                    Text("${telemetria.puntoRocio} °C", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Divider(modifier = Modifier.height(28.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bomba Riego", fontSize = 11.sp, color = TextSecondary)
                    Text(if (telemetria.bombaFisicaActiva) "ACTIVA" else "REPOSO", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (telemetria.bombaFisicaActiva) FrostRedAlert else AgroGreenPrimary)
                }
                Divider(modifier = Modifier.height(28.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lazo OT", fontSize = 11.sp, color = TextSecondary)
                    Text(telemetria.modoOperacion, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 2. Calibración de Umbral de Helada con Slider Continuo (Rúbrica 2.1.1.2)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calibración de Umbral (Slider)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${String.format("%.1f", sliderValue)} °C",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AgroGreenPrimary
                    )
                }

                Text(
                    text = "Ajusta la temperatura crítica para liberar calor latente (80 cal/g).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Slider(
                    value = sliderValue,
                    onValueChange = { nuevo ->
                        if (esAdmin) {
                            sliderValue = nuevo
                        } else {
                            showDialogPermisoRbac = true
                        }
                    },
                    onValueChangeFinished = {
                        if (esAdmin) {
                            viewModel.calibrarUmbral(sliderValue)
                        }
                    },
                    valueRange = 0.0f..6.0f,
                    steps = 11,
                    enabled = esAdmin,
                    colors = SliderDefaults.colors(
                        thumbColor = AgroGreenPrimary,
                        activeTrackColor = AgroGreenSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0.0 °C (Mínimo)", fontSize = 11.sp, color = TextSecondary)
                    Text("6.0 °C (Preventivo)", fontSize = 11.sp, color = TextSecondary)
                }

                if (!esAdmin) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔒 Control bloqueado: Requiere rol Administrador (ISO 27001)",
                        fontSize = 11.sp,
                        color = WarningAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3. Modo de Operación con RadioButtons Excluyentes (Rúbrica 2.1.1.2)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Modo de Operación (RadioButtons)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                val opcionesModo = listOf(
                    "AUTOMATICO" to "Modo Automático (Fail-Safe en ESP32)",
                    "MANUAL" to "Modo Manual (Control Forzado)",
                    "MANTENIMIENTO" to "Modo Mantenimiento (Pruebas hidráulicas)"
                )

                opcionesModo.forEach { (codigo, etiqueta) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = (telemetria.modoOperacion == codigo),
                            onClick = { 
                                if (esAdmin) {
                                    viewModel.cambiarModo(codigo)
                                } else {
                                    showDialogPermisoRbac = true
                                }
                            },
                            enabled = esAdmin,
                            colors = RadioButtonDefaults.colors(selectedColor = InacapBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = etiqueta,
                            fontSize = 12.sp,
                            fontWeight = if (telemetria.modoOperacion == codigo) FontWeight.Bold else FontWeight.Normal,
                            color = if (esAdmin) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // 4. Simulador Interactivo de Demostración
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Simulador de Helada en Vivo (Evaluación Docente):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.simularHeladaCritica() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FrostRedAlert),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simular Helada (-1.2°)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.restaurarTemperaturaNormal() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgroGreenPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Normal (14.8°)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDialogPermisoRbac) {
        AlertDialog(
            onDismissRequest = { showDialogPermisoRbac = false },
            title = { Text("Acceso Restringido (RBAC)") },
            text = { Text("Se requieren permisos de Administrador para modificar el umbral y conmutar modos de operación.") },
            confirmButton = {
                TextButton(onClick = { showDialogPermisoRbac = false }) { Text("ENTENDIDO") }
            }
        )
    }
}
