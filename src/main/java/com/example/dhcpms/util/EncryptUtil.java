package com.example.dhcpms.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 访客二维码动态加密工具类
 * 支持：AES加密 + HMAC签名 + 时效验证
 */
@Component
@Slf4j
public class EncryptUtil {

    // AES密钥（从配置文件读取，建议256位）
    @Value("${visitor.qrcode.aes-key:DHCPMS2024VisitorQRCodeKey123456}")
    private String aesKey;

    // HMAC密钥（用于签名防篡改）
    @Value("${visitor.qrcode.hmac-key:DHCPMS2024HMACSignatureKey789012}")
    private String hmacKey;

    // 二维码有效期（小时）
    @Value("${visitor.qrcode.default-valid-hours:2}")
    private int defaultValidHours;

    private static final String AES_ALGORITHM = "AES";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成访客二维码加密内容
     * @param inviteId 邀请ID
     * @param visitorName 访客姓名
     * @param visitorPhone 访客电话
     * @param ownerId 业主ID
     * @param validHours 有效期（小时），null则使用默认值
     * @return Base64编码的加密字符串
     */
    public String generateQRCodeContent(Long inviteId, String visitorName,
                                        String visitorPhone, Long ownerId,
                                        Integer validHours) {
        try {
            // 1. 构建二维码数据
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = now.plusHours(validHours != null ? validHours : defaultValidHours);

            Map<String, Object> qrData = new HashMap<>();
            qrData.put("inviteId", inviteId);
            qrData.put("visitorName", visitorName);
            qrData.put("visitorPhone", visitorPhone);
            qrData.put("ownerId", ownerId);
            qrData.put("createTime", now.format(FORMATTER));
            qrData.put("expireTime", expireTime.format(FORMATTER));

            String jsonData = JSON.toJSONString(qrData);
            log.info("二维码原始数据: {}", jsonData);

            // 2. AES加密
            String encryptedData = aesEncrypt(jsonData);

            // 3. HMAC签名（防篡改）
            String signature = hmacSign(encryptedData);

            // 4. 组合：加密数据 + 签名
            String finalContent = encryptedData + "|" + signature;

            // 5. Base64编码
            return Base64.getEncoder().encodeToString(finalContent.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("生成二维码加密内容失败", e);
            throw new RuntimeException("二维码生成失败");
        }
    }

    /**
     * 解密并验证二维码
     * @param qrCodeContent Base64编码的二维码内容
     * @return 解密后的数据Map，验证失败返回null
     */
    public Map<String, Object> decryptAndVerify(String qrCodeContent) {
        try {
            // 1. Base64解码
            String decodedContent = new String(Base64.getDecoder().decode(qrCodeContent), StandardCharsets.UTF_8);

            // 2. 分离加密数据和签名
            String[] parts = decodedContent.split("\\|");
            if (parts.length != 2) {
                log.warn("二维码格式错误");
                return null;
            }

            String encryptedData = parts[0];
            String receivedSignature = parts[1];

            // 3. 验证签名（防篡改）
            String expectedSignature = hmacSign(encryptedData);
            if (!expectedSignature.equals(receivedSignature)) {
                log.warn("二维码签名验证失败，可能被篡改");
                return null;
            }

            // 4. AES解密
            String jsonData = aesDecrypt(encryptedData);

            // 5. JSON反序列化
            Map<String, Object> qrData = JSON.parseObject(jsonData, Map.class);

            // 6. 验证时效性
            String expireTimeStr = (String) qrData.get("expireTime");
            LocalDateTime expireTime = LocalDateTime.parse(expireTimeStr, FORMATTER);
            if (LocalDateTime.now().isAfter(expireTime)) {
                log.warn("二维码已过期，过期时间: {}", expireTimeStr);
                qrData.put("expired", true);
                return qrData;
            }

            qrData.put("expired", false);
            log.info("二维码验证成功，inviteId: {}", qrData.get("inviteId"));
            return qrData;

        } catch (Exception e) {
            log.error("解密验证二维码失败", e);
            return null;
        }
    }

    /**
     * AES加密
     */
    private String aesEncrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);

        // 使用固定IV（实际项目应使用随机IV并一起传输）
        SecureRandom random = new SecureRandom(aesKey.getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[16];
        random.nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // IV + 密文，Base64编码
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * AES解密
     */
    private String aesDecrypt(String encryptedText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        // 提取IV和密文
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * HMAC签名
     */
    private String hmacSign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(hmacKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(keySpec);

        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature);
    }
}
