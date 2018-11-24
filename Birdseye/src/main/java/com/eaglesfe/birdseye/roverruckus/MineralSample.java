package com.eaglesfe.birdseye.roverruckus;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;

class MineralSample {
    // Intentionally public to obviate the need for a custom TypeAdapter for serialization
    public final List<Integer> goldMineralLocations = new ArrayList<>();
    public final List<Integer> silverMineralLocations = new ArrayList<>();
    public GoldMineralArrangement goldMineralArrangement;
    public int silverSampleSize = 0;
    public int goldSampleSize = 0;
    public int sampleSize = 0;


    MineralSample(List<Recognition> recognitions) {
        for (Recognition recognition : recognitions) {
            sampleSize++;
            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                goldMineralLocations.add((int) recognition.getLeft());
                goldSampleSize++;
            } else {
                silverMineralLocations.add((int) recognition.getLeft());
                silverSampleSize++;
            }
        }

        goldMineralArrangement = getGoldMineralArrangement();
    }

    private GoldMineralArrangement getGoldMineralArrangement() {
        if (goldMineralLocations.size() == 1 && silverMineralLocations.size() == 2) {
            int goldMineralX = goldMineralLocations.get(0);
            int silverMineral1X = silverMineralLocations.get(0);
            int silverMineral2X = silverMineralLocations.get(1);

            if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                    return GoldMineralArrangement.LEFT;
                } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                    return GoldMineralArrangement.RIGHT;
                } else {
                    return GoldMineralArrangement.CENTER;
                }
            }
        }
        return GoldMineralArrangement.UNKNOWN;
    }

    public enum GoldMineralArrangement {
        UNKNOWN, LEFT, CENTER, RIGHT
    }
}