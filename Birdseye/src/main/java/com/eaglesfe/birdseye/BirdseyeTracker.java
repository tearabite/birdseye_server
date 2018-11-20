package com.eaglesfe.birdseye;

import android.support.annotation.NonNull;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.Parameters;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

public abstract class BirdseyeTracker
{
    private String vuforiaKey;

    protected VuforiaLocalizer vuforia;
    private VuforiaTrackables trackables;
    private boolean isActive;
    private boolean isInitialized;

    private int cameraForwardOffsetMm = 0;
    private int cameraVerticalOffsetMm = 0;
    private int cameraLeftOffsetMm = 0;
    private int cameraAngleOffsetDeg = 0;

    public BirdseyeTracker() { }

    public BirdseyeTracker(String vuforiaKey) { this.vuforiaKey = vuforiaKey; }

    public String getVuforiaKey() {
        return vuforiaKey;
    }

    public void setVuforiaKey(String vuforiaKey) {
        this.vuforiaKey = vuforiaKey;
    }

    public void setCameraForwardOffset(float offset) {
        this.cameraForwardOffsetMm = (int)(offset * VuforiaBase.MM_PER_INCH);
    }

    public void setCameraVerticalOffset(float offset) {
        this.cameraVerticalOffsetMm = (int)(offset * VuforiaBase.MM_PER_INCH);
    }

    public void cameraLeftOffsetMm(float offset) {
        this.cameraLeftOffsetMm = (int)(offset * VuforiaBase.MM_PER_INCH);
    }

    public void setCameraRotationalOffset(int offset) {
        this.cameraAngleOffsetDeg = offset;
    }

    public void initialize(HardwareMap hardwareMap, String webcamName, boolean preview) {

        //  Instantiate the Vuforia engine
        Parameters parameters = getVuforiaParameters(hardwareMap, webcamName, preview);

        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Get the trackables from the derived class.
        trackables = getTrackables();

        if (webcamName != null){
            OpenGLMatrix cameraLocationOnRobot = OpenGLMatrix
                    .translation(cameraForwardOffsetMm, cameraLeftOffsetMm, cameraVerticalOffsetMm)
                    .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XZY, AngleUnit.DEGREES, 90, cameraAngleOffsetDeg, 0));

            for (VuforiaTrackable trackable : trackables)
            {
                ((VuforiaTrackableDefaultListener)trackable.getListener()).setCameraLocationOnRobot(parameters.cameraName, cameraLocationOnRobot);
            }
        }
        else {
            OpenGLMatrix phoneLocationOnRobot = OpenGLMatrix
                    .translation(cameraForwardOffsetMm, cameraLeftOffsetMm, cameraVerticalOffsetMm)
                    .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.YZX, AngleUnit.DEGREES, cameraAngleOffsetDeg - 90, 0, 0));

            for (VuforiaTrackable trackable : trackables)
            {
                ((VuforiaTrackableDefaultListener)trackable.getListener()).setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
            }
        }

        isInitialized = true;
    }

    @NonNull
    protected Parameters getVuforiaParameters(HardwareMap hardwareMap, String webcamName, boolean preview) {
        Parameters parameters = new Parameters();
        parameters.vuforiaLicenseKey = vuforiaKey;

        if (preview){
            parameters.cameraMonitorViewIdParent = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        }

        boolean useWebcam = webcamName != null;
        if (useWebcam) {
            parameters.cameraName = hardwareMap.get(WebcamName.class, webcamName);
        } else {
            parameters.cameraDirection  = CameraDirection.BACK;
        }

        return parameters;
    }

    public void start()
    {
        assertInitialized();

        if (!isActive){
            trackables.activate();
            isActive = true;
        }
    }

    public void stop()
    {
        assertInitialized();

        if (isActive) {
            trackables.deactivate();
            isActive = false;
        }
    }

    public FieldPosition getCurrentPosition() throws IllegalStateException {

        OpenGLMatrix transformationMatrix = getRobotTransformationMatrix();
        return new FieldPosition(transformationMatrix);
    }

    public OpenGLMatrix getRobotTransformationMatrix() throws IllegalStateException {
        assertInitialized();
        assertTrackingStarted();

        for (VuforiaTrackable trackable : trackables) {
            VuforiaTrackableDefaultListener listener = (VuforiaTrackableDefaultListener) trackable.getListener();
            // getUpdatedRobotLocation() will return null if no new information is available since
            // the last time that call was made, or if the trackable is not currently visible.
            OpenGLMatrix transformationMatrix = listener.getUpdatedRobotLocation();
            if (transformationMatrix != null) {
                return transformationMatrix;
            }
        }
        return null;
    }

    protected abstract VuforiaTrackables getTrackables();

    private void assertInitialized() throws IllegalStateException {
        if (!isInitialized) {
            throw new IllegalStateException("Birdseye Tracker has not been initialized. Make sure you call initialize().");
        }
    }

    private void assertTrackingStarted() throws IllegalStateException {
        if (!isInitialized) {
            throw new IllegalStateException("Birdseye Tracker is not tracking. Make sure you call start().");
        }
    }
}

