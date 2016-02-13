package org.usfirst.frc.team2537.robot.shooter.flywheel;

import java.util.HashMap;
import org.usfirst.frc.team2537.robot.input.HumanInput;
import org.usfirst.frc.team2537.robot.input.Ports;
import org.usfirst.frc.team2537.robot.input.SensorEnum;
import org.usfirst.frc.team2537.robot.input.SensorListener;
import org.usfirst.frc.team2537.robot.shooter.ShootCommandGroup;
import org.usfirst.frc.team2537.robot.shooter.HarvestCommandGroup;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.command.Subsystem;
import static edu.wpi.first.wpilibj.CANTalon.FeedbackDevice.QuadEncoder;

/*
 * Important notice! If we use getSoftLimit() it will be in native units.
 */
public class FlywheelSubsystem extends Subsystem implements SensorListener {

	// Constants
	//private static final boolean DEBUG = true;
	private static final int ENCODER_TICKS_PER_REV = 20;
	private static final double UNITS_PER_100MS_TO_RPM = 100.0 / 4096 * 1000 * 60;
	private static final double SPEED_TOLERANCE = 2;
	// 5 volts per second ramp rate for the flywheels
	private static final double VOLTAGE_RAMP_RATE = 12.0;
	// yet
	private static final double P = .5, I = .05, D = 0.0;
	// Vars
	private boolean proximityValue = false;
	private CANTalon leftFlywheelMotor;
	private CANTalon rightFlywheelMotor;

	public FlywheelSubsystem() {
		// Make sure the the mode to velocity so we can modify it.
		leftFlywheelMotor = new CANTalon(Ports.LEFT_FLYWHEEL_PORT);
		rightFlywheelMotor = new CANTalon(Ports.RIGHT_FLYWHEEL_PORT);

		// Set encoder.
		leftFlywheelMotor.setFeedbackDevice(QuadEncoder);
		leftFlywheelMotor.configEncoderCodesPerRev(ENCODER_TICKS_PER_REV);
		rightFlywheelMotor.setFeedbackDevice(QuadEncoder);
		rightFlywheelMotor.configEncoderCodesPerRev(ENCODER_TICKS_PER_REV);

		// Make sure the soft limits are disabled.
		leftFlywheelMotor.enableForwardSoftLimit(false);
		leftFlywheelMotor.enableReverseSoftLimit(false);
		rightFlywheelMotor.enableForwardSoftLimit(false);
		rightFlywheelMotor.enableReverseSoftLimit(false);

		// Disable the limit switchs, as they do not exist.
		leftFlywheelMotor.enableLimitSwitch(false, false);
		rightFlywheelMotor.enableLimitSwitch(false, false);

		// Nominal voltages, not sure if this is needed
		leftFlywheelMotor.configNominalOutputVoltage(0.0, 0.0);
		leftFlywheelMotor.configPeakOutputVoltage(12.0, -12.0);
		rightFlywheelMotor.configNominalOutputVoltage(0.0, 0.0);
		rightFlywheelMotor.configPeakOutputVoltage(12.0, -12.0);

		// Set rightFlywheelMotor to be reversed of everything else.
		rightFlywheelMotor.reverseOutput(true);
		rightFlywheelMotor.reverseSensor(true);
		leftFlywheelMotor.reverseOutput(false);
		leftFlywheelMotor.reverseSensor(false);

		// Set PID's
		leftFlywheelMotor.setPID(P, I, D);
		leftFlywheelMotor.setF(0);
		rightFlywheelMotor.setPID(P, I, D);
		rightFlywheelMotor.setF(0);
		rightFlywheelMotor.setVoltageRampRate(VOLTAGE_RAMP_RATE);
		leftFlywheelMotor.setVoltageRampRate(VOLTAGE_RAMP_RATE);

		// Make the talons go into the speed control mode.
		leftFlywheelMotor.changeControlMode(CANTalon.TalonControlMode.Speed);
		rightFlywheelMotor.changeControlMode(CANTalon.TalonControlMode.Speed);
	}

	@Override
	public void initDefaultCommand() {
	}

	public void registerButtons() {
		HumanInput.registerPressedCommand(HumanInput.ballShootTrigger, new ShootCommandGroup());
		HumanInput.registerWhileHeldCommand(HumanInput.harvestBallTrigger, new HarvestCommandGroup());
	}

	// Set and get speed
	/**
	 * Set the speed of the motors.
	 *
	 * @param speed
	 *            in RPM.
	 */
	public void setSpeed(double speed) {
		leftFlywheelMotor.set(speed);
		rightFlywheelMotor.set(speed);
	}

	// LEFT
	/**
	 * Get the current speed of the left flywheel.
	 * 
	 * @return flywheel speed in RPM
	 */
	public double getLeftSpeed() {
		return leftFlywheelMotor.getSpeed();
	}

	/**
	 * Get the error of the left flywheel. This is the distance from the wanted
	 * speed and the current speed.
	 * 
	 * @return Difference between current speed and wanted speed. In RPM
	 */
	public double getLeftError() {
		// We want to change this from native units (units per 100ms) to RPM.
		// return leftFlywheelMotor.getError()/100/4096*1000*60;
		/*
		 * Here is how I came to this conversion factor.
		 * 
		 */
		// Simplified for speed.
		return leftFlywheelMotor.getError() * UNITS_PER_100MS_TO_RPM;
	}

	// RIGHT
	/**
	 * Get the current speed of the left flywheel.
	 * 
	 * @return flywheel speed in RPM
	 */
	public double getRightSpeed() {
		return rightFlywheelMotor.getSpeed();
	}

	/**
	 * Get the error of the left flywheel. This is the distance from the wanted
	 * speed and the current speed.
	 * 
	 * @return Difference between current speed and wanted speed in RPM.
	 */
	public double getRightError() {
		return rightFlywheelMotor.getError() * UNITS_PER_100MS_TO_RPM;
	}

	/**
	 * Check if a ball is in the shooter.
	 * 
	 * @return if the proximity sensor is returning true to seeing something.
	 */
	public boolean isBallPresent() {
		return proximityValue;
	}

	public boolean isAtSpeed(double speed) {
		if ((getRightSpeed() < speed && getRightSpeed() >= speed - SPEED_TOLERANCE)
				|| (getRightSpeed() > speed && getRightSpeed() <= speed + SPEED_TOLERANCE)) {
			if ((getLeftSpeed() < speed && getLeftSpeed() >= speed - SPEED_TOLERANCE)
					|| (getLeftSpeed() > speed && getLeftSpeed() <= speed + SPEED_TOLERANCE)) {
				return true;
			}
		}
		return false;

	}

	@Override
	// Proximity
	public void receivedValue(HashMap<SensorEnum, Double> sensorMap) {
		Double value = sensorMap.get(SensorEnum.SHOOTER_BALL);
		if (value != null)
			proximityValue = (value == 1);
	}
}
