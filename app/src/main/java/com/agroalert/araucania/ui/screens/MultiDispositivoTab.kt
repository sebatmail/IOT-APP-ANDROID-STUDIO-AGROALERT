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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.data.DispositivoConectado
import com.agroalert.araucania.data.TipoEvento
import com.agroalert.araucania.ui.theme.*
import com.agroalert.araucania.viewmodel.AgroAlertViewModel

@Composable
fun MultiDispositivoTab(viewModel: AgroAlertViewModel) {
    val dispositivos by viewModel.dispositivosConectados.collectAsState()
    val telemetria by viewModel.telemetria.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InacapBlueLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Devices, contentDescription = null, tint = InacapBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interconexión Multi-Dispositivo (Rúbrica 2.1.4)",
                            style = MaterialTheme.typography.titleLarge,
                            color = InacapBlue
                        )
                    }
                    Text(
                        text = "Arquitectura Cliente-Servidor y Pub/Sub sobre Wi-Fi. Resuelve la limitación de enlace único de Bluetooth clásico mediante sincronización reactiva en tiempo real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "Terminales Móviles Android Activos en el Predio:",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        items(dispositivos) { dev ->
            ItemDispositivoCard(dev)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, tint = AgroGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulación de Sincronización Concurrente",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Al presionar el botón inferior se simula un cambio de umbral enviado desde el Terminal de la Casa Patronal (Darío Candia) hacia el nodo ESP32 y reflejado en el teléfono del Operador (Sebastián Torres).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.calibrarUmbral(2.0f)
                            viewModel.registrarEvento(
                                TipoEvento.CAMBIO_MODO,
                                "Sincronización multi-cliente: Terminal B (Darío Candia) actualizó umbral a 2.0 °C en 112 ms."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simular Concurrencia desde Terminal Remoto", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDispositivoCard(dev: DispositivoConectado) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(InacapBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Smartphone, contentDescription = null, tint = InacapBlue)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(dev.nombreUsuario, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(dev.rol, fontSize = 11.sp, color = AgroGreenPrimary, fontWeight = FontWeight.SemiBold)
                Text(dev.modeloDispositivo, fontSize = 11.sp, color = TextSecondary)
                Text("IP: ${dev.ipLocal} • Latencia: ${dev.latenciaMs} ms", fontSize = 10.sp, color = TextSecondary)
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(AgroGreenSecondary)
            )
        }
    }
}
