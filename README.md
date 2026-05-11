# TiendaVirtual Android

Aplicacion Android academica para una tienda virtual con tres perfiles de uso:
comprador, vendedor y administrador. El proyecto implementa autenticacion
simulada, navegacion por rol, datos mock locales, pantallas CRUD y reportes
visuales simples usando Kotlin y vistas XML.

> Estado actual: el codigo fuente de la app esta creado bajo `app/src/main`,
> pero el repositorio aun no incluye archivos Gradle (`build.gradle`,
> `settings.gradle` o `gradlew`). Para ejecutarlo es necesario abrirlo como
> modulo Android o agregar la configuracion Gradle correspondiente.

## Caracteristicas

- Autenticacion con login, biometria simulada y recuperacion por OTP mock.
- Navegacion por rol:
  - Comprador con `BottomNavigationView`.
  - Vendedor con `DrawerLayout` y `NavigationView`.
  - Administrador con `DrawerLayout` y `NavigationView`.
- Catalogo de productos con filtros, detalle y carrito mock.
- Panel de vendedor con KPIs, productos, pedidos y perfil.
- Panel de administrador con usuarios, productos, metricas y reporte de ventas.
- Datos simulados centralizados en `MockRepository`.
- Recursos separados en `strings.xml`, `colors.xml`, `dimens.xml`, `styles.xml`
  y `themes.xml`.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Kotlin | Lenguaje principal |
| XML Views | Construccion de interfaces |
| ViewBinding | Acceso seguro a vistas |
| Material Design 3 | Componentes UI |
| Android Navigation | Graphs por rol |
| RecyclerView | Listas de productos, usuarios, pedidos y categorias |
| MockRepository | Datos locales simulados |

## Arquitectura

El proyecto esta organizado por responsabilidad y rol de usuario:

- `auth/`: pantallas de autenticacion.
- `buyer/`: flujo de comprador.
- `seller/`: flujo de vendedor.
- `admin/`: flujo de administrador.
- `adapters/`: adapters RecyclerView reutilizables.
- `models/`: data classes del dominio.
- `utils/`: repositorios y utilidades mock.
- `res/navigation/`: navigation graphs por rol.
- `res/menu/`: menus de bottom navigation y drawer.
- `res/layout/`: activities, fragments e items de listas.

La app usa un enfoque tipo MVVM simplificado sin capa de backend. Las pantallas
leen datos desde `MockRepository` y algunos estados temporales desde utilidades
como `CartManager`.

## Estructura de carpetas

```text
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
│   ├── auth/
│   ├── buyer/
│   ├── models/
│   ├── seller/
│   └── utils/
└── res/
    ├── drawable/
    ├── layout/
    ├── menu/
    ├── navigation/
    └── values/
```

## Pantallas por rol

| Rol | Pantalla | Archivo principal |
| --- | --- | --- |
| Auth | Splash | `SplashActivity.kt` |
| Auth | Login | `LoginActivity.kt` |
| Auth | Recuperar contrasena | `ForgotPasswordActivity.kt` |
| Comprador | Inicio | `HomeFragment.kt` |
| Comprador | Catalogo | `CatalogFragment.kt` |
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

## Credenciales mock

Los usuarios estan definidos en `MockRepository.kt`.

| Rol | Correo de ejemplo | Contrasena |
| --- | --- | --- |
| Administrador | `laura.admin@tienda.com` | `admin123` |
| Vendedor | `andres.seller@tienda.com` | `seller123` |
| Comprador | `valentina.buyer@tienda.com` | `buyer123` |

La biometria simulada inicia sesion como comprador. El OTP simulado para
recuperacion de contrasena es `123456`.

## Instrucciones de ejecucion

1. Abrir el proyecto en Android Studio.
2. Verificar que el modulo `app` tenga configuracion Gradle Android.
3. Habilitar ViewBinding en `build.gradle` del modulo:

```gradle
android {
    buildFeatures {
        viewBinding true
    }
}
```

4. Sincronizar Gradle.
5. Ejecutar en un emulador o dispositivo con Android 8.0 (API 26) o superior.

Cuando el proyecto incluya wrapper Gradle, se podra ejecutar desde terminal:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

En Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Capturas placeholder

| Splash | Login | Comprador |
| --- | --- | --- |
| `docs/screenshots/splash.png` | `docs/screenshots/login.png` | `docs/screenshots/buyer-home.png` |

| Vendedor | Administrador | Reporte |
| --- | --- | --- |
| `docs/screenshots/seller-dashboard.png` | `docs/screenshots/admin-dashboard.png` | `docs/screenshots/sales-report.png` |

## Datos mock

`MockRepository.kt` incluye:

- 9 usuarios: 3 administradores, 3 vendedores y 3 compradores.
- 8 productos.
- 3 categorias.
- 5 ordenes.

`CartManager.kt` mantiene un carrito simulado para el flujo de comprador.

## Notas de desarrollo

- No hay backend ni persistencia local permanente.
- Las operaciones de crear, editar y eliminar son simuladas con validaciones y
  mensajes de confirmacion.
- Las imagenes de producto usan un vector placeholder.
- Los reportes y graficos son visualizaciones simples basadas en datos mock.

## Licencia

Proyecto academico. Ajusta esta seccion segun los requisitos de tu institucion
o del repositorio.
