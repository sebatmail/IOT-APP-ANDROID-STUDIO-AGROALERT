package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.data.BitacoraRegistro
import com.agroalert.araucania.ui.theme.*
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

@Composable
fun MonitoreoTab(viewModel: AgroAlertViewModel) {
    val telemetria by viewModel.telemetria.collectAsState()
    val historial by viewModel.historialEventos.collectAsState()

    val esHeladaCritica = telemetria.temperatura <= telemetria.umbralHelada
    val esAdvertencia = telemetria.temperatura <= (telemetria.umbralHelada + 2.0f) && !esHeladaCritica

    val bannerColor = when {
        esHeladaCritica -> FrostRedAlert
        esAdvertencia -> WarningAmber
        else -> AgroGreenSecondary
    }

    val bannerFondo = when {
        esHeladaCritica -> FrostRedLight
        esAdvertencia -> WarningAmberLight
        else -> AgroGreenLight
    }

    val bannerTexto = when {
        esHeladaCritica -> "¡ALERTA CRÍTICA DE HELADA! RIEGO ACTIVO"
        esAdvertencia -> "ADVERTENCIA PREVENTIVA DE ENFRIAMIENTO"
        else -> "CONDICIONES AGROCLIMÁTICAS SEGURAS"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Píldora de Conectividad ESP32 Wi-Fi + Firebase (Diseño limpio y no deformable)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InacapBlueLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AgroGreenSecondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ESP32 Wi-Fi Huerto Freire",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InacapBlue,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = InacapBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${telemetria.latenciaMs} ms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InacapBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 2. Semáforo Virtual Reactivo y Banner de Alerta
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bannerFondo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (esHeladaCritica) Icons.Default.Warning else Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = bannerColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bannerTexto,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (esHeladaCritica) 
                                "Liberando 80 cal/g de calor latente para proteger las yemas."
                                else "Sensor One-Wire DHT11 en GPIO 4 con pull-up físico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // 3. Fila de Tarjetas Principales (Temperatura y Humedad)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarjeta Temperatura
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, tint = bannerColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Temperatura", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${telemetria.temperatura} °C",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = bannerColor
                        )
                        Text(
                            text = "Umbral: ${telemetria.umbralHelada} °C",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Tarjeta Humedad
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Humedad", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${telemetria.humedad} %",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = InacapBlue
                        )
                        Text(
                            text = "Sensor DHT11",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 4. Métricas Agroclimáticas Secundarias
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ItemMetricaSecundaria("Punto de Rocío", "${telemetria.puntoRocio} °C", Icons.Default.Cloud)
                    Divider(modifier = Modifier.height(32.dp).width(1.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ItemMetricaSecundaria("Estado Bomba", if (telemetria.bombaFisicaActiva) "ON (Activo)" else "OFF (Reposo)", Icons.Default.Power)
                    Divider(modifier = Modifier.height(32.dp).width(1.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    ItemMetricaSecundaria("Modo Lazo", telemetria.modoOperacion, Icons.Default.Settings)
                }
            }
        }

        // 5. Historial de Eventos y Bitácora en Vivo
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bitácora de Eventos de Campo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "En tiempo real",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        items(historial.take(6)) { evento ->
            ItemEventoRow(evento)
        }
    }
}

@Composable
fun ItemMetricaSecundaria(titulo: String, valor: String, icono: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(13.dp), tint = TextSecondary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(titulo, fontSize = 11.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(valor, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
    }
}

@Composable
fun ItemEventoRow(evento: BitacoraRegistro) {
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
                text = evento.hora,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InacapBlue,
                modifier = Modifier.width(55.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = evento.mensaje,
                fontSize = 12.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
