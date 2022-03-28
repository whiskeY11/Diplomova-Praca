package com.example.geoapp.misc;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class Compression {

    public static byte[] compress(final String str) throws IOException {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.flush();
        gzip.close();
        return obj.toByteArray();
    }

    public static String decompress(final byte[] compressed) throws IOException {
        final StringBuilder outStr = new StringBuilder();

        InputStream inflInstreamInner = new InflaterInputStream(new ByteArrayInputStream(compressed),
                new Inflater(true));
        final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inflInstreamInner, "UTF-8"));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            outStr.append(line);
        }

        return outStr.toString();
    }

    public static byte[] decodeBase64(String s) {
        return Base64.decode(s, Base64.DEFAULT);
    }
}
