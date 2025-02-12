/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common;

/**
 * NetworkEnum class provides all regex and name of network interface
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 04/02/2025
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

	public static final String PRESET_RECALL = "screen preset recall %s";
	public static final String SYSTEM_REBOOT = "system reboot";
	public static final String SCREEN_POSITION_CONTROL = "screen position set %s";
	public static final String MOVE_UP = "screen move up";
	public static final String MOVE_DOWN = "screen move down";
	public static final String STOP = "screen move stop";

	private String name;
	private String command;
	private boolean isMonitoring;

	/**
	 * VaddioCommand
	 *
	 * @name name of {@link #name}
	 * @command command of {@link #command}
	 */
	DaLiteCommand(String name, String command, boolean isMonitoring) {
		this.name = name;
		this.command = command;
		this.isMonitoring = isMonitoring;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name} value
	 *
	 * @param name new value of {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
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
	 * Sets {@link #command} value
	 *
	 * @param command new value of {@link #command}
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * Retrieves {@link #isMonitoring}
	 *
	 * @return value of {@link #isMonitoring}
	 */
	public boolean isMonitoring() {
		return isMonitoring;
	}

	/**
	 * Sets {@link #isMonitoring} value
	 *
	 * @param isMonitoring new value of {@link #isMonitoring}
	 */
	public void setMonitoring(boolean isMonitoring) {
		isMonitoring = isMonitoring;
	}
}
