import { Router } from "express";
import { register, login, me } from "./auth.controller";
import { requireAuth } from "../../middlewares/auth";

const router = Router();
router.post("/register", register);
router.post("/login", login);
router.get("/me", requireAuth, me);
export default router;
