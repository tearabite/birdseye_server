package eaglesfe;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;

import java.io.IOException;

import eaglesfe.common.BirdseyeServer;
import eaglesfe.common.FieldPosition;
import eaglesfe.common.FieldPositionHelpers;
import eaglesfe.roverruckus.RoverRuckusBirdseyeTracker;

@TeleOp(name="Birdseye: Tracker Test", group="Test")
public class TestOpmode extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        OpenGLMatrix matrix;
        RoverRuckusBirdseyeTracker tracker = new RoverRuckusBirdseyeTracker();
        BirdseyeServer server = new BirdseyeServer(3708, telemetry);

        // Set the vuforia key to our own key.
        tracker.setVuforiaKey("AUmjH6X/////AAABmeSd/rs+aU4giLmf5DG5vUaAfHFLv0/vAnAFxt5vM6cbn1/nI2sdkRSEf6HZLA/is/+VQY5/i6u5fbJ4TugEN8HOxRwvUvkrAeIpgnMYEe3jdD+dPxhE88dB58mlPfVwIPJc2KF4RE7weuRBoZ8KlrEKbNNu20ommdG7S/HXP9Kv/xocj82rgj+iPEaitftALZ6QaGBdfSl3nzVMK8/KgQJNlSbGic/Wf3VI8zcYmMyDslQPK45hZKlHW6ezxdGgJ7VJCax+Of8u/LEwfzqDqBsuS4/moNBJ1mF6reBKe1hIE2ffVTSvKa2t95g7ht3Z4M6yQdsI0ZaJ6AGnl1wTlm8Saoal4zTbm/VCsmZI081h");

        // Position the camera lense pointing outward through the front of the robot and
        // centered on the front face of the virtual 18x18" cube.
        tracker.setCameraForwardOffset(9);
        tracker.setCameraVerticalOffset(9);
        tracker.cameraLeftOffsetMm(0);
        tracker.setCameraRotationalOffset(0);

        // Initialize the tracker using the back-facing phone camera and a preview window on phone.
        tracker.initialize(this.hardwareMap, null, true);

        // Start tracking.
        tracker.start();
        server.start();

        // Wait for the OpMode to start.
        waitForStart();

        final double maxSpeed = 0.8;
        final double minSpeed = 0.2;  // The minimum speed we will slow to when approaching target
        final double backoffThresholdMax = 24; // The distance from target at which we will start linear backoff
        final double backoffThresholdMin = 2;

        // As long as the OpMode is active, calculate and print the translated X, Y point to the driver station.
        VectorF target = new VectorF(0, 48, 0);
        while (opModeIsActive()) try {
            matrix = tracker.getRobotTransformationMatrix();

            if (matrix != null) {
                FieldPosition position = new FieldPosition(matrix);
                VectorF input = FieldPositionHelpers
                        .getRequiredMovementVectorForTarget(
                                position,
                                target, 
                                minSpeed,
                                maxSpeed,
                                backoffThresholdMax,
                                backoffThresholdMin);

                server.addData("position", position);
                server.addData("input", input);
                server.beginObject("target");
                server.addData("field", target);
                server.addData("robot", position.transformPointToRobotSpace(target));
                server.endObject();
            }
            sleep(250);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Stop tracking.
        tracker.stop();
        server.stop();
    }
}