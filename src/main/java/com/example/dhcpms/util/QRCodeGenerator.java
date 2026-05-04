package com.example.dhcpms.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类（适配ZXing 3.5.2 + Java 17）
 */
@Component
@Slf4j
public class QRCodeGenerator {

    // 二维码尺寸（从配置文件读取，默认300）
    @Value("${qrcode.size:300}")
    private int qrCodeSize;

    /**
     * 生成二维码并返回Base64字符串
     * @param content 二维码内容（加密后的邀请ID）
     * @return Base64编码的二维码图片
     */
    public String generateBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 高容错
            hints.put(EncodeHintType.MARGIN, 1);

            // 生成二维码矩阵
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints);

            // Java 17 兼容的IO流处理
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream,
                    new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF));

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            throw new RuntimeException("二维码生成失败");
        }
    }
}