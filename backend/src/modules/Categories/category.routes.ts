import { Router } from "express";
import { CategoryModel } from "./category.model";

const router = Router();

router.get("/", async (_req, res) => {
  const categories = await CategoryModel.find().sort({ name: 1 }).select("slug name icon");
  res.json({ categories });
});

export default router;
