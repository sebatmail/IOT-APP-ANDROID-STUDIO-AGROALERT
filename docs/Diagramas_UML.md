# 📐 Modelamiento y Diagramas UML - AgroAlert Araucanía

---

## 1. Diagrama de Casos de Uso UML

```
┌────────────────────────────────────────────────────────────────────────────────────────────────┐
│                           SISTEMA CIBERFÍSICO IOT AGROALERT ARAUCANÍA                          │
└────────────────────────────────────────────────────────────────────────────────────────────────┘

  [Actor: Operador de Terreno]
        │
        ├───> (CU-01: Iniciar Sesión) ─────────<<include>>───> (Validar Complejidad Clave)
        │                                         ▲
        │                                         │
        │                                     <<extend>>
        │                                         │
        │                                  (Bloqueo 60s Fuerza Bruta)
        │
        ├───> (CU-02: Visualizar Telemetría en Vivo) ──<<extend>>──> (Alerta Crítica de Helada)
        │
        ├───> (CU-05: Telemando Manual de Bomba con Switch) ──<<include>>──> (Handshake de Relé)
        │
        └───> (CU-06: Registrar Observaciones en Bitácora con EditText)

  [Actor: Administrador de Riego]
        │
        ├───> (CU-03: Calibrar Umbral de Helada con Slider 0-6 °C) [Requiere Rol ADMIN]
        │
        └───> (CU-04: Conmutar Modo con RadioButtons) [Automático / Manual / Mantención]

  [Actor Técnico: Microcontrolador ESP32 / Firebase]
        │
        ├───> (CU-07: Publicar Telemetría Multi-Dispositivo vía Wi-Fi / Firebase)
        │
        └───> (CU-08: Ejecutar Control Autónomo Fail-Safe) [Lazo de emergencia local ante caída de red]
```

---

## 2. Matriz Descriptiva de Casos de Uso

| Caso de Uso | Nombre | Actor Primario | Precondición | Flujo Principal | Postcondición |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CU-01** | Autenticar Usuario | Operador / Admin | App abierta en `LoginScreen`. | El usuario ingresa email y clave. El sistema verifica formato regex y coteja con Keystore / Firebase Auth. | Sesión iniciada con token AES-256 GCM y rol asignado. |
| **CU-02** | Monitorear Telemetría | Operador / Admin | Sesión iniciada y Wi-Fi activo. | La app escucha el nodo Firebase vía WebSocket WSS y actualiza temperatura, humedad y semáforo virtual. | UI reactiva actualizada cada 2 segundos. |
| **CU-03** | Calibrar Umbral Térmico | Administrador | Rol de Administrador activo. | El Admin desplaza el `Slider` continuo. Se valida el permiso RBAC y se envía el nuevo valor al ESP32. | Umbral actualizado en nube y microcontrolador. |
| **CU-04** | Conmutar Modo | Administrador | Rol de Administrador activo. | El Admin selecciona un `RadioButton`. La app actualiza el modo de operación en la nube. | Comportamiento operativo modificado en campo. |
| **CU-05** | Telemando de Bomba | Operador / Admin | Sistema en Modo Manual. | El usuario acciona el `Switch`. La app emite la orden y espera confirmación del relé antes de cambiar de color. | Bomba encendida/apagada con confirmación (*handshake*). |
| **CU-06** | Registrar Bitácora | Operador / Admin | Sesión activa. | El usuario escribe notas en el `OutlinedTextField` y presiona "Guardar". | Registro auditado almacenado en memoria local. |
| **CU-07** | Sincronización Multi-App | ESP32 / Nube | Conexión Wi-Fi establecida. | Los cambios de estado se distribuyen instantáneamente a múltiples teléfonos Android en simultáneo. | Múltiples pantallas sincronizadas en menos de 120 ms. |
| **CU-08** | Lazo Autónomo Fail-Safe | Firmware ESP32 | Nodo energizado y sensor DHT11. | El ESP32 mide cada 2 s; si temp <= umbral y modo es Automático, activa relé independientemente de la red. | Cultivos protegidos contra heladas sin depender de Internet. |

---

## 3. Diagrama de Secuencia: Control de Actuador con Handshake Bidireccional

```
[Smartphone Android]           [Firebase RTDB (WSS)]           [Microcontrolador ESP32]       [Módulo Relé 5V]
        │                               │                                 │                         │
        │─── 1. Click Switch Manual ───>│                                 │                         │
        │    (Muestra spinner cargando) │                                 │                         │
        │                               │── 2. WebSocket Push Event ─────>│                         │
        │                               │                                 │── 3. GPIO23 LOW ───────>│
        │                               │                                 │   (Active LOW opto)     │── Contactos
        │                               │                                 │<─ 4. Conmutación OK ───│   cerrados
        │                               │<── 5. Notifica estado físico ───│                         │
        │<── 6. Confirma Handshake ─────│                                 │                         │
        │    (Switch pasa a VERDE)      │                                 │                         │
```
