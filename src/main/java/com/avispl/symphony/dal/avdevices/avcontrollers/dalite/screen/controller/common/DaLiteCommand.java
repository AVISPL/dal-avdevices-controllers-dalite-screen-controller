/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common;

/**
 * DaLiteCommand class defined the enum contains all overall command of the device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 10/30/2023
 * @since 1.0.0
 */
public enum DaLiteCommand {

	NETWORK_INFO("NetworkSettings", "network settings get", true),
	VERSION("SystemVersion", "version", true),
	FACTORY_RESET("FactoryReset", "system factory-reset get", true),
	SCREEN_INFO("ScreenInfo", "screen get-info", true),
	SERIAL_NUMBER("SerialNumber", "system serial-number", true),
	SCREEN_POSITION("ScreenPosition", "screen position get", false),
	PRESET_NAME("ScreenPreset", "screen preset name %s get", true),
	;
	public static final String PRESET_RECALL = "screen preset recall %s";
	public static final String SYSTEM_REBOOT = "system reboot";
	public static final String SCREEN_POSITION_CONTROL = "screen position set %s";
	public static final String MOVE_UP = "screen move up";
	public static final String MOVE_DOWN = "screen move down";
	public static final String STOP = "screen move stop";


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