import { cloudinary } from "../config/cloudinary";

export const uploadBufferToCloudinary = (buffer: Buffer, folder: string, filename?: string) =>
  new Promise<{ url: string; publicId: string }>((resolve, reject) => {
    const upload = cloudinary.uploader.upload_stream(
      { folder, resource_type: "image", overwrite: false, public_id: filename },
      (err, result) => {
        if (err || !result) return reject(err);
        resolve({ url: result.secure_url!, publicId: result.public_id! });
      }
    );
    upload.end(buffer);
  });

export const deleteFromCloudinary = (publicId: string) =>
  new Promise<void>((resolve, reject) => {
    cloudinary.uploader.destroy(publicId, (err, result) => {
      if (err) return reject(err);
      // result = { result: "ok" | "not found" | "error" }
      resolve();
    });
  });
