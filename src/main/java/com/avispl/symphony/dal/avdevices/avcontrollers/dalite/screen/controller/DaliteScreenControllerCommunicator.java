/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.CollectionUtils;

import javax.security.auth.login.FailedLoginException;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common.DaLiteCommand;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common.DaLiteConstant;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common.DaLiteUtil;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.AdapterMetadata;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.FactoryResetSoftware;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.NetworkInformation;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.ScreenInformation;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.VersionInformation;
import com.avispl.symphony.dal.communicator.SshCommunicator;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * DaliteScreenControllerCommunicator for controlling and monitoring DaLite screens over SSH.
 * Implements {@link Monitorable} and {@link Controller} interfaces.
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 04/02/2025
 * @since 1.0.0
 */
public class DaliteScreenControllerCommunicator extends SshCommunicator implements Monitorable, Controller {

	/**
	 * Set to a shorter timeout value for control commands in {@link SshCommunicator}
	 */
	private static final int controlSSHTimeout = 3000;

	/**
	 * Set back to default timeout value in {@link SshCommunicator}
	 */
	private static final int statisticsSSHTimeout = 30000;

	/**
	 * Number of Preset
	 */
	private final static int numberOfPreset = 2;

	/**
	 * List of preset name
	 */
	private final List<String> presetNames = new ArrayList<>();

	/**
	 * cache to store key and value
	 */
	private final Map<String, String> cacheKeyAndValue = new HashMap<>();

	/**
	 * count the failed command
	 */
	private final Map<String, String> failedMonitor = new HashMap<>();

	/**
	 * ReentrantLock to prevent telnet session is closed when adapter is retrieving statistics from the device.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * Device adapter instantiation timestamp.
	 */
	private final Long adapterInitializationTimestamp = System.currentTimeMillis();

	/**
	 * Application configuration loaded from {@code version.properties}.
	 */
	private final Properties versionProperties = new Properties();

	/**
	 * Store previous/current ExtendedStatistics
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * configManagement imported from the user interface
	 */
	private String configManagement;

	/**
	 * isConfigManagement to check if true accept all controllable properties, of false accept monitoring only
	 */
	private boolean isConfigManagement;

	/**
	 * isEmergencyDelivery to check if control flow is trigger
	 */
	private boolean isEmergencyDelivery;

	/**
	 * Retrieves {@link #configManagement}
	 *
	 * @return value of {@link #configManagement}
	 */
	public String getConfigManagement() {
		return configManagement;
	}

	/**
	 * Sets {@link #configManagement} value
	 *
	 * @param configManagement new value of {@link #configManagement}
	 */
	public void setConfigManagement(String configManagement) {
		this.configManagement = configManagement;
	}

	/**
	 * Constructor for DaliteScreenControllerCommunicator class
	 */
	public DaliteScreenControllerCommunicator() {
		this.setCommandErrorList(Collections.singletonList("Error: response error"));
		this.setCommandSuccessList(Arrays.asList("> ", "NOW!\r\r\n"));
		this.setLoginSuccessList(Collections.singletonList("> "));
		this.setLoginErrorList(Collections.singletonList("Permission denied, please try again."));
		this.loadProperties(this.versionProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		ExtendedStatistics extendedStatistics = new ExtendedStatistics();
		List<AdvancedControllableProperty> advancedControllableProperty = new ArrayList<>();
		Map<String, String> stats = new HashMap<>();
		Map<String, String> controlStats = new HashMap<>();
		reentrantLock.lock();
		try {
			convertConfigManagement();
			if (!isEmergencyDelivery) {
				retrieveMonitoring();
			}
			populateMonitoringAndControlling(stats, controlStats, advancedControllableProperty);
			if (isConfigManagement) {
				stats.putAll(controlStats);
				extendedStatistics.setControllableProperties(advancedControllableProperty);
			}
			extendedStatistics.setStatistics(stats);
			localExtendedStatistics = extendedStatistics;
			isEmergencyDelivery = false;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperties(List<ControllableProperty> list) throws Exception {
		if (CollectionUtils.isEmpty(list)) {
			throw new IllegalArgumentException("ControllableProperties can not be null or empty");
		}
		for (ControllableProperty p : list) {
			try {
				controlProperty(p);
			} catch (Exception e) {
				logger.error(String.format("Error when control property %s", p.getProperty()), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		reentrantLock.lock();
		try {
			this.timeout = controlSSHTimeout;
			if (localExtendedStatistics == null || localExtendedStatistics.getStatistics() == null) {
				return;
			}
			isEmergencyDelivery = true;
			String value = String.valueOf(controllableProperty.getValue());
			String property = controllableProperty.getProperty();
			String keyName = property;
			if (property.contains(DaLiteConstant.HASH)) {
				String[] group = property.split(DaLiteConstant.HASH);
				keyName = group[1];
			}

			switch (keyName) {
				case DaLiteConstant.REBOOT:
					sendControlCommand(keyName, DaLiteCommand.SYSTEM_REBOOT);
					break;
				case "MoveUp":
					sendControlCommand(keyName, DaLiteCommand.MOVE_UP);
					break;
				case "MoveDown":
					sendControlCommand(keyName, DaLiteCommand.MOVE_DOWN);
					break;
				case "MoveStop":
					sendControlCommand(keyName, DaLiteCommand.STOP);
					break;
				case "Position(%)":
					sendControlCommand(keyName, String.format(DaLiteCommand.SCREEN_POSITION_CONTROL, (int) Float.parseFloat(value)));
					break;
				default:
					for (int i = 1; i <= numberOfPreset; i++) {
						if (keyName.equals(presetNames.get(i - 1))) {
							sendControlCommand(keyName, String.format(DaLiteCommand.PRESET_RECALL, i));
							break;
						}
					}
					break;
			}
			if (!keyName.equals(DaLiteConstant.REBOOT)) {
				Thread.sleep(5000);
				updateScreenPositionValue();
			}
		} finally {
			this.timeout = statisticsSSHTimeout;
			reentrantLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		if (localExtendedStatistics != null && localExtendedStatistics.getStatistics() != null && localExtendedStatistics.getControllableProperties() != null) {
			localExtendedStatistics = null;
		}
		cacheKeyAndValue.clear();
		super.internalDestroy();
	}

	/**
	 * Loads version properties and sets initial values used to create general properties
	 * for the aggregator device.
	 *
	 * @param properties the properties to load and update
	 */
	private void loadProperties(Properties properties) {
		try {
			properties.load(this.getClass().getResourceAsStream("/version.properties"));
			properties.setProperty(AdapterMetadata.ADAPTER_UPTIME.getProperty(), String.valueOf(this.adapterInitializationTimestamp));
		} catch (IOException e) {
			this.logger.error("Failed to load version properties file", e);
		}
	}

	/**
	 * Populate monitoring and controlling data
	 *
	 * @param stats the stats are list of statistics
	 * @param controlStats the control stats are list of statistics
	 * @param advancedControllableProperty the advancedControllableProperty are AdvancedControllableProperty instance
	 */
	private void populateMonitoringAndControlling(Map<String, String> stats, Map<String, String> controlStats, List<AdvancedControllableProperty> advancedControllableProperty) {
		populateAdapterMetadata(stats);
		populateButtonControl(controlStats, advancedControllableProperty);
		for (DaLiteCommand command : DaLiteCommand.values()) {
			String data = Optional.ofNullable(cacheKeyAndValue.get(command.getName())).orElse(DaLiteConstant.NA);
			presetNames.clear();
			switch (command) {
				case NETWORK_INFO:
					populateStats(data, stats, NetworkInformation.class, DaLiteConstant.NETWORK);
					break;
				case VERSION:
					populateStats(data, stats, VersionInformation.class, DaLiteConstant.EMPTY);
					break;
				case SCREEN_INFO:
					populateStats(data, stats, ScreenInformation.class, DaLiteConstant.SCREEN_INFO);
					break;
				case FACTORY_RESET:
					populateStats(data, stats, FactoryResetSoftware.class, DaLiteConstant.SYSTEM);
					break;
				case SERIAL_NUMBER:
					data = handleResponse(command.getCommand(), data);
					String value = (DaLiteConstant.NA.equals(data) || data.contains("not set")) ? DaLiteConstant.NA : data;
					stats.put(command.getName(), value);
					break;
				case SCREEN_POSITION:
					if (DaLiteConstant.NA.equals(data)) {
						continue;
					}
					data = handleResponse(command.getCommand(), data);
					addAdvancedControlProperties(advancedControllableProperty, controlStats,
							createSlider(controlStats, DaLiteConstant.SCREEN_CONTROL + DaLiteConstant.HASH + "Position(%)", "0", "100", 0f, 100f, Float.valueOf(data)), data);
					controlStats.put(DaLiteConstant.SCREEN_CONTROL + DaLiteConstant.HASH + "PositionCurrentValue(%)", data);
					break;
				case PRESET_NAME:
					for (int i = 1; i <= numberOfPreset; i++) {
						String group = command.getName();
						data = handleResponse(String.format(command.getCommand(), i), cacheKeyAndValue.get("Preset" + i));
						String name = data.contains("not set") ? ("Preset" + i) : data;
						presetNames.add(name);
						addAdvancedControlProperties(advancedControllableProperty, controlStats,
								createButton(group + DaLiteConstant.HASH + name, DaLiteConstant.RECALL, DaLiteConstant.RECALLING, 0L), DaLiteConstant.EMPTY);
					}
					break;
				default:
					logger.debug(String.format("the command %s doesn't support", command.getName()));
					break;
			}
		}
	}

	/**
	 * Populates adapter metadata statistics into the provided map.
	 *
	 * @param stats the stats are list of statistics
	 */
	private void populateAdapterMetadata(Map<String, String> stats) {
		if (versionProperties == null) {
			logger.error("Version properties are null, adapter metadata statistic skipped");
			return;
		}
		Arrays.stream(AdapterMetadata.values()).forEach(adapterMetadata -> {
			String propertyName = DaLiteConstant.ADAPTER_METADATA + DaLiteConstant.HASH + adapterMetadata.getName();
			String propertyValue = versionProperties.getProperty(adapterMetadata.getProperty());
			switch (adapterMetadata) {
				case ADAPTER_BUILD_DATE:
				case ADAPTER_VERSION:
					stats.put(propertyName, DaLiteUtil.mapToValue(propertyValue));
					break;
				case ADAPTER_UPTIME:
					stats.put(propertyName, DaLiteUtil.mapToUptime(propertyValue));
					break;
				case ADAPTER_UPTIME_MIN:
					stats.put(propertyName, DaLiteUtil.mapToUptimeMin(propertyValue));
					break;
				default:
					break;
			}
		});
	}

	/**
	 * Populates the control buttons into the provided lists for control statistics and advanced controllable properties.
	 * Adds buttons for system reboot, screen movement (up, down, stop) to the control map and advanced control properties.
	 *
	 * @param controlStats containing the current control statistics.
	 * @param advancedControllableProperty to which the controls will be added.
	 */
	private void populateButtonControl(Map<String, String> controlStats, List<AdvancedControllableProperty> advancedControllableProperty) {
		addAdvancedControlProperties(advancedControllableProperty, controlStats,
				createButton(DaLiteConstant.SYSTEM + DaLiteConstant.HASH + DaLiteConstant.SYSTEM_REBOOT, DaLiteConstant.REBOOT, DaLiteConstant.REBOOTING, 240000L), DaLiteConstant.NA);
		addAdvancedControlProperties(advancedControllableProperty, controlStats,
				createButton(DaLiteConstant.SCREEN_CONTROL + DaLiteConstant.HASH + "MoveUp", "Up", "Moving", 0L), DaLiteConstant.NA);
		addAdvancedControlProperties(advancedControllableProperty, controlStats,
				createButton(DaLiteConstant.SCREEN_CONTROL + DaLiteConstant.HASH + "MoveDown", "Down", "Moving", 0L), DaLiteConstant.NA);
		addAdvancedControlProperties(advancedControllableProperty, controlStats,
				createButton(DaLiteConstant.SCREEN_CONTROL + DaLiteConstant.HASH + "MoveStop", "Stop", "Moving", 0L), DaLiteConstant.NA);
	}

	/**
	 * Updates the current screen position value by sending the appropriate command and handling the response.
	 * If successful, the screen position value is retrieved.
	 */
	private void updateScreenPositionValue() {
		try {
			sendCommandDetails(DaLiteCommand.SCREEN_POSITION.getCommand(), DaLiteCommand.SCREEN_POSITION.getName());
		} catch (Exception e) {
			logger.error("Error when get screen position", e);
		}
	}

	/**
	 * Populates a stats map with key-value pairs extracted from a response string using an enum.
	 * Values are extracted from the response using the enum constant values.
	 *
	 * @param response the response string to parse.
	 * @param stats the map to populate with extracted statistics.
	 * @param enumClass the enum class containing constants with `getValue()` and `getName()` methods.
	 * @param prefix optional prefix for keys in the stats map.
	 * @param <E> the type of the enum.
	 */
	private <E extends Enum<E>> void populateStats(String response, Map<String, String> stats, Class<E> enumClass, String prefix) {
		try {
			for (E item : enumClass.getEnumConstants()) {
				Method methodValue = item.getClass().getMethod("getValue");
				Method methodName = item.getClass().getMethod("getName");
				String value = methodValue.invoke(item).toString();
				String name = methodName.invoke(item).toString();
				String key = (StringUtils.isNotNullOrEmpty(prefix) ? prefix + DaLiteConstant.HASH : DaLiteConstant.EMPTY) + name;
				String extractedValue = extractResponseValue(response, value);
				String processedValue = DaLiteUtil.mapToValue(extractedValue);

				stats.put(key, processedValue);
			}
		} catch (Exception e) {
			logger.error("Error when populate " + enumClass.toString(), e);
		}
	}

	/**
	 * Extract value received from device
	 *
	 * @param response the response is response of device
	 * @param regex the regex is regex to extract the response value
	 * @return String is value of the device
	 */
	private static String extractResponseValue(String response, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(response);

		if (matcher.find()) {
			return matcher.group(1).trim();
		}

		return DaLiteConstant.NA;
	}

	/**
	 * This method is used to handle the response received from the device
	 *
	 * @param command input string
	 * @param response input string
	 * @return string after fix
	 */
	private String handleResponse(String command, String response) {
		return response.replaceAll(command, DaLiteConstant.EMPTY)
				.replaceAll("OK", DaLiteConstant.EMPTY).replaceAll(">", DaLiteConstant.EMPTY).trim();
	}

	/**
	 * Control SystemReboot
	 *
	 * @param groupName the groupName is name of command
	 */
	private void sendControlCommand(String groupName, String command) {
		try {
			String response = this.send(command);
			if (response.contains(DaLiteConstant.PRESET_NOT_DEFINED)) {
				throw new IllegalArgumentException("The preset button is undefined.");
			}
			if (StringUtils.isNullOrEmpty(response) || response.contains(DaLiteConstant.ERROR_RESPONSE) || !response.contains(DaLiteConstant.OK)) {
				throw new IllegalArgumentException(String.format("Error when control %s, Syntax error command: %s", groupName, response));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Can't control %s. %s", groupName, e.getMessage()));
		}
	}

	/**
	 * Retrieve monitoring data
	 *
	 * @throws FailedLoginException if get the FailedLoginException
	 */
	private void retrieveMonitoring() throws FailedLoginException {
		for (DaLiteCommand command : DaLiteCommand.values()) {
			if (command.isMonitoring() || isConfigManagement) {
				if (command.equals(DaLiteCommand.PRESET_NAME)) {
					for (int i = 1; i <= numberOfPreset; i++) {
						sendCommandDetails(String.format(command.getCommand(), i), "Preset" + i);
					}
				} else {
					sendCommandDetails(command.getCommand(), command.getName());
				}
			}
		}
	}

	/**
	 * Send command detail to get the data from device
	 *
	 * @param command the command is command to get data
	 * @param name the group is name of properties
	 * @throws FailedLoginException if authentication fails
	 */
	private void sendCommandDetails(String command, String name) throws FailedLoginException {
		try {
			String response = send(command.contains("\r") ? command : command.concat("\r"));
			cacheKeyAndValue.put(name, response.replaceAll(DaLiteConstant.REGEX_RESPONSE, DaLiteConstant.EMPTY));
		} catch (FailedLoginException e) {
			throw e;
		} catch (Exception ex) {
			logger.error(String.format("Error when get command: %s", command), ex);
			failedMonitor.put(command, ex.getMessage());
		}
	}

	/**
	 * This method is used to validate input config management from user
	 */
	private void convertConfigManagement() {
		isConfigManagement = StringUtils.isNotNullOrEmpty(this.configManagement) && this.configManagement.equalsIgnoreCase(DaLiteConstant.TRUE);
	}

	/**
	 * Updates the list of advanced controllable properties and the stats map with the given property and value.
	 * Removes any existing property with the same name before adding the new one.
	 *
	 * @param advancedControllableProperties the list to update
	 * @param stats the map to update with the property's value
	 * @param property the property to add or update
	 * @param value the value associated with the property
	 */
	private void addAdvancedControlProperties(List<AdvancedControllableProperty> advancedControllableProperties, Map<String, String> stats, AdvancedControllableProperty property, String value) {
		if (property != null) {
			for (AdvancedControllableProperty controllableProperty : advancedControllableProperties) {
				if (controllableProperty.getName().equals(property.getName())) {
					advancedControllableProperties.remove(controllableProperty);
					break;
				}
			}
			if (StringUtils.isNotNullOrEmpty(value)) {
				stats.put(property.getName(), value);
			} else {
				stats.put(property.getName(), DaLiteConstant.NA);
			}
			advancedControllableProperties.add(property);
		}
	}

	/***
	 * Create AdvancedControllableProperty slider instance
	 *
	 * @param stats extended statistics
	 * @param name name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty slider instance
	 */
	private AdvancedControllableProperty createSlider(Map<String, String> stats, String name, String labelStart, String labelEnd, Float rangeStart, Float rangeEnd, Float initialValue) {
		stats.put(name, initialValue.toString());
		AdvancedControllableProperty.Slider slider = new AdvancedControllableProperty.Slider();
		slider.setLabelStart(labelStart);
		slider.setLabelEnd(labelEnd);
		slider.setRangeStart(rangeStart);
		slider.setRangeEnd(rangeEnd);

		return new AdvancedControllableProperty(name, new Date(), slider, initialValue);
	}

	/**
	 * Create a button.
	 *
	 * @param name name of the button
	 * @param label label of the button
	 * @param labelPressed label of the button after pressing it
	 * @param gracePeriod grace period of button
	 * @return This returns the instance of {@link AdvancedControllableProperty} type Button.
	 */
	private AdvancedControllableProperty createButton(String name, String label, String labelPressed, long gracePeriod) {
		AdvancedControllableProperty.Button button = new AdvancedControllableProperty.Button();
		button.setLabel(label);
		button.setLabelPressed(labelPressed);
		button.setGracePeriod(gracePeriod);
		return new AdvancedControllableProperty(name, new Date(), button, DaLiteConstant.EMPTY);
	}
}