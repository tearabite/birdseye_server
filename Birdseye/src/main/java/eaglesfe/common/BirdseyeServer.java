package eaglesfe.common;

import com.google.gson.Gson;
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

public class BirdseyeServer {

    private BirdseyeServerImpl server;
    private boolean newOutgoingData = false;
    private Gson gson;
    private JsonWriter json;
    private StringWriter string;
    private Timer timer;
    private int updateInterval = 250;

    public BirdseyeServer(int port, Telemetry telemetry) {
        try {
            this.gson = Serializers.getGson();
            beginTelemetryBlock();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.server = new BirdseyeServerImpl(port, telemetry);
        timer = new Timer();
        timer.scheduleAtFixedRate(new MessageFlushTask(), 0, updateInterval);
    }

    protected void beginTelemetryBlock() throws IOException {
        this.string = new StringWriter();
        this.json = this.gson.newJsonWriter(string);
        json.beginObject();
    }

    public void addData(String key, Object value) {
        try {
            json.name(key);
            gson.toJson(value, value.getClass(), json);
            newOutgoingData = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void beginObject(String key) throws IOException {
        this.json.name(key);
        this.json.beginObject();
    }

    public void endObject() throws IOException {
        this.json.endObject();
    }

    public void beginArray() throws IOException {
        this.json.beginObject();
    }

    public void endArray() throws IOException {
        this.json.endObject();
    }

    public String endTelemetryBlock() throws IOException {
        this.json.endObject();
        this.json.close();
        return this.string.toString();
    }

    public void update() {
        if (server.isOpen) {
            if (newOutgoingData) {
                try {
                    String telemetry = endTelemetryBlock();
                    server.broadcast(telemetry);
                    beginTelemetryBlock();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                newOutgoingData = false;
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
            update();
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