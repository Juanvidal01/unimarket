import { Router } from "express";
import { Types } from "mongoose";
import { requireAuth } from "../../middlewares/auth";
import { ChatModel } from "./chat.model";
import { MessageModel } from "./message.model";

const router = Router();

/**
 * Crear chat nuevo o devolver el existente
 * POST /chats
 * body: { otherUserId, productId? }
 */
router.post("/", requireAuth, async (req, res) => {
  const userId = (req as any).user?.sub as string;
  const { otherUserId, productId } = req.body;

  if (!otherUserId) {
    return res.status(400).json({ error: "Falta otherUserId" });
  }
  if (!Types.ObjectId.isValid(otherUserId)) {
    return res.status(400).json({ error: "otherUserId inválido" });
  }
  if (productId && !Types.ObjectId.isValid(productId)) {
    return res.status(400).json({ error: "productId inválido" });
  }

  try {
    const userObjId = new Types.ObjectId(userId);
    const otherUserObjId = new Types.ObjectId(otherUserId);

    // Buscar si ya existe un chat entre estos dos usuarios (y producto opcional)
    let chat = await ChatModel.findOne({
      participants: { $all: [userObjId, otherUserObjId] },
      ...(productId ? { productId: new Types.ObjectId(productId) } : {})
    });

    if (!chat) {
      chat = await ChatModel.create({
        participants: [userObjId, otherUserObjId],
        productId: productId ? new Types.ObjectId(productId) : undefined
      });
    }

    res.status(201).json(chat);
  } catch (err: any) {
    console.error("❌ Create chat error:", err.message);
    res.status(500).json({ error: "Error creando chat", details: err.message });
  }
});

/**
 * Listar todos mis chats
 * GET /chats
 */
router.get("/", requireAuth, async (req, res) => {
  const userId = (req as any).user?.sub as string;

  try {
    const chats = await ChatModel.find({ participants: userId })
      .populate("participants", "name email")
      .populate("productId", "title price");
    res.json(chats);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * Enviar mensaje
 * POST /chats/:id/messages
 * body: { content }
 */
router.post("/:id/messages", requireAuth, async (req, res) => {
  const { id } = req.params;
  const { content } = req.body;
  const senderId = (req as any).user?.sub as string;

  if (!content) return res.status(400).json({ error: "Falta content" });

  try {
    const chat = await ChatModel.findById(id);
    if (!chat) return res.status(404).json({ error: "Chat no encontrado" });

    if (!chat.participants.some(p => p.toString() === senderId)) {
      return res.status(403).json({ error: "No eres parte de este chat" });
    }

    const msg = await MessageModel.create({ chatId: id, senderId, content });
    res.status(201).json(msg);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * Listar mensajes de un chat
 * GET /chats/:id/messages
 */
router.get("/:id/messages", requireAuth, async (req, res) => {
  const { id } = req.params;
  const userId = (req as any).user?.sub as string;

  try {
    const chat = await ChatModel.findById(id);
    if (!chat) return res.status(404).json({ error: "Chat no encontrado" });

    if (!chat.participants.some(p => p.toString() === userId)) {
      return res.status(403).json({ error: "No eres parte de este chat" });
    }

    const messages = await MessageModel.find({ chatId: id }).sort({ createdAt: 1 });
    res.json(messages);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
