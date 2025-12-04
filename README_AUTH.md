# DivTracker - Autenticación

## 📱 Implementación completada

Se ha implementado el sistema completo de autenticación con las siguientes funcionalidades:

### ✅ Características implementadas:

1. **Pantalla de Login** (`LoginScreen.kt`)
   - Formulario con email y password
   - Validación de campos
   - Indicador de carga
   - Manejo de errores con Snackbar
   - Navegación a pantalla de registro

2. **Pantalla de Register** (`RegisterScreen.kt`)
   - Formulario con email, password y confirmación
   - Validación de contraseñas coincidentes
   - Indicador de carga
   - Manejo de errores con Snackbar
   - Navegación de vuelta a login

3. **Modelos de datos**
   - `LoginRequest.kt` - Modelo para login
   - `SignupRequest.kt` - Modelo para registro
   - `AuthResponse.kt` - Respuesta del servidor con token

4. **Capa de API**
   - `AuthApiService.kt` - Interface de Retrofit con endpoints
   - `RetrofitClient.kt` - Cliente HTTP configurado
   - `AuthRepository.kt` - Repositorio para manejo de datos

5. **ViewModels**
   - `LoginViewModel.kt` - Lógica de negocio para login
   - `RegisterViewModel.kt` - Lógica de negocio para registro

6. **Navegación**
   - `AppNavigation.kt` - Sistema de navegación entre pantallas

### 📋 Endpoints de la API utilizados:

Según el OpenAPI proporcionado:

- **POST** `/api/auth/login`
  - Body: `{ "email": "string", "password": "string" }`
  - Response: `{ "token": "string", "email": "string" }`

- **POST** `/api/auth/signup`
  - Body: `{ "email": "string", "password": "string" }`
  - Response: `{ "token": "string", "email": "string" }`

### 🔧 Configuración necesaria:

1. **Sincronizar Gradle**: 
   - Haz clic en "Sync Now" cuando aparezca el banner
   - O ejecuta: `./gradlew build --refresh-dependencies`

2. **Configurar la URL del servidor**:
   Edita `RetrofitClient.kt` y cambia la URL base:
   ```kotlin
   // Para emulador Android (localhost en tu máquina)
   private const val BASE_URL = "http://10.0.2.2:8080/"
   
   // Para dispositivo físico en la misma red
   private const val BASE_URL = "http://TU_IP_LOCAL:8080/"
   
   // Para producción
   private const val BASE_URL = "https://api.divtracker.com/"
   ```

3. **Permisos de Internet**: Ya añadidos al `AndroidManifest.xml`

### 📦 Dependencias añadidas:

```gradle
// Navigation
implementation("androidx.navigation:navigation-compose:2.7.5")

// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### 🚀 Próximos pasos sugeridos:

1. **Guardar el token JWT**: 
   - Implementar DataStore o SharedPreferences para persistir el token
   - Añadir el token a los headers de las siguientes peticiones API

2. **Validación de email**:
   - Añadir validación de formato de email
   - Validación de fortaleza de contraseña

3. **Pantalla Home**:
   - Crear la pantalla principal después del login
   - Implementar watchlist y otras funcionalidades

4. **Manejo de sesión**:
   - Verificar si hay token guardado al iniciar la app
   - Navegar automáticamente a Home si está autenticado
   - Implementar logout

5. **Google Sign-In** (Opcional):
   - Integrar Firebase Auth o Google Identity Services
   - Configurar el OAuth en Google Cloud Console

### 🧪 Pruebas:

Para probar la app:
1. Asegúrate de que el backend está corriendo en `localhost:8080`
2. Ejecuta la app en el emulador o dispositivo
3. Intenta registrarte con un email y contraseña
4. Luego intenta hacer login con las mismas credenciales

### 📁 Estructura del proyecto:

```
app/src/main/java/com/rafiki81/divtracker/
├── MainActivity.kt
├── data/
│   ├── api/
│   │   ├── AuthApiService.kt
│   │   └── RetrofitClient.kt
│   ├── model/
│   │   ├── AuthResponse.kt
│   │   ├── LoginRequest.kt
│   │   └── SignupRequest.kt
│   └── repository/
│       └── AuthRepository.kt
├── navigation/
│   └── AppNavigation.kt
└── ui/
    ├── login/
    │   ├── LoginScreen.kt
    │   └── LoginViewModel.kt
    ├── register/
    │   ├── RegisterScreen.kt
    │   └── RegisterViewModel.kt
    └── theme/
```

### ⚠️ Notas importantes:

- La URL base está configurada para usar el emulador de Android (`10.0.2.2` es la IP del host desde el emulador)
- El logging de HTTP está habilitado para facilitar el debugging
- Los errores de red se muestran al usuario mediante Snackbar
- Las contraseñas se ocultan con `PasswordVisualTransformation`

---

**¿Necesitas algo más?** Puedes:
- Ajustar la URL del servidor
- Personalizar los estilos y colores
- Añadir más validaciones
- Implementar el guardado del token JWT

