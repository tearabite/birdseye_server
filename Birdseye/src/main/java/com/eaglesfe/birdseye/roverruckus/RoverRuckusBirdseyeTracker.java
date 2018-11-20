package com.eaglesfe.birdseye.roverruckus;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import com.eaglesfe.birdseye.BirdseyeTracker;

public class RoverRuckusBirdseyeTracker extends BirdseyeTracker
{
    private static final float mmTargetHeight   = 6 * VuforiaBase.MM_PER_INCH;          // the height of the center of the target image above the floor

    protected VuforiaTrackables getTrackables() {

        // Load the data sets that for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        VuforiaTrackables trackables = vuforia.loadTrackablesFromAsset("RoverRuckus");

        VuforiaTrackable blueRover = trackables.get(0);
        blueRover.setName("Blue-Rover");
        OpenGLMatrix blueRoverLocationOnField = OpenGLMatrix
                .translation(0, VuforiaBase.MM_FTC_FIELD_WIDTH / 2, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90, 0, 0));
        blueRover.setLocation(blueRoverLocationOnField);

        VuforiaTrackable redFootprint = trackables.get(1);
        redFootprint.setName("Red-Footprint");
        OpenGLMatrix redFootprintLocationOnField = OpenGLMatrix
                .translation(0, -VuforiaBase.MM_FTC_FIELD_WIDTH / 2, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90, 0, 180));
        redFootprint.setLocation(redFootprintLocationOnField);

        VuforiaTrackable frontCraters = trackables.get(2);
        frontCraters.setName("Front-Craters");
        OpenGLMatrix frontCratersLocationOnField = OpenGLMatrix
                .translation(-VuforiaBase.MM_FTC_FIELD_WIDTH / 2, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90, 0 , 90));
        frontCraters.setLocation(frontCratersLocationOnField);

        VuforiaTrackable backSpace = trackables.get(3);
        backSpace.setName("Back-Space");
        OpenGLMatrix backSpaceLocationOnField = OpenGLMatrix
                .translation(VuforiaBase.MM_FTC_FIELD_WIDTH / 2, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90, 0, -90));
        backSpace.setLocation(backSpaceLocationOnField);

        return trackables;
    }
}

