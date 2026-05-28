import sharp from 'sharp';

const PDF_MAKER_SUPPORTED_IMAGE_MIMES = new Set(['image/png', 'image/jpeg']);

export async function toPdfMakerCompatibleImageDataUrl(
  buffer: Buffer,
  mimeType: string,
): Promise<string | null> {
  try {
    if (PDF_MAKER_SUPPORTED_IMAGE_MIMES.has(mimeType)) {
      return `data:${mimeType};base64,${buffer.toString('base64')}`;
    }

    const pngBuffer = await sharp(buffer)
      // rotate 自动修复某些相片上传没有实现 对应的图片旋转问题
      .rotate()
      .png()
      .toBuffer();

    return `data:image/png;base64,${pngBuffer.toString('base64')}`;
  } catch {
    return null;
  }
}
