package org.firstinspires.ftc.teamcode.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.teamcode.Debug.Info;
import org.firstinspires.ftc.teamcode.RobotClasses.ArduVision;

@TeleOp(name = "Camera Test")
@Config
public class CameraTest extends LinearOpMode {
    public Info info;

    public static double simpleKp = 0.028;
    public static double simpleKf = 0;
    public static double simpleLowKp = 0.015;
    public static double simpleKd = 0.000;
    public static boolean locked = false;

    ElapsedTime timer = new ElapsedTime();
    ElapsedTime leftTimer = new ElapsedTime();
    ElapsedTime rightTimer = new ElapsedTime();

    ElapsedTime lock_timer = new ElapsedTime();
    public boolean lockreset = false;
    private double lastError = 0;
    ArduVision ardu;
    @Override
    public void runOpMode() {
        info = new Info(telemetry);
        ardu = new ArduVision(this);
        DcMotorEx motor = hardwareMap.get(DcMotorEx.class, "turretMotor");

        waitForStart();

        while (opModeIsActive()) {
            ardu.update();

            if (ardu.getTagByID(20)!=null) {
                if (!lockreset) {
                    lock_timer.reset();
                    lockreset = true;
                }
                motor.setPower(simplePD(ardu.getTx(20)));
            } else {
                locked = false;
                lockreset = false;
                motor.setPower(0);
            }

            info.add("tx", ardu.getTx(20));
            info.add("ty", ardu.getTy(20));
            info.send();
        }
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

        if (!locked) {
            adjust = simpleLowKp * headingError + simpleKd * derivative + simpleKf;
        } else if (Math.abs(headingError) > 3) {
            adjust = simpleKp * headingError + simpleKd * derivative + simpleKf;
        }
        return adjust;
    }
}
