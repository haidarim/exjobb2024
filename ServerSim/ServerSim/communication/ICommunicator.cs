using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerSim.communication
{
    public interface ICommunicator
    {

        public void Start();
        public void Stop();
        public bool IsRunning();
    }
}
