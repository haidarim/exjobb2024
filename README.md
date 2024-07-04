# Used classes in ignition-scripting module:

**<h3>Added packages:</h3>**
- communication
- util 

**<h3>ignition-sdk-training-main/scripting-function-g/common/src/main/java/com/inductiveautomation/ignition/examples/scripting/common/AbstractScriptModule.java</h3>**
```java
public class AXAMScriptModule extends AbstractScriptModule{

    private final TcpClient tcpClient = new TcpClient("127.0.0.1", 2024, 4000, 5000);


    @Override
    protected void start() {
        tcpClient.start();
    }

    @Override
    protected void stop() {
        tcpClient.tearDown();
    }


    /**Other implementations
     * ...
    */
}
```

**<h3>./ignition-sdk-training-main/scripting-function-g/common/src/main/java/com/inductiveautomation/ignition/examples/scripting/common/AXAMScriptModule.java</h3>**
```java
public abstract class AbstractScriptModule {
    public static final String MODULE_ID = "com.inductiveautomation.ignition.examples.scripting.ScriptingFunctionG";

    static {
        BundleUtil.get().addBundle(
                AbstractScriptModule.class.getSimpleName(),
                AbstractScriptModule.class.getClassLoader(),
                AbstractScriptModule.class.getName().replace('.', '/')
        );
    }



    // tcp communication..............................................................
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void axStart () {
        start();
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void axStop () {
        stop();
    }

    /**Other implementations
     * ...
    */


    // ...........................Abstractions to be implemented  ...............................................
    protected abstract void start();
    protected abstract void stop();

    /**Other implementations
     * ...
    */

}
```


# project directory to changed/created packages in ignition scripting module:

```sh

$ tree  ./ignition-sdk-training-main/scripting-function-g/common/src/main/java/com/inductiveautomation/ignition/examples/scripting/common/              
ignition-sdk-training-main/scripting-function-g/common/src/main/java/com/inductiveautomation/ignition/examples/scripting/common/
├── AXAMScriptModule.java # Implementation of the ./AbstractScriptModule.java
├── AbstractScriptModule.java # BeansInfo 
├── MathBlackBox.java
├── communication
│   ├── TcpClient.java
│   └── WPC.java
└── util
    ├── CentralLogger.java
    ├── KeyValueString.java
    └── Zipper.java

3 directories, 8 files
```

# Other READMEs
- [Ignition-Module](./ignitionModule/ignitionClientAxam/axClient/src/README.md)
- [AXAM-Module](./serverSim_axamModule/ServerSim/ServerSim/README.md)

# Classes' Links:
- [TcpServer.cs](./serverSim_axamModule/ServerSim/ServerSim/communication/TcpServer.cs)
- [TcpClient.java](./ignition-sdk-training-main/scripting-function-g/common/src/main/java/com/inductiveautomation/ignition/examples/scripting/common/communication/TcpClient.java)

see also:
- [RequestHandler.cs](./serverSim_axModule/SimServer/SimServer/util/RequestHandler.cs)
- [Zipper.java](./ignitionModule/ignitionClientAxam/axClient/src/util/Zipper.java)
- [Zipper.cs](serverSim_axamModule/ServerSim/ServerSim/utils/Zipper.cs)
- [KeyValueString.java](./ignitionModule/ignitionClientAxam/axClient/src/util/KeyValueString.java)

# Using in Designer:
In the current file directory there is a script file that show how the module can be used later in Ignition Desigenr using Scripting. 
