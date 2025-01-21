/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
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
	;
	public static final String STREAMING_MODE = "streaming mode set ";
	public static final String SYSTEM_REBOOT = "system reboot";
	public static final String VIDEO_COMMAND = "video mute ";
	public static final String AUDIO_COMMAND = "audio master mute ";

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