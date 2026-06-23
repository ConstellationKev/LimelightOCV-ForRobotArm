//package org.firstinspires.ftc.teamcode.Tests;
//
//import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;
//import static org.firstinspires.ftc.teamcode.Debug.Dashboard.addPacket;
//import static org.firstinspires.ftc.teamcode.Debug.Dashboard.sendPacket;
//
//import com.acmerobotics.dashboard.config.Config;
//import com.qualcomm.hardware.limelightvision.LLResult;
//import com.qualcomm.hardware.limelightvision.Limelight3A;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorEx;
//import com.qualcomm.robotcore.util.Range;
//
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
//import org.firstinspires.ftc.teamcode.RobotClasses.Drivetrain;
//import org.firstinspires.ftc.teamcode.RobotClasses.Turret;
//
//@TeleOp
//@Config
//public class turretTesting extends LinearOpMode {
//    public static boolean stopMotor = false;
//    public static double scale = 1000;
//    public static double mul = -1.0;
//    @Override
//    public void runOpMode() throws InterruptedException {
//
////        Drivetrain dt = new Drivetrain(this,0,0,0,false,false);
////        dt.odo.setPosition(new Pose2D(DistanceUnit.INCH,0,0, AngleUnit.RADIANS,0));
//        DcMotorEx motor = hardwareMap.get(DcMotorEx.class,"turretMotor");
//        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        Turret turret = new Turret();
//
//        double curTime = System.currentTimeMillis();
//        double deltaT = 10.0;
//        waitForStart();
//        while (opModeIsActive()) {
//            deltaT = (System.currentTimeMillis() - curTime)/scale;
//            curTime = System.currentTimeMillis();
//            dt.updateOdo();
//
//            double pow = turret.update(dt.pos,dt.vel,dt.rawangle,motor.getCurrentPosition(),deltaT);
//            if (!stopMotor) {
//                motor.setPower(pow);
//            }
//            else {
//                motor.setPower(0);
//            }
//            addInfo("powre",String.valueOf(pow));
//            addInfo("Position",dt.pos.x + " " + dt.pos.y + " " + dt.pos.h);
//            addInfo("aturretPos",String.valueOf(motor.getCurrentPosition()));
//            addInfo("velOfMotor",String.valueOf(turret.velOfMotor));
//            addInfo("velOfRobot",String.valueOf(turret.velOfRobot));
//            addInfo("res",String.valueOf(turret.res));
//            addInfo("turretPos",String.valueOf((turret.turretP > Math.PI*2) ? Math.PI*2-turret.turretP : turret.turretP));
//            addInfo("info",String.valueOf(turret.info));
//            addInfo("Loop Times",String.valueOf(1.0/(deltaT*scale/1000)));
//            sendInfo();
//        }
//
//    }
//    public void addInfo(String name, String value){
//        telemetry.addData(name, value);
//        addPacket(name, value);
//    }
//    public void sendInfo(){
//        sendPacket();
//        telemetry.update();
//    }
//}
