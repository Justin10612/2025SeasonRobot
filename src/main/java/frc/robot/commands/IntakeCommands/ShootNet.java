// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.IntakeCommands;

import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.EndEffectorConstants;
import frc.robot.Constants.LEDConstants;
import frc.robot.subsystems.ElevatorSubsystem;
import frc.robot.subsystems.EndEffectorSubsystem;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class ShootNet extends Command {
  /** Creates a new ShootNet_Elevator. */
  private final ElevatorSubsystem m_ElevatorSubsystem;
  private final EndEffectorSubsystem m_EndEffectorSubsystem;

  private final BooleanSupplier ifFeedFunc;

  private boolean arriveEndEffectorPrimition;
  private boolean ifFeed;
  public ShootNet(ElevatorSubsystem elevatorSubsystem, EndEffectorSubsystem endEffectorSubsystem, BooleanSupplier ifFeed) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.m_ElevatorSubsystem = elevatorSubsystem;
    this.m_EndEffectorSubsystem = endEffectorSubsystem;

    this.ifFeedFunc = ifFeed;

    addRequirements(m_ElevatorSubsystem, m_EndEffectorSubsystem);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    // m_ElevatorSubsystem.shootNet();
    // m_EndEffectorSubsystem.shootNet_Arm();
    m_EndEffectorSubsystem.netPrimitive_Arm();

    LEDConstants.intakeArriving = true;
    LEDConstants.arrivePosition_Intake = false;
    LEDConstants.LEDFlag = true;
  }
  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    ifFeed = ifFeedFunc.getAsBoolean();
    if(m_EndEffectorSubsystem.arriveSetPoint()) {
      arriveEndEffectorPrimition = true;
    }
    if(arriveEndEffectorPrimition) {
      m_ElevatorSubsystem.shootNet();
      if(m_ElevatorSubsystem.arriveSetPoint()) {
        m_EndEffectorSubsystem.shootNet_Arm();
        if(m_ElevatorSubsystem.arriveSetPoint() && m_EndEffectorSubsystem.arriveSetPoint()) {
          LEDConstants.arrivePosition_Intake = true;
          LEDConstants.LEDFlag = true;
        }else {
          LEDConstants.arrivePosition_Intake = false;
          LEDConstants.LEDFlag = true;
        }
      }
    }
    if (m_ElevatorSubsystem.arriveSetPoint() && ifFeed) {
      m_EndEffectorSubsystem.shootNet_Wheel();
    }else {
      m_EndEffectorSubsystem.holdAlgae();
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
