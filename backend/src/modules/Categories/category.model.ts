import { Schema, model } from "mongoose";

export interface ICategory {
  slug: string;
  name: string;
  icon?: string | null;
}

const categorySchema = new Schema<ICategory>(
  {
    slug: { type: String, required: true, unique: true, lowercase: true, trim: true, index: true },
    name: { type: String, required: true, trim: true },
    icon: { type: String, default: null }
  },
  { timestamps: true }
);

export const CategoryModel = model<ICategory>("Category", categorySchema);
