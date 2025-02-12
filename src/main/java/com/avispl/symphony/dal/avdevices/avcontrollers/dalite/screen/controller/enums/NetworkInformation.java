/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums;

import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common.DaLiteConstant;

/**
 * NetworkEnum class provides all regex and name of network interface
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 04/02/2025
 * @since 1.0.0
 */
public enum NetworkInformation {
	MAC_ADDRESS(DaLiteConstant.MAC_ADDRESS, "MAC Address(.*?)\r\n"),
	IP_ADDRESS(DaLiteConstant.IP_ADDRESS, "IP Address(.*?)\r\n"),
	SUBNET_MASK(DaLiteConstant.SUBNET_MASK, "Netmask(.*?)\r\n"),
	VLAN(DaLiteConstant.VLAN, "VLAN(.*?)\r\n"),
	GATEWAY(DaLiteConstant.GATEWAY, "Gateway(.*?)\r\n"),
	HOSTNAME(DaLiteConstant.HOSTNAME, "Hostname(.*?)\r\n");

	/**
	 * Constructor Instance
	 *
	 * @param name  of {@link #name}
	 * @param value of {@link #value}
	 */
	NetworkInformation(String name, String value) {
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
