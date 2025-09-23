/*
 *  Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * DaliteScreenControllerCommunicatorTest class
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 8/14/2023
 * @since 1.0.0
 */
public class DaliteScreenControllerCommunicatorTest {
	private DaliteScreenControllerCommunicator daliteScreenControllerCommunicator;

	@BeforeEach()
	public void setUp() throws Exception {
		daliteScreenControllerCommunicator = new DaliteScreenControllerCommunicator();
		daliteScreenControllerCommunicator.setHost("");
		daliteScreenControllerCommunicator.setPort(22);
		daliteScreenControllerCommunicator.setLogin("");
		daliteScreenControllerCommunicator.setPassword("");
		daliteScreenControllerCommunicator.init();
	}

	@AfterEach()
	public void destroy() throws Exception {
		daliteScreenControllerCommunicator.disconnect();
	}

	@Test
	void testPingLatency() throws Exception {
		long startTime = System.currentTimeMillis();
		int pingLatency = 1000;
		try {
			pingLatency = daliteScreenControllerCommunicator.ping();
		} finally {
			long endTime = System.currentTimeMillis();
			System.out.printf("Total time: %d%n", endTime - startTime);
			Assertions.assertTrue(pingLatency < 1000);
		}
	}

	@Test
	void testGetMultipleStatisticsLatency() throws Exception {
		daliteScreenControllerCommunicator.setConfigManagement(false);
		long startTime = System.currentTimeMillis();
		ExtendedStatistics extendedStatistics = null;
		try {
			extendedStatistics = (ExtendedStatistics) daliteScreenControllerCommunicator.getMultipleStatistics().get(0);
		} finally {
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.printf("Total time: %d%n", totalTime);
			Assertions.assertTrue(totalTime <= 30000);
			if (extendedStatistics == null) {
				fail("ExtendedStatistics cannot be null");
			}

			Map<String, String> stats = extendedStatistics.getStatistics();
			List<AdvancedControllableProperty> advancedControllableProperties = extendedStatistics.getControllableProperties();
			Assertions.assertEquals(28, stats.size());
			Assertions.assertEquals(7, advancedControllableProperties.size());
		}
	}

	@Test
	void testManagementValueIsTrue() throws Exception {
		daliteScreenControllerCommunicator.setConfigManagement(true);
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daliteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
		List<AdvancedControllableProperty> advancedControllableProperties = extendedStatistics.getControllableProperties();
		Assertions.assertEquals(28, stats.size());
		Assertions.assertEquals(7, advancedControllableProperties.size());
	}

	/**
	 * Test default config management
	 *
	 * Expect default config management successfully
	 */
	@Test
	void testDefaultManagement() throws Exception {
		daliteScreenControllerCommunicator.setConfigManagement(false);
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daliteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
		Assertions.assertEquals(20, stats.size());
	}

	@Test
	void testPosition() throws Exception {
		daliteScreenControllerCommunicator.setConfigManagement(true);
		daliteScreenControllerCommunicator.getMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String key = "ScreenControl#Position(%)";
		String value = "75.0";
		controllableProperty.setValue(value);
		controllableProperty.setProperty(key);
		daliteScreenControllerCommunicator.controlProperty(controllableProperty);
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daliteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
	}

	@Test
	void testPreset() throws Exception {
		daliteScreenControllerCommunicator.setConfigManagement(true);
		daliteScreenControllerCommunicator.getMultipleStatistics();
		ControllableProperty controllableProperty = new ControllableProperty();
		String key = "Preset#AVI Preset";
		String value = "0";
		controllableProperty.setValue(value);
		controllableProperty.setProperty(key);
		daliteScreenControllerCommunicator.controlProperty(controllableProperty);
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daliteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
	}
}
