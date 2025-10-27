import { Router } from "express";
import { Types, startSession } from "mongoose";
import { requireAuth } from "../../middlewares/auth";
import { RatingModel } from "./rating.model";
import { UserModel } from "../Users/user.model";
import { createRatingSchema, listRatingsSchema } from "./rating.schemas";

const router = Router();

/**
 * POST /ratings
 * body: { ratedUserId, productId?, score (1..5), comment? }
 * auth: cualquier usuario autenticado
 */
router.post("/", requireAuth, async (req, res) => {
  const parsed = createRatingSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.flatten() });
  }

  const { ratedUserId, productId, score, comment = "" } = parsed.data;
  const raterUserId = (req as any).user?.sub as string;

  if (!Types.ObjectId.isValid(ratedUserId)) return res.status(400).json({ error: "ratedUserId inválido" });
  if (productId && !Types.ObjectId.isValid(productId)) return res.status(400).json({ error: "productId inválido" });
  if (ratedUserId === raterUserId) return res.status(400).json({ error: "No puedes calificarte a ti mismo" });

  const session = await startSession();
  session.startTransaction();
  try {
    // Crear rating (fallará si existe por índice único)
    const rating = await RatingModel.create([{
      ratedUserId: new Types.ObjectId(ratedUserId),
      raterUserId: new Types.ObjectId(raterUserId),
      productId: productId ? new Types.ObjectId(productId) : undefined,
      score,
      comment
    }], { session });

    // Actualizar agregados del usuario calificado (promedio ponderado)
    const user = await UserModel.findById(ratedUserId).session(session);
    if (!user) throw new Error("Usuario a calificar no existe");

    const prevAvg = Number(user.get("ratingAvg") ?? 0);
    const prevCount = Number(user.get("ratingsCount") ?? 0);

    const newCount = prevCount + 1;
    const newAvg = Number(((prevAvg * prevCount + score) / newCount).toFixed(2));

    await UserModel.updateOne(
      { _id: ratedUserId },
      { $set: { ratingAvg: newAvg, ratingsCount: newCount } },
      { session }
    );

    await session.commitTransaction();
    session.endSession();
    return res.status(201).json({ rating: rating[0] });
  } catch (err: any) {
    await session.abortTransaction().catch(() => {});
    session.endSession();
    if (err?.code === 11000) {
      return res.status(409).json({ error: "Ya calificaste a este usuario para este producto" });
    }
    console.error("❌ Create rating error:", err?.message || err);
    return res.status(500).json({ error: "No se pudo crear la calificación" });
  }
});

/**
 * GET /users/:id/ratings  (listar calificaciones recibidas por un usuario)
 * query: page, limit
 */
router.get("/users/:id/ratings", async (req, res) => {
  const { id } = req.params;
  const parsed = listRatingsSchema.safeParse(req.query);
  const pageN = Math.max(parseInt((parsed.success && parsed.data.page) || "1", 10), 1);
  const limitN = Math.min(Math.max(parseInt((parsed.success && parsed.data.limit) || "10", 10), 1), 50);

  if (!Types.ObjectId.isValid(id)) return res.status(400).json({ error: "id inválido" });

  const skip = (pageN - 1) * limitN;
  const [items, total] = await Promise.all([
    RatingModel.find({ ratedUserId: id })
      .sort({ createdAt: -1 })
      .skip(skip).limit(limitN)
      .populate("raterUserId", "name email")
      .populate("productId", "title price"),
    RatingModel.countDocuments({ ratedUserId: id })
  ]);

  res.json({ total, page: pageN, limit: limitN, ratings: items });
});

/**
 * GET /users/:id/rating-summary  (resumen: promedio y conteo)
 */
router.get("/users/:id/rating-summary", async (req, res) => {
  const { id } = req.params;
  if (!Types.ObjectId.isValid(id)) return res.status(400).json({ error: "id inválido" });

  const user = await UserModel.findById(id).select("ratingAvg ratingsCount name");
  if (!user) return res.status(404).json({ error: "Usuario no encontrado" });

  res.json({
    user: { id: user._id, name: user.name },
    ratingAvg: Number(user.get("ratingAvg") ?? 0),
    ratingsCount: Number(user.get("ratingsCount") ?? 0)
  });
});

export default router;
