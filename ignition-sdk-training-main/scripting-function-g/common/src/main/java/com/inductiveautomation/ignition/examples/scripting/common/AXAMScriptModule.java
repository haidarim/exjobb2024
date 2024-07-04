package com.inductiveautomation.ignition.examples.scripting.common;

import com.inductiveautomation.ignition.examples.scripting.common.communication.TcpClient;
import com.inductiveautomation.ignition.examples.scripting.common.communication.WPC;
import com.inductiveautomation.ignition.examples.scripting.common.util.KeyValueString;

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

    @Override
    protected boolean isRunning() {
        return tcpClient.isRunning();
    }

    @Override
    protected String send(String request) {
        return tcpClient.send(request);
    }

    @Override
    protected void setConnectionTimeout(int connectionTimeout) {
        tcpClient.setConnectionTimeout(connectionTimeout);
    }

    @Override
    protected void setReadTimeout(int readTimeout) {
        tcpClient.setReadTimeout(readTimeout);
    }

    @Override
    protected int getReadTimeout() {
        return tcpClient.getReadTimeout();
    }

    @Override
    protected int getConnectionTimeout() {
        return tcpClient.getConnectionTimeout();
    }

    @Override
    protected String sendRequestByPipe(String request, boolean timerOn, int maxTimeInSec) {
        return WPC.sendRequestByPipe(request,timerOn,maxTimeInSec);
    }

    @Override
    protected boolean pipeExists(String pipeName) {
        return WPC.pipeExists(pipeName);
    }

    @Override
    protected String getJStringValue(String JString, String key) {
        return KeyValueString.getValue(JString,key);
    }

    @Override
    protected String toValidRequest(String key, String method, String reqId, String body) {
        return KeyValueString.toValidKeyValueRequest(key, method,reqId, body);
    }

    @Override
    protected String getResponseBody(String response) {
        return KeyValueString.getResponseBody(response);
    }

}
