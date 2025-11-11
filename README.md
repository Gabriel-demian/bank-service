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

### Build y ejecución
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
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── BankController.java
│   └── dto/
│       ├── BankApiMapper.java
│       ├── BankDto.java
│       ├── BankRequest.java
│       └── BankResponse.java
│
├── domain/                      
│   ├── model/                   
│   │   └── Bank.java
│   ├── port/                    
│   │   └── BankRepositoryPort.java
│   └── service/                
│       └── BankService.java
│
├── infrastructure/      
│   ├── config/
│   │   ├── JwtAuthFilter.java
│   │   ├── JwtService.java
│   │   ├── PersistenceConfig.java
│   │   └── SecurityConfig.java        
│   └── repository/
│       ├── entity/
│       │   └── BankJpaEntity.java  
│       ├── mapper/
│       │   └── BankJpaEntityMapper.java     
│       ├── SecurityConfig.java  
│       └── SecurityConfig.java  
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
  - Este endpoint consume su propio endpoint /v1/banks internamente mediante WebClient.
  Demuestra un patrón de API composition y uso de programación reactiva (o asíncrona) para evitar bloqueos.


### Arquitectura

- Arquitectura Hexagonal (Ports & Adapters):
- Domain: reglas de negocio puras (Bank, BankService, BankRepositoryPort)
- Application: controladores y DTOs (entrada/salida)
- Infrastructure: persistencia, seguridad, configuración

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
