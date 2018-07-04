package com.imooc.miaosha.util;

import java.security.*;
import java.util.Map;

import static com.paypal.base.SSLUtil.crc32;

/**
 * https://blog.csdn.net/learning_lb/article/details/76855940
 *
 <dependency>
 <groupId>com.paypal.sdk</groupId>
 <artifactId>rest-api-sdk</artifactId>
 <version>LATEST</version>
 </dependency>
 */
public class EncryptDemo {

    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static final String ENCODE_ALGORITHM = "SHA-256";
    public static final String PLAIN_TEXT = "test string";

    public static void main(String[] args) {
        // 公私钥对
        Map<String, byte[]> keyMap = RSA.generateKeyBytes();
        PublicKey publicKey = RSA.restorePublicKey(keyMap.get(RSA.PUBLIC_KEY));
        PrivateKey privateKey = RSA.restorePrivateKey(keyMap.get(RSA.PRIVATE_KEY));
        // 签名
        byte[] sing_byte = sign(privateKey, PLAIN_TEXT);
        // 验签
        verifySign(publicKey, PLAIN_TEXT, sing_byte);
    }

    /**
     * 签名
     *
     * @param privateKey 私钥
     * @param plain_text 明文
     * @return
     */
    public static byte[] sign(PrivateKey privateKey, String plain_text) {
        MessageDigest messageDigest;
        byte[] signed = null;
        try {
            // 摘要
            messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
            messageDigest.update(plain_text.getBytes());
            byte[] outputDigest_sign = messageDigest.digest();
            System.out.println("SHA-256加密后-----》" + bytesToHexString(outputDigest_sign));

            String digest = String.format("%s|%s|%s|%s", "93420a20-7b7c-11e8-a6f7-0de0de4a06f7",
                    "2018-06-29T09:12:41Z", "6CU06077US238882T", crc32("{id=WH-2WR32451HC0233532-67976317FL4543714, event_version=1.0, create_time=2014-10-23T17:23:52Z, resource_type=sale, event_type=PAYMENT.SALE.COMPLETED, summary=A successful sale payment was made for $ 0.48 USD, resource={id=80021663DE681814L, create_time=2014-10-23T17:22:56Z, update_time=2014-10-23T17:23:04Z, amount={total=0.48, currency=USD}, payment_mode=ECHECK, state=completed, protection_eligibility=ELIGIBLE, protection_eligibility_type=ITEM_NOT_RECEIVED_ELIGIBLE,UNAUTHORIZED_PAYMENT_ELIGIBLE, clearing_time=2014-10-30T07:00:00Z, parent_payment=PAY-1PA12106FU478450MKRETS4A, links=[{href=https://api.sandbox.paypal.com/v1/payments/sale/80021663DE681814L, rel=self, method=GET}, {href=https://api.sandbox.paypal.com/v1/payments/sale/80021663DE681814L/refund, rel=refund, method=POST}, {href=https://api.sandbox.paypal.com/v1/payments/payment/PAY-1PA12106FU478450MKRETS4A, rel=parent_payment, method=GET}]}, links=[{href=https://api.sandbox.paypal.com/v1/notifications/webhooks-events/WH-2WR32451HC0233532-67976317FL4543714, rel=self, method=GET}, {href=https://api.sandbox.paypal.com/v1/notifications/webhooks-events/WH-2WR32451HC0233532-67976317FL4543714/resend, rel=resend, method=POST}]}"));

            // 签名
            Signature Sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            Sign.initSign(privateKey);
            Sign.update(/*outputDigest_sign*/digest.getBytes());
            signed = Sign.sign();
            System.out.println("SHA256withRSA签名后-----》" + bytesToHexString(signed));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return signed;
    }

    /**
     * 验签
     *
     * @param publicKey  公钥
     * @param plain_text 明文
     * @param signed     签名
     */
    public static boolean verifySign(PublicKey publicKey, String plain_text, byte[] signed) {

        MessageDigest messageDigest;
        boolean SignedSuccess = false;
        try {
            // 摘要
            messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
            messageDigest.update(plain_text.getBytes());
            byte[] outputDigest_verify = messageDigest.digest();
            //System.out.println("SHA-256加密后-----》" +bytesToHexString(outputDigest_verify));

            String digest = String.format("%s|%s|%s|%s", "93420a20-7b7c-11e8-a6f7-0de0de4a06f7",
                    "2018-06-29T09:12:41Z", "6CU06077US238882T", crc32("{id=WH-2WR32451HC0233532-67976317FL4543714, event_version=1.0, create_time=2014-10-23T17:23:52Z, resource_type=sale, event_type=PAYMENT.SALE.COMPLETED, summary=A successful sale payment was made for $ 0.48 USD, resource={id=80021663DE681814L, create_time=2014-10-23T17:22:56Z, update_time=2014-10-23T17:23:04Z, amount={total=0.48, currency=USD}, payment_mode=ECHECK, state=completed, protection_eligibility=ELIGIBLE, protection_eligibility_type=ITEM_NOT_RECEIVED_ELIGIBLE,UNAUTHORIZED_PAYMENT_ELIGIBLE, clearing_time=2014-10-30T07:00:00Z, parent_payment=PAY-1PA12106FU478450MKRETS4A, links=[{href=https://api.sandbox.paypal.com/v1/payments/sale/80021663DE681814L, rel=self, method=GET}, {href=https://api.sandbox.paypal.com/v1/payments/sale/80021663DE681814L/refund, rel=refund, method=POST}, {href=https://api.sandbox.paypal.com/v1/payments/payment/PAY-1PA12106FU478450MKRETS4A, rel=parent_payment, method=GET}]}, links=[{href=https://api.sandbox.paypal.com/v1/notifications/webhooks-events/WH-2WR32451HC0233532-67976317FL4543714, rel=self, method=GET}, {href=https://api.sandbox.paypal.com/v1/notifications/webhooks-events/WH-2WR32451HC0233532-67976317FL4543714/resend, rel=resend, method=POST}]}"));

            // 签名
            Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifySign.initVerify(publicKey);
            verifySign.update(/*outputDigest_verify*/digest.getBytes());
            SignedSuccess = verifySign.verify(signed);
            System.out.println("验证成功？---" + SignedSuccess);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return SignedSuccess;
    }

    /**
     * bytes[]换成16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

}
