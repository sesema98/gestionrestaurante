# 🧩 Proyecto: Gestión de Restaurante – Módulo de Clientes y Mesas

**Autor:** Sergio Sebastián  
**Framework:** Spring Boot 3.3+  
**Template Engine:** Thymeleaf  
**Seguridad:** Spring Security  
**Base de Datos:** MySQL  
**Objetivo:** Gestionar clientes, mesas y su relación en un entorno visual, optimizando la atención de un restaurante.

---

## 📁 Estructura de Paquetes

```
com.serva.gestionrestaurante
├── controllers
│   ├── ClienteController.java
│   └── MesaController.java
│
├── entities
│   ├── Cliente.java
│   └── Mesa.java
│
├── repositories
│   ├── ClienteRepository.java
│   └── MesaRepository.java
│
├── services
│   ├── ClienteService.java
│   ├── MesaService.java
│   ├── impl
│   │   ├── ClienteServiceImpl.java
│   │   └── MesaServiceImpl.java
│
└── GestionRestauranteApplication.java
```

---

## 🧠 Entidades (JPA)

### 🧍‍♂️ Cliente.java
Representa un cliente del restaurante.  
Tiene una relación **ManyToOne** con `Mesa`.

```java
@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;
}
```

### 🍽️ Mesa.java
Representa una mesa física del restaurante.

```java
@Entity
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int numero;
    private int capacidad;
    private String estado; // Disponible, Ocupada, Reservada
    private String ubicacion;
}
```

---

## 🧩 Servicios

Interfaz base:

```java
public interface MesaService {
    List<Mesa> listar();
    Optional<Mesa> buscar(Long id);
    Mesa guardar(Mesa mesa);
    void eliminar(Long id);
}
```

---

## 🧭 Controladores

### ClienteController.java
- Ruta base: `/clientes`
- Funcionalidades:
  - Listar clientes
  - Registrar y editar clientes
  - Asignar mesas disponibles

### MesaController.java
- Ruta base: `/mesas`
- Funcionalidades:
  - Listar y editar mesas
  - Visualizar el mapa de mesas (`/mesas/mapa`)

---

## 🧱 Repositorios

```java
public interface MesaRepository extends JpaRepository<Mesa, Long> {}
public interface ClienteRepository extends JpaRepository<Cliente, Long> {}
```

---

## 🌐 Vistas (Thymeleaf)

- `clientes/listar.html` → Lista de clientes con su mesa asignada  
- `clientes/form.html` → Formulario con selector de mesa  
- `mesas/listar.html` → Tabla tradicional  
- `mesas/mapa.html` → Mapa visual tipo salón

```html
<td th:text="${c.mesa != null ? 'Mesa ' + c.mesa.numero : '—'}"></td>
```

---

## 🔄 Flujo General

1. El mozo registra un cliente (opcional).  
2. Se asigna una mesa disponible.  
3. El estado pasa a **"Ocupada"**.  
4. Al liberar, vuelve a **"Disponible"**.

---

## 🎯 Resultados

- Control visual de mesas ocupadas/disponibles  
- Registro de clientes frecuentes  
- Interfaz moderna, simple y funcional

---

## ⚙️ Configuración (application.properties)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_restaurante
spring.datasource.username=root
spring.datasource.password=123456
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8086
spring.thymeleaf.cache=false
```

---

## 🔒 Seguridad

Uso de `@PreAuthorize("hasRole('ADMIN')")`  
- Solo ADMIN puede crear, editar o eliminar.  
- Usuarios pueden visualizar mesas y clientes.

---

## 🪄 Resumen rápido

| Módulo  | Propósito                   | Controlador        | Vista clave           |
|----------|-----------------------------|--------------------|------------------------|
| Clientes | Registro y edición clientes | ClienteController  | clientes/listar.html  |
| Mesas    | Gestión visual de mesas     | MesaController     | mesas/mapa.html       |

---

## 🚀 Futuras mejoras

- Actualización en tiempo real (WebSocket/AJAX)  
- Módulo de pedidos por mesa  
- Reportes de clientes frecuentes  
- Control de reservas anticipadas

---

**Conclusión:**  
El sistema ofrece una arquitectura limpia, escalable y visualmente intuitiva.  
Ideal para gestionar clientes y mesas de manera eficiente en un entorno de restaurante.
