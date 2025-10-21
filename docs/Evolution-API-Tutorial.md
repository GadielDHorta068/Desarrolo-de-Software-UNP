# Evolution API (WhatsApp) – Guía de integración genérica

Esta guía te muestra cómo instalar y usar Evolution API (servicio HTTP para WhatsApp) y cómo integrarla desde cualquier backend (ejemplo en Spring Boot), con foco en variables de entorno y despliegue por Docker Compose.

## Resumen rápido
- Evolution API corre como un servicio HTTP y exige la cabecera `apikey` en cada request.
- En Docker, expón Evolution a tu host en `http://localhost:8081`. Dentro de la red de Docker, apúntalo como `http://evolution-api:8080`.
- Tu backend solo necesita enviar requests HTTP a Evolution con `apikey` y los payloads adecuados.

## Requisitos
- Docker Desktop 4.x
- Puertos libres: `8081` (Evolution API), y el de tu backend (por ejemplo `8080`).
- Opcional pero recomendado: Postgres y Redis para persistencia y cache (Evolution v2).

## Variables de entorno clave
- `EVOLUTION_API_KEY`: clave compartida entre tu backend y el contenedor de Evolution. Deben coincidir.
- `EVOLUTION_API_URL`: URL base para que el backend consuma Evolution.
  - En Docker: `http://evolution-api:8080`
  - Desde host/local: `http://localhost:8081`
- En el contenedor `evolution-api`:
  - `AUTHENTICATION_TYPE=apikey`
  - `AUTHENTICATION_API_KEY=${EVOLUTION_API_KEY}`
  - `CONFIG_SESSION_PHONE_VERSION`: versión de WhatsApp Web que Baileys emula. Útil si no se genera QR.
  - `DATABASE_ENABLED=true`, `DATABASE_PROVIDER=postgresql`, `DATABASE_CONNECTION_URI=postgresql://<user>:<pass>@db:5432/<db>?schema=evolution`
  - `CACHE_REDIS_ENABLED=true`, `CACHE_REDIS_URI=redis://redis:6379`
  - Variables `OPENAI_*`: opcionales. No las uses si no necesitas features de IA.

> Seguridad: nunca comites claves reales. Usa variables de entorno o archivos `.env` ignorados por Git.

## Instalación con Docker Compose
Ejemplo de servicio Evolution API (similar al que ya tienes en tu `docker-compose.yml`):

```yaml
services:
  evolution-api:
    image: atendai/evolution-api:latest
    container_name: evolution-api
    environment:
      AUTHENTICATION_TYPE: apikey
      AUTHENTICATION_API_KEY: ${EVOLUTION_API_KEY:-change-me}
      CONFIG_SESSION_PHONE_VERSION: "2.3000.1025195125"
      DATABASE_ENABLED: "true"
      DATABASE_PROVIDER: postgresql
      DATABASE_CONNECTION_URI: postgresql://raffy_user:raffy_password@db:5432/raffy_db?schema=evolution
      CACHE_REDIS_ENABLED: "true"
      CACHE_REDIS_URI: redis://redis:6379
      CACHE_LOCAL_ENABLED: "false"
    ports:
      - "8081:8080"  # Acceso desde host
    volumes:
      - evolution_store:/evolution/store
      - evolution_instances:/evolution/instances
    networks:
      - raffy-network
```

- Accede a Evolution API (Swagger/Manager) en `http://localhost:8081`.
- Tu backend deberá apuntar a `http://evolution-api:8080` dentro de la red Docker.

## Configuración en backend (Spring Boot – ejemplo)
Opciones para configurar URL y API key:

- Usando variables de entorno directamente:
```java
@Value("${EVOLUTION_API_URL:http://evolution-api:8080}")
private String baseUrl;
@Value("${EVOLUTION_API_KEY:change-me}")
private String apiKey;
```

- O usando `application.yml` (mapeo opcional):
```yaml
evolution:
  url: ${EVOLUTION_API_URL:http://evolution-api:8080}
  apikey: ${EVOLUTION_API_KEY:change-me}
  defaultInstance: ${EVOLUTION_INSTANCE:raffy}
```

### Servicio de integración (RestTemplate)
```java
@Service
public class EvolutionService {
  private final RestTemplate restTemplate;

  @Value("${EVOLUTION_API_URL:http://evolution-api:8080}")
  private String baseUrl;

  @Value("${EVOLUTION_API_KEY:change-me}")
  private String apiKey;

  public EvolutionService(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

  private HttpHeaders defaultHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("apikey", apiKey);
    return headers;
  }

  public Map<String, Object> createInstance(Map<String, Object> payload) {
    var entity = new HttpEntity<>(payload, defaultHeaders());
    var response = restTemplate.exchange(baseUrl + "/instance/create", HttpMethod.POST, entity, Map.class);
    return response.getBody();
  }

  public Map<String, Object> connectInstance(String instance, String number) {
    var builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/instance/connect/" + instance);
    if (number != null && !number.isBlank()) builder.queryParam("number", number);
    var response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(defaultHeaders()), Map.class);
    return response.getBody();
  }

  public Map<String, Object> sendText(String instance, Map<String, Object> payload) {
    var entity = new HttpEntity<>(payload, defaultHeaders());
    var response = restTemplate.exchange(baseUrl + "/message/sendText/" + instance, HttpMethod.POST, entity, Map.class);
    return response.getBody();
  }
}
```

### Controlador REST genérico
Puedes exponer endpoints mínimos para crear/conectar instancias y enviar texto.

> Nota: si tu proyecto usa un envoltorio de respuestas (por ejemplo `com.desarrollo.raffy.Response` con `Response.ok`/`Response.error`), ajusta los retornos a tu helper. Aquí mostramos `ResponseEntity<?>` genérico.

```java
@RestController
@RequestMapping("/api/evolution")
public class EvolutionController {
  @Autowired private EvolutionService evolutionService;

  @PostMapping("/instances")
  public ResponseEntity<?> createInstance(@RequestBody CreateInstanceRequest req) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("instanceName", req.getInstanceName());
    payload.put("qrcode", req.getQrcode());
    if (req.getToken() != null && !req.getToken().isBlank()) payload.put("token", req.getToken());
    if (req.getNumber() != null && !req.getNumber().isBlank()) payload.put("number", req.getNumber());
    if (req.getIntegration() != null && !req.getIntegration().isBlank()) payload.put("integration", req.getIntegration());
    return ResponseEntity.ok(evolutionService.createInstance(payload));
  }

  @GetMapping("/instances/{instance}/connect")
  public ResponseEntity<?> connectInstance(@PathVariable String instance,
                                           @RequestParam(required = false) String number) {
    return ResponseEntity.ok(evolutionService.connectInstance(instance, number));
  }

  @PostMapping("/messages/text")
  public ResponseEntity<?> sendText(@RequestBody SendTextRequest req) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("number", req.getNumber());
    payload.put("text", req.getText());
    if (req.getDelay() != null) payload.put("delay", req.getDelay());
    return ResponseEntity.ok(evolutionService.sendText(req.getInstance(), payload));
  }
}
```

DTOs de ejemplo (simplificados):
```java
public class CreateInstanceRequest {
  private String instanceName; // p.ej. "raffy"
  private Boolean qrcode = true;
  private String token;        // opcional
  private String number;       // opcional
  private String integration = "WHATSAPP-BAILEYS"; // opcional
  // getters/setters
}

public class SendTextRequest {
  private String instance; // p.ej. "raffy"
  private String number;   // E.164: 5511999999999
  private String text;
  private Integer delay;   // opcional
  // getters/setters
}
```

## Probar rápidamente (sin backend)
Usa `curl` contra Evolution expuesto en `http://localhost:8081` (incluye `apikey`).

- Crear instancia:
```bash
curl -X POST http://localhost:8081/instance/create \
  -H "Content-Type: application/json" \
  -H "apikey: $EVOLUTION_API_KEY" \
  -d '{"instanceName":"raffy","qrcode":true}'
```

- Conectar instancia y generar QR (o usar número):
```bash
curl -X GET "http://localhost:8081/instance/connect/raffy" -H "apikey: $EVOLUTION_API_KEY"
```

- Enviar texto:
```bash
curl -X POST http://localhost:8081/message/sendText/raffy \
  -H "Content-Type: application/json" \
  -H "apikey: $EVOLUTION_API_KEY" \
  -d '{"number":"5511999999999","text":"Hola desde Evolution API","delay":0}'
```

## Buenas prácticas y problemas comunes
- 401 desde Evolution: confirma que `EVOLUTION_API_KEY` (backend) coincide con `AUTHENTICATION_API_KEY` (contenedor).
- QR no aparece: actualiza `CONFIG_SESSION_PHONE_VERSION` a la versión vigente de WhatsApp Web.
- Persistencia: usa los volúmenes `evolution_store` y `evolution_instances` para no perder sesiones.
- Formato del número: usa E.164 (sin `+`, solo dígitos, con país).
- Secrets: gestiona claves via entorno o gestores de secretos; no las publiques.

## Qué endpoints principales expone Evolution
- `POST /instance/create` – crea una instancia.
- `GET /instance/connect/{instance}` – genera/conecta la instancia (QR o número).
- `POST /message/sendText/{instance}` – envía un mensaje de texto.

Con esto tienes un stack reproducible y un controlador genérico para integrar Evolution API en cualquier proyecto.