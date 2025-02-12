/*
* Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

/**
 * DaLiteScreenCommunicatorTest class
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 01/24/2025
 * @since 1.0.0
 */
public class DaLiteScreenCommunicatorTest {
	private DaLiteScreenControllerCommunicator daLiteScreenControllerCommunicator;

	@BeforeEach()
	public void setUp() throws Exception {
		daLiteScreenControllerCommunicator = new DaLiteScreenControllerCommunicator();
		daLiteScreenControllerCommunicator.setHost("");
		daLiteScreenControllerCommunicator.setPort(22);
		daLiteScreenControllerCommunicator.setLogin("");
		daLiteScreenControllerCommunicator.setPassword("");
		daLiteScreenControllerCommunicator.init();
		daLiteScreenControllerCommunicator.connect();
	}

	@AfterEach()
	public void destroy() throws Exception {
		daLiteScreenControllerCommunicator.disconnect();
	}

	/**
	 * Test default config management
	 *
	 * Expect default config management successfully
	 */
	@Test
	void testDefaultManagement() throws Exception {
		daLiteScreenControllerCommunicator.setConfigManagement("false");
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daLiteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
		Assertions.assertEquals(20, stats.size());
	}

	/**
	 * Test management value if true
	 *
	 * Expect default config management successfully
	 */
	@Test
	void testManagementValueIsTrue() throws Exception {
		daLiteScreenControllerCommunicator.setConfigManagement("true");
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daLiteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
		List<AdvancedControllableProperty> advancedControllableProperties = extendedStatistics.getControllableProperties();
		Assertions.assertEquals(28, stats.size());
		Assertions.assertEquals(7, advancedControllableProperties.size());
	}

	/**
	 * Test Position of button
	 *
	 * Expect return position of button
	 */
	@Test
	void testPosition() throws Exception {
		daLiteScreenControllerCommunicator.setConfigManagement("true");
		daLiteScreenControllerCommunicator.getMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String key = "ScreenControl#Position(%)";
		String value = "75.0";
		controllableProperty.setValue(value);
		controllableProperty.setProperty(key);
		daLiteScreenControllerCommunicator.controlProperty(controllableProperty);
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daLiteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
	}

	/**
	 * Test Preset button
	 *
	 * Expect return preset button
	 */
	@Test
	void testPreset() throws Exception {
		daLiteScreenControllerCommunicator.setConfigManagement("true");
		daLiteScreenControllerCommunicator.getMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String key = "Preset#AVI Preset";
		String value = "0";
		controllableProperty.setValue(value);
		controllableProperty.setProperty(key);
		daLiteScreenControllerCommunicator.controlProperty(controllableProperty);
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daLiteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
	}
}
