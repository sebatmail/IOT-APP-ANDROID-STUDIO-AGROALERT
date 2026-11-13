package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.agroalert.araucania.data.BitacoraRegistro
import com.agroalert.araucania.ui.theme.*
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

@Composable
fun AlertasTab(viewModel: AgroAlertViewModel) {
    val telemetria by viewModel.telemetria.collectAsState()
    val historial by viewModel.historialEventos.collectAsState()

    var notaCampo by rememberSaveable { mutableStateOf("Inspección de huerto: Aspersores y toberas revisadas en terreno.") }
    var guardadoExitoso by rememberSaveable { mutableStateOf(false) }

    val esHeladaCritica = telemetria.temperatura <= telemetria.umbralHelada

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner de Alerta Crítica / Normal
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esHeladaCritica) FrostRedLight else AgroGreenLight
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (esHeladaCritica) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (esHeladaCritica) FrostRedAlert else AgroGreenSecondary,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (esHeladaCritica) "¡ALERTA CRÍTICA DE HELADA!" else "MONITOREO NORMAL",
                            fontWeight = FontWeight.Bold,
                            color = if (esHeladaCritica) FrostRedAlert else AgroGreenSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (esHeladaCritica) 
                                "Temperatura bajo umbral (${telemetria.temperatura} °C <= ${telemetria.umbralHelada} °C). Bomba activada." 
                                else "Condición agroclimática segura. Microcontrolador en vigilancia continua.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Formulario de Reporte de Campo (EditText interactivo para Operador)
        item {
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
                            text = "Bitácora Agronómica de Faena (EditText)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "El operador y el administrador pueden registrar novedades de terreno auditadas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    OutlinedTextField(
                        value = notaCampo,
                        onValueChange = { 
                            notaCampo = it 
                            guardadoExitoso = false
                        },
                        label = { Text("Observaciones de toberas, aspersión o escarcha") },
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
                        if (guardadoExitoso) {
                            Text(
                                text = "✔ Reporte registrado en bitácora",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenSecondary
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                if (notaCampo.isNotBlank()) {
                                    viewModel.agregarReporteCampo(notaCampo.trim())
                                    guardadoExitoso = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = InacapBlue)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar Reporte", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Título de la Bitácora
        item {
            Text(
                text = "Registro Histórico de Auditoría OT en Vivo:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Lista de Eventos
        items(historial) { ev ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ev.hora,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InacapBlue,
                        modifier = Modifier.width(55.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ev.mensaje,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
