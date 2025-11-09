# Gestión de Restaurante – Spring Boot + Thymeleaf

Aplicación web que simula el flujo de salón de un restaurante: administración de mesas, clientes, menú e insumos bajo un dashboard moderno verde/blanco con componentes Bootstrap 5.3 y cartas interactivas. Está pensada para tres roles principales (ADMIN, MOZO y CLIENTE) con permisos diferenciados sobre las mismas pantallas.

---

## 🧩 Arquitectura general

| Capa | Tecnologías | Responsabilidad |
|------|-------------|-----------------|
| Presentación | Thymeleaf 3.1, Bootstrap 5.3, JavaScript modular (fetch/AJAX) | Render dinámico del dashboard, formularios y tablas inline. |
| Negocio | Spring Boot 3.5, Spring MVC, Services & DTOs | Reglas de asignación de mesas, validaciones de DNI, asociación de insumos a platos. |
| Persistencia | Spring Data JPA, Hibernate 6.6, MySQL 8 | CRUD transaccional y consultas específicas (buscar por DNI, mesas, insumos). |
| Seguridad | Spring Security 6.5, BCryptPasswordEncoder | Roles, login personalizado, protección CSRF, endpoints REST seguros. |

---

## 👥 Roles y simulación de usuarios

Usuarios precargados en `SecurityConfig` (se crean en memoria y sus contraseñas se encriptan con BCrypt):

| Usuario | Password | Roles | Uso principal |
|---------|----------|-------|---------------|
| `admin` | `12345`  | `ROLE_ADMIN` | Control total: clientes, mesas, menú, insumos, endpoint REST. |
| `mozo`  | `12345`  | `ROLE_MOZO`  | Gestión diaria de salón: crear clientes, asignar/liberar mesas, cambiar estados desde el dashboard. |
| `cliente@demo.com` | `12345` | `ROLE_CLIENTE` | Solo visualiza su reserva y el menú. |

**Encriptación:** cada contraseña se registra con `BCryptPasswordEncoder`, lo que genera un hash diferente en cada arranque (salt aleatorio). La capa de login nunca conoce el password plano después del primer encode.

---

## 🙋‍♂️ Flujo por rol

### Rol MOZO / ADMIN (dashboard interactivo)
1. **Mapa de mesas**: al hacer clic, se abre un modal con estado, cliente asignado y botones:
   - Asignar cliente (form modal o redirección al formulario clásico).
   - Editar datos del cliente existente.
   - Liberar mesa (vía `/api/mesas/{id}/liberar`).
   - Cambiar estado desde un dropdown inline que invoca `/api/mesas/{id}/estado`.
2. **Tabla lateral de clientes**: permite filtrar, editar/eliminar y ver su mesa. Si cambia el estado desde la tabla, se actualiza el mapa sin recargar.
3. **Validaciones integradas**:
   - DNI único (check previo a guardar, evita el 500).
   - DNI exactamente de 8 dígitos (rechaza entradas largas y muestra mensaje en el formulario).
   - Una mesa solo se asigna si está disponible (de lo contrario, se muestra error inline).

### Rol CLIENTE
- Ingresa con su correo y solo ve su reserva (si tiene).
- Puede recorrer el menú (platos + insumos) sin acceso a botones de edición.

### Rol ADMIN
Incluye todo lo anterior y además:
- Gestiona el módulo de **Menú** (platos/bebidas con precios y descripción).
- Gestiona **Insumos** (stock, stock mínimo y alertas visuales).
- Visualiza enlaces adicionales en el navbar (Clientes, Mesas, Menú, Insumos).

---

## 🗃️ Módulos actuales

### 1. Clientes & Mesas
- Entidades `Cliente` y `Mesa` (JPA) con relaciones ManyToOne.
- Controladores MVC + REST (`ClienteController`, `MesaController`, `DashboardRestController`, `MesaRestController`).
- Servicios (`ClienteService`, `MesaService`) con lógica de validación (DNI, estado de mesa, liberación automática).
- Vistas: `dashboard.html`, `clientes/listar|form.html`, `mesas/listar|form.html`.
- JavaScript: `static/js/dashboard.js` maneja fetch, modales, dropdowns y actualización de estadísticas.

### 2. Menú e Insumos
- `Plato` ↔ `Insumo` (ManyToMany) con validaciones de precio, stock y mínimo.
- Vistas: `menu/listar|form.html`, `insumos/listar|form.html` (mismo look & feel de las demás).
- Servicios/repositories dedicados (`PlatoService`, `InsumoService`, `PlatoRepository`, `InsumoRepository`).
- Gestión de stock: se valida al guardar insumos y se listan badges “Stock bajo” cuando el stock ≤ mínimo.

---

## 🔐 Seguridad y rutas

| Ruta | Rol requerido |
|------|---------------|
| `/` (dashboard), `/clientes/**`, `/mesas/**`, `/api/mesas/**`, `/api/clientes/**` | ADMIN o MOZO |
| `/menu` (GET) | ADMIN, MOZO, CLIENTE |
| `/menu/**` (POST/PUT/DELETE) | ADMIN |
| `/insumos/**` | ADMIN |
| `/login`, `/error/**`, `/css/**`, `/js/**` | Público |

Spring Security protege también los endpoints REST. Los fetch del dashboard incluyen token CSRF gracias a las meta tags (`<meta name="_csrf"...>`), y cada petición aplica el header correcto.

---

## ⚙️ Configuración

1. Ajusta `src/main/resources/application.properties` con tus credenciales MySQL.
2. Ejecuta migraciones automáticas (Hibernate `ddl-auto=update`) la primera vez.
3. Levanta la app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Navega a `http://localhost:8086/` e inicia sesión con uno de los usuarios demo.

---

## 📈 Extensiones futuras (roadmap)

- Módulo de pedidos/facturación (estados, facturas, métodos de pago).
- Vista “Cocina” y reportes de ventas.
- Gestión de proveedores/compras.

La base actual ya expone servicios REST, validación consistente y un dashboard listo para continuar creciendo sin perder el estilo ni la experiencia de usuario.
