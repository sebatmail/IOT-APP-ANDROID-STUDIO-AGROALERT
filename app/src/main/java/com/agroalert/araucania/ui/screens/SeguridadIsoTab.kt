package com.agroalert.araucania.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.ui.theme.*

@Composable
fun SeguridadIsoTab() {
    val vulnerabilidades = listOf(
        VulnerabilidadItem(
            titulo = "1. Contraseñas de Fábrica en Wi-Fi / Router Rural",
            riesgo = "Routers de faena con contraseñas por defecto (admin/admin) o claves predecibles permiten intrusiones de red o ataques Deauth.",
            mitigacion = "Segmentación de red en VLAN IoT agrícola aislada, contraseña WPA2/WPA3 robusta y rotación periódica. Fail-Safe autónomo en ESP32 si se corta la red."
        ),
        VulnerabilidadItem(
            titulo = "2. Vulnerabilidad de Bluetooth Clásico (Módulo HC-05)",
            riesgo = "PIN fijo de fábrica (1234 o 0000) sin cifrado. Cualquier intruso en 10 metros podía conectarse y secuestrar la bomba por puerto serial.",
            mitigacion = "Retiro total del HC-05 en pañol de INACAP y migración a ESP32 con Wi-Fi, eliminando el protocolo SPP inseguro y requiriendo tokens TLS 1.3."
        ),
        VulnerabilidadItem(
            titulo = "3. Ataques de Fuerza Bruta en Autenticación Móvil",
            riesgo = "Intentos reiterados automatizados para adivinar credenciales del operador de campo.",
            mitigacion = "Bloqueo por temporizador de 60 segundos tras 3 fallos consecutivos, política regex estricta (8+ caracteres con símbolos) y hash irreversible."
        ),
        VulnerabilidadItem(
            titulo = "4. Volcado de Memoria en Teléfonos Sustraídos",
            riesgo = "Extracción de contraseñas guardadas en texto plano en la memoria flash del teléfono móvil.",
            mitigacion = "Cifrado en reposo con EncryptedSharedPreferences respaldado en Android Keystore con la suite simétrica AES-256 GCM (ISO 27001 A.10.1)."
        ),
        VulnerabilidadItem(
            titulo = "5. Falsos Disparos por Ruido Eléctrico en Sensor",
            riesgo = "Cables jumpers largos generan lecturas de 0 °C o basura serial provocando encendidos erróneos de la bomba.",
            mitigacion = "Filtro isnan() en C++ del ESP32, resistencia pull-up física de 10 kΩ y operador Elvis (?:) en Kotlin para asignar valores defensivos."
        )
    )

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
                colors = CardDefaults.cardColors(containerColor = AgroGreenLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = AgroGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Matriz de Vulnerabilidades y Seguridad IoT",
                            style = MaterialTheme.typography.titleLarge,
                            color = AgroGreenPrimary
                        )
                    }
                    Text(
                        text = "Estándar industrial ISO/IEC 27400 (Seguridad en IoT), ISO/IEC 27001 y OWASP IoT Top 10 aplicados a la agroindustria de La Araucanía.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "Vulnerabilidades Mitigadas en el Proyecto:",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        items(vulnerabilidades.size) { index ->
            val v = vulnerabilidades[index]
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(InacapBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = InacapBlue, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(v.titulo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Riesgo: ${v.riesgo}",
                        fontSize = 11.sp,
                        color = FrostRedAlert,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Mitigación Implementada: ${v.mitigacion}",
                        fontSize = 11.sp,
                        color = AgroGreenPrimary
                    )
                }
            }
        }
    }
}

data class VulnerabilidadItem(
    val titulo: String,
    val riesgo: String,
    val mitigacion: String
)
