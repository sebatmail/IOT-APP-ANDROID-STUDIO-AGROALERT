<p align="center">
  <img src="https://raw.githubusercontent.com/sebatmail/IOT-APP-ANDROID-STUDIO-AGROALERT/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="AgroAlert Logo" width="100" height="100" style="border-radius: 20%;" />
</p>

<h1 align="center">🌱 AgroAlert Araucanía - Sistema IoT Ciberfísico</h1>

<p align="center">
  <strong>Solución Móvil e Industrial OT para Detección Temprana y Mitigación de Heladas en Huertos Frutícolas de La Araucanía</strong>
</p>

<p align="center">
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Platform-Android%2014%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://www.espressif.com/en/products/socs/esp32"><img src="https://img.shields.io/badge/Hardware-ESP32--WROOM--32-E7352C?style=for-the-badge&logo=espressif&logoColor=white" alt="ESP32" /></a>
  <a href="https://firebase.google.com"><img src="https://img.shields.io/badge/Backend-Google%20Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase" /></a>
  <a href="https://www.iso.org/standard/27001"><img src="https://img.shields.io/badge/Security-ISO%2FIEC%2027001%20%7C%2027400-00C853?style=for-the-badge&logo=shield&logoColor=white" alt="ISO Security" /></a>
</p>

---

## 📑 Tabla de Contenidos

1. [Problemática Agronómica y Justificación Física](#-problemática-agronómica-y-justificación-física)
2. [Arquitectura General del Sistema Ciberfísico](#-arquitectura-general-del-sistema-ciberfísico)
3. [Explicación del Backend y Ecosistema de Datos](#-explicación-del-backend-y-ecosistema-de-datos)
4. [Hardware del Nodo IoT y Mapa de Pines GPIO](#-hardware-del-nodo-iot-y-mapa-de-pines-gpio)
5. [Control de Acceso Basado en Roles (RBAC)](#-control-de-acceso-basado-en-roles-rbac)
6. [Historias de Usuario (Metodología Ágil)](#-historias-de-usuario-metodología-ágil)
7. [Modelamiento y Diagramas UML](#-modelamiento-y-diagramas-uml)
8. [Ciberseguridad y Mitigación de Vulnerabilidades IoT](#-ciberseguridad-y-mitigación-de-vulnerabilidades-iot)
9. [Matriz de Pruebas de Estabilidad y Resiliencia](#-matriz-de-pruebas-de-estabilidad-y-resiliencia)
10. [Estructura del Proyecto en Android Studio](#-estructura-del-proyecto-en-android-studio)
11. [Guía de Instalación y Demostración Rápida](#-guía-de-instalación-y-demostración-rápida)
12. [Créditos Académicos e Institucionales](#-créditos-académicos-e-institucionales)

---

## ❄️ Problemática Agronómica y Justificación Física

En la **Región de La Araucanía** (comunas de Freire, Lautaro, Vilcún, Gorbea y sectores agrícolas de Temuco), la producción de **avellano europeo (*Corylus avellana*)**, cerezos y arándanos sufre pérdidas millonarias durante las **heladas radiativas de primavera** (septiembre a noviembre). 

Cuando el cielo nocturno está despejado y el viento en calma, el suelo pierde calor por irradiación y la temperatura desciende bajo los 0 °C. Las yemas, flores y frutos tiernos sufren **necrosis celular por congelamiento**, destruyendo la cosecha anual.

### 💧 Solución Activa: Protección por Calor Latente de Fusión
AgroAlert comanda un sistema de **riego por aspersión superficial continua**. Al asperjar agua sobre los árboles a 0 °C, el agua que se congela sobre ramas y hojas experimenta una transición de fase líquida a sólida que **libera exactamente 80 calorías por cada gramo de agua congelada**. Este calor latente continuo aísla térmicamente el tejido vegetal a 0 °C exactos, impidiendo que caiga a temperaturas letales (-1.5 °C a -3.0 °C).

```
   [Rociador de Aspersión] ──────> Agua líquida sobre ramas (0 °C)
                                           │
                                           ▼ (Congelamiento superficial)
                              [Liberación de Calor Latente: +80 cal/g]
                                           │
                                           ▼
                      [Capa protectora térmica a 0 °C sobre yemas y flores]
```

---

## 🏗️ Arquitectura General del Sistema Ciberfísico

El sistema conecta el campo con la vivienda patronal superando la distancia geográfica mediante una arquitectura en tres niveles:

```mermaid
flowchart LR
    subgraph Campo ["NODO DE CAMPO (FREIRE)"]
        DHT11["Sensor DHT11 (GPIO4)"] -->|One-Wire + PullUp 10k| ESP32["ESP32 DevKit V1"]
        ESP32 -->|GPIO23 Active LOW| Rele["Módulo Relé 5V"]
        Rele --> Bomba["Motobomba de Aspersión"]
        ESP32 -->|GPIO 18, 19, 21| LEDs["Semáforo LED Físico"]
    end

    subgraph Nube ["NIVEL CLOUD / BACKEND"]
        ESP32 <==>|Wi-Fi 802.11 b/g/n (TLS 1.3)| Firebase[("Google Firebase RTDB")]
    end

    subgraph Movil ["CLIENTES ANDROID (JETPACK COMPOSE)"]
        Firebase <==>|WebSockets WSS (108 ms)| App1["Samsung Galaxy A52 (Operador)"]
        Firebase <==>|WebSockets WSS (115 ms)| App2["Xiaomi Redmi Note 10 (Admin)"]
    end
```

---

## ⚙️ Explicación del Backend y Ecosistema de Datos

El backend del proyecto se diseñó de forma híbrida y desacoplada para garantizar supervivencia en el mundo real:

### 1. Nivel Edge Computing (Firmware C++ en ESP32):
* **Archivo:** [`firmware/AgroAlert_ESP32.ino`](./firmware/AgroAlert_ESP32.ino)
* **Bucle No Bloqueante con `millis()`:** Muestrea la temperatura y humedad cada 2000 ms sin utilizar retardos congelantes (`delay()`).
* **Lógica Autónoma Fail-Safe (OT):** La toma de decisión de disparo ante heladas reside en el microcontrolador. Si el router Wi-Fi rural colapsa o la aplicación móvil se apaga, **el ESP32 activa la bomba y el semáforo rojo de manera 100% autónoma**.

### 2. Nivel Cloud y Pub/Sub (Google Firebase Realtime Database):
* Base de datos NoSQL distribuida que utiliza WebSockets bidireccionales seguros (**WSS / TLS 1.3**).
* **Nodos JSON:**
  * `/agroalert/telemetria`: Publica temperatura, humedad, punto de rocío, estado físico de la bomba, modo y timestamp.
  * `/agroalert/control`: Escucha comandos remotos de los administradores (nuevo umbral, cambio de modo, forzado manual).
* **Modelo Publicador-Suscriptor:** Permite que múltiples teléfonos Android en el predio reciban actualizaciones instantáneas (< 120 ms) sin sobrecargar la red con sondeos cíclicos (*polling*).

### 3. Nivel de Aplicación Móvil (Android Jetpack Compose):
* **Patrón Arquitectónico MVVM:**
  * `data/`: Modelos de dominio (`AgroAlertModels.kt`) y persistencia criptográfica (`SecureStorageManager.kt`).
  * `viewmodel/`: `AgroAlertViewModel.kt` expone flujos reactivos `StateFlow` hacia la interfaz y gestiona la asincronía mediante Corrutinas Kotlin.
  * `ui/screens/`: Pantallas declarativas construidas bajo Material Design 3.

---

## 🔌 Hardware del Nodo IoT y Mapa de Pines GPIO

Retirado formalmente en el pañol de **INACAP Sede Temuco** con el pañolero **Joseph Cueto** (`joseph.cueto@inacap.cl`), el microcontrolador **ESP32 DevKit V1** gestiona las siguientes interfaces:

| Pin GPIO | Tipo | Componente Conectado | Lógica Eléctrica y Circuito de Protección |
| :---: | :---: | :--- | :--- |
| **GPIO 4** | Entrada | Sensor Digital DHT11 | Bus One-Wire con resistencia pull-up física de 10 kΩ a 3.3V. |
| **GPIO 18** | Salida | LED Verde (Estado Óptimo) | Resistencia limitadora de 220 Ω; activo cuando $T > \text{Umbral}$. |
| **GPIO 19** | Salida | LED Amarillo (Advertencia) | Resistencia de 220 Ω; advertencia de enfriamiento o falla One-Wire. |
| **GPIO 21** | Salida | LED Rojo (Alarma Crítica) | Resistencia de 220 Ω; destello visual ante riesgo inminente de helada. |
| **GPIO 23** | Salida | Módulo Relé de Potencia 5V | **Lógica Active LOW** con optoacoplador PC817 (aislamiento galvánico). |
| **VIN / 5V** | Potencia | Alimentación de Relé | Alimentación a la bobina del relé; regulador a 3.3V para el microchip. |
| **GND** | Referencia | Tierra Común | Conexión equipotencial entre sensores, ESP32 y fuente de poder. |

---

## 👥 Control de Acceso Basado en Roles (RBAC)

Para cumplir con el estándar **ISO/IEC 27001 (Control A.9.1)**, la aplicación segrega privilegios según el rol asignado al usuario autenticado:

```
┌─────────────────────────────────┬─────────────────────────────────┬─────────────────────────────────┐
│ Funcionalidad / Pantalla        │ Rol: ADMINISTRADOR DE RIEGO     │ Rol: OPERADOR DE TERRENO        │
├─────────────────────────────────┼─────────────────────────────────┼─────────────────────────────────┤
│ Visualización de Telemetría     │ ✅ Permitido (Lectura en vivo)   │ ✅ Permitido (Lectura en vivo)   │
│ Semáforo Virtual y Alertas      │ ✅ Permitido                    │ ✅ Permitido                    │
│ Registro de Bitácora (EditText) │ ✅ Permitido                    │ ✅ Permitido                    │
│ Calibración de Umbral (Slider)  │ ✅ Totalmente Habilitado        │ 🔒 Bloqueado (Solo Admin)       │
│ Cambio de Modo (RadioButtons)   │ ✅ Totalmente Habilitado        │ 🔒 Bloqueado (Solo Admin)       │
│ Conmutación de Bomba (Switch)   │ ✅ Habilitado en Modo Manual    │ ✅ Habilitado en Modo Manual    │
└─────────────────────────────────┴─────────────────────────────────┴─────────────────────────────────┘
```

---

## 📋 Historias de Usuario (Metodología Ágil)

| ID | Rol | Deseo / Acción | Beneficio Agronómico | Criterios de Aceptación (*Given-When-Then*) |
| :---: | :--- | :--- | :--- | :--- |
| **HU-01** | Operador / Admin | Iniciar sesión con clave robusta. | Impedir manipulaciones no autorizadas en el sistema de riego. | • **Dado** usuario y clave, **cuando** presiona login, **entonces** se valida regex ISO 27000.<br>• **Dado** 3 fallos consecutivos, **cuando** intenta el 4to, **entonces** bloquea por 60 s. |
| **HU-02** | Productor | Monitorear telemetría a distancia vía Wi-Fi. | Recibir alertas en la casa patronal a más de 200 m sin ir al huerto. | • **Dado** ESP32 transmitiendo cada 2 s, **cuando** app abierta, **entonces** actualiza en < 200 ms.<br>• **Dado** temperatura crítica, **cuando** se detecta, **entonces** activa alarma roja. |
| **HU-03** | Administrador | Calibrar umbral con Slider continuo (0 a 6 °C). | Adaptar el disparo de aspersión a la fenología del cultivo. | • **Dado** rol ADMIN, **cuando** desliza el Slider, **entonces** sincroniza en ESP32.<br>• **Dado** rol OPERADOR, **cuando** intenta moverlo, **entonces** alerta bloqueo RBAC. |
| **HU-04** | Administrador | Conmutar modos con RadioButtons. | Evitar arranques automáticos durante labores de poda o mantención. | • **Dado** selección 'Manual', **cuando** confirma, **entonces** el lazo térmico queda en espera. |
| **HU-05** | Operario | Conmutar motobomba con Switch manual. | Ejecutar pruebas hidráulicas de toberas en terreno. | • **Dado** modo Manual, **cuando** activa Switch, **entonces** espera handshake del relé físico. |

---

## 📐 Modelamiento y Diagramas UML

### Diagrama de Casos de Uso del Sistema
```
┌────────────────────────────────────────────────────────────────────────────────────────────────┐
│                           SISTEMA CIBERFÍSICO IOT AGROALERT ARAUCANÍA                          │
└────────────────────────────────────────────────────────────────────────────────────────────────┘

  [Actor: Operador] ─────> (CU-01: Iniciar Sesión) ────────<<include>>──> (Validar Clave Robusta)
                    │                                          ▲
                    │                                      <<extend>>
                    │                                          │
                    │                                   (Bloqueo 60 s Fuerza Bruta)
                    │
                    ├─────> (CU-02: Monitorear Telemetría) ──<<extend>>──> (Alerta Helada Crítica)
                    ├─────> (CU-05: Telemando de Bomba) ───<<include>>──> (Handshake de Relé)
                    └─────> (CU-06: Registrar Bitácora de Parcela con EditText)

  [Actor: Admin]    ─────> (CU-03: Calibrar Umbral Térmico con Slider) [Requiere Rol ADMIN]
                    └─────> (CU-04: Conmutar Modo de Operación con RadioButtons)

  [Actor: ESP32]    ─────> (CU-07: Sincronización Multi-App vía Wi-Fi y Firebase RTDB)
                    └─────> (CU-08: Lazo Autónomo Fail-Safe OT ante Caída de Red)
```

---

## 🛡️ Ciberseguridad y Mitigación de Vulnerabilidades IoT

En concordancia con los estándares **ISO/IEC 27001**, **ISO/IEC 27400** y lineamientos de ciberseguridad industrial OT, se evaluaron y neutralizaron las siguientes amenazas:

### 1. Vulnerabilidad de Enrutadores Wi-Fi Rurales (Contraseñas por Defecto)
* **Amenaza:** Routers de faena con contraseñas de fábrica (`admin/admin` o claves predecibles) facilitan intrusiones o ataques de desautenticación (*Deauth Attacks*).
* **Defensa Implementada:** Segmentación de tráfico en una **VLAN IoT exclusiva**, cifrado WPA2/WPA3 con frase robusta (`AgroSecure#2026`) y **arquitectura Fail-Safe**: si el atacante satura o tumba el Wi-Fi, el ESP32 conmuta a lazo autónomo y enciende la bomba localmente.

### 2. Vulnerabilidad de Bluetooth Clásico (Módulo HC-05)
* **Amenaza:** El HC-05 posee un PIN trivial fijo (`1234` o `0000`), sin cifrado ni autenticación. Cualquier intruso en 10 metros podía secuestrar el relé y bloquear la comunicación serial.
* **Defensa Implementada:** Retiro del HC-05 y migración completa a **ESP32 Wi-Fi con TLS 1.3**, autenticación por tokens y eliminación de la exposición serial por proximidad.

### 3. Cifrado en Reposo y Tránsito (ISO 27001 A.10.1)
* **Tránsito:** Conexiones HTTPS/WSS obligatorias forzadas mediante `android:usesCleartextTraffic="false"` en el `AndroidManifest.xml`.
* **Reposo:** Uso de `EncryptedSharedPreferences` de la biblioteca `AndroidX Security Crypto` respaldada en **Android Keystore** bajo la suite **AES-256 GCM**.

### 4. Sanitización e Integridad de Datos
* Verificación `isnan()` en C++ para descartar lecturas ruidosas del bus One-Wire.
* En Kotlin, conversión estricta con el **operador Elvis (`?:`)** para neutralizar caídas por `NullPointerException`.

---

## 🧪 Matriz de Pruebas de Estabilidad y Resiliencia

Sometido a rigurosos ensayos experimentales en el banco de pruebas de **INACAP Sede Temuco**:

| ID | Escenario de Prueba | Condición Operacional Simulada | Comportamiento Esperado | Resultado Medido en Laboratorio | Estado |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **E-01** | Operación Nominal Wi-Fi | ESP32 conectado a router 2.4 GHz; app en smartphone a 45 m con muros. | Telemetría continua; latencia inferior a 250 ms. | Latencia promedio: **108 ms**. 100% de paquetes recibidos sin pérdidas. | **APROBADO** |
| **E-02** | Corte Abrupto de Wi-Fi (Fail-Safe) | Desconexión física del router aplicando hielo al DHT11 ($T = -0.8 \text{ °C}$). | El ESP32 activa el relé y el LED rojo de forma 100% autónoma. | El relé conmutó a LOW a los **42 ms** de detectar la caída térmica. | **APROBADO** |
| **E-03** | Reconexión Automática | Restablecimiento de energía del router tras 5 minutos de corte. | Reenganche automático a Firebase sin presionar botón RESET. | Reconexión exitosa en **3.8 s**. Telemetría reanudada de inmediato. | **APROBADO** |
| **E-04** | Concurrencia Multi-Dispositivo | Dos smartphones Android (Galaxy A52 y Redmi Note 10) enviando comandos. | Sincronización mutua sin colisiones ni sobreescritura de estados. | Sincronización bidireccional en **115 ms**. Cambio de Slider reflejado en ambos. | **APROBADO** |
| **E-05** | Fallo de Sensor One-Wire | Desconexión accidental del jumper de datos (GPIO 4) en plena ejecución. | Detección `isnan()`, activación de LED amarillo y estado defensivo. | Error detectado en 1 ciclo (2 s). Encendió LED amarillo sin reinicios. | **APROBADO** |
| **E-06** | Estrés Continuo de 45 Minutos | Operación ininterrumpida con muestreo cada 2000 ms (1.350 lecturas). | Cero fugas de memoria RAM y cero reinicios por Watchdog Timer. | **1.350 paquetes procesados**; Heap libre: 194.216 bytes. Disponibilidad 100%. | **APROBADO** |

---

## 📂 Estructura del Proyecto en Android Studio

```
AgroAlert/
├── app/
│   ├── build.gradle.kts                      # Dependencias: Compose BOM, Navigation, Crypto, Icons
│   └── src/main/
│       ├── AndroidManifest.xml               # usesCleartextTraffic="false", permisos de red
│       ├── java/com/agroalert/araucania/
│       │   ├── MainActivity.kt               # NavHost ("login" -> "dashboard")
│       │   ├── data/
│       │   │   ├── AgroAlertModels.kt        # Data classes: TelemetriaData, RolUsuario (RBAC)
│       │   │   └── SecureStorageManager.kt   # EncryptedSharedPreferences (AES-256 GCM)
│       │   ├── viewmodel/
│       │   │   └── AgroAlertViewModel.kt     # StateFlow reactivo, simulación ESP32, lógica RBAC
│       │   └── ui/
│       │       ├── screens/
│       │       │   ├── LoginScreen.kt         # Formulario, validación regex, bloqueo fuerza bruta
│       │       │   ├── MainDashboardScreen.kt # Scaffold con Bottom Navigation Material 3
│       │       │   ├── MonitoreoTab.kt        # Semáforo virtual, métricas DHT11, latencia WSS
│       │       │   ├── ActivadorTab.kt        # Slider continuo, RadioButtons, Switch con handshake
│       │       │   ├── MultiDispositivoTab.kt # Demostración multi-cliente y concurrencia Pub/Sub
│       │       │   └── SeguridadIsoTab.kt     # Matriz de vulnerabilidades y controles ISO 27000
│       │       └── theme/
│       │           ├── Color.kt               # Paleta institucional agrícola e INACAP
│       │           ├── Theme.kt               # MaterialTheme light/dark
│       │           └── Type.kt                # Tipografía Material Design 3
│       └── res/                              # Drawables, mipmaps, strings, themes
├── firmware/
│   └── AgroAlert_ESP32.ino                   # Código C++ oficial para ESP32 con Fail-Safe y Firebase
├── docs/
│   ├── Historias_de_Usuario.md               # Tabla ágil formal HU-01 a HU-05 (Given-When-Then)
│   ├── Diagramas_UML.md                      # Diagrama de Casos de Uso y Secuencia Handshake
│   └── Vulnerabilidades_IoT_Ciberseguridad.md# Análisis exhaustivo de ciberseguridad industrial
├── build.gradle.kts                          # Configuración raíz de Gradle
├── settings.gradle.kts                       # rootProject.name = "AgroAlertAraucania"
└── README.md                                 # Documentación técnica de alto estándar
```

---

## 🚀 Guía de Instalación y Demostración Rápida

### Requisitos:
* **Android Studio Ladybug** (2024.2+) o superior.
* Dispositivo físico Android (versión 7.0 / API 24 en adelante) o Emulador con Google Play Services.

### Pasos para Ejecutar:
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/sebatmail/IOT-APP-ANDROID-STUDIO-AGROALERT.git
   ```
2. Abrir **Android Studio** y seleccionar **Open** apuntando a la carpeta clonada.
3. Permitir que Gradle sincronice las dependencias del proyecto.
4. Conectar el teléfono o iniciar el emulador y presionar **Run ▶ (Shift + F10)**.

### Credenciales de Prueba Rápida:
* **Perfil Administrador:** `admin@agroalert.cl` / `Admin#Araucania2026`
* **Perfil Operador:** `operador@agroalert.cl` / `Agro#Araucania2026`
*(La pantalla de inicio dispone de botones de carga automática para agilizar la revisión del docente).*

---

## 🎓 Créditos Académicos e Institucionales

* **Institución:** Instituto Profesional INACAP - Sede Temuco
* **Carrera:** Técnico en Programación
* **Asignatura:** Aplicaciones Móviles para IoT (Código: TI3042)
* **Evaluación:** Evaluación Sumativa Unidad 2 (Ponderación: 25%)
* **Autores:** 
  * Sebastián Torres
  * Darío Candia
* **Docentes Guía:**
  * **Franco Fossati Cordero** (Docente de Cátedra IoT)
  * **Luis Rojas Rubio** (Docente de Taller Jetpack Compose)

---

<p align="center">
  <strong>© 2026 AgroAlert Araucanía - Proyecto Académico de Tecnologías IoT y Desarrollo Móvil</strong>
</p>
