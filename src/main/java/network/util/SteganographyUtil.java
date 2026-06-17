package network.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * LSB (Least Significant Bit) Steganography utility.
 *
 * Encoding:
 *   1. XOR the message bytes with the cycled password bytes.
 *   2. Prepend a 4-byte magic + 4-byte length header.
 *   3. Embed each bit into the LSB of the Blue channel of each pixel.
 *
 * Decoding:
 *   1. Extract LSBs from Blue channel.
 *   2. Verify magic header (validates password).
 *   3. Read length, extract bytes, XOR with password → original text.
 */
public class SteganographyUtil {

    private static final int MAGIC = 0xCAFEBABE;

    // ─────────────────────────────── ENCODE ───────────────────────────────

    /**
     * Embed {@code message} into {@code source} image using {@code password}.
     * Returns the resulting image as a PNG byte array.
     */
    public static byte[] encode(BufferedImage source, String message, String password) throws Exception {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] pwdBytes = password.getBytes(StandardCharsets.UTF_8);

        // XOR with password
        byte[] xored = xor(msgBytes, pwdBytes);

        // Build payload: MAGIC(4) + LENGTH(4) + data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(MAGIC);
        dos.writeInt(xored.length);
        dos.write(xored);
        dos.flush();
        byte[] payload = baos.toByteArray();

        int totalBits = payload.length * 8;
        int available = source.getWidth() * source.getHeight();
        if (totalBits > available) {
            throw new IllegalArgumentException("Ảnh quá nhỏ để chứa nội dung này!");
        }

        // Copy image
        BufferedImage result = copyImage(source);
        int bitIndex = 0;

        outer:
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                if (bitIndex >= totalBits) break outer;

                int byteIdx = bitIndex / 8;
                int bitPos  = 7 - (bitIndex % 8);
                int bit = (payload[byteIdx] >> bitPos) & 1;

                int rgb = result.getRGB(x, y);
                int b   = (rgb & 0xFF);
                b = (b & 0xFE) | bit;             // set LSB of blue channel
                rgb = (rgb & 0xFFFFFF00) | b;
                result.setRGB(x, y, rgb);
                bitIndex++;
            }
        }

        // Export to PNG bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(result, "PNG", out);
        return out.toByteArray();
    }

    // ─────────────────────────────── DECODE ───────────────────────────────

    /**
     * Extract hidden message from {@code image} using {@code password}.
     * Throws exception if password is wrong or image has no hidden content.
     */
    public static String decode(BufferedImage image, String password) throws Exception {
        byte[] pwdBytes = password.getBytes(StandardCharsets.UTF_8);

        // Extract enough bits for header first (8 bytes = 64 bits)
        byte[] header = extractBits(image, 0, 8);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(header));
        int magic  = dis.readInt();
        int length = dis.readInt();

        if (magic != MAGIC) {
            throw new IllegalArgumentException("Sai mật khẩu hoặc ảnh không chứa tin nhắn ẩn!");
        }
        if (length <= 0 || length > image.getWidth() * image.getHeight()) {
            throw new IllegalArgumentException("Dữ liệu ẩn bị hỏng!");
        }

        byte[] xored = extractBits(image, 8, 8 + length);
        byte[] plain = xor(xored, pwdBytes);
        return new String(plain, StandardCharsets.UTF_8);
    }

    // ─────────────────────────────── HELPERS ──────────────────────────────

    /** Extract bytes from [startByte, endByte) from image LSBs. */
    private static byte[] extractBits(BufferedImage img, int startByte, int endByte) {
        int count = endByte - startByte;
        byte[] result = new byte[count];
        
        int startPixel = startByte * 8;
        int endPixel = endByte * 8;
        int width = img.getWidth();
        
        for (int i = startPixel; i < endPixel; i++) {
            int y = i / width;
            int x = i % width;
            
            int byteIdx = (i - startPixel) / 8;
            int bitPos = 7 - (i % 8);
            
            int bit = img.getRGB(x, y) & 1;
            result[byteIdx] = (byte) (result[byteIdx] | (bit << bitPos));
        }
        
        return result;
    }

    private static byte[] xor(byte[] data, byte[] key) {
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return out;
    }

    private static BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        copy.getGraphics().drawImage(src, 0, 0, null);
        return copy;
    }

    /** Convert BufferedImage to PNG byte array. */
    public static byte[] imageToBytes(BufferedImage img) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return out.toByteArray();
    }

    /** Convert byte array to BufferedImage. */
    public static BufferedImage bytesToImage(byte[] data) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(data));
    }
}
