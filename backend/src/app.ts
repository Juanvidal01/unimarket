import express from "express";
import cors from "cors";
import helmet from "helmet";
import morgan from "morgan";
import authRoutes from "./modules/auth/auth.routes";
import productRoutes from "./modules/products/product.routes";
import categoryRoutes from "./modules/Categories/category.routes";
import chatRoutes from "./modules/chat/chat.routes";
import ratingRoutes from "./modules/ratings/rating.routes";
export const createApp = () => {
  const app = express();

 // app.use(cors());
  app.use(helmet());
  app.use(morgan("dev"));
  app.use(express.json());
  app.use(cors({
  origin: "*",
  methods: ["GET","POST","PUT","PATCH","DELETE","OPTIONS"],
  allowedHeaders: ["Content-Type","Authorization"]
}));


app.use((req, _res, next) => {
  console.log("➡️", req.method, req.path);
  next();
});
  app.get("/health", (_req, res) => res.json({ ok: true }));

  app.use("/auth", authRoutes);
  app.use("/categories", categoryRoutes);
  app.use("/products", productRoutes);
  app.use("/chats", chatRoutes);
  app.use("/ratings", ratingRoutes);

  return app;
};
