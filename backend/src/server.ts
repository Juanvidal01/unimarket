import { config } from "dotenv";
config();
import { createApp } from "./app";
import { connectDB } from "./config/db";

const app = createApp();
const port = Number(process.env.PORT) || 8080;

(async () => {
  try {
    // Escucha primero para poder probar /health desde navegador
    app.listen(port, "0.0.0.0", () => {
      console.log(`🚀 API escuchando en http://0.0.0.0:${port}`);
    });

    // Conecta a Mongo en segundo plano (loggea si falla)
    const uri = process.env.MONGODB_URI;        // 👈 nombre correcto
    if (!uri) {
      console.error("❌ MONGODB_URI no está definida en .env");
      return;
    }
    await connectDB(uri);
    console.log("✅ MongoDB conectado");
  } catch (err) {
    console.error("❌ Error al iniciar", err);
  }
})();

