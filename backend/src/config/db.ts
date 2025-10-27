import mongoose from "mongoose";

export const connectDB = async (uri: string) => {
  mongoose.set("strictQuery", true);
  await mongoose.connect(uri, {dbName: "unimarket"});
  const conn = mongoose.connection;
  console.log(`✅ MongoDB conectado → DB: ${conn.name} | Host: ${conn.host}`);
};
