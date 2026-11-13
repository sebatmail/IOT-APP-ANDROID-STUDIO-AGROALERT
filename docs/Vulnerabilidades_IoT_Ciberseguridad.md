# 🛡️ Análisis de Vulnerabilidades IoT y Estándares de Seguridad Industrial (OT)

**Proyecto:** AgroAlert Araucanía  
**Asignatura:** Aplicaciones Móviles para IoT (TI3042)  
**Marco Normativo:** ISO/IEC 27001:2022 | ISO/IEC 27400:2022 | OWASP IoT Top 10  

---

## 1. Vulnerabilidades Críticas Evaluadas y Mitigadas en el Proyecto

### ⚠️ Vulnerabilidad A: Credenciales por Defecto en Enrutadores Wi-Fi Rurales
* **Descripción del Riesgo:** En entornos agropecuarios de La Araucanía, los routers instalados en casetas de riego o galpones suelen operar con las contraseñas predeterminadas de fábrica (`admin/admin`, `12345678` o la clave genérica impresa en la etiqueta del proveedor ISP). Un atacante en las inmediaciones del predio podría asociarse a la red, interceptar paquetes o ejecutar ataques de desautenticación (*Deauth Attacks*), desconectando el nodo IoT en plena madrugada durante una helada.
* **Mitigación Implementada en AgroAlert:**
  1. **Segmentación de Red (VLAN IoT Agrícola):** Se aísla el tráfico del microcontrolador ESP32 en una red virtual dedicada (SSID oculto o con WPA2/WPA3 Personal), sin acceso a la red doméstica o administrativa del predio.
  2. **Contraseñas Robustas de Red:** Se prohíben claves genéricas; se implementa una frase de paso criptográfica con rotación semestral (`AgroSecure#2026`).
  3. **Resiliencia Fail-Safe (OT Industrial):** Si la red Wi-Fi es vulnerada, interferida o el router pierde energía eléctrica, **el microcontrolador ESP32 no depende de la red para salvar la cosecha**. Su lazo local evalúa la temperatura en el bus One-Wire cada 2000 ms y comanda de forma 100% autónoma el relé de la motobomba.

---

### ⚠️ Vulnerabilidad B: Módulo Bluetooth Clásico (HC-05) con PIN de Fábrica Inseguro
* **Descripción del Riesgo:** En el diseño inicial de la Unidad 1 se utilizaba un transceptor Bluetooth HC-05. Este módulo presenta dos fallas de seguridad y diseño insalvables:
  1. **PIN Fijo Trivial:** Viene configurado de fábrica con el PIN `1234` o `0000`, publicando abiertamente su dirección MAC y nombre por defecto. Cualquier persona con un teléfono móvil a menos de 10 metros podía emparejarse sin autenticación criptográfica, inyectar comandos seriales y apagar la bomba de agua.
  2. **Limitación de Enlace Único:** El perfil Bluetooth SPP solo admite una única conexión a la vez. Un atacante podía mantener ocupado el canal, impidiendo que el agricultor se conectara (Denegación de Servicio - DoS). Además, su alcance de 10 m resultaba inútil en un huerto real donde la vivienda patronal se encuentra a 250 metros.
* **Mitigación Implementada:**
  - **Recambio Total de Hardware:** Siguiendo la retroalimentación del docente Franco Fossati, se retiró el HC-05 en el pañol de INACAP con el pañolero Joseph Cueto (`joseph.cueto@inacap.cl`) y se migró al SoC **ESP32-WROOM-32**.
  - **Eliminación del Protocolo SPP Inseguro:** El ESP32 se comunica mediante Wi-Fi cifrado con **TLS 1.3** y autenticación basada en tokens de Google Firebase, impidiendo cualquier conexión directa por proximidad física no autorizada.

---

### ⚠️ Vulnerabilidad C: Ataques de Fuerza Bruta en la Aplicación Móvil
* **Descripción del Riesgo:** Intentos continuos y automatizados de adivinación de credenciales en el formulario de inicio de sesión de la app.
* **Mitigación Implementada:**
  1. **Política de Complejidad Regex:** Mínimo 8 caracteres, al menos una letra mayúscula, una minúscula, un dígito numérico y un símbolo especial (`@$!%*?&`).
  2. **Bloqueo por Temporizador (Rate Limiting en Cliente):** Si se registran 3 intentos fallidos consecutivos, la app bloquea los campos de entrada y el botón por 60 segundos, desplegando un contador regresivo.

---

### ⚠️ Vulnerabilidad D: Almacenamiento Inseguro en Reposo (CWE-312)
* **Descripción del Riesgo:** Si el teléfono móvil del operador es extraviado o robado en el huerto, un atacante podría extraer las credenciales si están guardadas en texto plano en `SharedPreferences`.
* **Mitigación Implementada:**
  - Uso de la biblioteca oficial `androidx.security:security-crypto` con `EncryptedSharedPreferences`.
  - La llave maestra se genera en el chip de seguridad **Android Keystore** bajo la especificación **AES-256 GCM**. Los datos sensibles se almacenan cifrados y disociados de la identidad del usuario.

---

### ⚠️ Vulnerabilidad E: Falsos Disparos por Ruido Eléctrico en Sensor One-Wire
* **Descripción del Riesgo:** En huertos comerciales, cables largos o interferencia electromagnética de motores pueden inducir lecturas de 0.0 °C o valores `NaN` en el sensor DHT11, provocando encendidos accidentales de la bomba o caídas de la aplicación por `NullPointerException`.
* **Mitigación Implementada:**
  1. **Resistencia Pull-Up Física:** Se fijó una resistencia de 10 kΩ a 3.3V en el pin de datos (GPIO 4).
  2. **Filtro `isnan()` en Firmware C++:** Si el sensor no responde, el ESP32 activa el LED amarillo de mantenimiento, mantiene el último estado seguro y no entra en pánico de reinicio.
  3. **Operador Elvis (`?:`) en Kotlin:** En Android, todas las conversiones de telemetría utilizan `toFloatOrNull() ?: 0.0f`, garantizando integridad de datos sin caídas de la app.

---

## 2. Matriz de Controles de Seguridad ISO/IEC 27001 e ISO/IEC 27400

| Control ISO | Estándar | Medida Concreta en AgroAlert Araucanía |
| :--- | :--- | :--- |
| **A.9.1 Control de Acceso (RBAC)** | ISO/IEC 27001 | Roles segregados: **ADMINISTRADOR** (calibra umbrales y cambia modos) y **OPERADOR** (monitoreo y bitácora de campo). |
| **A.10.1 Criptografía en Tránsito** | ISO/IEC 27001 | Cifrado obligatorio TLS 1.3 hacia Firebase (puerto 443 WSS/HTTPS) forzado con `android:usesCleartextTraffic="false"`. |
| **A.10.1 Criptografía en Reposo** | ISO/IEC 27001 | Cifrado simétrico AES-256 GCM mediante Android Keystore para tokens de sesión en el dispositivo móvil. |
| **Control 8.2 Autenticación IoT** | ISO/IEC 27400 | Políticas de contraseñas robustas y bloqueo temporal de 60 segundos ante 3 fallos consecutivos contra ataques de fuerza bruta. |
| **Control 6.4 Resiliencia OT** | ISO/IEC 27400 | Autonomía ciberfísica Fail-Safe en el microcontrolador ESP32 ante corte de red Wi-Fi o apagado del teléfono móvil. |
| **A.12.2 Protección contra Malware/Inyección** | ISO/IEC 27001 | Sanitización de datos entrantes con el operador Elvis (`?:`) y descarte de payloads JSON corruptos. |
| **Minimización de Datos** | ISO/IEC 27701 | Eliminación total de permisos intrusivos de Bluetooth y GPS (`neverForLocation`). Solo permisos estrictos de red `INTERNET`. |
