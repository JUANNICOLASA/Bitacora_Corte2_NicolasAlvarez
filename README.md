# Blue Velvet - API REST y sitio web

**Bitácora Corte 2 - DOSW 1**  
**Escuela Colombiana de Ingeniería**  
**Realizado por:** Juan Nicolás Álvarez Muñoz

---

## Índice

1. [Descripción](#descripción)
2. [Herramientas utilizadas](#herramientas-utilizadas)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Arquitectura](#arquitectura)
5. [Diagrama de clases](#diagrama-de-clases)
6. [Persistencia](#persistencia)
7. [Seguridad](#seguridad)
8. [Docker](#docker)
9. [CI/CD y despliegue](#cicd-y-despliegue)
10. [Funcionalidades](#funcionalidades)
11. [Endpoints](#endpoints)
12. [Detalle de request y response](#detalle-de-request-y-response)
13. [Manejo de errores](#manejo-de-errores)
14. [Diagramas de secuencia](#diagramas-de-secuencia)
15. [Pruebas](#pruebas)
16. [Evidencias](#evidencias)
17. [Fuera de alcance](#fuera-de-alcance)
18. [Bibliografía](#bibliografía)

---

## Descripción

Blue Velvet es una API para una coctelería. El proyecto permite manejar la carta de cócteles y de la cocina, los modificadores, las comandas que llegan a la barra, las reservas de mesa y las calificaciones de los clientes.

El sitio web, hecho en React, consume esta API y está en la carpeta [`frontend`](frontend). Su documento de arquitectura y diseño está en [frontend/README.md](frontend/README.md).

La información principal se guarda en PostgreSQL; el historial de eventos de las comandas y las reseñas se guardan en MongoDB. La receta de referencia de los cócteles clásicos se consulta en un servicio externo (TheCocktailDB) por medio de un cliente Feign. El acceso a la API está protegido con Spring Security: cada usuario inicia sesión, recibe un token JWT y solo puede usar los endpoints que corresponden a su rol.

La aplicación se empaqueta en una imagen Docker y se despliega en Azure por medio de pipelines de GitHub Actions, con un ambiente de QA y uno de producción.

### Reglas de negocio

| Regla | Descripción |
|---|---|
| Trazabilidad del alcohol | Los cócteles con alcohol deben indicar la marca o el tipo de destilado cuando se hace una comanda. |
| Cambio de licor | El destilado de un cóctel se puede cambiar una sola vez y antes de que la comanda pase a preparación. |
| Mocktails | Los modificadores que contienen alcohol no se pueden usar en un Mocktail. |
| Disponibilidad | No se pueden pedir cócteles o modificadores que estén agotados. |
| Estados de la comanda | La comanda avanza en este orden: Recibido, En Preparación, Listo y Entregado. |
| Información de agotados | Los productos agotados muestran la etiqueta `AGOTADO EN BARRA`, el icono `candado` y no se pueden seleccionar. |
| Comandas del cliente | Un cliente solo puede consultar y modificar las comandas que él mismo creó. |
| Reservas | Solo se reserva desde hoy en adelante, de martes a domingo, de 18:00 a 00:00 cada media hora y para 1 a 12 personas. Una reserva confirmada puede pasar a cumplida o cancelada. |
| Calificaciones | Solo los clientes califican, de 1 a 5 estrellas, y cada cliente califica una vez cada producto. |

### Diagrama de contexto

```mermaid
flowchart LR
    CL(["Cliente"]) --> WEB["Sitio web Blue Velvet<br/>React en Vercel"]
    ME(["Mesero"]) --> WEB
    BA(["Bartender"]) --> WEB
    AD(["Administrador"]) --> WEB
    WEB -- "HTTPS + JWT" --> API["API Blue Velvet<br/>Spring Boot en Azure"]
    API --> PG[("PostgreSQL")]
    API --> MG[("MongoDB")]
    API -- "Feign" --> EXT["TheCocktailDB<br/>servicio externo"]
    API -. "OAuth2 opcional" .-> GO["Google"]
```

El sistema tiene cuatro tipos de usuario que entran por el sitio web. El sitio llama a la API por HTTPS y envía el token JWT en cada petición. La API guarda la información en PostgreSQL y MongoDB, consulta TheCocktailDB para mostrar la receta de referencia de los clásicos y, con el perfil `oauth2`, permite iniciar sesión con Google.
---

## Herramientas utilizadas

| Herramienta | Uso |
|---|---|
| Java 17 | Desarrollo del proyecto |
| Maven | Construcción del proyecto |
| Spring Boot | Desarrollo de la API |
| Spring Security | Autenticación y autorización |
| JJWT | Generación y validación de tokens JWT |
| Lombok | Reducción de código repetido y logs |
| MapStruct | Conversión entre objetos |
| Bean Validation | Validación de datos de entrada |
| Spring Data JPA | Acceso a PostgreSQL |
| Spring Data MongoDB | Acceso a MongoDB |
| PostgreSQL | Base de datos relacional |
| MongoDB | Base de datos no relacional para el historial de eventos |
| H2 | Base de datos en memoria para las pruebas |
| Spring Cloud OpenFeign | Cliente para consumir TheCocktailDB |
| Swagger UI | Consulta y prueba de los endpoints |
| JUnit 5, Mockito y MockMvc | Pruebas |
| JaCoCo | Cobertura de pruebas |
| SonarQube | Revisión del código |
| Docker y Docker Compose | Empaquetado y ejecución de la aplicación |
| GitHub Actions | Integración y despliegue continuo |
| Azure | App Service, Azure Database for PostgreSQL y Azure Cosmos DB for MongoDB |
| React, Vite, React Router y Axios | Sitio web (ver [frontend/README.md](frontend/README.md)) |
| Vercel | Despliegue del sitio web |

---

## Estructura del proyecto

```text
src/main/java/com/restaurante
├── RestauranteApplication.java
├── client/
│   └── dto/
├── config/
├── controller/
├── service/
├── security/
├── model/
│   ├── domain/
│   └── dto/
│       ├── request/
│       └── response/
├── persistence/
│   ├── entity/
│   └── document/
├── repository/
├── mapper/
├── validator/
├── exception/
└── util/
```

- `controller`: recibe las solicitudes.
- `service`: contiene las operaciones de la aplicación.
- `client`: contiene el cliente Feign de TheCocktailDB y los DTOs de su respuesta.
- `security`: contiene el filtro JWT, la carga de usuarios y el manejo de errores de seguridad.
- `config`: contiene la configuración de seguridad, CORS, Swagger, la zona horaria y los datos iniciales.
- `model`: contiene las clases y datos usados por el proyecto.
- `persistence/entity`: contiene las entidades de las tablas de PostgreSQL.
- `persistence/document`: contiene los documentos de las colecciones de MongoDB.
- `repository`: contiene los repositorios de Spring Data JPA y Spring Data MongoDB.
- `mapper`: realiza las conversiones entre DTOs, dominio, entidades y documentos.
- `validator`: contiene las reglas del negocio.
- `exception`: maneja los errores.
- `util`: contiene funciones de apoyo.

---

## Arquitectura

### Diagrama de componentes general

```mermaid
flowchart LR
    subgraph Vercel
        FE["Frontend React<br/>pantallas, servicios Axios"]
    end
    subgraph Azure["Azure App Service"]
        API["API Blue Velvet"]
    end
    FE -- "REST /api/v1 · JSON" --> API
    API -- "JDBC" --> PG[("PostgreSQL<br/>carta, comandas, reservas, usuarios")]
    API -- "Protocolo MongoDB" --> MG[("MongoDB<br/>eventos y reseñas")]
    API -- "HTTP · Feign" --> EXT["TheCocktailDB"]
```

El sistema tiene dos aplicaciones: el frontend, publicado en Vercel, y la API, publicada en Azure. El frontend solo se comunica con la API; la API es la única que se conecta a las bases de datos y al servicio externo.

### Diagrama de componentes específico

```mermaid
flowchart TD
    C["Cliente"] --> SEC["Spring Security<br/>JwtAuthFilter"]
    SEC --> CTRL["Controller"]
    CTRL --> M1["Mapper"]
    M1 --> SVC["Service"]
    SVC --> VAL["Validator"]
    SVC --> ME["Mapper de entidades"]
    SVC --> MD["Mapper de documentos"]
    SVC --> FC["Cliente Feign"]
    FC --> EXT["TheCocktailDB"]
    ME --> REPO["Repository JPA"]
    MD --> MREPO["Repository Mongo"]
    REPO --> DB[("PostgreSQL")]
    MREPO --> MDB[("MongoDB")]
    SVC --> M2["Mapper"]
    M2 --> C
    CTRL -. errores .-> GEH["GlobalExceptionHandler"]
    SEC -. 401 / 403 .-> MES["ManejadorErroresSeguridad"]
```

El diagrama muestra el recorrido de una petición. Primero pasa por Spring Security, donde el filtro `JwtAuthFilter` revisa el token y establece el usuario y su rol. Si la petición no tiene un token válido o el rol no tiene permiso, `ManejadorErroresSeguridad` responde 401 o 403. Si la petición es válida, llega al controller, que convierte el DTO a dominio y llama al service. El service aplica las reglas con los validators y usa los mappers para guardar la información: las tablas en PostgreSQL por medio de los repositorios JPA y los eventos en MongoDB por medio del repositorio Mongo. Para la receta de referencia, el service usa el cliente Feign `CoctelesExternosClient`, que consulta TheCocktailDB con tiempos máximos de espera de 3 y 5 segundos. Los errores del negocio se responden desde `GlobalExceptionHandler`.

---

## Diagrama de clases

```mermaid
classDiagram
    direction LR

    class Coctel {
        -Long id
        -String nombre
        -String descripcion
        -Double precio
        -CategoriaCoctel categoria
        -TipoBebida tipo
        -String destiladoBase
        -Boolean disponible
    }

    class Modificador {
        -Long id
        -String nombre
        -Double graduacionAlcoholica
        -Double precioExtra
        -Boolean disponible
    }

    class Pedido {
        -Long id
        -Integer numeroMesa
        -List~ItemPedido~ items
        -EstadoPedido estado
        -LocalDateTime fechaCreacion
        -String usuario
    }

    class ItemPedido {
        -Long id
        -Long idCoctel
        -String nombreCoctel
        -TipoBebida tipo
        -Double precioUnitario
        -Integer cantidad
        -String destilado
        -List~Modificador~ modificadores
        -Integer cambiosDeLicor
    }

    class Usuario {
        -Long id
        -String nombre
        -String email
        -String password
        -Rol rol
    }

    class EventoComanda {
        -String id
        -Long idPedido
        -TipoEvento tipo
        -String descripcion
        -EstadoPedido estado
        -String usuario
        -LocalDateTime fecha
    }

    class Plato {
        -Long id
        -String nombre
        -String descripcion
        -Double precio
        -CategoriaPlato categoria
        -String porcion
        -Boolean disponible
    }

    class Reserva {
        -UUID id
        -String codigo
        -String nombre
        -String telefono
        -String correo
        -LocalDate fecha
        -LocalTime hora
        -Integer personas
        -String ocasion
        -String notas
        -EstadoReserva estado
        -String usuario
        -LocalDateTime fechaCreacion
    }

    class Resena {
        -String id
        -TipoProducto tipo
        -Long idProducto
        -String autor
        -String usuario
        -Integer estrellas
        -String comentario
        -LocalDateTime fecha
    }

    class RecetaExterna {
        -String nombre
        -String vaso
        -String instrucciones
        -List~String~ ingredientes
        -String imagen
        -String fuente
    }

    class TipoBebida {
        <<enumeration>>
        ALCOHOLICA
        MOCKTAIL
    }

    class CategoriaCoctel {
        <<enumeration>>
        CLASICO
        DE_AUTOR
        SOUR
        TROPICAL
        SIN_ALCOHOL
    }

    class EstadoPedido {
        <<enumeration>>
        RECIBIDO
        EN_PREPARACION
        LISTO
        ENTREGADO
        CANCELADO
    }

    class Rol {
        <<enumeration>>
        ADMIN
        BARTENDER
        MESERO
        CLIENTE
    }

    class TipoEvento {
        <<enumeration>>
        CREADA
        CAMBIO_ESTADO
        CAMBIO_LICOR
        CANCELADA
    }

    class CategoriaPlato {
        <<enumeration>>
        PARA_COMPARTIR
        PLATOS_FUERTES
        POSTRES
    }

    class EstadoReserva {
        <<enumeration>>
        CONFIRMADA
        CUMPLIDA
        CANCELADA
    }

    class TipoProducto {
        <<enumeration>>
        COCTEL
        PLATO
    }

    Pedido "1" *-- "1..*" ItemPedido
    ItemPedido "*" --> "1" Coctel : idCoctel
    ItemPedido "*" o-- "*" Modificador
    Pedido "*" --> "1" Usuario : usuario
    EventoComanda "*" --> "1" Pedido : idPedido
    Coctel ..> TipoBebida
    Coctel ..> CategoriaCoctel
    Pedido ..> EstadoPedido
    Usuario ..> Rol
    EventoComanda ..> TipoEvento
    Resena "*" --> "1" Coctel : idProducto
    Resena "*" --> "1" Plato : idProducto
    Reserva "*" --> "0..1" Usuario : usuario
    RecetaExterna ..> Coctel : se busca por nombre
    Plato ..> CategoriaPlato
    Reserva ..> EstadoReserva
    Resena ..> TipoProducto
```

El diagrama muestra las clases del dominio. Una comanda (`Pedido`) está compuesta por uno o varios `ItemPedido`; cada ítem apunta a un `Coctel` y puede tener varios `Modificador`. La comanda guarda el correo del `Usuario` que la creó, lo que permite que un cliente solo vea sus propias comandas. Cada `Usuario` tiene un `Rol`, que define lo que puede hacer en la API. `EventoComanda` representa un registro del historial de una comanda (creación, cambio de estado, cambio de licor o cancelación) y se relaciona con la comanda por su id. `Plato` representa los productos de la cocina. `Reserva` guarda una reserva de mesa y, si se hizo con la sesión iniciada, el correo del usuario. `Resena` es la calificación de un cliente sobre un cóctel o un plato. `RecetaExterna` no se guarda: se arma con la respuesta de TheCocktailDB. Los enumerados fijan los valores permitidos de tipo, categoría, estado, rol y tipo de evento.

---

## Persistencia

Blue Velvet usa una persistencia **híbrida**:

| Base de datos | Información | Motivo |
|---|---|---|
| PostgreSQL (relacional) | Cócteles, platos, modificadores, comandas, ítems, reservas y usuarios | La información tiene relaciones fijas, necesita transacciones ACID y restricciones de integridad. |
| MongoDB (no relacional) | Historial de eventos de las comandas y reseñas de los clientes | Es un registro que solo se agrega y se consulta, no necesita relaciones ni transacciones y puede crecer sin cambiar el esquema de las tablas. |

En PostgreSQL:

- La información tiene relaciones fijas: una comanda tiene ítems, cada ítem pertenece a un cóctel y puede tener modificadores.
- Las comandas y el inventario necesitan transacciones ACID. Si una comanda falla a la mitad, no debe quedar guardada una parte de ella.
- Las reglas del negocio se apoyan en restricciones de la base de datos: nombres y correos únicos y llaves foráneas entre comandas, ítems y cócteles.

Cada ítem guarda el nombre y el precio del cóctel al momento del pedido, y la tabla `item_modificador` guarda el precio del modificador. Así, si la carta cambia, las comandas anteriores conservan sus valores.

En MongoDB, cada vez que una comanda se crea, cambia de estado, cambia de licor o se cancela, se guarda un documento en la colección `eventos_comanda` con el usuario que hizo la acción y la fecha. El administrador consulta este historial en `GET /api/v1/eventos`.

Las reseñas se guardan en la colección `resenas`. Son textos cortos que solo se agregan y se consultan por producto, por eso encajan mejor como documentos que como una tabla.

La tabla `reserva` usa un identificador UUID, porque el id de una reserva puede viajar en enlaces y no debe revelar cuántas reservas hay ni permitir adivinar las de otras personas. Las demás tablas usan identificadores numéricos generados por la base de datos.

### Modelo relacional

```mermaid
erDiagram
    COCTEL {
        BIGINT id PK
        VARCHAR nombre UK
        VARCHAR descripcion
        DOUBLE precio
        VARCHAR categoria
        VARCHAR tipo
        VARCHAR destilado_base
        BOOLEAN disponible
        TIMESTAMP creado_en
    }
    MODIFICADOR {
        BIGINT id PK
        VARCHAR nombre UK
        DOUBLE graduacion_alcoholica
        DOUBLE precio_extra
        BOOLEAN disponible
    }
    PEDIDO {
        BIGINT id PK
        INT numero_mesa
        VARCHAR estado
        TIMESTAMP fecha_creacion
        VARCHAR usuario
    }
    ITEM_PEDIDO {
        BIGINT id PK
        BIGINT id_pedido FK
        BIGINT id_coctel FK
        VARCHAR nombre_coctel
        VARCHAR tipo
        DOUBLE precio_unitario
        INT cantidad
        VARCHAR destilado
        INT cambios_de_licor
    }
    ITEM_MODIFICADOR {
        BIGINT id PK
        BIGINT id_item FK
        BIGINT id_modificador FK
        DOUBLE precio_extra
    }
    USUARIO {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR email UK
        VARCHAR password
        VARCHAR rol
    }
    PLATO {
        BIGINT id PK
        VARCHAR nombre UK
        VARCHAR descripcion
        DOUBLE precio
        VARCHAR categoria
        VARCHAR porcion
        BOOLEAN disponible
    }
    RESERVA {
        UUID id PK
        VARCHAR codigo UK
        VARCHAR nombre
        VARCHAR telefono
        VARCHAR correo
        DATE fecha
        TIME hora
        INT personas
        VARCHAR ocasion
        VARCHAR notas
        VARCHAR estado
        VARCHAR usuario
        TIMESTAMP fecha_creacion
    }
    PEDIDO ||--|{ ITEM_PEDIDO : contiene
    COCTEL ||--o{ ITEM_PEDIDO : "se pide en"
    ITEM_PEDIDO ||--o{ ITEM_MODIFICADOR : tiene
    MODIFICADOR ||--o{ ITEM_MODIFICADOR : "se aplica en"
```

El modelo muestra las ocho tablas de PostgreSQL. `pedido` se relaciona con `item_pedido` (una comanda tiene uno o más ítems) y cada ítem apunta a un `coctel`. La relación entre ítems y modificadores se resuelve con la tabla intermedia `item_modificador`, que además guarda el precio del modificador en el momento del pedido. La tabla `usuario` guarda las cuentas con la contraseña cifrada con BCrypt y el rol; la columna `usuario` de `pedido` guarda el correo de quien creó la comanda. `plato` guarda la carta de la cocina y `reserva` las reservas de mesa, con un índice por fecha para la agenda del día y otro por usuario para "Mis reservas".

### Modelo de documentos

```mermaid
classDiagram
    class eventos_comanda {
        ObjectId _id
        Long id_pedido
        String tipo
        String descripcion
        String estado
        String usuario
        Date fecha
    }
    class resenas {
        ObjectId _id
        String tipo
        Long id_producto
        String autor
        String usuario
        Integer estrellas
        String comentario
        Date fecha
    }
```

Ejemplo de un documento:

```json
{
  "_id": "6aca524b11764517afeead09",
  "id_pedido": 1,
  "tipo": "CAMBIO_ESTADO",
  "descripcion": "Estado cambiado de RECIBIDO a EN_PREPARACION",
  "estado": "EN_PREPARACION",
  "usuario": "bartender@bluevelvet.com",
  "fecha": "2026-10-10T14:57:15.860Z"
}
```

La colección `eventos_comanda` guarda un documento por cada acción sobre una comanda. El campo `id_pedido` enlaza el evento con la comanda de PostgreSQL. Los campos `id_pedido` y `fecha` tienen índice, porque los eventos se filtran por comanda y se ordenan por fecha. En `resenas` hay un índice compuesto por `tipo` e `id_producto`, que es la forma en que se consultan las opiniones de cada producto.

### Configuración de las bases de datos

La conexión se configura con variables de entorno. Si no existen, se usan los valores por defecto:

| Variable | Valor por defecto |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/blue_velvet` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `MONGO_URI` | `mongodb://localhost:27017/blue_velvet` |

Las tablas se crean automáticamente al iniciar la aplicación (`ddl-auto=update`). Las pruebas usan H2 en memoria y simulan el repositorio de MongoDB, por lo que no necesitan ninguna base de datos instalada.

---

## Seguridad

La seguridad se implementó con Spring Security siguiendo tres preguntas: quién es el usuario (autenticación), qué puede hacer (autorización) y si la información llega sin alteraciones (integridad).

### Roles

| Rol | Puede hacer | No puede hacer |
|---|---|---|
| Administrador (`ADMIN`) | Administrar cócteles, platos y modificadores, marcar agotados, consultar y cancelar comandas, manejar la agenda de reservas y ver el historial de eventos | Crear comandas, cambiar el licor o el estado de una comanda, calificar productos |
| Bartender (`BARTENDER`) | Ver el tablero de la barra, cambiar el estado de las comandas y marcar cócteles y modificadores agotados | Administrar la carta, crear o cancelar comandas, ver reservas |
| Mesero (`MESERO`) | Ver la carta, crear comandas, cambiar el licor una vez, cancelar comandas en RECIBIDO y manejar la agenda de reservas | Administrar la carta, marcar agotados y cambiar el estado de las comandas |
| Cliente (`CLIENTE`) | Ver la carta, crear su comanda, cambiar su licor una vez, consultar sus comandas, calificar productos y consultar o cancelar sus reservas | Administrar la carta, ver otras comandas o el tablero, cambiar estados y ver reservas ajenas |
| Visitante (sin sesión) | Ver la carta, la cocina, las calificaciones y las recetas de referencia, y reservar una mesa | Pedir, calificar o consultar reservas |

La tabla completa por rol está en [docs/roles-blue-velvet.xlsx](docs/roles-blue-velvet.xlsx).

### Permisos por endpoint

| Endpoint | Método | ¿Quién puede? | Regla en Spring |
|---|---|---|---|
| `/api/v1/auth/login` | POST | Todos (público) | `permitAll()` |
| `/api/v1/auth/registro` | POST | Todos (público) | `permitAll()` |
| `/api/v1/auth/me` | GET | Cualquier usuario autenticado | `authenticated()` |
| `/api/v1/menu/**` | GET | Todos (público) | `permitAll()` |
| `/api/v1/platos` y `/{id}` | GET | Todos (público) | `permitAll()` |
| `/api/v1/platos` | POST | Administrador | `hasRole('ADMIN')` |
| `/api/v1/platos/{id}` | PUT y DELETE | Administrador | `hasRole('ADMIN')` |
| `/api/v1/platos/{id}/disponibilidad` | PATCH | Administrador | `hasRole('ADMIN')` |
| `/api/v1/resenas` y `/resenas/resumen` | GET | Todos (público) | `permitAll()` |
| `/api/v1/resenas` | POST | Cliente | `hasRole('CLIENTE')` |
| `/api/v1/reservas` | POST | Todos (público) | `permitAll()` |
| `/api/v1/reservas` | GET | Administrador y mesero | `hasAnyRole('ADMIN','MESERO')` |
| `/api/v1/reservas/mias` | GET | Cualquier usuario autenticado | `isAuthenticated()` |
| `/api/v1/reservas/{id}/estado` | PATCH | Administrador y mesero | `hasAnyRole('ADMIN','MESERO')` |
| `/api/v1/reservas/{id}` | DELETE | Administrador, mesero y cliente (el cliente solo la suya) | `hasAnyRole(...)` + validación del dueño |
| `/api/v1/cocteles` y `/{id}` | GET | Administrador y bartender | `hasAnyRole('ADMIN','BARTENDER')` |
| `/api/v1/cocteles` | POST | Administrador | `hasRole('ADMIN')` |
| `/api/v1/cocteles/{id}` | PUT | Administrador | `hasRole('ADMIN')` |
| `/api/v1/cocteles/{id}` | DELETE | Administrador | `hasRole('ADMIN')` |
| `/api/v1/cocteles/{id}/disponibilidad` | PATCH | Administrador y bartender | `hasAnyRole('ADMIN','BARTENDER')` |
| `/api/v1/modificadores` y `/{id}` | GET | Administrador, bartender y mesero | `hasAnyRole('ADMIN','BARTENDER','MESERO')` |
| `/api/v1/modificadores` | POST | Administrador | `hasRole('ADMIN')` |
| `/api/v1/modificadores/{id}/disponibilidad` | PATCH | Administrador y bartender | `hasAnyRole('ADMIN','BARTENDER')` |
| `/api/v1/pedidos` | POST | Mesero y cliente | `hasAnyRole('MESERO','CLIENTE')` |
| `/api/v1/pedidos` | GET | Administrador, bartender y mesero | `hasAnyRole('ADMIN','BARTENDER','MESERO')` |
| `/api/v1/pedidos/{id}` | GET | Todos los roles (el cliente solo la suya) | `hasAnyRole(...)` + validación del dueño |
| `/api/v1/pedidos/activos` | GET | Administrador y bartender | `hasAnyRole('ADMIN','BARTENDER')` |
| `/api/v1/pedidos/mias` | GET | Mesero y cliente | `hasAnyRole('MESERO','CLIENTE')` |
| `/api/v1/pedidos/{id}/estado` | PATCH | Bartender | `hasRole('BARTENDER')` |
| `/api/v1/pedidos/{id}/items/{idItem}/destilado` | PATCH | Mesero y cliente (el cliente solo la suya) | `hasAnyRole('MESERO','CLIENTE')` + validación del dueño |
| `/api/v1/pedidos/{id}` | DELETE | Administrador y mesero | `hasAnyRole('ADMIN','MESERO')` |
| `/api/v1/eventos` | GET | Administrador | `hasRole('ADMIN')` |

Las rutas públicas se definen en `SecurityConfig` y las reglas por rol se definen con `@PreAuthorize` en cada método de los controllers.

### Autenticación

Se implementaron tres formas de autenticación:

| Mecanismo | Uso en el proyecto |
|---|---|
| JWT | Es el mecanismo principal. El usuario inicia sesión en `POST /api/v1/auth/login` y recibe un token firmado con HMAC-SHA que dura una hora. El token lleva el correo y el rol, y se envía en cada petición en el encabezado `Authorization: Bearer <token>`. |
| HTTP Basic | Se puede enviar el correo y la contraseña en cada petición. Solo debe usarse sobre HTTPS, porque Base64 no cifra los datos. |
| OAuth2 con Google | Se activa con el perfil `oauth2` y las variables `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`. Al ingresar por `/oauth2/authorization/google`, el usuario se crea como cliente si no existe y la API devuelve su propio token JWT. |

Las contraseñas se guardan cifradas con BCrypt. Al iniciar, la aplicación crea un usuario por rol (`admin@bluevelvet.com`, `bartender@bluevelvet.com`, `mesero@bluevelvet.com` y `cliente@bluevelvet.com`) con la contraseña definida en la variable `USUARIOS_PASSWORD`. Los clientes nuevos se registran en `POST /api/v1/auth/registro`; la contraseña debe tener entre 8 y 64 caracteres, con mayúscula, minúscula, número y carácter especial.

```mermaid
sequenceDiagram
    participant C as Cliente
    participant A as AuthController
    participant M as AuthenticationManager
    participant J as JwtUtil
    participant F as JwtAuthFilter
    participant P as PedidoController

    C->>A: POST /api/v1/auth/login (correo y contraseña)
    A->>M: authenticate()
    M-->>A: Usuario válido (BCrypt)
    A->>J: generarToken(correo, rol)
    J-->>C: 200 OK + token JWT
    C->>F: GET /api/v1/pedidos/activos + Bearer token
    F->>J: esValido(token)
    alt Token válido y rol permitido
        F->>P: Petición autenticada con su rol
        P-->>C: 200 OK
    else Sin token, token inválido o expirado
        F-->>C: 401 Unauthorized
    else Rol sin permiso
        P-->>C: 403 Forbidden
    end
```

El diagrama de secuencia muestra el flujo con JWT. En el login, `AuthenticationManager` compara la contraseña con el hash BCrypt guardado y, si es correcta, `JwtUtil` genera el token. En las siguientes peticiones, `JwtAuthFilter` valida la firma y la fecha de expiración del token; si es válido, toma el correo y el rol del token y deja la petición autenticada. Luego Spring Security revisa el rol con `@PreAuthorize`: si no tiene permiso responde 403 y si no hay un token válido responde 401.

Para evitar ataques de fuerza bruta, después de 5 intentos fallidos el login de esa cuenta se bloquea durante 15 minutos (429). Los inicios de sesión fallidos, los accesos sin autenticación, los accesos denegados y los tokens rechazados quedan registrados en los logs.

### Integridad, SSL, CORS y cabeceras

| Tema | Implementación |
|---|---|
| Integridad del token | El JWT va firmado con una clave de al menos 256 bits. Si alguien cambia el contenido, la firma deja de coincidir y la API responde 401. |
| SSL / HTTPS | Con el perfil `ssl`, la API corre en `https://localhost:8443` con un certificado autofirmado (`keytool`, formato PKCS12). El certificado y su contraseña no se suben al repositorio. En Azure, App Service publica la API por HTTPS. |
| CORS | `CorsConfig` solo permite los orígenes definidos en la variable `CORS_ORIGENES` (por defecto `http://localhost:3000` y `http://localhost:5173`), con los métodos y encabezados que usa la API. En Azure se permite el dominio del sitio en Vercel (`https://blue-velvet-jnam.vercel.app`). |
| CSRF | Está desactivado porque la API no usa sesiones ni cookies: es stateless y el token se envía de forma explícita en cada petición. |
| XSS y clickjacking | La API envía `Content-Security-Policy`, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff` y `X-XSS-Protection: 1; mode=block`. |

### Checklist OWASP Top 10

| Riesgo | ¿Aplica? | Cómo se mitigó |
|---|---|---|
| A1 Broken Access Control | Sí | Reglas por rol en cada endpoint con `@PreAuthorize`; el cliente solo accede a sus propias comandas. Un cliente que intenta crear un cóctel recibe 403. |
| A2 Cryptographic Failures | Sí | Contraseñas con BCrypt, JWT firmado con clave de 256 bits o más, HTTPS con el perfil `ssl` y en Azure. |
| A3 Injection | Sí | Solo se usan métodos derivados de Spring Data y `@Query` con parámetros nombrados, tanto en JPA como en MongoDB. No se concatenan consultas. |
| A4 Insecure Design | Sí | Los DTO de respuesta no exponen contraseñas ni datos internos; el registro siempre crea clientes y no permite elegir el rol. |
| A5 Security Misconfiguration | Sí | Los errores 500 no muestran el stack trace, las credenciales se leen de variables de entorno y CORS solo acepta orígenes configurados. |
| A6 Vulnerable and Outdated Components | Sí | Dependencias de Maven Central en versiones estables recientes (Spring Boot 3.5.6, JJWT 0.12.6). |
| A7 Identification and Authentication Failures | Sí | Tokens con expiración de una hora, validación de complejidad de la contraseña, bloqueo tras 5 intentos fallidos y respuesta 401 (no 500) con tokens vencidos. |
| A8 Software and Data Integrity Failures | Sí | La firma del JWT se verifica en cada petición y las dependencias vienen de Maven Central. |
| A9 Security Logging and Monitoring Failures | Sí | Se registran en los logs los logins fallidos, los bloqueos, los tokens rechazados y los accesos 401 y 403. |
| A10 Server-Side Request Forgery | Sí | La única consulta externa es a TheCocktailDB. Su dirección está fija en la configuración (`cocteles-externos.url`) y la API solo envía el nombre del cóctel guardado en la carta, nunca una URL escrita por el usuario. |

### Evidencias de seguridad

**Login exitoso en Swagger (200 con el token JWT)**

![Login exitoso](docs/evidencias/seg-login.png)

**Botón Authorize con el token ingresado**

![Authorize](docs/evidencias/seg-authorize.png)

**Petición sin token (401)**

![Sin token](docs/evidencias/seg-401.png)

**Cliente intentando crear un cóctel (403)**

![Rol incorrecto](docs/evidencias/seg-403.png)

**Administrador creando un cóctel (201)**

![Rol correcto](docs/evidencias/seg-201.png)

**Cabeceras de seguridad en la respuesta**

![Cabeceras](docs/evidencias/seg-headers.png)

**Contraseñas cifradas con BCrypt en PostgreSQL**

![Usuarios en la base de datos](docs/evidencias/seg-bd-usuarios.png)

**Pruebas de seguridad en verde (`mvn test`)**

![Pruebas de seguridad](docs/evidencias/seg-pruebas.png)

---

## Docker

La API se empaqueta con un `Dockerfile` multietapa: la primera etapa compila el proyecto con Maven y la segunda solo copia el JAR sobre una imagen ligera de Java (`eclipse-temurin:21-jre-alpine`), que corre con un usuario sin privilegios y con la zona horaria de Bogotá. `docker-compose.yml` levanta la API junto con PostgreSQL y MongoDB en una misma red, con volúmenes para conservar los datos y healthchecks para que la API arranque cuando las bases de datos estén listas.

Para levantar el stack se copia `.env.example` como `.env`, se completan los valores y se ejecuta:

```bash
docker compose up --build -d
```

### Variables de entorno

Las credenciales no están en el código. Se definen en un archivo `.env` (que no se sube al repositorio); `.env.example` tiene los nombres de las variables sin valores.

| Variable | Descripción |
|---|---|
| `DB_PASSWORD` | Contraseña de PostgreSQL |
| `MONGO_USERNAME` | Usuario administrador de MongoDB |
| `MONGO_PASSWORD` | Contraseña de MongoDB |
| `MONGO_URI` | Cadena de conexión a MongoDB cuando la API se ejecuta fuera de Docker |
| `JWT_SECRET` | Clave en Base64 para firmar los tokens (mínimo 256 bits) |
| `USUARIOS_PASSWORD` | Contraseña de los usuarios iniciales de cada rol |
| `CORS_ORIGENES` | Orígenes que pueden llamar a la API desde el navegador, separados por comas |
| `COCTELES_EXTERNOS_URL` | URL base de TheCocktailDB (opcional, tiene valor por defecto) |
| `SSL_KEYSTORE_PASSWORD` | Contraseña del certificado para el perfil `ssl` |
| `GOOGLE_CLIENT_ID` | Client ID de Google para el perfil `oauth2` |
| `GOOGLE_CLIENT_SECRET` | Client Secret de Google para el perfil `oauth2` |

### Imagen en Docker Hub

La imagen está publicada en [hub.docker.com/r/juanamunoz/blue-velvet-api](https://hub.docker.com/r/juanamunoz/blue-velvet-api) para `linux/amd64` y `linux/arm64`.

| Tag | Origen |
|---|---|
| `1.0`, `1.0.0` y `latest` | Versión de producción |
| `qa-latest` y `qa-<sha>` | Generadas por el pipeline de QA en cada push a `master` |

### Evidencias de Docker

**Servicios en ejecución (`docker compose ps`)**

![docker compose ps](docs/evidencias/docker-compose-ps.png)

**Logs de la API con la conexión a las bases de datos**

![Logs de la API](docs/evidencias/docker-logs-api.png)

**Swagger funcionando en el contenedor**

![Swagger en Docker](docs/evidencias/docker-swagger.png)

**Imagen publicada en Docker Hub**

![Pagina de Docker Hub](docs/evidencias/dockerhub.png)

![Imagen en Docker Hub](docs/evidencias/docker-imagen.png)

---

## CI/CD y despliegue

### Ambientes

| Ambiente | Cuándo se despliega | Propósito | Aprobación |
|---|---|---|---|
| QA | En cada push a `master` o `develop` | Validar que el código funciona después de cada cambio | Automática |
| PROD | Al crear un tag de versión `v*.*.*` | API estable para los usuarios | Manual en GitHub (ambiente `production`) |

| Ambiente | URL |
|---|---|
| QA | https://blue-velvet-qa-jnam.azurewebsites.net/swagger-ui/index.html |
| PROD | https://blue-velvet-prod-jnam.azurewebsites.net/swagger-ui/index.html |

Cada ambiente tiene su propio App Service, su propia base de datos PostgreSQL, su propia cuenta de MongoDB y sus propias credenciales, de modo que un problema en QA no afecta a producción.

### Pipelines

| Workflow | Jobs |
|---|---|
| `.github/workflows/ci-qa.yml` | **Pruebas**: ejecuta `mvn test` y publica el reporte de JaCoCo. **Build frontend**: instala las dependencias del sitio web y verifica que compile. **Build imagen Docker**: construye la imagen y la sube a Docker Hub con los tags `qa-latest` y `qa-<sha>`. **Desplegar a QA**: configura las variables de entorno del App Service de QA y despliega la imagen. En los pull requests solo se ejecutan las pruebas. |
| `.github/workflows/ci-prod.yml` | **Build imagen PROD**: ejecuta las pruebas y publica la imagen con la versión del tag y `latest`. **Desplegar a PROD**: espera la aprobación manual, configura las variables del App Service de producción y despliega la versión. **Resumen**: muestra la versión y la URL desplegadas. |

### Secrets configurados en GitHub

| Secret | Uso |
|---|---|
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | Token de acceso de Docker Hub |
| `AZURE_CREDENTIALS` | Credenciales del Service Principal que despliega en Azure |
| `AZURE_RESOURCE_GROUP` | Grupo de recursos de Azure |
| `AZURE_WEBAPP_NAME_QA` / `AZURE_WEBAPP_NAME_PROD` | Nombre del App Service de cada ambiente |
| `DB_URL_QA` / `DB_URL_PROD` | URL de PostgreSQL de cada ambiente |
| `DB_USERNAME_QA` / `DB_USERNAME_PROD` | Usuario de PostgreSQL de cada ambiente |
| `DB_PASSWORD_QA` / `DB_PASSWORD_PROD` | Contraseña de PostgreSQL de cada ambiente |
| `MONGO_URI_QA` / `MONGO_URI_PROD` | Cadena de conexión de MongoDB de cada ambiente |
| `JWT_SECRET_QA` / `JWT_SECRET_PROD` | Clave de firma de los tokens de cada ambiente |
| `USUARIOS_PASSWORD_QA` / `USUARIOS_PASSWORD_PROD` | Contraseña de los usuarios iniciales de cada ambiente |

### Diagrama de despliegue

![Diagrama de despliegue](docs/diagramas/diagrama-despliegue.png)

El diagrama se construyó en draw.io ([docs/diagramas/diagrama-despliegue.drawio](docs/diagramas/diagrama-despliegue.drawio)) y muestra cómo está desplegado el sistema:

- **Computador del desarrollador**: contiene el código fuente y el `Dockerfile`. Envía los cambios a GitHub con `git push` por HTTPS.
- **GitHub**: guarda el repositorio. Un push a `master` o un tag de versión dispara los workflows.
- **GitHub Actions**: ejecuta `ci-qa.yml` y `ci-prod.yml` en una máquina Ubuntu. Corre las pruebas, genera el reporte de JaCoCo, construye la imagen y la publica en Docker Hub.
- **Docker Hub**: guarda las imágenes `juanamunoz/blue-velvet-api` con los tags de QA y de producción. Es el nodo intermedio desde el que los App Service descargan la imagen.
- **Azure QA y Azure PROD**: cada ambiente tiene un App Service Linux que ejecuta el contenedor de la API en el puerto 8080, un servidor de Azure Database for PostgreSQL con la base `blue_velvet` y una cuenta de Azure Cosmos DB for MongoDB con la colección `eventos_comanda`. La API se conecta a PostgreSQL por JDBC y a MongoDB con el protocolo de MongoDB, ambos con TLS.
- **Cliente**: usa la API desde el navegador o Swagger UI por HTTPS, enviando el token JWT.

El despliegue a QA es automático (línea sólida) y el de producción requiere aprobación manual (línea punteada).

El sitio web se despliega en Vercel desde la carpeta `frontend` del mismo repositorio. Vercel construye el sitio en cada push a `master` y lo publica en https://blue-velvet-jnam.vercel.app, conectado a la API de producción.

### Evidencias de CI/CD

**Pipeline de QA en verde (pruebas, imagen y despliegue)**

![Pipeline QA](docs/evidencias/cicd-pipeline-qa.png)

**API desplegada en Azure QA**

![Swagger en QA](docs/evidencias/cicd-qa-swagger.png)

**Aprobación manual del despliegue a PROD**

![Aprobacion PROD](docs/evidencias/cicd-aprobacion-prod.png)

**Pipeline de PROD en verde con la aprobación registrada**

![Pipeline PROD](docs/evidencias/cicd-pipeline-prod.png)

**App Service de producción en Azure con el contenedor `juanamunoz/blue-velvet-api:1.0.0`**

![App Service en Azure](docs/evidencias/azure-app-service.png)

**API desplegada en Azure PROD**

![Swagger en PROD](docs/evidencias/cicd-prod-swagger.png)

---

## Funcionalidades

| Funcionalidad | Descripción |
|---|---|
| Autenticación | Iniciar sesión con JWT, HTTP Basic u OAuth2 y registrar clientes. |
| Gestión de cócteles | Crear, actualizar, consultar, eliminar y cambiar la disponibilidad de los cócteles. |
| Carta digital | Consultar la carta y ver cuáles productos están disponibles o agotados. |
| Modificadores | Consultar y administrar los modificadores de los cócteles. |
| Comandas | Crear pedidos indicando la mesa y los cócteles solicitados. |
| Tablero de la barra | Consultar las comandas activas y cambiar su estado. |
| Cambio de licor | Permitir un solo cambio de destilado antes de la preparación. |
| Historial de eventos | Consultar en MongoDB lo que ha pasado con cada comanda. |
| Cocina | Consultar los platos de la cocina y administrarlos. |
| Reservas | Reservar una mesa con código de confirmación, consultar la agenda del día y marcar las reservas como cumplidas o canceladas. |
| Calificaciones | Calificar cócteles y platos y consultar el promedio y las opiniones de cada uno. |
| Receta de referencia | Consultar en TheCocktailDB cómo se prepara un cóctel clásico. |

### Casos de uso

```mermaid
flowchart LR
    V(["Visitante"])
    C(["Cliente"])
    M(["Mesero"])
    B(["Bartender"])
    A(["Administrador"])
    V --- U1(["Ver la carta y la cocina"])
    V --- U2(["Reservar una mesa"])
    V --- U3(["Registrarse e iniciar sesión"])
    C --- U4(["Pedir desde la mesa"])
    C --- U5(["Cambiar el licor una vez"])
    C --- U6(["Calificar un producto"])
    C --- U7(["Consultar sus comandas y reservas"])
    M --- U4
    M --- U5
    M --- U8(["Cancelar comandas en RECIBIDO"])
    M --- U9(["Manejar la agenda de reservas"])
    B --- U10(["Avanzar comandas en el tablero"])
    B --- U11(["Marcar productos agotados"])
    A --- U11
    A --- U12(["Administrar cócteles, platos y modificadores"])
    A --- U9
    A --- U13(["Consultar el historial de eventos"])
```

Todos los usuarios con cuenta también pueden hacer lo que hace el visitante; el diagrama solo muestra lo que agrega cada rol.

---

## Endpoints

Todas las rutas comienzan con `/api/v1/`. Los permisos de cada una están en [Permisos por endpoint](#permisos-por-endpoint).

### Autenticación

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/auth/login` | POST | Iniciar sesión y obtener el token |
| `/api/v1/auth/registro` | POST | Registrar un cliente |
| `/api/v1/auth/me` | GET | Consultar el usuario autenticado |

### Cócteles

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/cocteles` | GET | Consultar cócteles |
| `/api/v1/cocteles/{id}` | GET | Consultar un cóctel |
| `/api/v1/cocteles` | POST | Crear un cóctel |
| `/api/v1/cocteles/{id}` | PUT | Actualizar un cóctel |
| `/api/v1/cocteles/{id}/disponibilidad` | PATCH | Cambiar disponibilidad |
| `/api/v1/cocteles/{id}` | DELETE | Eliminar un cóctel |

### Menú

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/menu` | GET | Consultar la carta |
| `/api/v1/menu/disponibles` | GET | Consultar solo los disponibles |
| `/api/v1/menu/{id}` | GET | Consultar el detalle de un cóctel |
| `/api/v1/menu/{id}/modificadores` | GET | Consultar sus modificadores |
| `/api/v1/menu/{id}/receta` | GET | Consultar la receta de referencia en TheCocktailDB |

### Cocina

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/platos` | GET | Consultar los platos (filtro opcional `?categoria=`) |
| `/api/v1/platos/{id}` | GET | Consultar un plato |
| `/api/v1/platos` | POST | Crear un plato |
| `/api/v1/platos/{id}` | PUT | Actualizar un plato |
| `/api/v1/platos/{id}/disponibilidad` | PATCH | Cambiar disponibilidad |
| `/api/v1/platos/{id}` | DELETE | Eliminar un plato |

### Modificadores

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/modificadores` | GET | Consultar modificadores |
| `/api/v1/modificadores/{id}` | GET | Consultar un modificador |
| `/api/v1/modificadores` | POST | Crear un modificador |
| `/api/v1/modificadores/{id}/disponibilidad` | PATCH | Cambiar disponibilidad |

### Pedidos

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/pedidos` | POST | Crear una comanda |
| `/api/v1/pedidos` | GET | Consultar comandas |
| `/api/v1/pedidos/activos` | GET | Consultar las comandas activas |
| `/api/v1/pedidos/mias` | GET | Consultar las comandas creadas por el usuario |
| `/api/v1/pedidos/{id}` | GET | Consultar una comanda |
| `/api/v1/pedidos/{id}/estado` | PATCH | Cambiar el estado de una comanda |
| `/api/v1/pedidos/{id}/items/{idItem}/destilado` | PATCH | Cambiar el licor de un ítem |
| `/api/v1/pedidos/{id}` | DELETE | Cancelar una comanda |

### Eventos

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/eventos` | GET | Consultar el historial de eventos (filtro opcional `?idPedido=`) |

### Reseñas

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/resenas?tipo=&idProducto=` | GET | Consultar las opiniones de un producto |
| `/api/v1/resenas/resumen` | GET | Consultar el promedio y la distribución de estrellas de cada producto |
| `/api/v1/resenas` | POST | Calificar un cóctel o un plato |

### Reservas

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/reservas` | POST | Reservar una mesa |
| `/api/v1/reservas` | GET | Consultar la agenda (filtro opcional `?fecha=`) |
| `/api/v1/reservas/mias` | GET | Consultar las reservas del usuario |
| `/api/v1/reservas/{id}/estado` | PATCH | Marcar una reserva como cumplida o cancelada |
| `/api/v1/reservas/{id}` | DELETE | Cancelar una reserva |

---

## Detalle de request y response

Todos los cuerpos van en JSON. Los campos obligatorios se validan con Bean Validation y, si no cumplen, la API responde 400 con el detalle de cada campo.

### Requests

**CoctelRequestDTO** (`POST` y `PUT /api/v1/cocteles`)

| Campo | Tipo | Restricciones |
|---|---|---|
| `nombre` | String | Obligatorio, máximo 60 caracteres, único |
| `descripcion` | String | Opcional, máximo 250 caracteres |
| `precio` | Double | Obligatorio, mayor a 0 |
| `categoria` | Enum `CategoriaCoctel` | Obligatorio: `CLASICO`, `DE_AUTOR`, `SOUR`, `TROPICAL`, `SIN_ALCOHOL` |
| `tipo` | Enum `TipoBebida` | Obligatorio: `ALCOHOLICA` o `MOCKTAIL` |
| `destiladoBase` | String | Máximo 40 caracteres. Obligatorio si es `ALCOHOLICA` y vacío si es `MOCKTAIL` |
| `disponible` | Boolean | Opcional, por defecto `true` |

**PlatoRequestDTO** (`POST` y `PUT /api/v1/platos`)

| Campo | Tipo | Restricciones |
|---|---|---|
| `nombre` | String | Obligatorio, máximo 60 caracteres, único |
| `descripcion` | String | Opcional, máximo 250 caracteres |
| `precio` | Double | Obligatorio, mayor a 0 |
| `categoria` | Enum `CategoriaPlato` | Obligatorio: `PARA_COMPARTIR`, `PLATOS_FUERTES`, `POSTRES` |
| `porcion` | String | Opcional, máximo 60 caracteres |
| `disponible` | Boolean | Opcional, por defecto `true` |

**ModificadorRequestDTO** (`POST /api/v1/modificadores`)

| Campo | Tipo | Restricciones |
|---|---|---|
| `nombre` | String | Obligatorio, máximo 60 caracteres, único |
| `graduacionAlcoholica` | Double | Obligatorio, de 0 a 100 |
| `precioExtra` | Double | Obligatorio, 0 o mayor |
| `disponible` | Boolean | Opcional, por defecto `true` |

**PedidoRequestDTO** (`POST /api/v1/pedidos`)

| Campo | Tipo | Restricciones |
|---|---|---|
| `numeroMesa` | Integer | Obligatorio, mayor a 0 |
| `items` | Lista de `ItemPedidoRequestDTO` | Obligatoria, al menos un ítem |
| `items[].idCoctel` | Long | Obligatorio, el cóctel debe existir y estar disponible |
| `items[].cantidad` | Integer | Obligatorio, de 1 a 20 |
| `items[].destilado` | String | Máximo 60 caracteres. Obligatorio en cócteles con alcohol y vacío en Mocktails |
| `items[].idsModificadores` | Lista de Long | Opcional. Los modificadores deben estar disponibles y no tener alcohol si el cóctel es Mocktail |

**CambioEstadoRequestDTO**, **CambioDestiladoRequestDTO** y **DisponibilidadRequestDTO**

| DTO | Campo | Tipo | Restricciones |
|---|---|---|---|
| CambioEstadoRequestDTO | `estado` | Enum `EstadoPedido` | Obligatorio, debe ser el siguiente estado |
| CambioDestiladoRequestDTO | `destilado` | String | Obligatorio, máximo 60 caracteres |
| DisponibilidadRequestDTO | `disponible` | Boolean | Obligatorio |

**ReservaRequestDTO** (`POST /api/v1/reservas`)

| Campo | Tipo | Restricciones |
|---|---|---|
| `nombre` | String | Obligatorio, de 3 a 80 caracteres |
| `telefono` | String | Obligatorio, de 7 a 20 caracteres entre dígitos, espacios, `+`, `(`, `)` y `-` |
| `correo` | String | Obligatorio, formato de correo, máximo 120 caracteres |
| `fecha` | LocalDate (`AAAA-MM-DD`) | Obligatoria, desde hoy y distinta de lunes |
| `hora` | LocalTime (`HH:mm`) | Obligatoria, de 18:00 a 23:30 cada media hora, o 00:00 |
| `personas` | Integer | Obligatorio, de 1 a 12 |
| `ocasion` | String | Opcional, máximo 40 caracteres |
| `notas` | String | Opcional, máximo 300 caracteres |

**CambioEstadoReservaRequestDTO** (`PATCH /api/v1/reservas/{id}/estado`): `estado` (Enum `EstadoReserva`, obligatorio, `CUMPLIDA` o `CANCELADA`).

**ResenaRequestDTO** (`POST /api/v1/resenas`)

| Campo | Tipo | Restricciones |
|---|---|---|
| `tipo` | Enum `TipoProducto` | Obligatorio: `COCTEL` o `PLATO` |
| `idProducto` | Long | Obligatorio, el producto debe existir |
| `estrellas` | Integer | Obligatorio, de 1 a 5 |
| `comentario` | String | Opcional, máximo 280 caracteres |

**LoginRequestDTO** y **RegistroRequestDTO** (`/api/v1/auth`)

| Campo | Tipo | Restricciones |
|---|---|---|
| `nombre` (solo registro) | String | Obligatorio, máximo 80 caracteres |
| `email` | String | Obligatorio, formato de correo, máximo 120 caracteres |
| `password` | String | Obligatoria. En el registro: de 8 a 64 caracteres con mayúscula, minúscula, número y carácter especial |

### Responses

| DTO | Campos |
|---|---|
| `TokenResponseDTO` | `token` (String), `tipo` (`Bearer`) |
| `UsuarioResponseDTO` | `id`, `nombre`, `email`, `rol` (sin contraseña) |
| `CoctelResponseDTO` | `id`, `nombre`, `descripcion`, `precio`, `categoria`, `tipo`, `destiladoBase`, `disponible` |
| `MenuItemResponseDTO` | Los campos del cóctel más `etiqueta` (`DISPONIBLE` o `AGOTADO EN BARRA`), `icono` (`check` o `candado`) y `seleccionable` |
| `ModificadorMenuResponseDTO` | `id`, `nombre`, `graduacionAlcoholica`, `precioExtra`, `habilitado`, `etiqueta` y `icono` |
| `PlatoResponseDTO` | `id`, `nombre`, `descripcion`, `precio`, `categoria`, `porcion`, `disponible` |
| `PedidoResponseDTO` | `id`, `numeroMesa`, `estado`, `fechaCreacion`, `items` y `total` |
| `ItemPedidoResponseDTO` | `id`, `idCoctel`, `nombreCoctel`, `tipo`, `precioUnitario`, `cantidad`, `destilado`, `modificadores` (nombres), `cambiosDeLicor` y `subtotal` |
| `ReservaResponseDTO` | `id` (UUID), `codigo` (`BV-` y seis caracteres), `nombre`, `telefono`, `correo`, `fecha`, `hora`, `personas`, `ocasion`, `notas`, `estado` y `fechaCreacion` |
| `ResenaResponseDTO` | `id`, `tipo`, `idProducto`, `autor`, `estrellas`, `comentario` y `fecha` (sin el correo del cliente) |
| `ResumenCalificacionResponseDTO` | `tipo`, `idProducto`, `promedio` (un decimal), `total` y `distribucion` (cantidad de opiniones de 1 a 5 estrellas) |
| `RecetaExternaResponseDTO` | `nombre`, `vaso`, `instrucciones` (en español cuando el proveedor las tiene), `ingredientes`, `imagen` y `fuente` |
| `EventoComandaResponseDTO` | `id`, `idPedido`, `tipo`, `descripcion`, `estado`, `usuario` y `fecha` |

---

## Manejo de errores

Los errores se devuelven con un formato común.

```json
{
  "status": 422,
  "codigo": "BV-422",
  "mensaje": "El coctel Negroni requiere especificar la marca o tipo exacto de destilado",
  "ruta": "/api/v1/pedidos",
  "timestamp": "2026-09-22T21:15:03.120",
  "detalles": []
}
```

| Código | Uso |
|---|---|
| 400 `BV-400` | Datos de entrada incorrectos |
| 401 `BV-401` | Sin token, token inválido o vencido, o credenciales incorrectas |
| 403 `BV-403` | El rol no tiene permiso o la comanda pertenece a otro cliente |
| 404 `BV-404` | Recurso no encontrado |
| 405 `BV-405` | Método no permitido |
| 409 `BV-409` | Nombre o correo duplicado, o cóctel con comandas registradas |
| 422 `BV-422` | Regla de negocio no cumplida |
| 429 `BV-429` | Demasiados intentos de login fallidos |
| 500 `BV-500` | Error inesperado |
| 502 `BV-502` | TheCocktailDB no responde o tarda más de lo permitido |

### Mensajes por escenario

| Escenario | Código | Mensaje |
|---|---|---|
| Campo obligatorio vacío o con formato incorrecto | 400 | `La peticion tiene datos invalidos` con el detalle de cada campo |
| Falta un parámetro de la URL | 400 | `Falta el parametro idProducto` |
| Correo o contraseña incorrectos | 401 | `Correo o contrasena incorrectos` |
| Rol sin permiso o comanda de otro cliente | 403 | `No tiene permisos para realizar esta accion` |
| Cóctel, plato, comanda o reserva inexistente | 404 | `Coctel con id 9 no existe` |
| Cóctel sin receta en TheCocktailDB | 404 | `Receta de referencia del coctel con id 1 no existe` |
| Nombre repetido | 409 | `Ya existe un coctel con el nombre Negroni` |
| Segunda calificación del mismo producto | 409 | `Ya calificaste este producto` |
| Cóctel con comandas que se intenta eliminar | 409 | `El coctel tiene comandas registradas. Marquelo como agotado en lugar de eliminarlo` |
| Cóctel con alcohol sin destilado | 422 | `El coctel Negroni requiere especificar la marca o tipo exacto de destilado` |
| Modificador con alcohol en un Mocktail | 422 | `El modificador ... tiene alcohol y no se puede agregar al Mocktail ...` |
| Producto agotado | 422 | `El coctel Pina Colada esta AGOTADO EN BARRA` |
| Cambio de estado que salta un paso | 422 | `Transicion invalida de RECIBIDO a LISTO` |
| Segundo cambio de licor | 422 | `El licor de este coctel ya fue cambiado una vez` |
| Reserva en lunes | 422 | `Los lunes la casa descansa. Elija otro dia` |
| Reserva en fecha pasada | 422 | `La fecha de la reserva ya paso. Elija hoy o un dia proximo` |
| Reserva fuera del horario | 422 | `Las reservas se reciben de 18:00 a 00:00 cada media hora` |
| Cinco intentos de login fallidos | 429 | `Demasiados intentos fallidos. Intente de nuevo en 15 minutos` |
| TheCocktailDB no disponible | 502 | `El servicio de recetas no esta disponible en este momento` |

---

## Diagramas de secuencia

### Crear una comanda

```mermaid
sequenceDiagram
    participant W as Sitio web
    participant C as PedidoController
    participant S as PedidoServiceImpl
    participant V as PedidoValidator
    participant R as PedidoRepository
    participant E as EventoComandaService
    participant M as MongoDB

    W->>C: POST /api/v1/pedidos (mesa e ítems)
    C->>S: crear(pedido, usuario)
    loop Cada ítem
        S->>V: validarCoctelDisponible y validarTrazabilidad
        S->>V: validarModificador
    end
    alt Alguna regla no se cumple
        V-->>W: 422 con el mensaje de la regla
    else Todo es válido
        S->>R: save(pedido)
        R-->>S: Comanda con id
        S->>E: registrar(CREADA)
        E->>M: Guarda el evento
        S-->>W: 201 con la comanda en RECIBIDO
    end
```

El controller recibe la comanda y la convierte a dominio. El service completa cada ítem con el nombre y el precio del cóctel y valida que esté disponible, que tenga destilado si lleva alcohol y que los modificadores sean válidos. Si todo está bien guarda la comanda en PostgreSQL, registra el evento en MongoDB y responde 201.

### Reservar una mesa

```mermaid
sequenceDiagram
    participant W as Sitio web
    participant C as ReservaController
    participant S as ReservaServiceImpl
    participant V as ReservaValidator
    participant R as ReservaRepository

    W->>C: POST /api/v1/reservas
    C->>C: Bean Validation del DTO
    alt Datos con formato incorrecto
        C-->>W: 400 con el detalle de los campos
    else Formato correcto
        C->>S: crear(reserva, usuario o null)
        S->>V: validarFecha y validarHora
        alt Lunes, fecha pasada u hora fuera del horario
            V-->>W: 422
        else Fecha y hora válidas
            S->>R: existsByCodigo hasta encontrar un código libre
            S->>R: save(reserva CONFIRMADA)
            S-->>W: 201 con el código de la reserva
        end
    end
```

La reserva es pública. Primero se valida el formato de los datos y luego las reglas de la casa con la fecha de Bogotá. Si el usuario inició sesión, la reserva queda asociada a su correo para que aparezca en "Mis reservas".

### Consultar la receta de referencia

```mermaid
sequenceDiagram
    participant W as Sitio web
    participant C as MenuController
    participant S as RecetaExternaServiceImpl
    participant F as CoctelesExternosClient (Feign)
    participant T as TheCocktailDB

    W->>C: GET /api/v1/menu/{id}/receta
    C->>S: buscarPorCoctel(id)
    S->>S: Busca el cóctel en la carta
    S->>F: buscarPorNombre(nombre)
    F->>T: GET /search.php?s=nombre
    alt El proveedor no responde a tiempo
        F-->>S: FeignException
        S-->>W: 502 BV-502
    else Responde
        T-->>F: Lista de bebidas
        alt Ninguna coincide con el nombre
            S-->>W: 404
        else Hay coincidencia
            S-->>W: 200 con ingredientes, vaso y preparación
        end
    end
```

El service busca el cóctel en la carta y le pide a Feign la búsqueda por nombre. Solo acepta la bebida con el mismo nombre, para no mostrar la receta de otro trago parecido. Si TheCocktailDB falla o tarda más de lo permitido, la API responde 502 y el sitio simplemente no muestra la sección.

---

## Pruebas

Se ejecutan 282 pruebas, con una cobertura de JaCoCo del 99 % en instrucciones y del 94 % en ramas.

| Parte | Pruebas |
|---|---|
| Servicios | Creación, actualización, eliminación, búsquedas, reglas de pedidos, login, registro, bloqueo por intentos, registro de eventos, reservas, reseñas, platos y receta externa con el cliente Feign simulado |
| Validadores | Alcohol, Mocktails, cambio de licor, estados, dueño de la comanda, fecha y hora de las reservas y nombres de platos |
| Seguridad | Generación y validación de JWT, filtro JWT, carga de usuarios, respuestas 401 y 403 y login con OAuth2 |
| Utilidades | Normalización de nombres y etiquetas |
| Mappers | Conversión entre DTOs, dominio, entidades y documentos |
| Repositorios | Consultas a la base de datos con H2 |
| Dominio | Totales, subtotales y estados |
| Controladores | Respuestas HTTP y errores |
| Integración | Flujo completo de una comanda con roles, CRUD de un cóctel y de un plato, reserva pública con la agenda del mesero y verificación de seguridad: login, 401 sin token, token inválido y vencido, 403 por rol, HTTP Basic, cabeceras, registro con contraseña cifrada y CORS |

---

## Evidencias

### Swagger

![Swagger UI](docs/evidencias/swagger-ui.png)

### Ejecución de la aplicación

![Aplicación en ejecución](docs/evidencias/ejecucion.png)

### Pruebas y cobertura

![Pruebas ejecutadas](docs/evidencias/pruebas-mvn-test.png)

![Cobertura JaCoCo](docs/evidencias/jacoco.png)

### SonarQube

![SonarQube](docs/evidencias/sonar.png)

### Persistencia en PostgreSQL

Se probó el CRUD de un cóctel desde Swagger y se revisó el resultado directamente en la base de datos.

**Tablas creadas por Hibernate en PostgreSQL**

![Tablas en la base de datos](docs/evidencias/bd-tablas.png)

**Crear un cóctel (201)**

![Crear coctel](docs/evidencias/bd-crud-crear.png)

**Consultar el cóctel creado (200)**

![Consultar coctel](docs/evidencias/bd-crud-consultar.png)

**Registro guardado en la tabla `coctel`**

![Coctel guardado en la base de datos](docs/evidencias/bd-crud-coctel.png)

**Eliminar el cóctel (204)**

![Eliminar coctel](docs/evidencias/bd-crud-eliminar.png)

**Los datos se conservan al reiniciar la aplicación**

Al volver a iniciar, la aplicación detecta que la base ya tiene información y no vuelve a cargar la carta inicial. La consulta de cócteles devuelve los mismos registros.

![Reinicio de la aplicacion](docs/evidencias/bd-reinicio-log.png)

![Cocteles despues del reinicio](docs/evidencias/bd-reinicio-listado.png)

### Persistencia en MongoDB

Al crear una comanda y cambiar su estado, los eventos quedan guardados en la colección `eventos_comanda`.

![Eventos en MongoDB](docs/evidencias/bd-mongo-eventos.png)

### Pruebas funcionales

| Funcionalidad | Evidencia |
|---|---|
| Crear cóctel | ![Cocteles](docs/evidencias/Solicitud%20POST%20crear%20coctel.png) |
| Carta con productos agotados | ![Menu](docs/evidencias/func-menu.png) |
| Modificadores bloqueados en Mocktail | ![Mocktail](docs/evidencias/func-mocktail.png) |
| Crear comanda | ![Comanda](docs/evidencias/func-pedido.png) |
| Trazabilidad del alcohol | ![Trazabilidad](docs/evidencias/func-trazabilidad.png) |
| Cambio de estado en el KDS | ![KDS](docs/evidencias/func-kds.png) |
| Cambio de licor una sola vez | ![Cambio de licor](docs/evidencias/func-cambio-licor.png) |
| Validación del precio | ![Validacion](docs/evidencias/func-validacion.png) |

---

## Fuera de alcance

- Pagos y facturación electrónica.
- Envío de correos de confirmación de las reservas.
- Pedidos de la cocina (las comandas son solo de la barra).
- Orquestación con Kubernetes.

---

## Bibliografía

- Spring. *Spring Boot Reference Documentation*. https://docs.spring.io/spring-boot/
- Spring. *Spring Security Reference*. https://docs.spring.io/spring-security/reference/
- Spring. *Spring Data JPA* y *Spring Data MongoDB*. https://spring.io/projects/spring-data
- Spring. *Spring Cloud OpenFeign*. https://docs.spring.io/spring-cloud-openfeign/reference/
- TheCocktailDB. *Free Cocktail API*. https://www.thecocktaildb.com/api.php
- OWASP Foundation. *OWASP Top 10*. https://owasp.org/www-project-top-ten/
- JWT. *Introduction to JSON Web Tokens*. https://jwt.io/introduction
- Docker. *Docker Documentation*. https://docs.docker.com/
- GitHub. *GitHub Actions Documentation*. https://docs.github.com/actions
- Microsoft. *Azure App Service Documentation*. https://learn.microsoft.com/azure/app-service/
- Material de clase DOSW 1, semanas 7 a 11. Escuela Colombiana de Ingeniería Julio Garavito.
