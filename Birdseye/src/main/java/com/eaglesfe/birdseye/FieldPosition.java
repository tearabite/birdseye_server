package birdseye;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaBase;

public class FieldPosition {

    /* To help in constructing a mental model, these four properties are relative to the FIELD's coordinate system... */
    private float x;
    private float y;
    private float z;
    private float heading;

    /* ... and these two properties are relative to the ROBOT's coordinate system */
    private float pitch;
    private float roll;
    private long acquisitionTime;

    private OpenGLMatrix robotToField;

    public FieldPosition(OpenGLMatrix matrix) {

        this.robotToField = matrix;

        VectorF translation = matrix.getTranslation();
        this.x = translation.get(0) / VuforiaBase.MM_PER_INCH;
        this.y = translation.get(1) / VuforiaBase.MM_PER_INCH;
        this.z = translation.get(2) / VuforiaBase.MM_PER_INCH;

        Orientation rotation = Orientation.getOrientation(matrix, AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES);
        this.roll = rotation.firstAngle * -1;
        this.pitch = rotation.secondAngle * -1;
        this.heading = rotation.thirdAngle;

        this.acquisitionTime = System.nanoTime();
    }

    public static FieldPosition forParsed(float x, float y, float z, float pitch, float roll, float heading, long acquisitionTime){
        OpenGLMatrix matrix = new OpenGLMatrix();
        matrix.translate(x, y, z);
        matrix.rotate(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, pitch, roll, heading);
        FieldPosition result = new FieldPosition(matrix);
        result.acquisitionTime = acquisitionTime;
        return result;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getHeading() {
        return heading;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public long getAcquisitionTime() {
        return acquisitionTime;
    }

    public OpenGLMatrix getRobotToField() {
        return robotToField;
    }

    public OpenGLMatrix getFieldToRobot() {
        return robotToField.inverted();
    }

    public VectorF transformPointToRobotSpace(VectorF point){

        return this.getFieldToRobot().transform(point.multiplied(VuforiaBase.MM_PER_INCH)).multiplied(1/VuforiaBase.MM_PER_INCH);
    }
}