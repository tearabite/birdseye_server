package com.eaglesfe.birdseye.roverruckus;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import com.eaglesfe.birdseye.BirdseyeTracker;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_GOLD_MINERAL;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.LABEL_SILVER_MINERAL;
import static org.firstinspires.ftc.robotcore.external.tfod.TfodRoverRuckus.TFOD_MODEL_ASSET;

public class RoverRuckusBirdseyeTracker extends BirdseyeTracker
{
    private static final float mmTargetHeight   = 6 * VuforiaBase.MM_PER_INCH;          // the height of the center of the target image above the floor
    private TFObjectDetector tfod;

    @Override
    public void start() {
        super.start();
        tfod.activate();
    }

    @Override
    public void stop() {
        super.stop();
        tfod.shutdown();
    }

    @Override
    public void initialize(HardwareMap hardwareMap, String webcamName, boolean preview) {
        super.initialize(hardwareMap, webcamName, preview);

        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters();
        if (preview) {
            tfodParameters.tfodMonitorViewIdParent = hardwareMap.appContext.getResources().getIdentifier("tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        }

        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

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

    public MineralSample trySampleMinerals() {
        assertInitialized();
        assertTrackingStarted();

        return new MineralSample(tfod.getRecognitions());
    }
}

