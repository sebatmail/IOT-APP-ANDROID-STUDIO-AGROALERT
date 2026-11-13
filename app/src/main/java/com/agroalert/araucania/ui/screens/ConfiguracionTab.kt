package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun ConfiguracionTab(
    viewModel: AgroAlertViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val telemetria by viewModel.telemetria.collectAsState()

    val esAdmin = usuarioActual?.rol == RolUsuario.ADMINISTRADOR

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Tarjeta de Perfil de Usuario y Rol Activo
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (esAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (esAdmin) AgroGreenPrimary else InacapBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = usuarioActual?.nombre ?: "Sebastián Torres",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = usuarioActual?.email ?: "admin@agroalert.cl",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (esAdmin) AgroGreenLight else InacapBlueLight
                    ) {
                        Text(
                            text = if (esAdmin) "ADMIN" else "OPERADOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (esAdmin) AgroGreenPrimary else InacapBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (esAdmin) "Permisos: Calibración total de umbrales, cambio de modos y telemando de bomba."
                           else "Permisos: Monitoreo en tiempo real y redacción de reportes de campo.",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }
        }

        // 2. Información del Hardware IoT (ESP32)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nodo de Campo ESP32", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Placa: ESP32-WROOM-32 DevKit V1 (Entregado por pañol con Joseph Cueto)", fontSize = 11.sp, color = TextSecondary)
                Text("• Sensor: DHT11 en GPIO 4 (One-Wire pull-up 10k a 3.3V)", fontSize = 11.sp, color = TextSecondary)
                Text("• Actuador: Relé 5V Active LOW optoacoplado en GPIO 23", fontSize = 11.sp, color = TextSecondary)
                Text("• Conectividad: Wi-Fi 802.11 b/g/n (WSS / Firebase)", fontSize = 11.sp, color = TextSecondary)
                Text("• Latencia Actual: ${telemetria.latenciaMs} ms", fontSize = 11.sp, color = InacapBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        // 3. Ciberseguridad ISO 27000 / ISO 27400
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = InacapBlueLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ciberseguridad IoT (ISO 27000 / 27400)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InacapBlue)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("✔ Cifrado en Reposo: AES-256 GCM en Android Keystore.", fontSize = 11.sp, color = TextPrimary)
                Text("✔ Control de Acceso: Segregación RBAC (Admin vs Operador).", fontSize = 11.sp, color = TextPrimary)
                Text("✔ Protección Brute-Force: Bloqueo de 60s tras 3 fallos.", fontSize = 11.sp, color = TextPrimary)
                Text("✔ Tráfico de Red Seguro: usesCleartextTraffic='false'.", fontSize = 11.sp, color = TextPrimary)
                Text("✔ Lazo Resiliente OT: Fail-Safe autónomo en ESP32.", fontSize = 11.sp, color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Botón de Cerrar Sesión
        Button(
            onClick = onCerrarSesion,
            colors = ButtonDefaults.buttonColors(containerColor = FrostRedAlert),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
