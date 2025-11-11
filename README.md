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