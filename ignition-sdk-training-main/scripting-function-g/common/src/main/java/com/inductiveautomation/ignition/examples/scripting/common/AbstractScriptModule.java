package com.inductiveautomation.ignition.examples.scripting.common;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

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

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public boolean axRunning () {
        return isRunning();
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public String axSend (@ScriptArg("request") String request) {
        return send(request);
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void axSetConnectionTimeout(@ScriptArg("connectionTimeout") int connectionTimeout) {
        setConnectionTimeout(connectionTimeout);
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public void axSetReadTimeout(@ScriptArg("readTimeout") int readTimeout) {
        setReadTimeout(readTimeout);
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public int axGetReadTimeout() {
        return getReadTimeout();
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public int axGetConnectionTimeout() {
        return getConnectionTimeout();
    }



    // pipe communication...................................................................
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public String axPipe(@ScriptArg("request") String request, @ScriptArg("timerOn") boolean timerOn, @ScriptArg("maxTimeInSec") int maxTimeInSec) {
        return sendRequestByPipe(request, timerOn, maxTimeInSec);
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public boolean axPipeExists (@ScriptArg("pipeName") String pipeName) {
        return pipeExists(pipeName);
    }


    // keyValue utility......................................................................
    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public String axGetJStringValue (@ScriptArg("JString") String JString, @ScriptArg("key") String key) {
        return getJStringValue(JString, key);
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public String axToValidRequest (@ScriptArg("key") String key, @ScriptArg("method") String method, @ScriptArg("reqId") String reqId, @ScriptArg("body") String body) {
        return toValidRequest(key, method, reqId, body);
    }

    @ScriptFunction(docBundlePrefix = "AbstractScriptModule")
    public String axGetResponseBody (@ScriptArg("response") String response) {
        return getResponseBody(response);
    }

    // ...........................Abstractions to be implemented  ...............................................
    protected abstract void start();
    protected abstract void stop();
    protected abstract boolean isRunning();
    protected abstract String send(String request);
    protected  abstract  void setConnectionTimeout(int connectionTimeout);
    protected  abstract  void setReadTimeout(int readTimeout);
    protected abstract  int getReadTimeout();
    protected abstract  int getConnectionTimeout();

    protected abstract  String sendRequestByPipe(String request, boolean timerOn, int maxTimeInSec);
    protected abstract boolean pipeExists(String pipeName);

    protected  abstract String getJStringValue(String JString, String key);
    protected abstract String toValidRequest(String key, String method, String reqId, String body);
    protected abstract String getResponseBody(String response);
}
