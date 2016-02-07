package org.usfirst.frc.team2537.robot.shooter.flywheel;

import org.usfirst.frc.team2537.robot.Robot;
import edu.wpi.first.wpilibj.command.Command;

/**
 * Change the speed of the flywheels to a particular speed.
 * 
 * @author Matthew Schweiss
 *
 */
/*
 * This will be overhauled because, now the PID is used in the flywheel subsystem.
 */
public class FlywheelCommand extends Command {
	// constants
	private static final long 	TIMEOUT 		= 1000;//seconds
	private static final double SPEED_PROXIMITY = 5;//error in percent
	// vars
	private double targetSpeed;

	/**
	 * Create the flywheel command. Move the flywheels to speed. 
	 * If timeout is less than or equal to zero, timeout will never happen.
	 * 
	 * @param speed   The speed in RPM that will reached.
	 * @param timeout The max time for this to run. (t<=0 means no timeout).
	 */
	public FlywheelCommand(double speed) {
		// Use requires() here to declare subsystem dependencies
		// eg. requires(chassis);
		super(TIMEOUT);
		targetSpeed = speed;
	}

	@Override
	protected void initialize() {
		// Get the motor values to start with.
		Robot.shooterFlywheelSys.setLeftSpeed(targetSpeed);
		Robot.shooterFlywheelSys.setRightSpeed(targetSpeed);
	}

	@Override
	public boolean isFinished() {

		return (isInRange(Robot.shooterFlywheelSys.getLeftSpeed())
				&& isInRange(Robot.shooterFlywheelSys.getRightSpeed()));

	}

	@Override
	protected void end() {
		//Nothing to finish with.
		//Keep wheels spinning.
	}

	@Override
	protected void execute() {
		//Nothing needs to be done because it auto ramps.
	}

	@Override
	protected void interrupted() {
		//Interruped, stop the wheels
		Robot.shooterFlywheelSys.stop();
	}

	private boolean isInRange(double speed) {
		return Math.abs(speed - targetSpeed) <= SPEED_PROXIMITY;
	}
}
