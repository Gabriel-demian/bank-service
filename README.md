# bank-service


## Quick test para security

### Login → obtener token
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"admin","password":"admin123"}'

### Usar el token para acceder a bancos
curl -X GET http://localhost:8080/v1/banks \
-H "Authorization: Bearer token"


## 📂 Estructura del Proyecto
```
com.example.bankservice
├── BankServiceApplication.java
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
│   ├── repository/
│   │   └── BankRepositoryAdapter.java
│   └── config/
│       ├── PersistenceConfig.java
│       └── SecurityConfig.java
│
├── application/                
│   ├── controller/
│   │   └── BankController.java
│   └── dto/
│       ├── BankDto.java
│       └── Mappers.java
│
└── shared/                      
├── exception/
│   ├── DuplicateResourceException.java
│   └── NotFoundException.java
└── error/
└── GlobalExceptionHandler.java
```