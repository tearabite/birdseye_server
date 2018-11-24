package com.eaglesfe.birdseye;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.eaglesfe.birdseye.util.Serializers;

public class BirdseyeServer {

    private BirdseyeServerImpl server;
    private boolean updateRequested;
    private Gson gson;
    private JsonWriter json;
    private StringWriter string;
    private Timer timer;
    private int updateInterval = 250;
    private Semaphore mutex = new Semaphore(1);

    public BirdseyeServer(int port, Telemetry telemetry) {
        try {
            this.gson = Serializers.getGson();
            startNewTelemetryBlock();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.server = new BirdseyeServerImpl(port, telemetry);
        timer = new Timer();
        timer.scheduleAtFixedRate(new MessageFlushTask(), 0, updateInterval);
    }

    protected String startNewTelemetryBlock() throws IOException {
        String telemetry = "";
        try {
            if (mutex.tryAcquire(this.updateInterval, TimeUnit.MILLISECONDS)) {
                if (json != null && string != null) {
                    this.json.endObject();
                    this.json.close();
                    telemetry = this.string.toString();
                }
                this.string = new StringWriter();
                this.json = this.gson.newJsonWriter(string);
                json.beginObject();
                mutex.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return telemetry;
    }

    public void addData(String key, Object value) {
        try {
            if (mutex.tryAcquire(this.updateInterval, TimeUnit.MILLISECONDS)) {
                json.name(key);
                gson.toJson(value, value.getClass(), json);
                mutex.release();
            }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
    }

    public void beginObject(String key) throws IOException {
        try {
            if (mutex.tryAcquire(this.updateInterval, TimeUnit.MILLISECONDS)) {
                this.json.name(key);
                this.json.beginObject();
                mutex.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void endObject() throws IOException {
        try {
            if (mutex.tryAcquire(this.updateInterval, TimeUnit.MILLISECONDS)) {
                this.json.endObject();
                mutex.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void beginArray(String key) throws IOException {
        try {
            if (mutex.tryAcquire(this.updateInterval, TimeUnit.MILLISECONDS)) {
                this.json.name(key);
                this.json.beginObject();
                mutex.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void endArray() throws IOException {
        try {
            if (mutex.tryAcquire(this.updateInterval, TimeUnit.MILLISECONDS)) {
                this.json.endObject();
                mutex.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void update() {
        this.updateRequested = true;
    }

    private void processUpdate() {
        if (server.isOpen) {
            if (this.updateRequested) {
                this.updateRequested = false;
                try {
                    String telemetry = startNewTelemetryBlock();
                    server.broadcast(telemetry);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public JSONObject getData() {
        return this.server.getIncoming();
    }

    public int getUpdateInterval() {
        return this.updateInterval;
    }

    public void setUpdateInterval(int interval) {
        this.timer.cancel();
        this.updateInterval = interval;
        timer.scheduleAtFixedRate(new MessageFlushTask(), 0, updateInterval);
    }

    class MessageFlushTask extends TimerTask {
        public void run() {
            processUpdate();
        }
    }

    public void start() {
        this.server.start();
    }

    public void stop(){
        try {
            this.server.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class BirdseyeServerImpl extends WebSocketServer {
        private boolean isOpen;
        private Telemetry opModeTelemetry;
        private JSONObject incoming;

        public BirdseyeServerImpl(int port, Telemetry telemetry) {
            super(new InetSocketAddress(port));
            setReuseAddr(true);
            opModeTelemetry = telemetry;
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            this.isOpen = true;
            conn.send("BIRDSEYE VIEW ESTABLISHED");
            postToTelemetry("BIRDSEYE VIEW ESTABLISHED!");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            this.isOpen = false;
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            this.setIncoming(message);
            postToTelemetry(message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            postToTelemetry("Server encountered an error. " + ex.getMessage());
        }

        @Override
        public void onStart() {
            postToTelemetry(String.format(Locale.US, "The eagle has perched on port %d!", this.getPort()));
            opModeTelemetry.update();
            setConnectionLostTimeout(0);
            setConnectionLostTimeout(100);
        }

        public JSONObject getIncoming() {
            return incoming;
        }

        private void setIncoming(String incoming) {
            try {
                this.incoming = new JSONObject(incoming);
            } catch (JSONException e) {
                this.incoming = null;
            }
        }

        private void postToTelemetry(String message) {
            opModeTelemetry.addData("BIRDSEYE", message);
            opModeTelemetry.update();
        }
    }
}