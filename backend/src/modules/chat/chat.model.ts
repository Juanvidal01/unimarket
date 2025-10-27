import { Schema, model, Types } from "mongoose";

export interface IChat {
  participants: Types.ObjectId[];
  productId?: Types.ObjectId;
  createdAt: Date;
  updatedAt: Date;
}

const chatSchema = new Schema<IChat>(
  {
    participants: [
      { type: Schema.Types.ObjectId, ref: "User", required: true }
    ],
    productId: { type: Schema.Types.ObjectId, ref: "Product" }
  },
  { timestamps: true }
);

export const ChatModel = model<IChat>("Chat", chatSchema);
