package com.agroalert.araucania.data

/**
 * Modelos de datos para el ecosistema IoT AgroAlert Araucanía
 * Cumple con normas ISO 27000 de integridad, control de acceso RBAC y disociación de datos personales.
 */

enum class RolUsuario {
    ADMINISTRADOR, // Acceso total: calibración de umbrales, cambio de modos, control manual
    OPERADOR       // Acceso operativo: lectura en tiempo real, alertas y registro de bitácora
}

data class TelemetriaData(
    val temperatura: Float = 1.2f,
    val humedad: Float = 89.0f,
    val puntoRocio: Float = -0.4f,
    val voltajeBateria: Float = 3.32f,
    val bombaFisicaActiva: Boolean = true,
    val modoOperacion: String = "AUTOMATICO", // AUTOMATICO, MANUAL, MANTENIMIENTO
    val umbralHelada: Float = 1.5f,
    val wifiSignalDbm: Int = -58,
    val latenciaMs: Long = 108L,
    val timestamp: Long = System.currentTimeMillis(),
    val nodoId: String = "ESP32-WROOM-32 (Huerto Freire)"
)

data class DispositivoConectado(
    val id: String,
    val nombreUsuario: String,
    val rol: String,
    val modeloDispositivo: String,
    val ipLocal: String,
    val estado: String = "Conectado vía WSS (TLS 1.3)",
    val latenciaMs: Long = 112L
)

data class BitacoraRegistro(
    val id: Long = System.currentTimeMillis(),
    val hora: String,
    val tipo: TipoEvento,
    val mensaje: String
)

enum class TipoEvento {
    ALERTA_HELADA,
    COMANDO_RELÉ,
    CAMBIO_MODO,
    SISTEMA_OT,
    LOGIN_SEGURIDAD,
    CONTROL_RBAC,
    BITACORA
}

data class UsuarioSesion(
    val email: String,
    val nombre: String,
    val rol: RolUsuario,
    val tokenCifrado: String,
    val fechaIngreso: Long = System.currentTimeMillis()
)
