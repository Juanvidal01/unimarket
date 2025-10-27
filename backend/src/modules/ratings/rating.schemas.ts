import { z } from "zod";

export const createRatingSchema = z.object({
  ratedUserId: z.string().min(1, "ratedUserId requerido"),
  productId: z.string().optional(),   // si no quieres ligarlo al producto, hazlo opcional
  score: z.number().int().min(1).max(5),
  comment: z.string().max(500).optional()
});

export const listRatingsSchema = z.object({
  page: z.string().optional(),
  limit: z.string().optional()
});
