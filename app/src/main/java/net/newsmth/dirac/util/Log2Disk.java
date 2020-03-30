package net.newsmth.dirac.util;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Log2Disk {
    private static Log2Disk log = new Log2Disk();
    FileOutputStream fos;

    private Log2Disk() {
        try {
            fos = new FileOutputStream(
                    new File(Environment.getExternalStorageDirectory(), "log.txt"),
                    true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Log2Disk getInstance() {
        return log;
    }

    public Log2Disk log(String msg) {
        if (fos == null || TextUtils.isEmpty(msg)) {
            return this;
        }
        try {
            fos.write(msg.getBytes(StandardCharsets.UTF_8));
            fos.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void close() {
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
