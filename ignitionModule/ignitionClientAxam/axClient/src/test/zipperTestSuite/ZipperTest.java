package test.zipperTestSuite;

import org.junit.jupiter.api.Test;
import util.CentralLogger;
import util.Zipper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This class tests the functionality and behavior of the Zipper class
 * @author Mehdi Haidari
 * */
public class ZipperTest {

    private final Logger _log = Logger.getLogger(ZipperTest.class.getSimpleName());



    // test compressor and decompressor functionality by checking data consistent
    @Test
    void testCompressAndDecompressFunctionalityAndDataConsistent(){

        String _5MB = readFile("./src/test/textfile/5MB.txt");
        assertEquals(_5MB, compressAndDecompress(_5MB));

        String _10MB = readFile("./src/test/textfile/10MB.txt");
        assertEquals(_10MB, compressAndDecompress(_10MB));

        String _50MB = readFile("./src/test/textfile/50MB.txt");
        assertEquals(_50MB, compressAndDecompress(_50MB));

        String _100MB = readFile("./src/test/textfile/100MB.txt");
        assertEquals(_100MB, compressAndDecompress(_100MB));
    }


    private String compressAndDecompress(String data) {
        try {
            byte[] compressed = Zipper.compress(data);
            CentralLogger.logInfo("compressed data from: " + data.length() + " to: "+ compressed.length, _log);
            return Zipper.decompress(compressed);
        } catch (Exception e) {
            CentralLogger.logInfo("Failed when testing data consistency: " + e.getMessage(), _log);
            throw new RuntimeException("very pain", e);
        }
    }

    private String readFile(String filePath){
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            CentralLogger.logInfo("Failed when testing data consistency: " + e.getMessage(), _log);
            throw new RuntimeException("very pain", e);
        }
        return sb.toString();
    }
}
