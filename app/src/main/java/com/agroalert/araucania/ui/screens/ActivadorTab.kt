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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.data.RolUsuario
import com.agroalert.araucania.data.TipoEvento
import com.agroalert.araucania.ui.theme.*
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

@Composable
fun ActivadorTab(viewModel: AgroAlertViewModel) {
    val telemetria by viewModel.telemetria.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val isSwitchLoading by viewModel.isSwitchLoading.collectAsState()

    val esAdmin = usuarioActual?.rol == RolUsuario.ADMINISTRADOR

    var sliderValue by rememberSaveable { mutableFloatStateOf(telemetria.umbralHelada) }
    var observaciones by rememberSaveable { mutableStateOf("Sector 3 - Huerto Freire (Variedad Tonda di Giffoni)") }
    var showDialogAdvertenciaModo by rememberSaveable { mutableStateOf(false) }
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Banner informativo de Rol Activo (RBAC)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (esAdmin) AgroGreenLight else InacapBlueLight
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (esAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (esAdmin) AgroGreenPrimary else InacapBlue
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (esAdmin) "Perfil: ADMINISTRADOR DE RIEGO (Acceso Total)"
                               else "Perfil: OPERADOR DE TERRENO (Monitoreo y Bitácora)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (esAdmin) AgroGreenPrimary else InacapBlue
                    )
                    Text(
                        text = if (esAdmin) "Puede calibrar umbrales y conmutar modos de control."
                               else "Calibración restringida según política de seguridad ISO 27001 (A.9.1).",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        // 1. Tarjeta Slider: Calibración Dinámica de Umbral de Helada
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = AgroGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Calibración de Umbral (Slider)",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    if (!esAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningAmberLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Solo Admin", fontSize = 10.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = "Ajusta la temperatura crítica para encender la aspersión térmica y liberar 80 cal/g de calor latente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Punto Crítico de Helada:", fontSize = 13.sp, color = TextPrimary)
                    Text(
                        text = "${String.format("%.1f", sliderValue)} °C",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                }

                Slider(
                    value = sliderValue,
                    onValueChange = { 
                        if (esAdmin) {
                            sliderValue = it 
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
            }
        }

        // 2. Tarjeta RadioButtons: Selector Excluyente de Modo de Operación
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = InacapBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modo de Operación (RadioButtons)",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    if (!esAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningAmberLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Solo Admin", fontSize = 10.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = "Define la autonomía del lazo industrial OT en el microcontrolador ESP32.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                val opcionesModo = listOf(
                    "AUTOMATICO" to "Modo Automático (Fail-Safe autónomo en ESP32)",
                    "MANUAL" to "Modo Manual (Control Forzado por Operador)",
                    "MANTENIMIENTO" to "Modo Mantenimiento (Pruebas Hidráulicas)"
                )

                opcionesModo.forEach { (codigo, etiqueta) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = etiqueta,
                            fontSize = 13.sp,
                            fontWeight = if (telemetria.modoOperacion == codigo) FontWeight.Bold else FontWeight.Normal,
                            color = if (esAdmin) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // 3. Tarjeta Switch: Telemando del Actuador con Handshake Bidireccional
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = if (telemetria.bombaFisicaActiva) AgroGreenSecondary else TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Actuador Motobomba de Aspersión (Switch)",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (telemetria.bombaFisicaActiva) "BOMBA ENERGIZADA (Relé 5V Active LOW)" 
                                   else "BOMBA APAGADA (Contactos abiertos)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetria.bombaFisicaActiva) AgroGreenSecondary else TextSecondary
                        )
                        Text(
                            text = if (isSwitchLoading) "Esperando handshake de red Wi-Fi (108 ms)..." 
                                   else "Verificado con optoacoplador PC817 en GPIO 23.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    if (isSwitchLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Switch(
                            checked = telemetria.bombaFisicaActiva,
                            onCheckedChange = { nuevoEstado ->
                                if (telemetria.modoOperacion != "MANUAL") {
                                    showDialogAdvertenciaModo = true
                                } else {
                                    viewModel.toggleBombaManual(nuevoEstado)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CardBackground,
                                checkedTrackColor = AgroGreenSecondary
                            )
                        )
                    }
                }
            }
        }

        // 4. Tarjeta OutlinedTextField: Registro de Observaciones de Campo (EditText)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = InacapBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bitácora Agronómica de Parcela (EditText)",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones del Operador / Estado de Toberas") },
                    supportingText = { Text("Registro auditado en memoria segura ISO 27000.") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val autor = usuarioActual?.nombre ?: "Operador"
                        viewModel.registrarEvento(
                            TipoEvento.SISTEMA_OT,
                            "[$autor]: $observaciones"
                        )
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = InacapBlue)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar en Bitácora", fontSize = 12.sp)
                }
            }
        }
    }

    // Cuadro de Diálogo: Modo Automático Activo
    if (showDialogAdvertenciaModo) {
        AlertDialog(
            onDismissRequest = { showDialogAdvertenciaModo = false },
            title = { Text("Lazo Automático Activo") },
            text = {
                Text("Para accionar la bomba manualmente debe seleccionar 'Modo Manual'. Esto previene interrupciones accidentales al ciclo de protección Fail-Safe.")
            },
            confirmButton = {
                TextButton(onClick = { showDialogAdvertenciaModo = false }) {
                    Text("ENTENDIDO")
                }
            }
        )
    }

    // Cuadro de Diálogo: RBAC Acceso Restringido a Operador
    if (showDialogPermisoRbac) {
        AlertDialog(
            onDismissRequest = { showDialogPermisoRbac = false },
            title = { Text("Acceso Restringido (RBAC ISO 27001)") },
            text = {
                Text("Solo el usuario con rol de ADMINISTRADOR DE RIEGO posee autorización para calibrar umbrales y conmutar modos ciberfísicos. El Operador solo tiene permisos de monitoreo y bitácora.")
            },
            confirmButton = {
                TextButton(onClick = { showDialogPermisoRbac = false }) {
                    Text("CERRAR")
                }
            }
        )
    }
}
