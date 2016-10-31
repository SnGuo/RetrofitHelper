package com.sag.retrofitlibrary.base;

import android.util.Base64;
import android.util.Log;

import com.sag.foundationlibrary.base.initial.InitialHelper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * 加密器
 * <p>
 * Created by SAG on 2016/10/11 0011.
 */

class DESUtil {

    private static final String KEY = InitialHelper.getDesKey();//秘钥

    /**
     * 解密
     *
     * @param message 需要解密的字符串
     * @return 返回解密后字符串
     */
    public static String decryptDoNet(String message) {
        byte[] bytesrc = Base64.decode(message.getBytes(), Base64.DEFAULT);
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(KEY.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(KEY.getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] retByte = cipher.doFinal(bytesrc);
            return new String(retByte);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DES", "解密异常：" + message);
            return message;
        }

    }

    /**
     * 加密
     *
     * @param message 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String encryptAsDoNet(String message) {
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(KEY.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(KEY.getBytes("UTF-8"));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encryptbyte = cipher.doFinal(message.getBytes());
            return new String(Base64.encode(encryptbyte, Base64.DEFAULT));
        } catch (Exception e) {
            return message;
        }
    }


}
