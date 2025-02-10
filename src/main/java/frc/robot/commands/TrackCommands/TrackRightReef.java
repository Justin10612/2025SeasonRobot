// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.TrackCommands;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.Constants.LEDConstants;
import frc.robot.Constants.PhotonConstants;
import frc.robot.subsystems.PhotonVisionSubsystem;
import frc.robot.subsystems.SwerveSubsystem;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class TrackRightReef extends Command {
  /** Creates a new TrackReef. */
  private final PhotonVisionSubsystem m_PhotonVisionSubsystem;
  private final SwerveSubsystem m_SwerveSubsystem;

  private PIDController rotationPidController;
  private PIDController xPidController;
  private PIDController yPidController;

  private double xPidMeasurements;
  private double yPidMeasurements;
  private double rotationPidMeasurements;

  private double xPidError;
  private double yPidError;
  private double rotationPidError;

  private double xPidOutput;
  private double yPidOutput;
  private double rotationPidOutput;

  public TrackRightReef(PhotonVisionSubsystem photonVisionSubsystem, SwerveSubsystem swerveSubsystem) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.m_PhotonVisionSubsystem = photonVisionSubsystem;
    this.m_SwerveSubsystem = swerveSubsystem;

    addRequirements(m_PhotonVisionSubsystem, m_SwerveSubsystem);
    // PID
    xPidController = new PIDController(PhotonConstants.xPidController_Kp, PhotonConstants.xPidController_Ki, PhotonConstants.xPidController_Kd);
    yPidController = new PIDController(PhotonConstants.yPidController_Kp, PhotonConstants.yPidController_Ki, PhotonConstants.yPidController_Kd);
    rotationPidController = new PIDController(PhotonConstants.rotationPidController_Kp, PhotonConstants.rotationPidController_Ki, PhotonConstants.rotationPidController_Kd);
    // Set limits
    xPidController.setIntegratorRange(PhotonConstants.xPidMinOutput_Reef, PhotonConstants.xPidMaxOutput_Reef);
    yPidController.setIntegratorRange(PhotonConstants.yPidMaxOutput_Reef, PhotonConstants.yPidMaxOutput_Reef);
    rotationPidController.setIntegratorRange(PhotonConstants.rotationPidMaxOutput_Reef, PhotonConstants.rotationPidMaxOutput_Reef);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_SwerveSubsystem.drive(0, 0, 0, false);

    LEDConstants.tracking = true;
    LEDConstants.arrivePosition_Base = false;
    LEDConstants.LEDFlag = true;
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if(m_PhotonVisionSubsystem.hasFrontRightTarget()) {
      // Rotation-PID calculations
      rotationPidMeasurements = m_PhotonVisionSubsystem.getRotationMeasurements_FrontRight();
      rotationPidError = Math.abs(rotationPidMeasurements - PhotonConstants.rotationPidSetPoint_RightReef);
      rotationPidMeasurements = (rotationPidError > 0.5) ? rotationPidMeasurements : PhotonConstants.rotationPidSetPoint_RightReef;
      rotationPidOutput = rotationPidController.calculate(rotationPidMeasurements, PhotonConstants.rotationPidSetPoint_RightReef);
      rotationPidOutput = Constants.setMaxOutput(rotationPidOutput, PhotonConstants.rotationPidMaxOutput_Reef);
      // Y-PID calculations
      yPidMeasurements = m_PhotonVisionSubsystem.getYPidMeasurements_FrontRight();
      yPidError = Math.abs(yPidMeasurements - PhotonConstants.yPidSetPoint_RightReef);
      yPidMeasurements = (yPidError > 0.05) ? yPidMeasurements : PhotonConstants.yPidSetPoint_RightReef;
      yPidOutput = -yPidController.calculate(yPidMeasurements, PhotonConstants.yPidSetPoint_RightReef);
      yPidOutput = Constants.setMaxOutput(xPidOutput, PhotonConstants.yPidMaxOutput_Reef);
      // Y-PID calculations
      xPidMeasurements = m_PhotonVisionSubsystem.getXPidMeasurements_FrontRight();
      xPidError = Math.abs(xPidMeasurements - PhotonConstants.xPidSetPoint_RightReef);
      xPidMeasurements = (xPidError > 0.05) ? xPidMeasurements : PhotonConstants.xPidSetPoint_RightReef;
      xPidOutput = -xPidController.calculate(xPidMeasurements, PhotonConstants.xPidSetPoint_RightReef);
      xPidOutput = Math.min(PhotonConstants.xPidMaxOutput_Reef, Math.max(PhotonConstants.xPidMinOutput_Reef, xPidOutput));
    }else {
      xPidOutput = 0;
      yPidOutput = 0;
      rotationPidOutput = 0;
    }
    // impl
    if(xPidMeasurements == PhotonConstants.xPidSetPoint_RightReef 
    && yPidMeasurements == PhotonConstants.yPidSetPoint_RightReef 
    && rotationPidMeasurements == PhotonConstants.rotationPidSetPoint_RightReef) {
      LEDConstants.arrivePosition_Base = true;
      LEDConstants.LEDFlag = true;
    }
    m_SwerveSubsystem.drive(xPidOutput, yPidOutput, rotationPidOutput, false);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_SwerveSubsystem.drive(0, 0, 0, false);
    LEDConstants.arrivePosition_Base = false;
    LEDConstants.tracking = false;
    LEDConstants.LEDFlag = true;
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return LEDConstants.arrivePosition_Base;
  }
}
