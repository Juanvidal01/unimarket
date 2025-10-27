import { Schema, model, Types } from "mongoose";

export interface IRating {
  ratedUserId: Types.ObjectId;  // vendedor calificado
  raterUserId: Types.ObjectId;  // comprador que califica
  productId?: Types.ObjectId;   // opcional: producto asociado
  score: number;                // 1..5
  comment?: string;
  createdAt: Date;
}

const ratingSchema = new Schema<IRating>(
  {
    ratedUserId: { type: Schema.Types.ObjectId, ref: "User", required: true, index: true },
    raterUserId: { type: Schema.Types.ObjectId, ref: "User", required: true, index: true },
    productId:   { type: Schema.Types.ObjectId, ref: "Product", index: true },
    score:       { type: Number, required: true, min: 1, max: 5 },
    comment:     { type: String, default: "" }
  },
  { timestamps: { createdAt: true, updatedAt: false } }
);

// Evitar duplicados (mismo comprador → mismo vendedor → mismo producto)
ratingSchema.index(
  { ratedUserId: 1, raterUserId: 1, productId: 1 },
  { unique: true, name: "uniq_rater_rated_product" }
);

export const RatingModel = model<IRating>("Rating", ratingSchema);
