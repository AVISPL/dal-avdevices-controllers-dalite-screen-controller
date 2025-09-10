/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums;

/**
 * Enum representing version-related information with display names and corresponding response patterns.
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 04/02/2025
 * @since 1.0.0
 */
public enum VersionInformation {
	COMMIT("SoftwareCommitID", "Commit(.*?)\r\n"),
	SCREEN_VERSION("ScreenVersion", "Screen Version(.*?)\r\n"),
	SWITCH_1_VERSION("Switch1Version", "Switch 1 Version(.*?)\r\n"),
	SWITCH_2_VERSION("Switch2Version", "Switch 2 Version(.*?)\r\n"),
	SYSTEM_VERSION("SystemVersion", "System Version(.*?)\r\n"),
	;

	/**
	 * Constructor Instance
	 *
	 * @param name of {@link #name}
	 * @param value of {@link #value}
	 */
	VersionInformation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	final private String name;
	final private String value;

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}
}