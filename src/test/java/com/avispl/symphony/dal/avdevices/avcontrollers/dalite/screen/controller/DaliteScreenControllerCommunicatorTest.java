/*
 *  Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common.DaLiteConstant;

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
		daliteScreenControllerCommunicator.connect();
	}

	@AfterEach()
	public void destroy() throws Exception {
		daliteScreenControllerCommunicator.disconnect();
	}

	@Test
	void testManagementValueIsTrue() throws Exception {
		daliteScreenControllerCommunicator.setConfigManagement("true");
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daliteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
		List<AdvancedControllableProperty> advancedControllableProperties = extendedStatistics.getControllableProperties();
		Assertions.assertEquals(24, stats.size());
		Assertions.assertEquals(2, advancedControllableProperties.size());
	}

	/**
	 * Test default config management
	 *
	 * Expect default config management successfully
	 */
	@Test
	void testDefaultManagement() throws Exception {
		daliteScreenControllerCommunicator.setConfigManagement("false");
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) daliteScreenControllerCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
		Assertions.assertEquals(21, stats.size());
	}
}
