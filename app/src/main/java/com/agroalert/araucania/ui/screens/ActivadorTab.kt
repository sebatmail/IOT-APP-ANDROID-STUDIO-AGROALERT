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
fun ActivadorTab(viewModel: AgroAlertViewModel) {
    val telemetria by viewModel.telemetria.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val isSwitchLoading by viewModel.isSwitchLoading.collectAsState()

    val esAdmin = usuarioActual?.rol == RolUsuario.ADMINISTRADOR

    var sliderValue by rememberSaveable { mutableFloatStateOf(telemetria.umbralHelada) }
    var observaciones by rememberSaveable { mutableStateOf("Revisión de toberas en Sector 3: Aspersores libres de obstrucción.") }
    var reporteGuardadoConfirmacion by rememberSaveable { mutableStateOf(false) }
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

        // Banner informativo del Rol Activo (RBAC)
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
                    tint = if (esAdmin) AgroGreenPrimary else InacapBlue,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (esAdmin) "Administrador de Riego: Sebastián Torres"
                               else "Operador de Terreno: Modo Monitoreo e Inspección",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (esAdmin) AgroGreenPrimary else InacapBlue
                    )
                    Text(
                        text = if (esAdmin) "Permisos completos: Calibración de umbrales y telemando de bomba."
                               else "Parámetros críticos protegidos. Habilitado para redactar reportes de campo.",
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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Umbral de Helada (Slider)",
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
                    text = "Ajusta la temperatura de disparo para asperjar agua y liberar 80 cal/g de calor latente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Slider(
                    value = sliderValue,
                    onValueChange = { nuevoValor ->
                        if (esAdmin) {
                            sliderValue = nuevoValor
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
                        text = "🔒 Control bloqueado para Operador (Requiere privilegios de Administrador)",
                        fontSize = 11.sp,
                        color = WarningAmber,
                        fontWeight = FontWeight.SemiBold
                    )
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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Modo de Operación del Lazo OT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Define la autonomía del lazo en el microcontrolador ESP32.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                val opcionesModo = listOf(
                    "AUTOMATICO" to "Modo Automático (Lazo Fail-Safe autónomo en ESP32)",
                    "MANUAL" to "Modo Manual (Telemando forzado por Administrador)",
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

                if (!esAdmin) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔒 Conmutación restringida a perfil Administrador",
                        fontSize = 11.sp,
                        color = WarningAmber,
                        fontWeight = FontWeight.SemiBold
                    )
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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew, 
                        contentDescription = null, 
                        tint = if (telemetria.bombaFisicaActiva) AgroGreenSecondary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Actuador Motobomba (Switch)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (telemetria.bombaFisicaActiva) "BOMBA ENERGIZADA (Relé ON)" 
                                   else "BOMBA EN REPOSO (Relé OFF)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetria.bombaFisicaActiva) AgroGreenSecondary else TextSecondary
                        )
                        Text(
                            text = if (isSwitchLoading) "Esperando handshake Wi-Fi..." 
                                   else "Relé 5V optoacoplado en GPIO 23.",
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
                                if (!esAdmin) {
                                    showDialogPermisoRbac = true
                                } else if (telemetria.modoOperacion != "MANUAL") {
                                    showDialogAdvertenciaModo = true
                                } else {
                                    viewModel.toggleBombaManual(nuevoEstado)
                                }
                            },
                            enabled = esAdmin,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CardBackground,
                                checkedTrackColor = AgroGreenSecondary
                            )
                        )
                    }
                }
            }
        }

        // 4. Tarjeta OutlinedTextField: Registro de Observaciones de Campo (Función Clave de Operador)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reporte de Inspección Agrícola (Campo)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "El operador y el administrador pueden registrar observaciones de toberas, escarcha o faena.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { 
                        observaciones = it 
                        reporteGuardadoConfirmacion = false
                    },
                    label = { Text("Nota de Terreno / Observaciones de Riego") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reporteGuardadoConfirmacion) {
                        Text(
                            text = "✔ Reporte guardado en bitácora",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenSecondary
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (observaciones.isNotBlank()) {
                                viewModel.agregarReporteCampo(observaciones.trim())
                                reporteGuardadoConfirmacion = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InacapBlue)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar en Bitácora", fontSize = 12.sp)
                    }
                }
            }
        }

        // 5. Panel Interactivo de Demostración para la Evaluación Docente
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulador de Demostración en Clase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Prueba la reacción inmediata de la app y del microcontrolador ante un evento térmico:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.simularHeladaCritica() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FrostRedAlert),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simular Helada (-1.2°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.restaurarTemperaturaNormal() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgroGreenPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Normal (14.8°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
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
            title = { Text("Acceso Restringido (RBAC)") },
            text = {
                Text("El perfil de Operador de Terreno solo cuenta con permisos de lectura y redacción de reportes. Para alterar umbrales o activar la bomba inicie sesión como Administrador.")
            },
            confirmButton = {
                TextButton(onClick = { showDialogPermisoRbac = false }) {
                    Text("CERRAR")
                }
            }
        )
    }
}
