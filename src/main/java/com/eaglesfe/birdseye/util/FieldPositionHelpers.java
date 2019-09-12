package com.eaglesfe.birdseye.util;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;

import com.eaglesfe.birdseye.FieldPosition;

import static org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix.identityMatrix;

public class FieldPositionHelpers {
    public static void addToTelemetry(FieldPosition position, Telemetry telemetry){
        final String positionFormatter = "X: %-10.0f Y: %-10.0f Z: %-10.0f";
        final String rotationFormatter = "R: %-10.0f P: %-10.0f H: %-10.0f";

        telemetry.addData("", positionFormatter, position.getX(), position.getY(), position.getZ());
        telemetry.addData("", rotationFormatter, position.getRoll(), position.getPitch(), position.getHeading());
    }

    /** Given a {@link FieldPosition} and a vector which identifies a point in the field's coordinate
     * space toward which the robot should trend, calculate a vector whose first element represents
     * the required forward and reverse input the robot needs to move toward that point, and whose
     * second element represents the side to side input needed.
     * @param position The position of the robot in field-space.
     * @param target The point in field-space the robot should move toward.
     * @return A 2-vector whose first element represents
     * the required forward and reverse input the robot needs to move toward the target point,
     * and whose second element represents the side to side input needed.
     */
    public static VectorF getTargetVector(FieldPosition position, VectorF target) {

        // The robot coordinate system is aligned such that FORWARD points out toward +X.
        // This is somewhat unintuitive, so for this method, we rotate it around Z 90 degrees
        // in order to align it on +Y. This way, we can think of a positive Y value in the
        // resultant vector as a forward translation, a positive X value as a rightward translation, etc.
        // We accomplish this by creating an identity matrix, rotating it 90 degrees, and then
        // using that to transform the already transformed target vector. See the documentation for
        // FieldPosition.fieldToRobot for more information on how that vector is calculated.
        VectorF translated = OpenGLMatrix.identityMatrix()
                .rotated(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 0,0, 90)
                .transform(position.fieldToRobot(target));

        // We now have the point we're interested in in robot space, and we can think of the robot's
        // position as being at [0, 0, 0]. So, to get the X and Y components of the resultant vector,
        // we simply find cos and sin respectively. Refer to the following diagram for help.

        //            |   *
        //            |  /|
        //            | / | opposite
        //            |/  |
        // -----------*---+--------
        //            | adjacent
        //            |
        //            |
        //            |

        // sin = Opposite over Hypotenuse - Represents the Y component
        // cos = Adjacent over Hypotenuse - Represents the X component

        double opposite = translated.get(1); // The Y component of our translated target point.
        double adjacent = translated.get(0); // The X component of our translated target point.

        // The hypotenuse is simply the magnitude of our translated target vector. We could also use
        // the Pythagorean theorem to get this, but as its already a vector, we can call the
        // `magnitude()` method.

        double hypotenuse = translated.magnitude();

        double x = adjacent / hypotenuse;
        double y = opposite / hypotenuse;

        return new VectorF((float)x, (float)y);
    }

    public static double getTargetDistance(FieldPosition position, VectorF target) {
        VectorF translated = OpenGLMatrix.identityMatrix()
                .rotated(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 0,0, 90)
                .transform(position.fieldToRobot(target));
        return translated.magnitude();
    }

    public static double getTargetAngle(FieldPosition position, VectorF target) {
        VectorF translated = OpenGLMatrix.identityMatrix()
                .rotated(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 0,0, 90)
                .transform(position.fieldToRobot(target));
        return Math.toDegrees(Math.atan(translated.get(1) / translated.get(0)));
    }
}
