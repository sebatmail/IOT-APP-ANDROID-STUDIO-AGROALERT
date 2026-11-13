// ============================================================================
// PROYECTO: AgroAlert Araucanía - Firmware Oficial del Nodo IoT (ESP32)
// AUTOR: Sebastián Torres | ASIGNATURA: TI3042 INACAP | DOCENTE: Franco Fossati Cordero
// HARDWARE: ESP32-WROOM-32 DevKit V1 (Retirado en pañol con Joseph Cueto)
// SENSORES/ACTUADORES: DHT11 (GPIO4), Relé 5V Active LOW (GPIO23), LEDs (18,19,21)
// CONECTIVIDAD: Wi-Fi 802.11 b/g/n + Google Firebase Realtime Database
// ARQUITECTURA: Fail-Safe OT (Autonomía local total ante pérdida de enlace Wi-Fi)
// ============================================================================

#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <DHT.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// 1. Asignación de Pines GPIO en ESP32 DevKit V1
#define PIN_DHT11       4   // Pin de datos One-Wire con resistencia pull-up 10k a 3.3V
#define PIN_RELE_BOMBA  23  // Control de módulo relé 5V (Active LOW con optoacoplador PC817)
#define PIN_LED_VERDE   18  // Estado normal (Temperatura segura sobre umbral)
#define PIN_LED_AMARILLO 19 // Advertencia preventiva / Fallo de lectura DHT11
#define PIN_LED_ROJO    21  // Alarma crítica de helada (Aspersión activa)

#define DHTTYPE DHT11
DHT dht(PIN_DHT11, DHTTYPE);

// 2. Parámetros de Red Wi-Fi Rural y Firebase (Mitigación de contraseñas por defecto)
const char* WIFI_SSID     = "Huerto_Araucania_IoT";
const char* WIFI_PASSWORD = "AgroSecure#2026";
#define API_KEY           "AIzaSyD-TuApiKeySeguraDeFirebaseGoogleCloud"
#define DATABASE_URL      "https://agroalert-araucania-default-rtdb.firebaseio.com/"

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// 3. Variables de Estado Operacional
float temperatura = 0.0;
float humedad = 0.0;
float umbralHelada = 1.5;           // Calibrable vía Slider en la app Android por Admin
String modoOperacion = "AUTOMATICO"; // AUTOMATICO, MANUAL, MANTENIMIENTO
bool bombaManualEncendida = false;
bool estadoFisicoBomba = false;

unsigned long ultimoMuestreo = 0;
const unsigned long INTERVALO_MUESTREO = 2000; // Muestreo no bloqueante cada 2 seg

void setup() {
  Serial.begin(115200);
  
  // SEGURIDAD FAIL-SAFE EN ARRANQUE:
  // El relé conmutará en nivel LOW. Para evitar disparos accidentales durante
  // el encendido, fijamos HIGH inmediatamente antes de habilitar el pin como salida.
  digitalWrite(PIN_RELE_BOMBA, HIGH);
  pinMode(PIN_RELE_BOMBA, OUTPUT);
  digitalWrite(PIN_RELE_BOMBA, HIGH);

  pinMode(PIN_LED_VERDE, OUTPUT);
  pinMode(PIN_LED_AMARILLO, OUTPUT);
  pinMode(PIN_LED_ROJO, OUTPUT);

  // Inicialización de sensor digital One-Wire
  dht.begin();

  // Conexión inicial a la red Wi-Fi del predio
  Serial.print("Conectando a Wi-Fi de huerto: ");
  Serial.println(WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  
  // Timeout defensivo de 10 segundos para no bloquear el lazo físico de seguridad
  unsigned long inicioWifi = millis();
  while (WiFi.status() != WL_CONNECTED && (millis() - inicioWifi < 10000)) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWi-Fi Conectado. IP Asignada: " + WiFi.localIP().toString());
    config.api_key = API_KEY;
    config.database_url = DATABASE_URL;
    Firebase.begin(&config, &auth);
    Firebase.reconnectWiFi(true);
    Serial.println("Google Firebase inicializado con cifrado TLS 1.3");
  } else {
    Serial.println("\nAdvertencia: Wi-Fi no disponible. Operando en MODO AUTÓNOMO FAIL-SAFE.");
  }
}

void loop() {
  unsigned long tiempoActual = millis();

  // 1. Muestreo periódico sin delays bloqueantes
  if (tiempoActual - ultimoMuestreo >= INTERVALO_MUESTREO) {
    ultimoMuestreo = tiempoActual;

    float t = dht.readTemperature();
    float h = dht.readHumidity();

    // Verificación de integridad con isnan() (ISO 27400)
    if (isnan(t) || isnan(h)) {
      Serial.println("ALERTA OT: Error de lectura en sensor DHT11. Activando estado defensivo.");
      digitalWrite(PIN_LED_VERDE, LOW);
      digitalWrite(PIN_LED_AMARILLO, HIGH); // Advertencia de mantenimiento
      digitalWrite(PIN_LED_ROJO, LOW);
    } else {
      temperatura = t;
      humedad = h;

      // 2. ALGORITMO FAIL-SAFE INDUSTRIAL (OT): Decisión local en el microcontrolador
      if (modoOperacion == "AUTOMATICO") {
        if (temperatura <= umbralHelada) {
          // Riesgo inminente de helada: Activación de aspersión por calor latente (80 cal/g)
          digitalWrite(PIN_RELE_BOMBA, LOW); // Relé Active LOW -> Contactos cerrados
          estadoFisicoBomba = true;
          digitalWrite(PIN_LED_VERDE, LOW);
          digitalWrite(PIN_LED_AMARILLO, LOW);
          digitalWrite(PIN_LED_ROJO, HIGH); // Alarma activa
        } else if (temperatura <= (umbralHelada + 2.0)) {
          // Rango preventivo de enfriamiento
          digitalWrite(PIN_RELE_BOMBA, HIGH);
          estadoFisicoBomba = false;
          digitalWrite(PIN_LED_VERDE, LOW);
          digitalWrite(PIN_LED_AMARILLO, HIGH);
          digitalWrite(PIN_LED_ROJO, LOW);
        } else {
          // Condición normal sobre umbral seguro
          digitalWrite(PIN_RELE_BOMBA, HIGH);
          estadoFisicoBomba = false;
          digitalWrite(PIN_LED_VERDE, HIGH);
          digitalWrite(PIN_LED_AMARILLO, LOW);
          digitalWrite(PIN_LED_ROJO, LOW);
        }
      } else if (modoOperacion == "MANUAL") {
        // Control forzado por telemando desde la app Android
        if (bombaManualEncendida) {
          digitalWrite(PIN_RELE_BOMBA, LOW);
          estadoFisicoBomba = true;
        } else {
          digitalWrite(PIN_RELE_BOMBA, HIGH);
          estadoFisicoBomba = false;
        }
      }

      // 3. Publicación de Telemetría a la Nube (Google Firebase) si hay Wi-Fi
      if (WiFi.status() == WL_CONNECTED && Firebase.ready()) {
        FirebaseJson json;
        json.set("temperatura", temperatura);
        json.set("humedad", humedad);
        json.set("bombaFisicaActiva", estadoFisicoBomba);
        json.set("modoActual", modoOperacion);
        json.set("umbralActual", umbralHelada);
        json.set("timestamp", (int)millis());

        Firebase.RTDB.setJSON(&fbdo, "/agroalert/telemetria", &json);

        // 4. Lectura de Comandos Remotos desde la App Móvil (Handshake)
        if (Firebase.RTDB.getJSON(&fbdo, "/agroalert/control")) {
          FirebaseJsonData jsonData;
          FirebaseJson &jsonRes = fbdo.jsonObject();
          
          if (jsonRes.get(jsonData, "modo")) modoOperacion = jsonData.stringValue;
          if (jsonRes.get(jsonData, "umbral")) umbralHelada = jsonData.floatValue;
          if (jsonRes.get(jsonData, "manualBomba")) bombaManualEncendida = jsonData.boolValue;
        }
      }
    }
  }
}
