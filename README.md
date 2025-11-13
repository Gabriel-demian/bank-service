# Bank Service

Microservicio REST desarrollado en **Java 21** + **Spring Boot 3.3+**, que implementa un CRUD completo sobre entidades bancarias.  
Sigue una **arquitectura hexagonal (Ports & Adapters)** y utiliza **H2 en memoria** como base de datos.

---

## Tecnologías principales

- Java 21 (Virtual Threads-ready)
- Spring Boot 3 (MVC, Data JPA, Validation, Security)
- H2 Database (en memoria)
- MapStruct (mapeo DTO ↔ entidad)
- JWT (autenticación)
- Swagger / OpenAPI (documentación)
- Maven
- Docker / Docker Compose

---

## Cómo ejecutar el servicio

### Requisitos previos
- Java 21
- Maven 3.9+
- Docker y Docker Compose


### Cómo correr local sin Docker
```bash
mvn spring-boot:run
```

### Build y ejecución con Docker
```bash
docker compose up --build
```

## Quick test para security

### Login → obtener token
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"admin","password":"admin123"}'

### Usar el token para acceder a bancos
curl -X GET http://localhost:8080/v1/banks \
-H "Authorization: Bearer token"

### Swagger UI:  
 - http://localhost:8080/swagger-ui/index.html

##  Estructura del Proyecto
```
com.example.bankservice
├── BankServiceApplication.java
│
├── application/
│   ├── dto/
│   │   └── BankDto.java
│   ├── mapper/
│   │   └── BankAppMapper.java        // Bank <-> BankDto
│   └── service/
│       └── BankService.java
│
├── domain/
│   ├── model/
│   │   └── Bank.java
│   └── port/
│       └── BankRepositoryPort.java
│
├── infrastructure/
│   ├── config/
│   │   ├── JwtAuthFilter.java
│   │   ├── JwtService.java
│   │   ├── PersistenceConfig.java
│   │   └── SecurityConfig.java
│   │
│   ├── web/
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── BankController.java
│   │   └── dto/
│   │       ├── BankRequest.java
│   │       ├── BankResponse.java
│   │       └── BankApiMapper.java    // Request/Response <-> BankDto
│   │
│   └── repository/
│       ├── entity/
│       │   └── BankJpaEntity.java
│       ├── mapper/
│       │   └── BankEntityMapper.java // Bank <-> BankJpaEntity
│       └── adapter/
│           └── BankRepositoryAdapter.java  // implements BankRepositoryPort
│
└── shared/
    ├── error/
    │   └── GlobalExceptionHandler.java
    └── exception/
        ├── DuplicateResourceException.java
        └── NotFoundException.java

```


### Endpoint especial: Proxy

- GET /v1/banks/proxy?country=AR
  - Este endpoint utiliza `WebClient` para consumir el propio API del servicio.  
    Demuestra un patrón de **API composition**, manteniendo el estilo no bloqueante interno del client 
    aunque el controlador exponga una API síncrona tradicional.


### Arquitectura

El servicio implementa **Arquitectura Hexagonal (Ports & Adapters)**:

- **Domain**: reglas de negocio puras
  - `Bank`, `BankRepositoryPort`

- **Application (Use Cases)**: orquestación, lógica de casos de uso
  - `BankService`, `BankDto`, `BankAppMapper`

- **Infrastructure**: controladores HTTP, persistencia JPA, seguridad, JWT, config
  - Controllers, DTOs de transporte, WebClient, repositorio JPA, JWT, filtros, etc.

---

## Observabilidad y Monitoreo

El servicio incorpora una configuración básica de **observabilidad** lista para entornos reales:

### Logs estructurados
Los logs se generan en formato **JSON** mediante **Logback** y **Logbook**, lo que permite:
- Lectura y análisis automático por herramientas como **ELK (Elasticsearch + Logstash + Kibana)** o **Grafana Loki**.
- Registro de cada request/response HTTP con tiempos y estado.
- Ofuscación de cabeceras sensibles (`Authorization`, `Set-Cookie`).
- Niveles de log coherentes (`INFO`, `DEBUG`, `WARN`, `ERROR`).

Ejemplo de log JSON:
```json
{
  "timestamp": "2025-11-11T02:35:00.412Z",
  "level": "INFO",
  "logger": "com.example.bankservice.application.controller.BankController",
  "message": "Bank created id=123e4567-e89b-12d3-a456-426614174000 version=0",
  "thread": "http-nio-8080-exec-1"
}
