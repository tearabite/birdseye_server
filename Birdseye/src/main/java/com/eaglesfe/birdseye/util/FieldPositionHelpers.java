package com.eaglesfe.birdseye.util;

import android.graphics.Point;

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
        // Invert the matrix that gave us our robot position and use it to transform a point
        // in field space into the corresponding point in robot space
        VectorF translated = OpenGLMatrix.identityMatrix()
                .rotated(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 0,0, 90)
                .transform(position.transformPointToRobotSpace(target));

        // Calculate the dist from robot-space origin (0,0) and the transformed target point.
        // Convert our current heading (which is the rotation of the robot's coordinate plane
        // about the field's Z axis) to radians.
        double theta = Math.atan(translated.get(1) / translated.get(0));

        // Use sin and cos of the angle formed between x_field and x_robot and the distance
        // between the two points of interest in order to get component X and Y
        // parts of the line representing the path we are to travel.
        double y = Math.copySign(Math.sin(theta), translated.get(1));
        double x = Math.copySign(Math.cos(theta), translated.get(0));

        return new VectorF((float)x, (float)y);
    }
}
