
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
public class Autoaim extends LinearOpMode {
    public static String name = "turretMotor";
    public DcMotorEx leftFlyMotor;
    public DcMotorEx rightFlyMotor;

    Info info = new Info(telemetry);

    public static double simpleKp = 0.028;
    public static double simpleKf = 0;
    public static double simpleLowKp = 0.015;
    public static double simpleKd = 0.000;
    public static boolean locked = false;

    public static boolean b_leftFly = false;
    public static double b_leftFlyPow = 0.85;
    public static boolean c_rightFly = false;
    public static double c_rightFlyPow = 0;

    public static double fLeftTune = 1;
    public static double fRightTune = 1;
    public static double bLeftTune = 0.97087;
    public static double bRightTune = 0.97087;
    public static double rotational = 0.7;
    public static double turret_tuning = -0.65;

    public static double thru_pos = 0;
    public static double block_pos = 0;

    DcMotorEx frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor, intakeMotor;
    CRServo leftRecServo, rightRecServo;
    Servo blockServo, hoodServo;

    ElapsedTime timer = new ElapsedTime();
    ElapsedTime leftTimer = new ElapsedTime();
    ElapsedTime rightTimer = new ElapsedTime();

    ElapsedTime lock_timer = new ElapsedTime();
    public boolean lockreset = false;
    private double lastError = 0;

    // in mm
    public double limelightHeight = 436;
    public double tagHeight = 749.5;
    public double limelightAngle = 18;

    public static double a_shooterPow = 1700;
    public static double a_hoodPos = 1;
    public static double leftIntegralSum = 0;
    public static double leftLastError = 0;
    public static double leftKp = 0.06;
    public static double leftKi = 0.0000005;
    public static double leftKd = 0.00002;
    public static double leftKf = 0.031;
    public static double rightIntegralSum = 0;
    public static double rightLastError = 0;
    public static double rightKp = leftKp;
    public static double rightKi = leftKi;
    public static double rightKd = leftKd;
    public static double rightKf = leftKf;

    public static double leftFastKP = 0.00002;
    public static double rightFastKP = 0.00002;
    public static double hahahThresh = 30;

    public double leftRPM = 0;
    public double rightRPM = 0;
    public static double alpha = 0.93;
    double leftTarget = 0;
    double rightTarget = 0;
    double rpmAtChangeLeft, rpmAtChangeRight;
    double shooterPow = a_shooterPow;

    @Override
    public void runOpMode() {
        DcMotorEx motor = hardwareMap.get(DcMotorEx.class, name);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        leftFlyMotor = hardwareMap.get(DcMotorEx.class, "leftFlyMotor");
        leftFlyMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        rightFlyMotor = hardwareMap.get(DcMotorEx.class, "rightFlyMotor");
        rightFlyMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        blockServo = hardwareMap.get(Servo.class, "blockServo");
        hoodServo = hardwareMap.get(Servo.class, "hoodServo");
        limelight.pipelineSwitch(0);

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

        boolean changingPowerLeft = true;
        double timeAtChangeLeft = 0;
        boolean changingPowerRight = true;
        boolean recoverShoot = false;

        waitForStart();

        while (opModeIsActive()) {

            LLResult result = limelight.getLatestResult();

            if (result != null && result.getTx() != 0 && result.getFiducialResults().get(0).getFiducialId()==20) {
                if (!lockreset) {
                    lock_timer.reset();
                    lockreset = true;
                }
                motor.setPower(simplePD(result.getTx(), distance(result.getTy())) + (turret_tuning * gamepad1.right_stick_x));
                info.add("Tx: ", result.getTx());
                info.add("Distance: ", distance(result.getTy()));
            } else {
                locked = false;
                lockreset = false;
                motor.setPower(0);
            }

            leftRPM = -leftFlyMotor.getVelocity();
            rightRPM = -rightFlyMotor.getVelocity();

            hoodServo.setPosition(a_hoodPos);

            if (gamepad1.a) {
                blockServo.setPosition(block_pos);
            } else {
                blockServo.setPosition(thru_pos);
            }

            double targetRPM = 0;
            if (gamepad1.left_trigger > 0.2 && result!=null) {
                targetRPM = a_shooterPow;
            }
            leftFlyMotor.setPower(leftFlywheelPID(targetRPM, rightRPM));
            rightFlyMotor.setPower(rightFlywheelPID(targetRPM, leftRPM));

            info.add("left", -leftFlyMotor.getVelocity());
            info.add("right", -rightFlyMotor.getVelocity());
            info.add("target",targetRPM);

            if (gamepad1.right_trigger > 0.2) {
                intakeMotor.setPower(1);
                leftRecServo.setDirection(DcMotorSimple.Direction.FORWARD);
                leftRecServo.setPower(1);
                rightRecServo.setDirection(DcMotorSimple.Direction.REVERSE);
                rightRecServo.setPower(1);
                recoverShoot = true;
            } else if (gamepad1.right_bumper) {
                intakeMotor.setPower(-1);
                leftRecServo.setDirection(DcMotorSimple.Direction.REVERSE);
                leftRecServo.setPower(1);
                rightRecServo.setDirection(DcMotorSimple.Direction.FORWARD);
                rightRecServo.setPower(1);
            } else {
                intakeMotor.setPower(0);
                leftRecServo.setPower(0);
                rightRecServo.setPower(0);
                recoverShoot = false;
            }


//
//            motor.setPower(PIDCtrl(targetPos, motor.getCurrentPosition()*(360.0/390.0)));
            info.add("Lock reset: ", lockreset);
            info.add("Locked: ", locked);
            telemetry.update();
            info.send();

            double y = gamepad1.left_stick_y;
            double x = -gamepad1.left_stick_x;
            double rx = -rotational * gamepad1.right_stick_x;


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

    public double simplePD(double error, double distance) {
        double headingError = -error;
        if (distance > 75) {
            headingError -= 7.7;
        } else if (distance > 43) {
            headingError -= 4;
        }
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

        if (!locked) {
            adjust = simpleLowKp * headingError + simpleKd * derivative + simpleKf;
        } else if (Math.abs(headingError) > 0.1) {
            adjust = simpleKp * headingError + simpleKf;
        }
        return adjust;
    }

    // in inches
    public double distance(double ty) {
        return ((tagHeight - limelightHeight) / (Math.tan(Math.toRadians(limelightAngle - ty)))) / 25.4;
    }

    public int calcShooterPow(double distance) {
        if (distance < 75) {
            return (int)Math.round((5.26255*distance+1282.78717)/ 10.0)*10;
        } else {
            return (int) Math.round((6.0*distance+1282.0)/ 10.0)*10;
        }
    }
    public double calcHoodAngle(double distance) {
        if (distance < 75) {
            return 1;
        } else {
            return 0.6;
        }
    }

    public double leftFlywheelPID(double target, double current) {
        double error = target - current;
        leftIntegralSum += error * leftTimer.seconds();
        double derivative = (error - leftLastError) / leftTimer.seconds();
        leftLastError = error;
        if (Math.abs(error) < 100) { leftIntegralSum = 0; }
        leftTimer.reset();
        double power = (leftKf) + (error * leftKp) + (leftIntegralSum * -leftKi) + (derivative * leftKd);
        return Range.clip(power, 0, 1);
    }

    public double rightFlywheelPID(double target, double current) {
        double error = target - current;
        rightIntegralSum += error * rightTimer.seconds();
        double derivative = (error - rightLastError) / rightTimer.seconds();
        rightLastError = error;
        if (Math.abs(error) < 100) { rightIntegralSum = 0; }
        rightTimer.reset();
        double power = (leftKf) + (error * leftKp) + (leftIntegralSum * -leftKi) + (derivative * leftKd);
        return Range.clip(power, 0, 1);
    }

    public double recoveryLeftPID(double target, double current) {
        double error = target - current;
        return Range.clip(leftFlyMotor.getPower() + (error * leftFastKP), 0, 1);
    }

    public double recoveryRightPID(double target, double current) {
        double error = target - -1 * current;
        return Range.clip(rightFlyMotor.getPower() + (error * -rightFastKP), 0, 1);
    }
}
