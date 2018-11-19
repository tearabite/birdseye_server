package birdseye.util;

import android.graphics.Point;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;

import birdseye.FieldPosition;

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
     * @param minSpeed The minimum speed at which we want the robot to move [-1, 1]
     * @param maxSpeed The maximum speed at which we want the robot to move [-1, 1]
     * @param backoffThresholdMax The distance from the target at which we will begin linearly scaling
     *                            down the resultant vector in order to slow the robot as it approaches target.
     * @param backoffThresholdMin The distance from the target at which the returned vectors elements
     *                            both become zero. i.e, the radius around the target point that
     *                            the robot should stop moving once it has entered.
     * @return A 2-vector whose first element represents
     * the required forward and reverse input the robot needs to move toward the target point,
     * and whose second element represents the side to side input needed.
     */
    public static VectorF getRequiredMovementVectorForTarget(FieldPosition position,
                                                             VectorF target,
                                                             double minSpeed,
                                                             double maxSpeed,
                                                             double backoffThresholdMax,
                                                             double backoffThresholdMin) {
        // Invert the matrix that gave us our robot position and use it to transform a point
        // in field space into the corresponding point in robot space
        VectorF translated = position.transformPointToRobotSpace(target);

        // Calculate the dist from robot-space origin (0,0) and the transformed target point.
        // Convert our current heading (which is the rotation of the robot's coordinate plane
        // about the field's Z axis) to radians.
        double radius = MathHelpers.getDistanceBetweenTwoPoints(new Point(0, 0), new Point((int)translated.get(0), (int)translated.get(1)));
        double theta = Math.toRadians(position.getHeading());

        // Use sin and cos of the angle formed between x_field and x_robot and the distance
        // between the two points of interest in order to get component X and Y
        // parts of the line representing the path we are to travel.
        double y = Math.sin(theta) * radius;
        double x = Math.cos(theta) * radius;

        // Scale the x and y we have to be in [-1,1]
        double max = Math.max(Math.abs(x), Math.abs(y));
        double scale = Math.max(minSpeed, Math.min(1, Math.abs(radius) / backoffThresholdMax)) * maxSpeed;

        y /= max / scale;
        x /= max / scale;

        if (Math.abs(radius) < backoffThresholdMin) {
            x = 0;
            y = 0;
        }

        return new VectorF((float)x, (float)y);
    }
}
