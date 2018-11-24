package com.eaglesfe.birdseye.util;

import com.eaglesfe.birdseye.roverruckus.RoverRuckusBirdseyeTracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;

import java.io.IOException;

import com.eaglesfe.birdseye.FieldPosition;

public class Serializers {

    public static Gson getGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Serializers.register(gsonBuilder);
        return gsonBuilder.create();
    }

    public static void register(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(VectorF.class, new VectorAdapter());
        gsonBuilder.registerTypeAdapter(FieldPosition.class, new FieldPositionAdapter());
    }
}

class FieldPositionAdapter extends TypeAdapter<FieldPosition> {
    @Override
    public void write(JsonWriter writer, FieldPosition value) throws IOException {
        if (value == null){
            writer.nullValue();
            return;
        }

        writer.beginObject();

        writer.name("x").value(value.getX());
        writer.name("y").value(value.getY());
        writer.name("z").value(value.getZ());
        writer.name("pitch").value(value.getPitch());
        writer.name("roll").value(value.getRoll());
        writer.name("heading").value(value.getHeading());
        writer.name("acquisitionTime").value(value.getAcquisitionTime());

        writer.endObject();
    }

    @Override
    public FieldPosition read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        float x, y, z, pitch, roll, heading;
        long acquisitionTime;
        x = y = z = pitch = roll = heading = acquisitionTime = 0;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "x":
                    x = Float.parseFloat(reader.nextString()) * VuforiaBase.MM_PER_INCH;
                    break;
                case "y":
                    y = Float.parseFloat(reader.nextString()) * VuforiaBase.MM_PER_INCH;
                    break;
                case "z":
                    z = Float.parseFloat(reader.nextString()) * VuforiaBase.MM_PER_INCH;
                    break;
                case "pitch":
                    pitch = Float.parseFloat(reader.nextString());
                    break;
                case "roll":
                    roll = Float.parseFloat(reader.nextString());
                    break;
                case "heading":
                    heading = Float.parseFloat(reader.nextString());
                    break;
                case "acquisitionTime":
                    acquisitionTime = Long.parseLong(reader.nextString());
                    break;
            }
        }
        reader.endObject();

        return FieldPosition.forParsed(x, y, z, pitch, roll, heading, acquisitionTime);
    }
}

class VectorAdapter extends TypeAdapter<VectorF> {
    private final String[] keys = { "x", "y", "z", "w" };

    @Override
    public void write(JsonWriter writer, VectorF value) throws IOException {
        if (value == null){
            writer.nullValue();
            return;
        }

        float[] data = value.getData();
        int rank = data.length;

        writer.beginObject();

        for(int i = 0; i < rank; i++) {
            writer.name(keys[i]).value(value.get(i));
        }
        writer.endObject();
    }

    @Override
    public VectorF read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        float x, y, z, w;
        x = y = z = w = 0;
        int card = 0;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "x":
                    x = Float.parseFloat(reader.nextString());
                    break;
                case "y":
                    y = Float.parseFloat(reader.nextString());
                    break;
                case "z":
                    z = Float.parseFloat(reader.nextString());
                    break;
                case "w":
                    w = Float.parseFloat(reader.nextString());
                    break;
            }
            card++;
        }
        reader.endObject();

        switch(card) {
            case 1:
                return new VectorF(x);
            case 2:
                return new VectorF(x, y);
            case 3:
                return new VectorF(x, y, z);
            case 4:
                return new VectorF(x, y, z, w);
            default:
                return null;
        }
    }
}