using System;
using System.IO;
using System.IO.Compression;
using System.Text;
using log4net;
using ServerSim.Log;

namespace ServerSim.utils
{
    public class Zipper
    {
        private static readonly ILog _log = LogManager.GetLogger(typeof(Zipper));

        public Zipper() { }


        public  byte[] Compress(string data)
        {
            using (MemoryStream memoryStream = new MemoryStream())
            {
                using (GZipStream gzipStream = new GZipStream(memoryStream, CompressionMode.Compress, true))
                {
                    byte[] bytes = Encoding.UTF8.GetBytes(data);
                    gzipStream.Write(bytes, 0, bytes.Length);
                }
                // MemoryStream should not be closed before the data is read
                CentralLogger.LogInfo($"Compressed from: {data.Length} bytes to: {memoryStream.Length} bytes", _log);
                return memoryStream.ToArray();
            }
        }

        public  string Decompress(byte[] compressedData)
        {
            using (MemoryStream memoryStream = new MemoryStream(compressedData))
            {
                using (GZipStream gzipStream = new GZipStream(memoryStream, CompressionMode.Decompress))
                {
                    using (StreamReader streamReader = new StreamReader(gzipStream, Encoding.UTF8))
                    {
                        return streamReader.ReadToEnd();
                    }
                }
            }
        }
    }
}
