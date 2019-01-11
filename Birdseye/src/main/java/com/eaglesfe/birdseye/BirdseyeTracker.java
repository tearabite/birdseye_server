package com.eaglesfe.birdseye;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraManager;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.SwitchableCamera;
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
import org.firstinspires.ftc.robotcore.internal.camera.delegating.SwitchableCameraName;

import java.util.ArrayList;

public abstract class BirdseyeTracker
{
    protected String vuforiaKey;
    protected boolean showCameraPreview = true;
    protected String[] webcamNames = null;

    protected VuforiaLocalizer vuforia;
    private VuforiaTrackables trackables;
    protected boolean isActive;
    protected boolean isInitialized;

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

    public void setShowCameraPreview(boolean value) {
        if (!isActive && !isInitialized) {
            this.showCameraPreview = value;
        }
    }

    public boolean getShowCameraPreview() {
        return this.showCameraPreview;
    }

    public String[] getWebcamNames() {
        return webcamNames;
    }

    public void setWebcamNames(String ... names) {
        this.webcamNames = names;
    }

    public void setActiveWebcam(int index) {
        if (this.webcamNames == null || this.vuforia == null || !(this.vuforia.getCameraName().isSwitchable())) {
            return;
        }

        SwitchableCameraName switchableCameraName = (SwitchableCameraName)this.vuforia.getCameraName();
        CameraName individualCameraname = switchableCameraName.getMembers()[index];
        ((SwitchableCamera)this.vuforia.getCamera()).setActiveCamera(individualCameraname);
    }

    public void initialize(HardwareMap hardwareMap) {

        //  Instantiate the Vuforia engine
        Parameters parameters = getVuforiaParameters(hardwareMap, this.vuforiaKey, webcamNames, this.showCameraPreview);

        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Get the trackables from the derived class.
        trackables = getTrackables();

        if (this.webcamNames != null){
            OpenGLMatrix cameraLocationOnRobot = OpenGLMatrix
                    .translation(cameraForwardOffsetMm, cameraLeftOffsetMm, cameraVerticalOffsetMm)
                    .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XZY, AngleUnit.DEGREES, 90, 90 + cameraAngleOffsetDeg, 0));

            for (VuforiaTrackable trackable : trackables)
            {
                ((VuforiaTrackableDefaultListener)trackable.getListener()).setCameraLocationOnRobot(parameters.cameraName, cameraLocationOnRobot);
            }
        }
        else {
            OpenGLMatrix phoneLocationOnRobot = OpenGLMatrix
                    .translation(cameraForwardOffsetMm, cameraLeftOffsetMm, cameraVerticalOffsetMm)
                    .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.YZX, AngleUnit.DEGREES, -90, cameraAngleOffsetDeg, 0));

            for (VuforiaTrackable trackable : trackables)
            {
                ((VuforiaTrackableDefaultListener)trackable.getListener()).setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
            }
        }

        isInitialized = true;
    }

    protected static Parameters getVuforiaParameters(HardwareMap hardwareMap, String vuforiaKey, CameraName webcamName, boolean preview) {
        Parameters parameters = new Parameters();
        parameters.vuforiaLicenseKey = vuforiaKey;

        if (preview){
            parameters.cameraMonitorViewIdParent = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        }

        boolean useWebcam = webcamName != null;
        if (useWebcam) {
            parameters.cameraName = webcamName;
        } else {
            parameters.cameraDirection  = CameraDirection.BACK;
        }

        return parameters;
    }

    protected static Parameters getVuforiaParameters(HardwareMap hardwareMap, String vuforiaKey, String[] webcamNames, boolean preview) {

        CameraName[] webcamNamesArray = new CameraName[webcamNames.length];
        for (int i = 0; i < webcamNames.length; i++) {
            webcamNamesArray[i] = hardwareMap.get(WebcamName.class, webcamNames[i]);
        }

        return getVuforiaParameters(hardwareMap, vuforiaKey, ClassFactory.getInstance().getCameraManager().nameForSwitchableCamera(webcamNamesArray), preview);
    }

    protected static Parameters getVuforiaParameters(HardwareMap hardwareMap, String vuforiaKey, String webcamName, boolean preview) {
        return getVuforiaParameters(hardwareMap, vuforiaKey, hardwareMap.get(WebcamName.class, webcamName), preview);
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
        if (transformationMatrix != null) {
            return new FieldPosition(transformationMatrix);
        }
        return null;
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

    protected void assertInitialized() throws IllegalStateException {
        if (!isInitialized) {
            throw new IllegalStateException("Birdseye Tracker has not been initialized. Make sure you call initialize().");
        }
    }

    protected void assertTrackingStarted() throws IllegalStateException {
        if (!isInitialized) {
            throw new IllegalStateException("Birdseye Tracker is not tracking. Make sure you call start().");
        }
    }
}

