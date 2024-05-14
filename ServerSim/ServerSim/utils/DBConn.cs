using StackExchange.Redis;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.utils
{


    public class DBConn
    {
        private static readonly IDatabase db = ConnectionMultiplexer.Connect("localhost").GetDatabase();


        public static bool SetData(string key, string value)
        {
            lock (db) {
                return db.StringSet(key, value);
            }
        }

        public static string GetData(string key)
        {   
            var res = db.StringGet(key) ;
            return res.HasValue ? res.ToString() : "null";
        }
    }
}
