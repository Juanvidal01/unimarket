import { Schema, model, Types } from "mongoose";

export interface IProduct {
  ownerId: Types.ObjectId;
  title: string;
  description: string;
  category: string;
  price: number;
  condition: "nuevo" | "como_nuevo" | "usado";
  images: { url: string; publicId: string }[];
  status: "publicado" | "pausado" | "vendido";
  location?: {
    campus?: string | null;
    city?: string | null;
    lat?: number | null;
    lng?: number | null;
  };
  keywords?: string[];
}


const imageSchema = new Schema<{ url: string; publicId: string }>(
  {
    url: { type: String, required: true },
    publicId: { type: String, required: true }
  },
  { _id: false, id: false } 
);

const productSchema = new Schema<IProduct>(
  {
    ownerId: { type: Schema.Types.ObjectId, ref: "User", required: true, index: true },
    title: { type: String, required: true, trim: true },
    description: { type: String, required: true, trim: true },
    category: { type: String, required: true, index: true },
    price: { type: Number, required: true, min: 0, index: true },
    condition: { type: String, enum: ["nuevo", "como_nuevo", "usado"], required: true },
    images: { type: [imageSchema], default: [] }, 
    status: { type: String, enum: ["publicado", "pausado", "vendido"], default: "publicado", index: true },
    location: {
      campus: { type: String, default: null },
      city: { type: String, default: null },
      lat: { type: Number, default: null },
      lng: { type: Number, default: null }
    },
    keywords: { type: [String], default: [] }
  },
  { timestamps: true }
);

// Índice de texto para búsquedas
productSchema.index({ title: "text", description: "text", keywords: "text" });

export const ProductModel = model<IProduct>("Product", productSchema);
