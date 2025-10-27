import { Schema, model } from "mongoose";

export interface IUser {
  name: string;
  email: string;
  passwordHash: string;
  photoUrl?: string | null;
  ratingAvg: number;
  ratingsCount: number;
  role: "user" | "admin";
  university?: string | null;
  phone?: string | null;
  isActive: boolean;
}

const userSchema = new Schema<IUser>(
  {
    name: { type: String, required: true, trim: true },
    email: { type: String, required: true, unique: true, lowercase: true, index: true },
    passwordHash: { type: String, required: true },
    photoUrl: { type: String, default: null },
    ratingAvg: { type: Number, default: 0 },
    ratingsCount: { type: Number, default: 0 },
    role: { type: String, enum: ["user", "admin"], default: "user" },
    university: { type: String, default: null },
    phone: { type: String, default: null },
    isActive: { type: Boolean, default: true }
  },
  { timestamps: true }
);

export const UserModel = model<IUser>("User", userSchema);
