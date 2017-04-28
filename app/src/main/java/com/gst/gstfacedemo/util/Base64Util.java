package com.gst.gstfacedemo.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by zhenghangxia on 17-4-28.
 */

public class Base64Util {

    // 加密
    public static String encode(String str) {
        byte[] b = null;
        String s = null;
        try {
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            s = Base64.encodeToString(b, Base64.NO_WRAP);
        }
        return s;
    }

    // 解密
    public static String decode(String s) {
        byte[] b = null;
        String result = null;
        if (s != null) {
            try {
                b = Base64.decode(s.getBytes(), Base64.NO_WRAP);
                result = new String(b,"utf-8");
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
