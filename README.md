# 📈 DivTracker

**DivTracker** es una aplicación Android nativa para inversores de dividendos que permite hacer seguimiento de acciones, analizar valoraciones y recibir alertas de precios objetivo.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

---

## ✨ Características

### 📋 Watchlist Personalizada
- Añade tickers a tu lista de seguimiento con búsqueda integrada
- Visualiza métricas clave: **Dividend Yield**, **Margin of Safety**, **FCF Yield**
- Ordena por diferentes criterios (margen, yield, ticker, fecha)
- Pull-to-refresh para actualizar cotizaciones en tiempo real

### 📊 Análisis de Valoración
- **Margin of Safety**: Indica si una acción está infravalorada o sobrevalorada
- **DCF Intrinsic Value**: Valor intrínseco calculado por flujo de caja descontado
- **FCF Yield & Dividend Yield**: Métricas de rentabilidad
- **Chowder Rule**: Regla para evaluar dividendos
- **Payout Ratio (FCF)**: Sostenibilidad del dividendo

### 🔔 Notificaciones Push
- Alertas cuando el precio cae por debajo de tu precio objetivo
- Integración con **Firebase Cloud Messaging (FCM)**
- Configuración por ticker individual

### 💰 Métricas Financieras
- Precio actual con cambio diario (%)
- Market Cap (en formato legible: B = Billions)
- P/E Ratio, P/FCF
- Target Price personalizable
- Notas personales por cada posición

### 🎨 Interfaz Moderna
- **Material Design 3** con Jetpack Compose
- Animaciones de precio cuando hay cambios
- Indicadores visuales de estado (verde/rojo para infravalorado/sobrevalorado)
- Soporte para tema claro y oscuro

---

## 🛠️ Tecnologías

| Categoría | Tecnología |
|-----------|------------|
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Arquitectura** | MVVM + Repository Pattern |
| **Networking** | Retrofit + OkHttp |
| **Base de Datos Local** | Room |
| **Push Notifications** | Firebase Cloud Messaging |
| **Seguridad** | EncryptedSharedPreferences |
| **Navegación** | Navigation Compose |
| **Concurrencia** | Kotlin Coroutines + Flow |

---

## 📱 Capturas de Pantalla

| Watchlist | Detalle | Búsqueda |
|-----------|---------|----------|
| Lista de seguimiento con métricas | Análisis detallado de valoración | Buscar y añadir tickers |

---

## 🚀 Instalación

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- Android SDK 35
- Kotlin 1.9+
- JDK 11

### Pasos

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/DivTracker.git
   cd DivTracker
   ```

2. **Configurar Firebase**
   - Crea un proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Descarga `google-services.json` y colócalo en `app/`
   - Habilita Cloud Messaging

3. **Configurar el backend** (opcional)
   - La app se conecta a un backend en AWS Elastic Beanstalk
   - Endpoint configurado en `RetrofitClient.kt`

4. **Compilar y ejecutar**
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📂 Estructura del Proyecto

```
app/src/main/java/com/rafiki81/divtracker/
├── data/
│   ├── api/          # Retrofit services y cliente HTTP
│   ├── model/        # Data classes (Request/Response)
│   └── repository/   # Repositorios para acceso a datos
├── navigation/       # Configuración de navegación
├── service/          # Firebase Messaging Service
├── ui/
│   ├── components/   # Componentes reutilizables
│   ├── login/        # Pantalla de login
│   ├── register/     # Pantalla de registro
│   ├── ticker/       # Búsqueda de tickers
│   ├── watchlist/    # Watchlist y detalle
│   └── theme/        # Tema Material 3
└── util/             # Utilidades (colores, formateo)
```

---

## 🔐 Autenticación

La app implementa autenticación JWT:

- **Login**: `POST /api/auth/login`
- **Registro**: `POST /api/auth/signup`
- Token almacenado de forma segura con `EncryptedSharedPreferences`
- Interceptor automático para añadir token a requests

---

## 📡 API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/watchlist` | Obtener watchlist |
| `POST` | `/api/watchlist` | Añadir item |
| `GET` | `/api/watchlist/{id}` | Detalle de item |
| `PUT` | `/api/watchlist/{id}` | Actualizar item |
| `DELETE` | `/api/watchlist/{id}` | Eliminar item |
| `GET` | `/api/tickers/search` | Buscar tickers |
| `POST` | `/api/devices` | Registrar dispositivo FCM |

---

## 🎯 Roadmap

- [ ] Gráficos históricos de precios
- [ ] Portfolio tracking con posiciones reales
- [ ] Calculadora de dividendos
- [ ] Exportar datos a CSV
- [ ] Widget para home screen
- [ ] Soporte multi-idioma

---

## 📄 Licencia

Este proyecto es de uso privado.

---

## 👤 Autor

Desarrollado por **Rafael Pérez-Beato. rperezbeato@gmail.com**

---

<p align="center">
  <i>Invierte con cabeza, sigue tus dividendos 📈</i>
</p>

