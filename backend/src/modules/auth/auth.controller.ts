import { Request, Response } from "express";
import { registerSchema, loginSchema } from "./auth.schemas";
import { UserModel } from "../Users/user.model";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";

const allowedDomains = (process.env.ALLOWED_DOMAINS ?? "")
  .split(",")
  .map(d => d.trim().toLowerCase())
  .filter(Boolean);

const sign = (sub: string, email: string, role: string) =>
  jwt.sign({ sub, email, role }, process.env.JWT_SECRET!, { expiresIn: "7d" });

export const register = async (req: Request, res: Response) => {
  // Validación de entrada
  const parsed = registerSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.flatten() });
  }

  const { name, email, password } = parsed.data;

  // Validación de dominio institucional
  const domain = email.split("@")[1]?.toLowerCase();
  if (!allowedDomains.includes(domain)) {
    return res.status(400).json({ error: "Email no permitido (debe ser institucional)" });
  }

  // Evitar duplicados
  const exists = await UserModel.findOne({ email });
  if (exists) return res.status(409).json({ error: "Email ya registrado" });

  try {
    const passwordHash = bcrypt.hashSync(password, 10);

    // 👇 Insertamos TODOS los campos esperados por el validador de Atlas
    const user = await UserModel.create({
      name,
      email,
      passwordHash,
      isActive: true,
      role: "user",
      ratingAvg: 0,
      ratingsCount: 0,
      photoUrl: null,
      university: null,
      phone: null
      // createdAt/updatedAt los agrega Mongoose (timestamps)
    });

    const token = sign(user._id.toString(), user.email, user.role);
    return res.status(201).json({
      token,
      user: {
        id: user._id.toString(),
        name: user.name,
        email: user.email,
        role: user.role
      }
    });
  } catch (err: any) {
    // 👇 Log detallado para ver exactamente qué regla del validator falló
    console.error("❌ Register insert error (raw):");
    console.dir(err, { depth: 10 });
    return res.status(500).json({
      error: "DB validation failed",
      details: err?.errInfo ?? err?.message ?? "unknown"
    });
  }
};

export const login = async (req: Request, res: Response) => {
  try {
    const parsed = loginSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({ error: parsed.error.flatten() });
    }
    const { email, password } = parsed.data;

    const user = await UserModel.findOne({ email });
    if (!user) return res.status(401).json({ error: "Credenciales inválidas" });

    const ok = bcrypt.compareSync(password, user.passwordHash);
    if (!ok) return res.status(401).json({ error: "Credenciales inválidas" });

    const token = sign(user._id.toString(), user.email, user.role);
    return res.json({
      token,
      user: { id: user._id.toString(), name: user.name, email: user.email, role: user.role }
    });
  } catch (err: any) {
    console.error("❌ Login error:", err?.message ?? err);
    return res.status(500).json({ error: "Internal error" });
  }
};

export const me = async (req: Request, res: Response) => {
  try {
    const userId = (req as any).user?.sub as string;
    const user = await UserModel.findById(userId).select("name email role photoUrl");
    if (!user) return res.status(404).json({ error: "Usuario no encontrado" });
    return res.json({ user });
  } catch (err: any) {
    console.error("❌ Me error:", err?.message ?? err);
    return res.status(500).json({ error: "Internal error" });
  }
};
