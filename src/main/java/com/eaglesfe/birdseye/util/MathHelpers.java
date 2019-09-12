package com.eaglesfe.birdseye.util;

import android.graphics.Point;

import static java.lang.Double.NaN;

public class MathHelpers {

    public static double getDistanceBetweenTwoPoints (Point start, Point end) {
        float o = end.y - start.y;
        float a = end.x - start.x;
        return Math.sqrt(Math.pow(o, 2) + Math.pow(a, 2));
    }

    public static double getAngleBetweenTwoPoints(Point start, Point end) {
        float o = end.y - start.y;
        float a = end.x - start.x;

        double inRads = Math.atan2(o, a);
        return  (inRads >= 0 ? inRads : inRads + (2 * Math.PI)) * 180 / Math.PI;
    }

    public static double max(double ...values) {
        double max = NaN;
        for (int i = 0; i < values.length; i++) {
            max = values[i] > max ? values[i] : max;
        }
        return max;
    }

    public static double min(double ...values) {
        double min = NaN;
        for (int i = 0; i < values.length; i++) {
            min = values[i] > min ? values[i] : min;
        }
        return min;
    }
}

