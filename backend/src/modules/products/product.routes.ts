import { Router } from "express";
import { ProductModel } from "./product.model";
import { requireAuth } from "../../middlewares/auth";
import { Types } from "mongoose";
import multer from "multer";
import { uploadBufferToCloudinary, deleteFromCloudinary } from "../../utils/upload";
import { cloudinary } from "../../config/cloudinary";

const router = Router();

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB por archivo
  fileFilter: (_req, file, cb) => {
    if (file.mimetype.startsWith("image/")) cb(null, true);
    else cb(new Error("Tipo de archivo no permitido"));
  }
});
/**
 * POST /products/:id/images
 * form-data:
 *   images: <file> (puedes repetir la misma key para varias)
 * Auth: dueño del producto
 */
router.post("/:id/images", requireAuth, upload.array("images", 5), async (req, res) => {
  const { id } = req.params;
  if (!Types.ObjectId.isValid(id)) return res.status(400).json({ error: "id inválido" });

  const product = await ProductModel.findById(id);
  if (!product) return res.status(404).json({ error: "Producto no encontrado" });

  const userId = (req as any).user?.sub as string;
  if (product.ownerId.toString() !== userId) return res.status(403).json({ error: "No autorizado" });

  const files = req.files as Express.Multer.File[] | undefined;
  if (!files || files.length === 0) return res.status(400).json({ error: "No se enviaron imágenes" });

  try {
    const folder = process.env.CLOUDINARY_FOLDER || "unimarket/products";
    const uploads = await Promise.all(
      files.map((f) => uploadBufferToCloudinary(f.buffer, folder))
    );

    // Agrega al producto
    product.images.push(...uploads);
    await product.save();

    res.status(201).json({
      added: uploads.length,
      images: uploads,
      product
    });
  } catch (err: any) {
    console.error("❌ Upload images error:", err?.message || err);
    res.status(500).json({ error: "Error subiendo imágenes" });
  }
});

/**
 * DELETE /products/:id/images/:publicId
 * Auth: dueño del producto
 */
router.delete("/:id/images", requireAuth, async (req, res) => {
  const { id } = req.params;
  const publicIdRaw = (req.query.publicId as string) || "";
  const publicId = decodeURIComponent(publicIdRaw);

  if (!Types.ObjectId.isValid(id)) {
    return res.status(400).json({ error: "id inválido" });
  }
  if (!publicId) {
    return res.status(400).json({ error: "Falta query param publicId" });
  }

  const product = await ProductModel.findById(id);
  if (!product) return res.status(404).json({ error: "Producto no encontrado" });

  const userId = (req as any).user?.sub as string;
  if (product.ownerId.toString() !== userId) {
    return res.status(403).json({ error: "No autorizado" });
  }

  const existeEnProducto = product.images.some((img) => img.publicId === publicId);
  if (!existeEnProducto) {
    return res.status(404).json({ error: "Imagen no encontrada en el producto" });
  }

  try {
    await deleteFromCloudinary(publicId);
    product.images = product.images.filter((img) => img.publicId !== publicId);
    await product.save();
    return res.json({ ok: true, product });
  } catch (err: any) {
    console.error("❌ Delete image error:", err?.message || err);
    return res.status(500).json({ error: "No se pudo eliminar la imagen", details: err?.message || err });
  }
});


/**
 * POST /products  (crear)
 * body: { title, description, category, price, condition, images?, keywords?, location? }
 * usa req.user.sub como ownerId
 */
router.post("/", requireAuth, async (req, res) => {
  const ownerId = (req as any).user?.sub as string;
  const { title, description, category, price, condition, images = [], keywords = [], location } = req.body;

  if (!title || !description || !category || price == null || !condition) {
    return res.status(400).json({ error: "Faltan campos obligatorios" });
  }

  const doc = await ProductModel.create({
    ownerId: new Types.ObjectId(ownerId),
    title, description, category, price, condition,
    images, keywords, location
  });
  res.status(201).json({ product: doc });
});

/**
 * GET /products  (listar con filtros)
 * query: q (texto), category, minPrice, maxPrice, status, ownerId, page, limit
 */
router.get("/", async (req, res) => {
  const {
    q, category, status = "publicado",
    ownerId, minPrice, maxPrice,
    page = "1", limit = "12"
  } = req.query as Record<string, string>;

  const filter: any = {};
  if (status) filter.status = status;
  if (category) filter.category = category;
  if (ownerId) filter.ownerId = new Types.ObjectId(ownerId);
  if (minPrice || maxPrice) {
    filter.price = {};
    if (minPrice) filter.price.$gte = Number(minPrice);
    if (maxPrice) filter.price.$lte = Number(maxPrice);
  }
  if (q) filter.$text = { $search: q };

  const pageN = Math.max(parseInt(page, 10) || 1, 1);
  const limitN = Math.min(Math.max(parseInt(limit, 10) || 12, 1), 50);
  const skip = (pageN - 1) * limitN;

  const [items, total] = await Promise.all([
    ProductModel.find(filter)
      .sort(q ? { score: { $meta: "textScore" } } : { createdAt: -1 })
      .select(q ? { score: { $meta: "textScore" } } : {})
      .skip(skip).limit(limitN),
    ProductModel.countDocuments(filter)
  ]);

  res.json({
    total, page: pageN, limit: limitN,
    products: items
  });
});

/** GET /products/:id */
router.get("/:id", async (req, res) => {
  const { id } = req.params;
  if (!Types.ObjectId.isValid(id)) return res.status(400).json({ error: "id inválido" });
  const product = await ProductModel.findById(id);
  if (!product) return res.status(404).json({ error: "No encontrado" });
  res.json({ product });
});

/** PATCH /products/:id (solo dueño) */
router.patch("/:id", requireAuth, async (req, res) => {
  const { id } = req.params;
  if (!Types.ObjectId.isValid(id)) return res.status(400).json({ error: "id inválido" });
  const userId = (req as any).user?.sub as string;

  const product = await ProductModel.findById(id);
  if (!product) return res.status(404).json({ error: "No encontrado" });
  if (product.ownerId.toString() !== userId) return res.status(403).json({ error: "No autorizado" });

  const updatable = ["title","description","category","price","condition","images","keywords","location","status"];
  const data: any = {};
  for (const k of updatable) if (k in req.body) data[k] = req.body[k];

  const updated = await ProductModel.findByIdAndUpdate(id, { $set: data }, { new: true });
  res.json({ product: updated });
});

/** DELETE /products/:id (solo dueño) */

router.delete("/:id", requireAuth, async (req, res) => {
  const { id } = req.params;
  if (!Types.ObjectId.isValid(id)) return res.status(400).json({ error: "id inválido" });
  const userId = (req as any).user?.sub as string;

  const product = await ProductModel.findById(id);
  if (!product) return res.status(404).json({ error: "No encontrado" });
  if (product.ownerId.toString() !== userId) return res.status(403).json({ error: "No autorizado" });

  await ProductModel.findByIdAndDelete(id);
  res.json({ ok: true });
});


export default router;
