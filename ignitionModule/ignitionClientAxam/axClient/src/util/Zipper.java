package  util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.logging.Logger;



/**
 * The Zipper class is responsible to compress string-data as well as decompress to the original format.
 * This class utilizes:
 *  GZIPOutputStream
 *  and
 *  GZIPInputStream
 *
 *  the Zipper utilizes UTF8 as the encoding format
 *
 * @author  Mehdi Haidari
 * */

public class Zipper {

    /**
     * @param data, string data to be compressed
     * @throws RuntimeException if any I/O Exception occurs during compression
     * @return  byte[] representing compressed data
     * */
    public static byte[] compress(String data) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param compressedData, byte[] data to be decompressed
     * @throws RuntimeException if any I/O Exception occurs during decompression
     * @return  String representing decompressed data
     * */
    public static String decompress(byte[] compressedData) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzipInputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            }
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }
}
