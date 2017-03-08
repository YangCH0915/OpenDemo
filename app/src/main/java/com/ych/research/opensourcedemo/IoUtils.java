package com.ych.research.opensourcedemo;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

public class IoUtils {

    public static void closeAll(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
