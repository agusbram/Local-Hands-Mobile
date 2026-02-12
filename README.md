# ✨ Manos Locales - Local Hands App ✨

<div align="center">
  <img src="app/src/main/res/drawable/localhandslogo.png" alt="Logo Manos Locales" width="200"/>

  <p align="center">
    <em>🌱 Conectando comunidades con productores locales 🛍️</em>
  </p>

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-2.7.2-brightgreen?style=for-the-badge)](https://developer.android.com/training/data-storage/room)
[![Hilt](https://img.shields.io/badge/Hilt-2.51.1-orange?style=for-the-badge)](https://dagger.dev/hilt/)
[![API Level](https://img.shields.io/badge/API-26%2B-blue?style=for-the-badge)](https://developer.android.com/studio/releases/platforms)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

</div>

---

## 📑 Tabla de Contenidos

- [📖 Descripción](#-descripción)
- [✨ Características Principales](#-características-principales)
- [🛠️ Stack Tecnológico](#️-stack-tecnológico)
- [🏗️ Arquitectura](#️-arquitectura)
- [📋 Requisitos Previos](#-requisitos-previos)
- [📥 Instalación y Configuración](#-instalación-y-configuración)
- [📂 Estructura del Proyecto](#-estructura-del-proyecto)
- [🗄️ Esquema de Base de Datos](#️-esquema-de-base-de-datos)
- [🌐 API REST Endpoints](#-api-rest-endpoints)
- [🔐 Características Avanzadas](#-características-avanzadas)
- [🧪 Testing](#-testing)
- [📱 Demo](#-demo)
- [💻 Desarrollo](#-desarrollo)
- [🗺️ Roadmap](#️-roadmap)
- [❓ FAQ](#-faq)
- [🤝 Contribuir](#-contribuir)
- [📄 Licencia](#-licencia)
- [✉️ Contacto](#️-contacto)

---

## 📖 Descripción

**Manos Locales** es una aplicación móvil Android nativa desarrollada para la materia de **Tecnologías Móviles** de la carrera de **Ingeniería en Informática**. 

La aplicación está construida íntegramente con tecnologías modernas de Android utilizando **Jetpack Compose**, **Room**, **Hilt**, y **MVVM**, con el objetivo principal de **conectar a usuarios con productores y emprendedores locales**, fomentando el consumo regional, el contacto directo y la promoción de productos auténticos.

### 🎯 Objetivo Principal

Crear un marketplace local donde:
- 👥 Los **usuarios** pueden explorar y descubrir productos de emprendedores locales
- 🏪 Los **vendedores** pueden promocionar sus productos y emprendimientos
- 📍 Se facilita el **contacto directo** mediante ubicaciones en mapas
- ⭐ Se pueden **guardar favoritos** y recibir notificaciones de novedades
- 🔄 La información se **sincroniza** con un backend mediante API REST

---

## ✨ Características Principales

### 🔐 Autenticación y Seguridad
- ✅ **Registro de usuarios** con validación de email
- ✅ **Inicio de sesión** seguro con BCrypt para hash de contraseñas
- ✅ **Verificación de email** mediante códigos temporales
- ✅ **Recuperación de contraseña** con códigos de reseteo
- ✅ **Gestión de sesiones** persistente con DataStore
- ✅ **Conversión a vendedor** para usuarios que quieran vender productos

### 🛍️ Gestión de Productos
- ✅ **Catálogo completo** de productos con imágenes (1-10 por producto)
- ✅ **Búsqueda avanzada** por nombre, categoría o vendedor
- ✅ **Filtrado por categorías** dinámicas
- ✅ **Detalles completos** con nombre, descripción, precio, ubicación
- ✅ **Sincronización** automática con API REST backend
- ✅ **Modo offline** con persistencia local mediante Room
- ✅ **CRUD completo** para vendedores (crear, editar, eliminar productos)

### 🏪 Vendedores y Emprendedores
- ✅ **Perfiles de vendedores** con información de contacto
- ✅ **Vista de productos por vendedor**
- ✅ **Ubicación en mapa** de cada vendedor
- ✅ **Información de contacto** (teléfono, WhatsApp, email, redes sociales)
- ✅ **Conversión dinámica** de usuario a vendedor

### ⭐ Sistema de Favoritos
- ✅ **Marcar productos como favoritos**
- ✅ **Lista personalizada de favoritos**
- ✅ **Notificaciones** a usuarios interesados cuando hay cambios
- ✅ **Sincronización** de favoritos con backend

### 🗺️ Mapas e Integración
- ✅ **Google Maps** integrado para mostrar ubicaciones
- ✅ **Selector de ubicación** al crear/editar productos
- ✅ **Mapa de productos cercanos**
- ✅ **Navegación a ubicaciones** de vendedores

### 🔄 Compartir y Comunicación
- ✅ **Compartir productos** por WhatsApp, redes sociales, o cualquier app
- ✅ **Envío de emails** de soporte a desarrolladores
- ✅ **Intents nativos** de Android para comunicación

### ⚙️ Configuraciones
- ✅ **Ajustes de preferencias** de usuario
- ✅ **Gestión de perfil** con edición de datos
- ✅ **Cierre de sesión** seguro
- ✅ **Persistencia de configuraciones** con DataStore

---

## 🛠️ Stack Tecnológico

### 📱 Core
- **Lenguaje**: Kotlin
- **SDK Mínimo**: API 26 (Android 8.0 Oreo)
- **SDK Objetivo**: API 36
- **Compilación**: SDK 36
- **Java**: JDK 17

### 🎨 UI Framework
- **Jetpack Compose**: 100% UI declarativa
- **Material Design 3**: Componentes modernos de UI
- **Coil**: v2.7.0 - Carga de imágenes
- **Navigation Compose**: Navegación entre pantallas
- **Icons Extended**: Iconografía completa

### 🗄️ Persistencia y Data
- **Room**: v2.7.2 - Base de datos local SQLite
  - Room KTX para Coroutines
  - Room Paging para paginación
- **DataStore Preferences**: v1.1.1 - Configuraciones de usuario
- **Type Converters**: Para tipos complejos (listas, enums)

### 🌐 Networking
- **Retrofit**: v2.9.0 - Cliente HTTP
- **Gson Converter**: v2.9.0 - Serialización JSON
- **OkHttp Logging**: v4.11.0 - Logging de red para debugging

### 💉 Inyección de Dependencias
- **Dagger Hilt**: v2.51.1 - DI framework
- **Hilt Navigation Compose**: v1.2.0 - Integración con Compose

### 🗺️ Mapas y Ubicación
- **Google Maps Compose**: Integración de mapas
- **Play Services Maps**: Servicios de ubicación
- **Secrets Gradle Plugin**: v2.0.1 - Gestión segura de API keys

### 🔐 Seguridad
- **BCrypt**: v0.10.2 - Hashing seguro de contraseñas (at.favre.lib)

### 📧 Comunicación
- **Android Mail**: v1.6.7 - Envío de emails
- **Android Activation**: v1.6.7 - Soporte para JavaMail

### 🧪 Testing
- **JUnit**: v4.13.2 - Framework de testing
- **MockK**: v1.13.9 - Mocking para Kotlin
- **Coroutines Test**: v1.8.0 - Testing de coroutines
- **Turbine**: v1.0.0 - Testing de Flows

### 🔧 Build Tools
- **Gradle**: Kotlin DSL
- **KSP**: v2.0.21-1.0.27 - Procesamiento de anotaciones de Kotlin
- **Android Gradle Plugin**: Version Catalog

---

## 🏗️ Arquitectura

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** con **Clean Architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Composables (Screens & Components)                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ▲                                  │
│                          │                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ViewModels (State Management)                       │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Use Cases (Business Logic)                          │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Repositories (Data Coordination)                    │  │
│  │     │                           │                     │  │
│  │     ▼                           ▼                     │  │
│  │  ┌─────────────┐          ┌──────────────┐          │  │
│  │  │ Local (Room)│          │ Remote (API) │          │  │
│  │  │   - DAOs    │          │  - Retrofit  │          │  │
│  │  │  - Entities │          │    - DTOs    │          │  │
│  │  └─────────────┘          └──────────────┘          │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 📐 Principios de Diseño

- ✅ **Separación de Responsabilidades**: Cada capa tiene una única responsabilidad
- ✅ **Inyección de Dependencias**: Con Hilt para gestión automática
- ✅ **Single Source of Truth**: Room como fuente única de verdad
- ✅ **Offline First**: La app funciona sin conexión
- ✅ **Reactive Programming**: Flows para manejo de datos asíncronos
- ✅ **Repository Pattern**: Abstracción de fuentes de datos
- ✅ **State Hoisting**: Estados gestionados en ViewModels

---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- ✅ **Android Studio**: Hedgehog (2023.1.1) o superior
- ✅ **JDK**: OpenJDK 17 o superior
- ✅ **Gradle**: 8.0+ (incluido con el proyecto)
- ✅ **Git**: Para clonar el repositorio
- ✅ **Emulador Android** o dispositivo físico con API 26+

### 🔑 Requisitos de Configuración

Necesitarás obtener:
1. **Google Maps API Key** - [Obtener aquí](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
2. **Credenciales de Email** - Para el servicio de verificación de emails

---

## 📥 Instalación y Configuración

### 1️⃣ Clonar el Repositorio

```bash
git clone https://github.com/MasterxDual/Local-Hands-Mobile.git
cd Local-Hands-Mobile
```

### 2️⃣ Configurar `local.properties`

Crea el archivo `local.properties` en la raíz del proyecto con el siguiente contenido:

```properties
# SDK Location
sdk.dir=/ruta/a/tu/Android/Sdk

# Google Maps API Key
MAPS_API_KEY=tu_google_maps_api_key_aqui

# Email Configuration (opcional - para verificación de emails)
EMAIL_USER=tu_email@gmail.com
EMAIL_PASS=tu_app_password_aqui
```

#### 📧 Configurar Email (Opcional)

Para habilitar la verificación de emails:

1. Usa una cuenta de Gmail
2. Habilita la verificación en 2 pasos
3. Genera una contraseña de aplicación en [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
4. Agrega las credenciales al `local.properties`

### 3️⃣ Sincronizar Proyecto

Abre el proyecto en Android Studio y sincroniza Gradle:

```bash
# O desde Android Studio: File > Sync Project with Gradle Files
./gradlew build
```

### 4️⃣ Ejecutar la Aplicación

#### Opción A: Desde Android Studio
1. Abre el proyecto en Android Studio
2. Selecciona un dispositivo/emulador
3. Click en el botón "Run" ▶️ o presiona `Shift + F10`

#### Opción B: Desde la terminal
```bash
# Instalar en dispositivo conectado
./gradlew installDebug

# O ejecutar directamente
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Estructura del Proyecto

```
Local-Hands-Mobile/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/undef/localhandsbrambillafunes/
│   │   │   │   │
│   │   │   │   ├── 📁 data/              # Capa de Datos
│   │   │   │   │   ├── dao/              # Data Access Objects (Room)
│   │   │   │   │   │   ├── UserDao.kt
│   │   │   │   │   │   ├── ProductDao.kt
│   │   │   │   │   │   ├── SellerDao.kt
│   │   │   │   │   │   └── FavoriteDao.kt
│   │   │   │   │   │
│   │   │   │   │   ├── db/               # Base de datos Room
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   └── Converters.kt
│   │   │   │   │   │
│   │   │   │   │   ├── entity/           # Entidades de Room
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── Product.kt
│   │   │   │   │   │   ├── Seller.kt
│   │   │   │   │   │   ├── Favorite.kt
│   │   │   │   │   │   └── UserRole.kt
│   │   │   │   │   │
│   │   │   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   │   │   ├── ProductCreateDTO.kt
│   │   │   │   │   │   └── SellerPatchDTO.kt
│   │   │   │   │   │
│   │   │   │   │   ├── model/            # Modelos de dominio
│   │   │   │   │   │   ├── Category.kt
│   │   │   │   │   │   ├── ProductProvider.kt
│   │   │   │   │   │   ├── ProductWithLocation.kt
│   │   │   │   │   │   └── ...
│   │   │   │   │   │
│   │   │   │   │   ├── remote/           # API y networking
│   │   │   │   │   │   ├── ApiService.kt
│   │   │   │   │   │   ├── ApiModule.kt
│   │   │   │   │   │   └── IntTypeAdapter.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/       # Repositorios
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   ├── ProductRepository.kt
│   │   │   │   │   │   ├── SellerRepository.kt
│   │   │   │   │   │   ├── FavoriteRepository.kt
│   │   │   │   │   │   ├── UserRepository.kt
│   │   │   │   │   │   └── UserPreferencesRepository.kt
│   │   │   │   │   │
│   │   │   │   │   └── exception/        # Excepciones personalizadas
│   │   │   │   │       └── NotAuthenticatedException.kt
│   │   │   │   │
│   │   │   │   ├── 📁 di/                # Inyección de Dependencias
│   │   │   │   │   └── DatabaseModule.kt
│   │   │   │   │
│   │   │   │   ├── 📁 service/           # Servicios
│   │   │   │   │   └── EmailService.kt
│   │   │   │   │
│   │   │   │   ├── 📁 ui/                # Capa de UI
│   │   │   │   │   │
│   │   │   │   │   ├── components/       # Componentes reutilizables
│   │   │   │   │   │   ├── CommonButton.kt
│   │   │   │   │   │   ├── CommonTextField.kt
│   │   │   │   │   │   ├── PasswordField.kt
│   │   │   │   │   │   ├── LoadingDialog.kt
│   │   │   │   │   │   ├── ErrorDialog.kt
│   │   │   │   │   │   ├── TopAppBar.kt
│   │   │   │   │   │   ├── ProductListItem.kt
│   │   │   │   │   │   ├── EditImageList.kt
│   │   │   │   │   │   ├── LocationMapSelector.kt
│   │   │   │   │   │   └── SellerConversionHandler.kt
│   │   │   │   │   │
│   │   │   │   │   ├── navigation/       # Configuración de navegación
│   │   │   │   │   │   ├── Navigation.kt
│   │   │   │   │   │   └── AppScreens.kt
│   │   │   │   │   │
│   │   │   │   │   ├── screens/          # Pantallas
│   │   │   │   │   │   ├── splash/
│   │   │   │   │   │   │   └── SplashScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── auth/
│   │   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   │   ├── RegisterScreen.kt
│   │   │   │   │   │   │   └── ForgotPasswordScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   └── components/
│   │   │   │   │   │   │       ├── ProductCard.kt
│   │   │   │   │   │   │       ├── CategoryChips.kt
│   │   │   │   │   │   │       ├── CategoryScreen.kt
│   │   │   │   │   │   │       └── SearchBarScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── productdetail/
│   │   │   │   │   │   │   └── ProductDetailScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── category/
│   │   │   │   │   │   │   └── ProductsByCategoryScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── entrepreneur/
│   │   │   │   │   │   │   ├── SellScreen.kt
│   │   │   │   │   │   │   ├── EditProductScreen.kt
│   │   │   │   │   │   │   └── ProductOwnerDetailScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── favorites/
│   │   │   │   │   │   │   └── FavoritesScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── profile/
│   │   │   │   │   │   │   └── ProfileScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── settings/
│   │   │   │   │   │   │   └── SettingsScreen.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── map/
│   │   │   │   │   │       ├── MapScreen.kt
│   │   │   │   │   │       └── LocationPickerScreen.kt
│   │   │   │   │   │
│   │   │   │   │   ├── theme/            # Temas y estilos
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Type.kt
│   │   │   │   │   │   └── Theme.kt
│   │   │   │   │   │
│   │   │   │   │   └── viewmodel/        # ViewModels
│   │   │   │   │       ├── auth/
│   │   │   │   │       │   ├── LoginViewModel.kt
│   │   │   │   │       │   ├── RegisterViewModel.kt
│   │   │   │   │       │   └── ForgotPasswordViewModel.kt
│   │   │   │   │       │
│   │   │   │   │       ├── products/
│   │   │   │   │       │   └── ProductViewModel.kt
│   │   │   │   │       │
│   │   │   │   │       ├── category/
│   │   │   │   │       │   └── CategoryViewModel.kt
│   │   │   │   │       │
│   │   │   │   │       ├── favorites/
│   │   │   │   │       │   └── FavoriteViewModel.kt
│   │   │   │   │       │
│   │   │   │   │       ├── profile/
│   │   │   │   │       │   └── ProfileViewModel.kt
│   │   │   │   │       │
│   │   │   │   │       ├── sell/
│   │   │   │   │       │   └── SellViewModel.kt
│   │   │   │   │       │
│   │   │   │   │       ├── session/
│   │   │   │   │       │   └── SessionViewModel.kt
│   │   │   │   │       │
│   │   │   │   │       └── settings/
│   │   │   │   │           └── SettingsViewModel.kt
│   │   │   │   │
│   │   │   │   ├── 📁 util/              # Utilidades
│   │   │   │   │   ├── PasswordManager.kt
│   │   │   │   │   ├── FileStorageManager.kt
│   │   │   │   │   ├── PermissionManager.kt
│   │   │   │   │   └── FileUtils.kt
│   │   │   │   │
│   │   │   │   ├── 📁 utils/             # Utilidades adicionales
│   │   │   │   │   └── LocationUtils.kt
│   │   │   │   │
│   │   │   │   ├── LocalHandsApplication.kt
│   │   │   │   └── MainActivity.kt
│   │   │   │
│   │   │   ├── res/                      # Recursos de la app
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap/
│   │   │   │   └── values/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/                         # Tests unitarios
│   │       └── java/com/undef/localhandsbrambillafunes/
│   │           ├── util/
│   │           │   └── PasswordManagerTest.kt
│   │           │
│   │           └── data/repository/
│   │               ├── AuthRepositoryTest.kt
│   │               ├── ProductRepositoryTest.kt
│   │               └── FavoriteRepositoryTest.kt
│   │
│   └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties                      # (No incluido en Git)
├── localhandsappvideo.gif                # Demo GIF
├── TESTING.md                            # Documentación de tests
├── COMO_EJECUTAR_TESTS.md
└── README.md                             # Este archivo
```

### 📝 Descripción de Directorios Clave

- **`data/dao/`**: Interfaces DAO de Room para acceso a base de datos
- **`data/entity/`**: Entidades de Room (tablas de base de datos)
- **`data/repository/`**: Implementación del patrón Repository
- **`data/remote/`**: Configuración de Retrofit y llamadas API
- **`ui/screens/`**: Pantallas de la aplicación en Compose
- **`ui/components/`**: Componentes reutilizables de UI
- **`ui/viewmodel/`**: ViewModels para gestión de estado
- **`di/`**: Módulos de Hilt para inyección de dependencias
- **`util/`**: Clases de utilidad (PasswordManager, FileUtils, etc.)

---

## 🗄️ Esquema de Base de Datos

La aplicación utiliza **Room** con 4 tablas principales:

### 📊 Diagrama ER

```
┌─────────────────┐         ┌──────────────────┐
│   UserEntity    │1      1 │  SellerEntity    │
├─────────────────┤◄────────┤──────────────────┤
│ id (PK)         │         │ id (PK, FK)      │
│ name            │         │ name             │
│ lastName        │         │ lastname         │
│ email (unique)  │         │ email            │
│ password        │         │ phone            │
│ isEmailVerified │         │ whatsapp         │
│ verificationCode│         │ facebook         │
│ resetCode       │         │ instagram        │
│ createdAt       │         │ location         │
└─────────────────┘         │ latitude         │
       │                    │ longitude        │
       │                    │ website          │
       │                    └──────────────────┘
       │                              │
       │                              │1
       │                              │
       │                              │
       │                              │*
       │1                   ┌──────────────────┐
       │                    │  ProductEntity   │
       │                    ├──────────────────┤
       │                    │ id (PK)          │
       │                    │ name             │
       │                    │ description      │
       │                    │ producer         │
       │                    │ category         │
       │                    │ images (List)    │
       │                    │ price            │
       │                    │ location         │
       │                    │ latitude         │
       │                    │ longitude        │
       │                    │ ownerId (FK)     │
       │*                   │ createdAt        │
┌─────────────────┐         └──────────────────┘
│ FavoriteEntity  │                  │*
├─────────────────┤                  │
│ userId (PK, FK) │◄─────────────────┘
│ productId(PK,FK)│
└─────────────────┘
```
---

## 🌐 API REST Endpoints

La aplicación se conecta a un backend mediante Retrofit. Principales endpoints:

### 🔐 Autenticación
```
POST /api/auth/register          - Registrar nuevo usuario
POST /api/auth/login             - Iniciar sesión
POST /api/auth/verify            - Verificar email
POST /api/auth/reset-password    - Resetear contraseña
```

### 🛍️ Productos
```
GET    /api/products              - Obtener todos los productos
GET    /api/products/{id}         - Obtener producto por ID
POST   /api/products              - Crear nuevo producto
PUT    /api/products/{id}         - Actualizar producto
DELETE /api/products/{id}         - Eliminar producto
GET    /api/products/category/{cat} - Filtrar por categoría
GET    /api/products/search?q=    - Buscar productos
```

### 🏪 Vendedores
```
GET    /api/sellers               - Obtener todos los vendedores
GET    /api/sellers/{id}          - Obtener vendedor por ID
POST   /api/sellers               - Convertirse en vendedor
PATCH  /api/sellers/{id}          - Actualizar perfil de vendedor
GET    /api/sellers/{id}/products - Productos de un vendedor
```

### ⭐ Favoritos
```
GET    /api/favorites/{userId}    - Favoritos de un usuario
POST   /api/favorites             - Agregar favorito
DELETE /api/favorites             - Eliminar favorito
```

### 📝 Respuesta JSON Ejemplo

```json
{
  "id": 1,
  "name": "Miel Orgánica",
  "description": "Miel 100% natural de abejas locales",
  "producer": "Juan Pérez",
  "category": "Alimentos",
  "images": [
    "https://example.com/miel1.jpg",
    "https://example.com/miel2.jpg"
  ],
  "price": "$500",
  "location": "San Luis Capital",
  "latitude": -33.3017,
  "longitude": -66.3378,
  "ownerId": 5,
  "createdAt": 1704067200000
}
```

---

## 🔐 Características Avanzadas

### 🛡️ Seguridad con BCrypt

La aplicación utiliza **BCrypt** para el hashing seguro de contraseñas:

```kotlin
// PasswordManager.kt
object PasswordManager {
    // Genera hash BCrypt con salt aleatorio
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults()
            .hashToString(12, password.toCharArray())
    }
    
    // Verifica contraseña contra hash
    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer()
            .verify(password.toCharArray(), hash).verified
    }
}
```

### 📧 Verificación de Email

Sistema de verificación de email con códigos temporales:

1. Usuario se registra
2. Sistema genera código de 6 dígitos
3. Email enviado con código de verificación
4. Usuario ingresa código para activar cuenta
5. Verificación exitosa → acceso completo

```kotlin
// AuthRepository.kt
suspend fun generateVerificationCode(email: String): String {
    val code = Random.nextInt(100000, 999999).toString()
    // Guardar código en BD asociado al email
    // Enviar email con código
    return code
}
```

### 🗺️ Integración con Google Maps

Integración completa de Google Maps para:

- **Mostrar ubicaciones de vendedores** en mapa interactivo
- **Selector de ubicación** al crear/editar productos
- **Navegación** a ubicaciones de vendedores
- **Marcadores personalizados** por categoría

```kotlin
@Composable
fun LocationMapSelector(
    initialLatLng: LatLng,
    onLocationSelected: (LatLng) -> Unit
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = selectedLocation),
            title = "Ubicación seleccionada"
        )
    }
}
```

### 🔄 Conversión de Usuario a Vendedor

Sistema dinámico para que usuarios se conviertan en vendedores:

1. Usuario registrado ingresa a "Convertirse en Vendedor"
2. Completa formulario con datos de contacto y ubicación
3. Sistema crea entrada en `SellerEntity` vinculada a `User.id`
4. Usuario obtiene acceso a funcionalidades de vendedor
5. Puede crear, editar y eliminar sus productos

```kotlin
// SellerRepository.kt
suspend fun convertUserToSeller(
    userId: Int,
    sellerData: SellerPatchDTO
): Result<Seller> {
    // Crear entrada en SellerEntity con userId como PK
    val seller = Seller(
        id = userId,  // Mismo ID que el User
        name = sellerData.name,
        // ... otros campos
    )
    return sellerDao.insertSeller(seller)
}
```

### 🔄 Sistema de Compartir

Funcionalidad nativa de Android para compartir productos:

```kotlin
fun shareProduct(context: Context, product: Product) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, """
            🛍️ Mira este producto en Manos Locales!
            
            ${product.name}
            💰 ${product.price}
            📍 ${product.location}
            
            👤 Vendedor: ${product.producer}
        """.trimIndent())
    }
    context.startActivity(Intent.createChooser(shareIntent, "Compartir producto"))
}
```

### 💾 Persistencia con DataStore

DataStore para configuraciones y preferencias de usuario:

```kotlin
// UserPreferencesRepository.kt
class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore
    
    // Guardar ID de sesión
    suspend fun saveUserId(userId: Int) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }
    
    // Leer ID de sesión
    val userIdFlow: Flow<Int?> = dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }
}
```

---

## 🧪 Testing

La aplicación cuenta con una **suite completa de 52+ tests unitarios** para garantizar la calidad del código.

### 📊 Cobertura de Tests

| Componente | Tests | Descripción |
|------------|-------|-------------|
| **PasswordManager** | 12 | Hashing y verificación de contraseñas |
| **AuthRepository** | 17 | Autenticación, registro, recuperación de contraseña |
| **ProductRepository** | 15+ | CRUD de productos, sincronización con API |
| **FavoriteRepository** | 8 | Gestión de favoritos |

### 🔬 Categorías de Tests

#### ✅ Tests de Seguridad (PasswordManager)
- Generación de hashes BCrypt válidos
- Hashes únicos con salt aleatorio
- Verificación correcta/incorrecta de contraseñas
- Manejo de caracteres especiales y Unicode
- Edge cases (contraseñas vacías, largas, cortas)

#### ✅ Tests de Autenticación (AuthRepository)
- Registro exitoso de usuarios
- Validación de emails duplicados
- Login con credenciales correctas/incorrectas
- Verificación de códigos de email
- Recuperación de contraseña
- Gestión de sesiones

#### ✅ Tests de Productos (ProductRepository)
- Sincronización con API
- Manejo de duplicados
- CRUD completo (Create, Read, Update, Delete)
- Fallback offline
- Búsqueda y filtrado
- Integración con favoritos

#### ✅ Tests de Favoritos (FavoriteRepository)
- Agregar/eliminar favoritos
- Validación de autenticación
- Obtener favoritos de usuario
- Notificaciones a interesados

### 🚀 Ejecutar Tests

#### Todos los tests:
=======
## Guía Rápida: Ejecutar Tests

### Requisitos Previos
- Android Studio instalado
- JDK 17 configurado
- Gradle configurado

### Comandos para Ejecutar Tests

#### 1. Ejecutar TODOS los tests unitarios
```bash
./gradlew test
```

#### 2. Ejecutar tests con reporte detallado
```bash
./gradlew test --info
```

#### 3. Ejecutar tests específicos

##### Tests de PasswordManager (Seguridad)
```bash
./gradlew test --tests "*PasswordManagerTest*"
```

##### Tests de AuthRepository (Autenticación)
```bash
./gradlew test --tests "*AuthRepositoryTest*"
```

##### Tests de FavoriteRepository (Favoritos)
```bash
./gradlew test --tests "*FavoriteRepositoryTest*"
```

##### Tests de ProductRepository (Productos)
```bash
./gradlew test --tests "*ProductRepositoryTest*"
```

#### 4. Ejecutar un test individual
```bash
./gradlew test --tests "PasswordManagerTest.hashPassword_generatesValidBCryptHash"
```

#### 5. Ver reportes de tests
Los reportes HTML se generan en:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

Ábrelo en tu navegador para ver resultados detallados.

#### 6. Limpiar y ejecutar tests
```bash
./gradlew clean test
```

### Desde Android Studio

1. **Ver todos los tests**:
    - Panel izquierdo → `app/src/test/java`
    - Click derecho en carpeta → "Run Tests"

2. **Ejecutar una clase de test**:
    - Abrir archivo de test
    - Click en el icono verde junto al nombre de la clase
    - O: Click derecho → "Run 'NombreTest'"

3. **Ejecutar un test individual**:
    - Click en el icono verde junto al método `@Test`
    - O: Click derecho en el método → "Run 'nombreDelTest'"

4. **Ver cobertura de código**:
    - Click derecho en test → "Run with Coverage"

### Verificar que los tests están funcionando

#### Ejecuta el test de ejemplo primero:

```bash
./gradlew test --tests "*ExampleUnitTest*"
```

Si este comando no arroja errores, puedes ejecutar el resto de los tests.

### Solución de Problemas

#### Error: "Task 'test' not found"
```bash
./gradlew :app:test
```

#### Error de compilación
```bash
./gradlew clean
./gradlew build
```

#### Limpiar cache de Gradle
```bash
./gradlew clean --no-daemon
rm -rf .gradle
./gradlew test
```

#### Tests específicos por clase:
```bash
./gradlew test --tests "PasswordManagerTest"
./gradlew test --tests "AuthRepositoryTest"
./gradlew test --tests "ProductRepositoryTest"
./gradlew test --tests "FavoriteRepositoryTest"
```

#### Ver reporte HTML:
```bash
./gradlew test
# Abre: app/build/reports/tests/testDebugUnitTest/index.html
```

### 📖 Documentación de Testing

Para más información detallada sobre la arquitectura de testing:

- 📄 **[TESTING.md](TESTING.md)** - Arquitectura completa de tests, patrones y mejores prácticas
- 📄 **[RESUMEN_TESTING.md](RESUMEN_TESTING.md)** - Resumen ejecutivo de la cobertura
- 📁 **`app/src/test/`** - Código fuente de los tests

### 🎯 Patrón AAA (Arrange-Act-Assert)

Todos los tests siguen este patrón:

```kotlin
@Test
fun `when password is correct then verification succeeds`() = runTest {
    // Given (Arrange)
    val password = "mySecurePassword123"
    val hash = PasswordManager.hashPassword(password)
    
    // When (Act)
    val result = PasswordManager.verifyPassword(password, hash)
    
    // Then (Assert)
    assertTrue("Password should be verified successfully", result)
}
```

---

## 📱 Demo

<div align="center">
  <img src="localhandsappvideo.gif" width="300" alt="Demo de la aplicación Local Hands"/>
  <p><em>Demo completa de la aplicación mostrando las principales funcionalidades</em></p>
</div>

### 🎬 Características Mostradas en el Demo

- ✅ Splash screen con logo
- ✅ Login y registro de usuarios
- ✅ Navegación por el catálogo de productos
- ✅ Búsqueda y filtrado por categorías
- ✅ Detalles de productos con imágenes
- ✅ Sistema de favoritos
- ✅ Mapa de ubicaciones
- ✅ Perfil de vendedores
- ✅ Compartir productos
- ✅ Configuraciones de usuario

---

## 💻 Desarrollo

### 🔨 Build

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Build con tests
./gradlew build
```

### ▶️ Ejecutar

```bash
# Instalar en dispositivo conectado
./gradlew installDebug
adb shell am start -n com.undef.localhandsbrambillafunes/.MainActivity

# O ejecutar directamente desde Android Studio
# Run > Run 'app' (Shift + F10)
```

### 🐛 Debugging

```bash
# Ver logs de la aplicación
adb logcat | grep "LocalHands"

# Ver logs de Room
adb logcat | grep "RoomDatabase"

# Ver logs de Retrofit
adb logcat | grep "OkHttp"
```

### 📦 Generar APK

#### Debug APK:
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

#### Release APK (firmado):
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### 🧹 Limpiar Proyecto

```bash
# Limpiar builds anteriores
./gradlew clean

# Limpiar y rebuild
./gradlew clean build
```

### 🔄 Actualizar Dependencias

```bash
# Ver dependencias actualizables
./gradlew dependencyUpdates

# Sincronizar Gradle
./gradlew --refresh-dependencies
```
---

## ❓ FAQ

### ❔ ¿Qué es Manos Locales?

Manos Locales es una aplicación móvil Android que conecta usuarios con productores y emprendedores locales, facilitando el descubrimiento y compra de productos auténticos de la región.

### ❔ ¿Qué tecnologías utiliza la aplicación?

La app está construida con Kotlin, Jetpack Compose, Room, Hilt, Retrofit, Google Maps, y BCrypt. Ver la sección [Stack Tecnológico](#️-stack-tecnológico) para más detalles.

### ❔ ¿Funciona sin conexión a internet?

Sí, la aplicación utiliza Room como base de datos local, permitiendo ver productos previamente cargados sin conexión. La sincronización con el servidor ocurre cuando hay internet disponible.

### ❔ ¿Cómo puedo convertirme en vendedor?

Desde la pantalla de perfil, selecciona "Convertirse en Vendedor", completa tus datos de contacto y ubicación, y ¡listo! Podrás crear, editar y eliminar tus propios productos.

### ❔ ¿Es segura mi contraseña?

Absolutamente. Utilizamos BCrypt con factor de coste 12 para hashear todas las contraseñas. Nunca almacenamos contraseñas en texto plano.

### ❔ ¿Cómo funciona el sistema de favoritos?

Puedes marcar cualquier producto como favorito haciendo clic en el ícono de estrella. Tus favoritos se sincronizan con el servidor y recibirás notificaciones cuando haya cambios en productos que te interesan.

### ❔ ¿Puedo compartir productos con mis amigos?

Sí, cada producto tiene un botón de compartir que te permite enviar la información por WhatsApp, redes sociales, o cualquier otra app instalada en tu dispositivo.

### ❔ ¿Cómo se manejan las ubicaciones?

Utilizamos Google Maps API para mostrar ubicaciones de vendedores en un mapa interactivo. Al crear un producto, puedes seleccionar tu ubicación exacta en el mapa.

### ❔ ¿Cómo ejecuto los tests?

Ejecuta `./gradlew test` desde la terminal. Ver la sección [Testing](#-testing) para más opciones.

---

## ✉️ Contacto

### 👥 Equipo de Desarrollo

<div align="center">

| 👤 Desarrollador | 📧 Email | 🔗 GitHub |
|------------------|----------|-----------|
| **Tobias Funes** | [tobiasfunes@hotmail.com.ar](mailto:tobiasfunes@hotmail.com.ar) | [@TobiasFunes](https://github.com/TobiasFunes) |
| **Agustín Brambilla** | [agustinbram@gmail.com](mailto:agustinbram@gmail.com) | [@agusbram](https://github.com/agusbram) |

</div>

### 🏫 Institución

**Instituto Universitario Aeronáutico**  
Ingenieria en Informática  
Materia: Tecnologías Móviles  
Año: 2025

---

<div align="center">

### ⭐ Si te gusta el proyecto, déjanos una estrella en GitHub ⭐

[![Volver arriba](https://img.shields.io/badge/Volver%20arriba-↑-blue?style=for-the-badge)](#-manos-locales---local-hands-app-)

</div>
