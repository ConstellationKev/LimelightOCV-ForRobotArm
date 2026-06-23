
package org.firstinspires.ftc.teamcode.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Debug.Info;

@TeleOp
//@Disabled
@Config
public class BallTracking extends LinearOpMode {
    Info info = new Info(telemetry);

    public static double simpleKp = 0.028;
    public static double simpleKf = 0;
    public static double simpleLowKp = 0.015;
    public static double simpleKd = 0.000;
    public static boolean locked = false;

    public static double fLeftTune = 1;
    public static double fRightTune = 1;
    public static double bLeftTune = 0.97087;
    public static double bRightTune = 0.97087;
    public static double rotational = 0.7;

    public static double hahaConstant = 0.3;

    DcMotorEx frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor, intakeMotor;
    CRServo leftRecServo, rightRecServo;

    ElapsedTime timer = new ElapsedTime();

    ElapsedTime lock_timer = new ElapsedTime();
    public boolean lockreset = false;
    private double lastError = 0;




    @Override
    public void runOpMode() {
        double rx = 0;
        double y = 0;

        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(1);

        limelight.start();

        locked = false;

        frontLeftMotor = hardwareMap.get(DcMotorEx.class, "motorFrontLeft");
        frontRightMotor = hardwareMap.get(DcMotorEx.class, "motorFrontRight");
        backLeftMotor = hardwareMap.get(DcMotorEx.class, "motorBackLeft");
        backRightMotor = hardwareMap.get(DcMotorEx.class, "motorBackRight");

        leftRecServo = hardwareMap.get(CRServo.class, "leftRecServo");
        rightRecServo = hardwareMap.get(CRServo.class, "rightRecServo");

        frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);


        waitForStart();

        while (opModeIsActive()) {

            LLResult result = limelight.getLatestResult();

            if (result != null && result.getTx() != 0) {
                if (!lockreset) {
                    lock_timer.reset();
                    lockreset = true;
                }
                y = hahaConstant;
                rx = simplePD(result.getTx());
                info.add("Tx: ", result.getTx());
            } else {
                locked = false;
                lockreset = false;
                y = 0;
                rx = 0;
            }

//
//            motor.setPower(PIDCtrl(targetPos, motor.getCurrentPosition()*(360.0/390.0)));
            info.add("Lock reset: ", lockreset);
            info.add("Locked: ", locked);
            telemetry.update();
            info.send();

            double x = -gamepad1.left_stick_x;


            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;


            frontLeftMotor.setPower(frontLeftPower * fLeftTune);
            frontRightMotor.setPower(frontRightPower * fRightTune);
            backLeftMotor.setPower(backLeftPower * bLeftTune);
            backRightMotor.setPower(backRightPower * bRightTune);

        }
        limelight.stop();
    }

    public double simplePD(double error) {
        double headingError = -error;
        double adjust = 0.0;
        double locksecs = lock_timer.seconds();

        double dtsecs = timer.seconds();
        double dt = dtsecs > 0 ? dtsecs : 1e-6;
        double derivative = (headingError - lastError) / dt;
        lastError = headingError;
        timer.reset();

        if (Math.abs(headingError) < 4.0 && locksecs > 0.5) {
            locked = true;
        }

        if (!locked && Math.abs(headingError) > 6) {
            adjust = simpleLowKp * headingError + simpleKd * derivative + simpleKf;
        } else if (Math.abs(headingError) > 6) {
            adjust = simpleKp * headingError + simpleKf;
        }
        return adjust;
    }
}