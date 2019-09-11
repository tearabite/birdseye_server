package com.eaglesfe.birdseye.roverruckus;

import android.graphics.Point;
import android.graphics.Rect;

import com.vuforia.Rectangle;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;

public class MineralSample {
    // Intentionally public to obviate the need for a custom TypeAdapter for serialization
    public final List<Point> goldMineralLocations = new ArrayList<>();
    public final List<Point> silverMineralLocations = new ArrayList<>();
    public GoldMineralArrangement goldMineralArrangement;
    public GoldMineralArrangementFromLander goldMineralArrangementFromLander;
    public int silverSampleSize = 0;
    public int goldSampleSize = 0;
    public int sampleSize = 0;
    public double angleToGoldMineral = Double.MIN_VALUE;
    public Rect boundingBox;
    public Rect largestGoldMineralBoundingBox = new Rect(0,0,0,0);

    MineralSample(List<Recognition> recognitions) {
        this.boundingBox = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for (Recognition recognition : recognitions) {
            Rect recognitionBoundingBox = getBoundingBox(recognition);
            Point position = new Point(recognitionBoundingBox.centerX(), recognitionBoundingBox.centerY());

            sampleSize++;
            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                if (recognitionBoundingBox.width() * recognitionBoundingBox.height() > largestGoldMineralBoundingBox.width() * largestGoldMineralBoundingBox.height()) {
                    largestGoldMineralBoundingBox = recognitionBoundingBox;
                }
                goldMineralLocations.add(position);
                goldSampleSize++;
                angleToGoldMineral = goldSampleSize == 1 ? recognition.estimateAngleToObject(AngleUnit.DEGREES) : Double.MIN_VALUE;
            } else {
                silverMineralLocations.add(position);
                silverSampleSize++;
            }

            boundingBox.union(recognitionBoundingBox);
        }

        goldMineralArrangement = getGoldMineralArrangement();
        goldMineralArrangementFromLander = getGoldMineralArrangementFromLander();
    }

    private Rect getBoundingBox(Recognition recognition) {
        float totalWidth = recognition.getImageWidth();
        float totalHeight = recognition.getImageHeight();

        float left = (recognition.getLeft() / totalWidth) * 100;
        float right = (recognition.getRight() / totalWidth) * 100;
        float top = (recognition.getTop() / totalHeight) * 100;
        float bottom = (recognition.getBottom() / totalHeight) * 100;

        Rect recognitionBoundingBox = new Rect((int)left, (int)top, (int)right, (int)bottom);

        return recognitionBoundingBox;
    }

    private GoldMineralArrangement getGoldMineralArrangement() {
        if (goldMineralLocations.size() == 1 && silverMineralLocations.size() == 2) {
            double goldMineralX = goldMineralLocations.get(0).x;
            double silverMineral1X = silverMineralLocations.get(0).x;
            double silverMineral2X = silverMineralLocations.get(1).x;
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

    private GoldMineralArrangementFromLander getGoldMineralArrangementFromLander() {
        if (sampleSize == 2) {
            if (goldSampleSize == 1 && silverSampleSize == 1) {
                if (goldMineralLocations.get(0).x > silverMineralLocations.get(0).x) {
                    return  GoldMineralArrangementFromLander.RIGHT;
                } else {
                    return  GoldMineralArrangementFromLander.CENTER;
                }
            } else if (silverSampleSize == 2) {
                return GoldMineralArrangementFromLander.LEFT;
            }
        } else if (goldSampleSize == 1 && silverSampleSize == 2) {
            if (goldMineralLocations.get(0).x > silverMineralLocations.get(0).x && goldMineralLocations.get(0).x > silverMineralLocations.get(1).x) {
                return GoldMineralArrangementFromLander.RIGHT;
            } else if ( goldMineralLocations.get(0).x > silverMineralLocations.get(0).x && goldMineralLocations.get(0).x < silverMineralLocations.get(1).x ||
                    goldMineralLocations.get(0).x < silverMineralLocations.get(0).x && goldMineralLocations.get(0).x > silverMineralLocations.get(1).x
                    ) {
                return  GoldMineralArrangementFromLander.CENTER;
            } else if (goldMineralLocations.get(0).x < silverMineralLocations.get(0).x && goldMineralLocations.get(0).x < silverMineralLocations.get(1).x) {
                return GoldMineralArrangementFromLander.LEFT;
            }
        }
        return GoldMineralArrangementFromLander.UNKNOWN;
    }

    public enum  GoldMineralArrangementFromLander {
        UNKNOWN, LEFT, CENTER, RIGHT
    }
}