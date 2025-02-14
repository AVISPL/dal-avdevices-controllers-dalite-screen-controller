/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common;

/**
 * DaLiteCommand class provides all regex and name of DaLite Command
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 02/04/2025
 * @since 1.0.0
 */
public enum DaLiteCommand {

	NETWORK_INFO("Network", "network settings get", true),
	VERSION("SystemVersion", "version", true),
	FACTORY_RESET("FactoryReset", "system factory-reset get", true),
	SCREEN_INFO("ScreenInfo", "screen get-info", true),
	SERIAL_NUMBER("SerialNumber", "system serial-number", true),
	SCREEN_POSITION("ScreenPosition", "screen position get", false),
	PRESET_NAME("ScreenPreset", "screen preset name %s get", true),
	;

	/**
	 * Command to recall a specific screen preset.
	 */
	public static final String PRESET_RECALL = "screen preset recall %s";

	/**
	 * Command to reboot the system.
	 */
	public static final String SYSTEM_REBOOT = "system reboot";

	/**
	 * Command to set the screen position.
	 */
	public static final String SCREEN_POSITION_CONTROL = "screen position set %s";

	/**
	 * Command to move the screen up.
	 */
	public static final String MOVE_UP = "screen move up";

	/**
	 * Command to move the screen down.
	 */
	public static final String MOVE_DOWN = "screen move down";

	/**
	 * Command to stop the screen movement.
	 */
	public static final String STOP = "screen move stop";

	/**
	 * Constructs a DaLiteCommand with the specified name, command, and monitoring status.
	 *
	 * @param name         the name of the command
	 * @param command      the command string to be executed
	 * @param isMonitoring whether the command is used for monitoring purposes
	 */
	DaLiteCommand(String name, String command, boolean isMonitoring) {
		this.name = name;
		this.command = command;
		this.isMonitoring = isMonitoring;
	}

	private String name;
	private String command;
	private boolean isMonitoring;

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #command}
	 *
	 * @return value of {@link #command}
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Retrieves {@link #isMonitoring}
	 *
	 * @return value of {@link #isMonitoring}
	 */
	public boolean isMonitoring() {
		return isMonitoring;
	}
}