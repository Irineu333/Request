package com.neo.request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Stream {
    static String read(InputStream inputStream) throws IOException {

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder result = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }

        reader.close();

        return result.toString();
    }

    public static boolean put(OutputStream outputStream, String text) {
        BufferedWriter writer = null;
       try {
           OutputStream out = new BufferedOutputStream(outputStream);
           writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
           writer.write(text);
           writer.flush();
           return true;
       } catch (IOException e) {
           e.printStackTrace();

           try {
               writer.close();
           } catch (IOException e2) {
               e2.printStackTrace();
           }

           return false;
       }
    }
}
