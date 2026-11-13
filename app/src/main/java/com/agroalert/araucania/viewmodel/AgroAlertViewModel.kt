package com.agroalert.araucania.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroalert.araucania.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgroAlertViewModel : ViewModel() {

    private val _telemetria = MutableStateFlow(TelemetriaData())
    val telemetria: StateFlow<TelemetriaData> = _telemetria.asStateFlow()

    private val _usuarioActual = MutableStateFlow<UsuarioSesion?>(null)
    val usuarioActual: StateFlow<UsuarioSesion?> = _usuarioActual.asStateFlow()

    private val _historialEventos = MutableStateFlow<List<BitacoraRegistro>>(emptyList())
    val historialEventos: StateFlow<List<BitacoraRegistro>> = _historialEventos.asStateFlow()

    private val _dispositivosConectados = MutableStateFlow<List<DispositivoConectado>>(emptyList())
    val dispositivosConectados: StateFlow<List<DispositivoConectado>> = _dispositivosConectados.asStateFlow()

    private val _isSwitchLoading = MutableStateFlow(false)
    val isSwitchLoading: StateFlow<Boolean> = _isSwitchLoading.asStateFlow()

    init {
        inicializarDispositivosMultiCliente()
        iniciarFlujoTelemetriaEsp32()
        registrarEvento(TipoEvento.SISTEMA_OT, "Nodo ESP32 inicializado. Conexión Wi-Fi 802.11 b/g/n en Huerto Freire (WPA2-PSK).")
    }

    fun setUsuario(usuario: UsuarioSesion) {
        _usuarioActual.value = usuario
        registrarEvento(
            TipoEvento.LOGIN_SEGURIDAD,
            "Inicio de sesión autenticado: ${usuario.nombre} [Rol: ${usuario.rol.name}] (ISO 27001)."
        )
    }

    private fun inicializarDispositivosMultiCliente() {
        _dispositivosConectados.value = listOf(
            DispositivoConectado(
                id = "DEV-01",
                nombreUsuario = "Sebastián Torres",
                rol = "Administrador AgroAlert (Casa Patronal)",
                modeloDispositivo = "Samsung Galaxy A52 (Android 13)",
                ipLocal = "192.168.1.104",
                latenciaMs = 95L
            ),
            DispositivoConectado(
                id = "DEV-02",
                nombreUsuario = "Operador de Terreno (Agrónomo)",
                rol = "Operador Huerto Lautaro",
                modeloDispositivo = "Motorola Moto G84 (Android 14)",
                ipLocal = "192.168.1.118",
                latenciaMs = 115L
            )
        )
    }

    /**
     * Simulación asíncrona del flujo de datos del microcontrolador ESP32 vía Wi-Fi.
     * Muestra datos realistas con fluctuaciones agroclimáticas de La Araucanía.
     */
    private fun iniciarFlujoTelemetriaEsp32() {
        viewModelScope.launch {
            var ciclo = 0
            while (true) {
                delay(3000) // Muestreo cada 3 segundos
                ciclo++

                _telemetria.update { current ->
                    val delta = (Math.sin(ciclo * 0.4) * 0.25).toFloat()
                    val nuevaTemp = String.format(Locale.US, "%.1f", (current.temperatura + delta).coerceIn(-1.5f, 5.0f)).toFloat()
                    val nuevaHum = String.format(Locale.US, "%.1f", (current.humedad + (delta * 0.5f)).coerceIn(80f, 95f)).toFloat()
                    val nuevoPuntoRocio = String.format(Locale.US, "%.1f", nuevaTemp - ((100 - nuevaHum) / 5)).toFloat()

                    // LÓGICA FAIL-SAFE OT: En modo automático, la bomba se enciende si la temp cae bajo el umbral
                    val bombaCalculada = if (current.modoOperacion == "AUTOMATICO") {
                        nuevaTemp <= current.umbralHelada
                    } else {
                        current.bombaFisicaActiva
                    }

                    if (bombaCalculada != current.bombaFisicaActiva) {
                        val estadoTxt = if (bombaCalculada) "ACTIVADA (Calor latente 80 cal/g en curso)" else "APAGADA (Temperatura segura)"
                        registrarEvento(TipoEvento.ALERTA_HELADA, "Lazo Fail-Safe: Bomba de aspersión $estadoTxt a $nuevaTemp °C.")
                    }

                    current.copy(
                        temperatura = nuevaTemp,
                        humedad = nuevaHum,
                        puntoRocio = nuevoPuntoRocio,
                        bombaFisicaActiva = bombaCalculada,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    /**
     * Calibración interactiva del umbral de helada mediante Slider continuo (0.0 a 6.0 °C).
     * Control de Acceso RBAC: Solo ADMINISTRADOR puede calibrar.
     */
    fun calibrarUmbral(nuevoUmbral: Float): Boolean {
        if (_usuarioActual.value?.rol != RolUsuario.ADMINISTRADOR) {
            registrarEvento(
                TipoEvento.CONTROL_RBAC,
                "Intento de calibración bloqueado: El usuario ${_usuarioActual.value?.nombre} (OPERADOR) no tiene privilegios de ADMIN."
            )
            return false
        }
        val umbralRedondeado = String.format(Locale.US, "%.1f", nuevoUmbral).toFloat()
        _telemetria.update { it.copy(umbralHelada = umbralRedondeado) }
        registrarEvento(TipoEvento.CAMBIO_MODO, "Administrador reconfiguró umbral de helada a $umbralRedondeado °C. Sincronizado en ESP32.")
        return true
    }

    /**
     * Selección de modo de operación del sistema con RadioButtons.
     * Control de Acceso RBAC: Solo ADMINISTRADOR puede conmutar modos.
     */
    fun cambiarModo(nuevoModo: String): Boolean {
        if (_usuarioActual.value?.rol != RolUsuario.ADMINISTRADOR) {
            registrarEvento(
                TipoEvento.CONTROL_RBAC,
                "Intento de cambio de modo bloqueado: Se requieren credenciales de Administrador de Riego."
            )
            return false
        }
        _telemetria.update { it.copy(modoOperacion = nuevoModo) }
        registrarEvento(TipoEvento.CAMBIO_MODO, "Modo de operación cambiado a '$nuevoModo' por el Administrador.")
        return true
    }

    /**
     * Conmutación manual de la bomba con Switch mediante Handshake de seguridad.
     */
    fun toggleBombaManual(activar: Boolean) {
        viewModelScope.launch {
            _isSwitchLoading.value = true
            delay(250) // Simulación de tiempo de ida y vuelta (Handshake de red Wi-Fi hacia ESP32)
            _telemetria.update { it.copy(bombaFisicaActiva = activar) }
            _isSwitchLoading.value = false
            
            val accion = if (activar) "ENCENDIDA MANUALMENTE" else "APAGADA MANUALMENTE"
            val autor = _usuarioActual.value?.nombre ?: "Operador"
            registrarEvento(TipoEvento.COMANDO_RELÉ, "Comando telemandado por $autor: Relé de motobomba 5V $accion con éxito.")
        }
    }

    /**
     * Simulación interactiva de evento crítico de helada para demostración docente.
     * Desciende la temperatura a -1.2 °C y verifica la respuesta Fail-Safe del sistema.
     */
    fun simularHeladaCritica() {
        _telemetria.update {
            it.copy(
                temperatura = -1.2f,
                humedad = 94.0f,
                puntoRocio = -2.1f,
                bombaFisicaActiva = true,
                timestamp = System.currentTimeMillis()
            )
        }
        val autor = _usuarioActual.value?.nombre ?: "Usuario"
        registrarEvento(
            TipoEvento.ALERTA_HELADA,
            "¡ALERTA CRÍTICA SIMULADA (-1.2 °C)! Lazo Fail-Safe OT del ESP32 activó aspersión superficial para liberar 80 cal/g de calor latente."
        )
    }

    /**
     * Restaura las condiciones agroclimáticas seguras (14.8 °C).
     */
    fun restaurarTemperaturaNormal() {
        _telemetria.update {
            it.copy(
                temperatura = 14.8f,
                humedad = 68.0f,
                puntoRocio = 8.8f,
                bombaFisicaActiva = false,
                timestamp = System.currentTimeMillis()
            )
        }
        val autor = _usuarioActual.value?.nombre ?: "Usuario"
        registrarEvento(
            TipoEvento.CAMBIO_MODO,
            "Temperatura normalizada a 14.8 °C por $autor. Riego en reposo."
        )
    }

    /**
     * Permite al Operador o Administrador agregar notas de campo a la bitácora oficial.
     */
    fun agregarReporteCampo(nota: String) {
        val autor = _usuarioActual.value?.nombre ?: "Operador"
        val rol = if (_usuarioActual.value?.rol == RolUsuario.ADMINISTRADOR) "Admin" else "Operador"
        registrarEvento(
            TipoEvento.BITACORA,
            "[$rol - $autor]: $nota"
        )
    }

    fun registrarEvento(tipo: TipoEvento, mensaje: String) {
        val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val nuevoEvento = BitacoraRegistro(hora = horaActual, tipo = tipo, mensaje = mensaje)
        _historialEventos.update { listOf(nuevoEvento) + it.take(24) }
    }
}
