import { Schema, model, Types } from "mongoose";

export interface IMessage {
  chatId: Types.ObjectId;
  senderId: Types.ObjectId;
  content: string;
  createdAt: Date;
  read: boolean;
}

const messageSchema = new Schema<IMessage>(
  {
    chatId: { type: Schema.Types.ObjectId, ref: "Chat", required: true },
    senderId: { type: Schema.Types.ObjectId, ref: "User", required: true },
    content: { type: String, required: true },
    read: { type: Boolean, default: false }
  },
  { timestamps: { createdAt: true, updatedAt: false } }
);

export const MessageModel = model<IMessage>("Message", messageSchema);
