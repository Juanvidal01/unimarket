# 🎓 UniMarket


**UniMarket** es una aplicación de marketplace exclusiva para estudiantes universitarios, que permite comprar y vender productos entre la comunidad estudiantil de manera segura y sencilla.

## 📱 Características Principales

- ✅ **Autenticación segura** con email institucional (@usc.edu.co)
- 🛍️ **Publicar productos** con hasta 5 imágenes
- 🔍 **Búsqueda avanzada** de productos
- ❤️ **Sistema de favoritos**
- 💬 **Chat en tiempo real** entre compradores y vendedores
- ⭐ **Sistema de calificaciones** para vendedores
- 📍 **Ubicación de productos** por campus

## 🏗️ Arquitectura del Proyecto

```
unimarket/
├── backend/                 # API REST con Node.js + Express
│   ├── src/
│   │   ├── modules/
│   │   │   ├── auth/       # Autenticación JWT
│   │   │   ├── products/   # CRUD de productos
│   │   │   ├── chat/       # Sistema de mensajería
│   │   │   ├── ratings/    # Calificaciones
│   │   │   └── Categories/ # Categorías de productos
│   │   ├── config/         # Configuraciones (DB, Cloudinary)
│   │   ├── middlewares/    # Auth middleware
│   │   └── utils/          # Utilidades (upload a Cloudinary)
│   └── .env
│
└── android/                 # App Android (Kotlin)
    └── app/src/main/
        ├── java/com/tuorg/unimarket/
        │   ├── network/    # ApiClient, ApiService
        │   ├── ui/
        │   │   └── home/   # Activities y Adapters
        │   └── models/     # Modelos de datos
        └── res/            # Recursos (layouts, drawables, colors)
```

## 🛠️ Tecnologías Utilizadas

### Backend
- **Node.js** + **Express.js** - Framework web
- **MongoDB** + **Mongoose** - Base de datos NoSQL
- **JWT** - Autenticación segura
- **Cloudinary** - Almacenamiento de imágenes
- **Bcrypt** - Hash de contraseñas
- **Zod** - Validación de esquemas
- **Multer** - Manejo de archivos multipart

### Frontend (Android)
- **Kotlin** - Lenguaje principal
- **Retrofit** - Cliente HTTP
- **Glide** - Carga de imágenes
- **Coroutines** - Programación asíncrona
- **Material Design** - Diseño UI/UX
- **RecyclerView** - Listas eficientes

## 📋 Requisitos Previos

### Backend
- Node.js v18+ 
- MongoDB Atlas o MongoDB local
- Cuenta en Cloudinary (para imágenes)

### Android
- Android Studio Hedgehog o superior
- JDK 17+
- Android SDK (API 24+)
- Dispositivo físico o emulador

## 🚀 Instalación y Configuración

### 1. Backend

```bash
# Clonar el repositorio
git clone https://github.com/Juanvidal01/unimarket.git
cd unimarket/backend

# Instalar dependencias
npm install

# Configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales
```

**Archivo `.env` necesario:**
```env
# MongoDB
MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/unimarket

# JWT
JWT_SECRET=tu_secreto_super_seguro_aqui

# Cloudinary
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret
CLOUDINARY_FOLDER=unimarket/products

# Dominios permitidos (separados por coma)
ALLOWED_DOMAINS=usc.edu.co

# Puerto
PORT=8080
```

```bash
# Iniciar servidor de desarrollo
npm run dev

# El servidor estará en http://localhost:8080
```

### 2. Android App

1. Abrir Android Studio
2. **File → Open** → Seleccionar carpeta `android/`
3. Esperar a que Gradle sincronice
4. Editar `ApiClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://TU_IP_LOCAL:8080/"
   ```
5. **Build → Make Project**
6. **Run → Run 'app'**

## 🌐 Configuración de Red para Desarrollo

### Opción 1: Dispositivo Físico (Recomendado)

1. Conecta tu PC y teléfono a la **misma red WiFi**
2. Obtén tu IP local:
   - **Windows**: `ipconfig` → Busca IPv4
   - **Mac/Linux**: `ifconfig` → Busca inet
3. Usa esa IP en `BASE_URL`: `http://192.168.1.100:8080/`

### Opción 2: Emulador

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

### Opción 3: ngrok (para desarrollo remoto)

```bash
ngrok http 8080
# Usa la URL generada: https://abc123.ngrok.io/
```

## 📱 Endpoints del API

### Autenticación
```
POST /auth/register    # Registro de usuario
POST /auth/login       # Inicio de sesión
GET  /auth/me          # Obtener usuario actual
```

### Productos
```
POST   /products              # Crear producto
GET    /products              # Listar productos (con filtros)
GET    /products/:id          # Obtener producto por ID
PATCH  /products/:id          # Actualizar producto
DELETE /products/:id          # Eliminar producto
POST   /products/:id/images   # Subir imágenes
DELETE /products/:id/images   # Eliminar imagen
```

### Categorías
```
GET /categories    # Listar todas las categorías
```

### Chat
```
POST /chats              # Crear/obtener chat
GET  /chats              # Mis chats
POST /chats/:id/messages # Enviar mensaje
GET  /chats/:id/messages # Obtener mensajes
```

### Calificaciones
```
POST /ratings                     # Calificar usuario
GET  /users/:id/ratings           # Calificaciones recibidas
GET  /users/:id/rating-summary    # Resumen de calificaciones
```

## 🎨 Paleta de Colores

```xml
<!-- UniMarket Brand Colors -->
<color name="brand_cyan">#00BCD4</color>
<color name="brand_orange">#FF9800</color>
<color name="bg_dark">#1E1E1E</color>
<color name="input_bg">#2A2A2A</color>
<color name="text_primary">#FFFFFF</color>
<color name="text_secondary">#9E9E9E</color>
```

## 🐛 Solución de Problemas Comunes

### Backend no inicia
```bash
# Verificar que MongoDB esté corriendo
# Revisar variables de entorno en .env
# Ver logs del servidor para más detalles
```

### App no se conecta al backend
- Verificar que ambos dispositivos estén en la misma red WiFi
- Confirmar que la IP en `BASE_URL` sea correcta
- Verificar que el firewall permita conexiones en el puerto 8080
- Probar acceder desde el navegador del teléfono: `http://IP:8080/health`

### Imágenes no se cargan
- Verificar credenciales de Cloudinary en `.env`
- Revisar que las imágenes se estén subiendo correctamente al backend
- Verificar conexión a internet del dispositivo

### Error de permisos (Android)
- Desinstalar completamente la app
- Reinstalar
- Aceptar permisos cuando se soliciten

## 📄 Licencia

Este proyecto es parte de un trabajo académico de la Universidad Santiago de Cali.

## 👥 Contribuidores

- **Juan Vidal** - Desarrollador Principal
- **Jhonatan Hernandez** - Desarrollador 
- Universidad Santiago de Cali - 2025

## 📧 Contacto

Para reportar bugs o sugerencias:
- GitHub Issues: [https://github.com/Juanvidal01/unimarket/issues](https://github.com/Juanvidal01/unimarket/issues)

---

⭐ Si te gustó este proyecto, dale una estrella en GitHub!

**Universidad Santiago de Cali** 🎓
*"Somos calidad, somos USC"*
