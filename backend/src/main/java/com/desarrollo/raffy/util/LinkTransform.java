package com.desarrollo.raffy.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;   


public class LinkTransform {
    /**
     * Convertir un link en un QR
     * @param link Link a convertir
     * @return QR codificado en Base64
     */
    public static String linkToQr(String link) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = qrCodeWriter.encode(link, BarcodeFormat.QR_CODE, 256, 256, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] pngBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(pngBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Acortar una URL
     * @param url URL a acortar
     * @return URL acortada en base 62
     */
    public static String shortenUrl(String url) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(url.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            long value = 0L;
            for (int i = 0; i < 8 && i < hash.length; i++) {
                value = (value << 8) | (hash[i] & 0xFF);
            }
            if (value < 0) {
                value = -value;
            }

            String base62 = toBase62(value);
            int maxLen = 8;
            return base62.length() > maxLen ? base62.substring(0, maxLen) : base62;
        } catch (java.security.NoSuchAlgorithmException e) {
            long val = Math.abs(url.hashCode());
            return Long.toString(val, 36);
        }
    }

    private static String toBase62(long num) {
        if (num == 0) return "0";
        final char[] alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int idx = (int) (num % 62);
            sb.append(alphabet[idx]);
            num /= 62;
        }
        return sb.reverse().toString();
    }
}