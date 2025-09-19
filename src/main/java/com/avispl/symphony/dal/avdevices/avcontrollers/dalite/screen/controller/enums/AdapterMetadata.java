/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums;

/**
 * Represents general properties of an aggregator device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum AdapterMetadata {
	ADAPTER_BUILD_DATE("AdapterBuildDate", "adapter.build.date"),
	ADAPTER_UPTIME("AdapterUptime", "adapter.uptime"),
	ADAPTER_UPTIME_MIN("AdapterUptime(min)", "adapter.uptime"),
	ADAPTER_VERSION("AdapterVersion", "adapter.version");

	private final String name;
	private final String property;

	AdapterMetadata(String name, String property) {
		this.name = name;
		this.property = property;
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
	 * Retrieves {@link #property}
	 *
	 * @return value of {@link #property}
	 */
	public String getProperty() {
		return property;
	}
	}
