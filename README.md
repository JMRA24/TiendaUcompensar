# TiendaVirtual Android

Aplicación Android nativa desarrollada como proyecto académico de Ingeniería de Software en UCompensar. Implementa una tienda virtual con tres roles de usuario (comprador, vendedor y administrador), navegación por rol, autenticación simulada y datos mock locales, usando Kotlin y vistas XML con Material Design 3.

---

## Características

- Autenticación con login, biometría simulada, inicio con Google simulado y recuperación de contraseña por OTP mock.
- Navegación diferenciada por rol:
  - Comprador con `BottomNavigationView`.
  - Vendedor con `DrawerLayout` y `NavigationView`.
  - Administrador con `DrawerLayout` y `NavigationView`.
- Catálogo de productos con filtros por categoría, detalle de producto y carrito de compras.
- Checkout con selección de dirección y método de pago.
- Panel de vendedor con KPIs, gestión de productos (CRUD), pedidos y perfil.
- Panel de administrador con gestión de usuarios (CRUD), productos, métricas y reporte de ventas.
- Datos simulados centralizados en `MockRepository`.
- Recursos completamente centralizados en `strings.xml`, `colors.xml`, `dimens.xml`, `styles.xml` y `themes.xml`. Sin valores hardcodeados.

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| XML Views | Construcción de interfaces |
| ViewBinding | Acceso seguro a vistas |
| Material Design 3 | Componentes UI (botones, cards, inputs, chips) |
| Navigation Component | Graphs de navegación por rol |
| RecyclerView | Listas de productos, usuarios, pedidos y categorías |
| ConstraintLayout | Layout principal de pantallas |
| MockRepository | Datos locales simulados sin backend |

---

## Arquitectura

El proyecto está organizado por responsabilidad y rol de usuario, siguiendo un enfoque MVVM simplificado sin capa de backend:

- `auth/` — pantallas de autenticación (Splash, Login, Recuperar contraseña).
- `buyer/` — flujo completo del comprador.
- `seller/` — flujo completo del vendedor.
- `admin/` — flujo completo del administrador.
- `adapters/` — adapters RecyclerView reutilizables.
- `models/` — data classes del dominio (User, Product, Order, Category).
- `utils/` — MockRepository, CartManager y utilidades.
- `res/navigation/` — navigation graphs por rol.
- `res/menu/` — menús de bottom navigation y drawer.
- `res/layout/` — activities, fragments e ítems de listas.
- `res/values/` — recursos centralizados (colors, strings, dimens, styles, themes).

---

## Estructura de carpetas

```
app/src/main/
├── AndroidManifest.xml
├── java/com/project/store/
│   ├── adapters/
│   │   ├── ProductAdapter.kt
│   │   ├── OrderAdapter.kt
│   │   ├── UserAdapter.kt
│   │   ├── CategoryAdapter.kt
│   │   ├── CartAdapter.kt
│   │   └── OrderHistoryAdapter.kt
│   ├── admin/
│   │   ├── AdminDashboardFragment.kt
│   │   ├── UserListFragment.kt
│   │   ├── CreateUserFragment.kt
│   │   ├── EditUserFragment.kt
│   │   ├── AdminProductsFragment.kt
│   │   └── SalesReportFragment.kt
│   ├── auth/
│   │   ├── SplashActivity.kt
│   │   ├── LoginActivity.kt
│   │   └── ForgotPasswordActivity.kt
│   ├── buyer/
│   │   ├── HomeFragment.kt
│   │   ├── CatalogFragment.kt
│   │   ├── ProductDetailFragment.kt
│   │   ├── CartFragment.kt
│   │   ├── CheckoutFragment.kt
│   │   └── ProfileFragment.kt
│   ├── models/
│   │   ├── User.kt
│   │   ├── Product.kt
│   │   ├── Order.kt
│   │   └── Category.kt
│   ├── seller/
│   │   ├── SellerDashboardFragment.kt
│   │   ├── ProductListFragment.kt
│   │   ├── CreateProductFragment.kt
│   │   ├── EditProductFragment.kt
│   │   ├── SellerOrdersFragment.kt
│   │   └── SellerProfileFragment.kt
│   └── utils/
│       ├── MockRepository.kt
│       └── CartManager.kt
└── res/
    ├── drawable/
    ├── layout/
    ├── menu/
    ├── navigation/
    │   ├── nav_buyer.xml
    │   ├── nav_seller.xml
    │   └── nav_admin.xml
    └── values/
        ├── colors.xml
        ├── strings.xml
        ├── dimens.xml
        ├── styles.xml
        └── themes.xml
```

---

## Pantallas por rol

| Rol | Pantalla | Archivo |
|---|---|---|
| Auth | Splash | `SplashActivity.kt` |
| Auth | Login | `LoginActivity.kt` |
| Auth | Recuperar contraseña | `ForgotPasswordActivity.kt` |
| Comprador | Inicio | `HomeFragment.kt` |
| Comprador | Catálogo | `CatalogFragment.kt` |
| Comprador | Detalle producto | `ProductDetailFragment.kt` |
| Comprador | Carrito | `CartFragment.kt` |
| Comprador | Checkout | `CheckoutFragment.kt` |
| Comprador | Perfil | `ProfileFragment.kt` |
| Vendedor | Dashboard | `SellerDashboardFragment.kt` |
| Vendedor | Mis productos | `ProductListFragment.kt` |
| Vendedor | Crear producto | `CreateProductFragment.kt` |
| Vendedor | Editar producto | `EditProductFragment.kt` |
| Vendedor | Pedidos | `SellerOrdersFragment.kt` |
| Vendedor | Perfil | `SellerProfileFragment.kt` |
| Administrador | Dashboard | `AdminDashboardFragment.kt` |
| Administrador | Usuarios | `UserListFragment.kt` |
| Administrador | Crear usuario | `CreateUserFragment.kt` |
| Administrador | Editar usuario | `EditUserFragment.kt` |
| Administrador | Productos | `AdminProductsFragment.kt` |
| Administrador | Reportes | `SalesReportFragment.kt` |

---

## Credenciales mock

Todos los usuarios están definidos en `MockRepository.kt`.

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `laura.admin@tienda.com` | `admin123` |
| Vendedor | `andres.seller@tienda.com` | `seller123` |
| Comprador | `valentina.buyer@tienda.com` | `buyer123` |

La biometría simulada inicia sesión como comprador. El OTP simulado para recuperación de contraseña es `123456`.

---

## Instrucciones de ejecución

1. Clonar el repositorio:
```bash
git clone https://github.com/TU_USUARIO/TiendaVirtual.git
```

2. Abrir la carpeta del proyecto en Android Studio.

3. Esperar que Gradle sincronice automáticamente.

4. Ejecutar en un emulador o dispositivo físico con Android 8.0 (API 26) o superior:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

En Windows:
```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

---

## Datos mock

`MockRepository.kt` incluye:

- 9 usuarios: 3 administradores, 3 vendedores y 3 compradores.
- 8 productos con nombre, descripción, precio en COP, stock y categoría.
- 3 categorías (Tecnología, Hogar, Moda).
- 5 órdenes con estados variados (Entregado, Pendiente, En proceso).

`CartManager.kt` mantiene el carrito simulado durante la sesión del comprador.
