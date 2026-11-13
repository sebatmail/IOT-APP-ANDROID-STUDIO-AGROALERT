package com.agroalert.araucania.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroalert.araucania.data.RolUsuario
import com.agroalert.araucania.data.SecureStorageManager
import com.agroalert.araucania.ui.theme.*
import com.agroalert.araucania.viewmodel.AgroAlertViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AgroAlertViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val secureStorage = remember { SecureStorageManager(context) }

    var usuario by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMensaje by rememberSaveable { mutableStateOf<String?>(null) }
    var intentosFallidos by rememberSaveable { mutableIntStateOf(0) }
    var bloqueadoHasta by rememberSaveable { mutableLongStateOf(0L) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val estaBloqueado = System.currentTimeMillis() < bloqueadoHasta
    var tiempoRestante by remember { mutableLongStateOf(0L) }

    LaunchedEffect(bloqueadoHasta) {
        while (System.currentTimeMillis() < bloqueadoHasta) {
            tiempoRestante = (bloqueadoHasta - System.currentTimeMillis()) / 1000
            delay(1000)
        }
        tiempoRestante = 0
    }

    // Comprobación de reglas ISO 27000 en tiempo real
    val tieneLongitud = password.length >= 8
    val tieneMayuscula = password.any { it.isUpperCase() }
    val tieneMinuscula = password.any { it.isLowerCase() }
    val tieneNumero = password.any { it.isDigit() }
    val tieneSimbolo = password.any { "@$!%*?&".contains(it) }
    val claveCumplePolitica = tieneLongitud && tieneMayuscula && tieneMinuscula && tieneNumero && tieneSimbolo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logotipo / Icono AgroAlert Araucanía
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AgroGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "AgroAlert Logo",
                tint = AgroGreenPrimary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "AgroAlert Araucanía",
            style = MaterialTheme.typography.headlineLarge,
            color = InacapBlue,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Control Ciberfísico de Heladas (ESP32 IoT)",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Acceso Autenticado (ISO 27001 / RBAC)",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Roles habilitados: Administrador de Riego u Operador",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Campo Usuario / Correo
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        errorMensaje = null
                    },
                    label = { Text("Correo o Usuario") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    singleLine = true,
                    enabled = !estaBloqueado && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMensaje = null
                    },
                    label = { Text("Contraseña Segura") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = "Toggle password")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !estaBloqueado && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Checklist de Seguridad de Contraseñas (ISO 27400)
                AnimatedVisibility(visible = password.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text("Requisitos de contraseña robusta (ISO/IEC 27000):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        ItemReglaPassword("Mínimo 8 caracteres", tieneLongitud)
                        ItemReglaPassword("Al menos una mayúscula", tieneMayuscula)
                        ItemReglaPassword("Al menos una minúscula", tieneMinuscula)
                        ItemReglaPassword("Al menos un número", tieneNumero)
                        ItemReglaPassword("Al menos un símbolo (@$!%*?&)", tieneSimbolo)
                    }
                }

                // Error o bloqueo
                if (errorMensaje != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMensaje!!,
                        color = FrostRedAlert,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (estaBloqueado) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sistema bloqueado contra fuerza bruta: reintente en $tiempoRestante s.",
                        color = FrostRedAlert,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Iniciar Sesión
                Button(
                    onClick = {
                        if (estaBloqueado) return@Button

                        if (usuario.isBlank() || password.isBlank()) {
                            errorMensaje = "Ingrese usuario y contraseña completos."
                            return@Button
                        }

                        if (!claveCumplePolitica) {
                            errorMensaje = "La contraseña no cumple las reglas de seguridad requeridas."
                            return@Button
                        }

                        isLoading = true

                        // Verificación de credenciales y asignación de rol (RBAC)
                        val u = usuario.trim().lowercase()
                        if (u == "admin@agroalert.cl" && password == "Admin#Araucania2026") {
                            secureStorage.guardarSesion(u, "Sebastián Torres (Admin)", RolUsuario.ADMINISTRADOR)
                            val sesion = secureStorage.obtenerUsuario()!!
                            viewModel.setUsuario(sesion)
                            intentosFallidos = 0
                            isLoading = false
                            onLoginSuccess()
                        } else if (u == "operador@agroalert.cl" && (password == "Agro#Araucania2026" || password == "Operador#Araucania2026")) {
                            secureStorage.guardarSesion(u, "Operador de Terreno", RolUsuario.OPERADOR)
                            val sesion = secureStorage.obtenerUsuario()!!
                            viewModel.setUsuario(sesion)
                            intentosFallidos = 0
                            isLoading = false
                            onLoginSuccess()
                        } else {
                            intentosFallidos++
                            isLoading = false
                            if (intentosFallidos >= 3) {
                                bloqueadoHasta = System.currentTimeMillis() + 60_000L
                                errorMensaje = "Demasiados intentos fallidos. Bloqueado por 60 segundos."
                            } else {
                                errorMensaje = "Credenciales incorrectas (Intento $intentosFallidos de 3 permitidos)."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !estaBloqueado && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("INICIAR SESIÓN SEGURA", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Acceso Rápido para Demostración Docente (RBAC):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            usuario = "admin@agroalert.cl"
                            password = "Admin#Araucania2026"
                            errorMensaje = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Admin", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            usuario = "operador@agroalert.cl"
                            password = "Agro#Araucania2026"
                            errorMensaje = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Operador", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "INACAP Sede Temuco - TI3042 Aplicaciones Móviles para IoT",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
