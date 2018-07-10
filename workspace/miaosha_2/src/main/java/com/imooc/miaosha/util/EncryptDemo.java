package com.imooc.miaosha.util;


import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.security.*;
import java.util.Arrays;

import static com.imooc.miaosha.util.MD5Util.bytesToHexString;
import static com.imooc.miaosha.util.MD5Util.hexStringToByte;
import static com.paypal.base.SSLUtil.crc32;

/**
 * 加密类
 * 包括对称 非对称 盐值加密demo
 * <p>
 * Base64
 * https://blog.csdn.net/wang8978/article/details/52279661
 * 盐值
 * https://www.cnblogs.com/ljp-sun/p/6553910.html
 * https://blog.csdn.net/qq_31080089/article/details/53715910
 * https://www.cnblogs.com/birdsmaller/p/5377104.html
 * 非对称
 * https://blog.csdn.net/learning_lb/article/details/76855940
 *
 * <dependency>
 * <groupId>com.paypal.sdk</groupId>
 * <artifactId>rest-api-sdk</artifactId>
 * <version>LATEST</version>
 * </dependency>
 */
public class EncryptDemo {

    public static final String SIGNATURE_ALGORITHM = "withRSA";
    public static final String ENCODE_ALGORITHM = "SHA-256";
    public static final String PLAIN_TEXT = "iucnoc-2ahdie-9dqwia";
    public static final int SALT_LENGTH = 12;

    public static void main(String[] args) throws Exception {
        // ******************************** 非对称
//        // 公私钥对
//        Map<String, byte[]> keyMap = RSA.generateKeyBytes();
//        PublicKey publicKey = RSA.restorePublicKey(keyMap.get(RSA.PUBLIC_KEY));
//        PrivateKey privateKey = RSA.restorePrivateKey(keyMap.get(RSA.PRIVATE_KEY));
//        // 签名
//        byte[] sing_byte = sign(privateKey, PLAIN_TEXT);
//        // 验签
//        verifySign(publicKey, PLAIN_TEXT, sing_byte);

//        int[] i1 = new int[]{1, 2, 3};
//        int[] i2 = new int[]{4, 5};
//        int[] i3 = new int[5];
//        System.arraycopy(i1, 0, i3, 0, 3);
//        System.arraycopy(i2, 0, i3, 3, 2);
//        System.out.println(Arrays.toString(i1));
//        System.out.println(Arrays.toString(i2));
//        System.out.println(Arrays.toString(i3));

        // ******************************** 对称

        // ******** 1.字节数组传输成功
        byte[] before = encryptSalt(PLAIN_TEXT);
        BASE64Encoder enc = new BASE64Encoder();
        // 客户端使用BASE64编码
        String mes = enc.encodeBuffer(before);
        BASE64Decoder dec = new BASE64Decoder();
        // 服务端使用BASE64解码
        byte[] after = dec.decodeBuffer(mes);
        System.out.println(verifySalt(PLAIN_TEXT, after));
        System.out.println();

        /******** 2.String传输成功
         * 使用：
         * 两端约定：盐值长度
         * 检测：传入数据 + 密钥
         */
        String simple = encryptSaltSimple(PLAIN_TEXT);
        System.out.println(verifySaltSimple(PLAIN_TEXT, simple));
        System.out.println();



        // ******** 1.16进制转换失败
        byte[] b = hexStringToByte(PLAIN_TEXT);
        System.out.println("convert 16 " + bytesToHexString(b));

        // ******** 2.简易转换成功
        byte[] bytes = PLAIN_TEXT.getBytes();
        System.out.println("convert 10 " + new String(bytes));
        System.out.println();

        // ******** MD5加密
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(("123" + PLAIN_TEXT).getBytes("UTF-8"));
        System.out.println(bytesToHexString(md.digest()));
    }

    // ************************************* 非对称加密 *************************************

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

//    /**
//     * bytes[]换成16进制字符串
//     *
//     * @param src
//     * @return
//     */
//    public static String bytesToHexString(byte[] src) {
//        StringBuilder stringBuilder = new StringBuilder("");
//        if (src == null || src.length <= 0) {
//            return null;
//        }
//        for (int i = 0; i < src.length; i++) {
//            int v = src[i] & 0xFF;
//            String hv = Integer.toHexString(v);
//            if (hv.length() < 2) {
//                stringBuilder.append(0);
//            }
//            stringBuilder.append(hv);
//        }
//        return stringBuilder.toString();
//    }
//
//    private static final String HEX_NUMS_STR = "0123456789ABCDEF";
//
//    /**
//     * 将16进制字符串转换成字节数组
//     *
//     * @param hex
//     * @return
//     */
//    public static byte[] hexStringToByte(String hex) {
//        int len = (hex.length() / 2);
//        byte[] result = new byte[len];
//        char[] hexChars = hex.toCharArray();
//        for (int i = 0; i < len; i++) {
//            int pos = i * 2;
//            result[i] = (byte) (HEX_NUMS_STR.indexOf(hexChars[pos]) << 4
//                    | HEX_NUMS_STR.indexOf(hexChars[pos + 1]));
//        }
//        return result;
//    }


    // ************************************* 盐值加密16进制版 *************************************
    public static byte[] encryptSalt(String plainText) throws Exception {
        //声明加密后的口令数组变量
        byte[] pwd = null;
        //随机数生成器
        SecureRandom random = new SecureRandom();
        //声明盐数组变量
        byte[] salt = new byte[SALT_LENGTH];
        //将随机数放入盐变量中
        random.nextBytes(salt);
        System.out.println("salt " + bytesToHexString(salt));

        //创建消息摘要MD5
        MessageDigest md = MessageDigest.getInstance("MD5");
        //将盐数据传入消息摘要对象
        md.update(salt);
        //将口令的数据传给消息摘要对象
        md.update(plainText.getBytes("UTF-8"));
        //获得消息摘要的字节数组
        byte[] digest = md.digest();
        System.out.println("digest " + bytesToHexString(digest));

        //因为要在口令的字节数组中存放盐，所以加上盐的字节长度
        pwd = new byte[digest.length + SALT_LENGTH];
        //将盐的字节拷贝到生成的加密口令字节数组的前12个字节，以便在验证口令时取出盐
        System.arraycopy(salt, 0, pwd, 0, SALT_LENGTH);
        //将消息摘要拷贝到加密口令字节数组从第13个字节开始的字节
        System.arraycopy(digest, 0, pwd, SALT_LENGTH, digest.length);

        //1.将字节数组格式加密后的口令转化为16进制字符串格式的口令
//        return bytesToHexString(pwd);
        //2.直接返回字节数组
        return pwd;
        //3.客户端使用BASE64编码
//        BASE64Encoder enc = new BASE64Encoder();
//        return enc.encodeBuffer(pwd);
    }

    public static boolean verifySalt(String plainText, byte[] encrypt) throws Exception {
//        //1.将16进制字符串格式口令转换成字节数组
//        byte[] pwdInDb = hexStringToByte(encrypt);
        //声明盐变量
        byte[] salt = new byte[SALT_LENGTH];
        //将盐从数据库中保存的口令字节数组中提取出来
        System.arraycopy(encrypt, 0, salt, 0, SALT_LENGTH);
        System.out.println("verify salt " + bytesToHexString(salt));

        //创建消息摘要对象
        MessageDigest md = MessageDigest.getInstance("MD5");
        //将盐数据传入消息摘要对象
        md.update(salt);
        //将口令的数据传给消息摘要对象
        md.update(plainText.getBytes("UTF-8"));
        //生成输入口令的消息摘要
        byte[] digest = md.digest();
        System.out.println("verify digest " + bytesToHexString(digest));
        //声明一个保存数据库中口令消息摘要的变量
        byte[] digestInDb = new byte[encrypt.length - SALT_LENGTH];
        //取得数据库中口令的消息摘要
        System.arraycopy(encrypt, SALT_LENGTH, digestInDb, 0, digestInDb.length);
        //比较根据输入口令生成的消息摘要和数据库中消息摘要是否相同
        if (Arrays.equals(digest, digestInDb)) {
            //口令正确返回口令匹配消息
            return true;
        } else {
            //口令不正确返回口令不匹配消息
            return false;
        }
    }

    // ************************************* 盐值加密10进制版 *************************************

    public static String encryptSaltSimple(String plainText) throws Exception {
        //声明加密后的口令数组变量
        byte[] pwd = null;
        //随机数生成器
        SecureRandom random = new SecureRandom();
        //声明盐数组变量
        byte[] salt = new byte[SALT_LENGTH];
        //将随机数放入盐变量中
        random.nextBytes(salt);
        System.out.println("salt " + bytesToHexString(salt));

        //创建消息摘要MD5
        MessageDigest md = MessageDigest.getInstance("MD5");
        //将盐数据传入消息摘要对象
        md.update(salt);
        //将口令的数据传给消息摘要对象
        md.update(plainText.getBytes("UTF-8"));
        //获得消息摘要的字节数组
        byte[] digest = md.digest();
        System.out.println("digest " + bytesToHexString(digest));

        //因为要在口令的字节数组中存放盐，所以加上盐的字节长度
        pwd = new byte[digest.length + SALT_LENGTH];
        //将盐的字节拷贝到生成的加密口令字节数组的前12个字节，以便在验证口令时取出盐
        System.arraycopy(salt, 0, pwd, 0, SALT_LENGTH);
        //将消息摘要拷贝到加密口令字节数组从第13个字节开始的字节
        System.arraycopy(digest, 0, pwd, SALT_LENGTH, digest.length);

        //1.将字节数组格式加密后的口令转化为16进制字符串格式的口令
//        return bytesToHexString(pwd);
        //2.直接返回字节数组
//        return pwd;
        //3.客户端使用BASE64编码
        BASE64Encoder enc = new BASE64Encoder();
        return enc.encodeBuffer(pwd);
    }

    public static boolean verifySaltSimple(String plainText, String encrypt) throws Exception {
        //1.将16进制字符串格式口令转换成字节数组
//        byte[] pwdInDb = hexStringToByte(encrypt);
        //2.服务端使用BASE64解码
        BASE64Decoder dec = new BASE64Decoder();
        byte[] after = dec.decodeBuffer(encrypt);

        //声明盐变量
        byte[] salt = new byte[SALT_LENGTH];
        //将盐从数据库中保存的口令字节数组中提取出来
        System.arraycopy(after, 0, salt, 0, SALT_LENGTH);
        System.out.println("verify salt " + bytesToHexString(salt));

        //创建消息摘要对象
        MessageDigest md = MessageDigest.getInstance("MD5");
        //将盐数据传入消息摘要对象
        md.update(salt);
        //将口令的数据传给消息摘要对象
        md.update(plainText.getBytes("UTF-8"));
        //生成输入口令的消息摘要
        byte[] digest = md.digest();
        System.out.println("verify digest " + bytesToHexString(digest));
        //声明一个保存数据库中口令消息摘要的变量
        byte[] digestInDb = new byte[after.length - SALT_LENGTH];
        //取得数据库中口令的消息摘要
        System.arraycopy(after, SALT_LENGTH, digestInDb, 0, digestInDb.length);
        //比较根据输入口令生成的消息摘要和数据库中消息摘要是否相同
        if (Arrays.equals(digest, digestInDb)) {
            //口令正确返回口令匹配消息
            return true;
        } else {
            //口令不正确返回口令不匹配消息
            return false;
        }
    }
}
