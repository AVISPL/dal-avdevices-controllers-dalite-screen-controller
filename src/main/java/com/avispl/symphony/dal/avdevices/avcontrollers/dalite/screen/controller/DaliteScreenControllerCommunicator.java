/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common.DaLiteCommand;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common.DaLiteConstant;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.FactoryResetSoftware;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.NetworkInformation;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.ScreenInformation;
import com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums.VersionInformation;
import com.avispl.symphony.dal.communicator.SshCommunicator;
import com.avispl.symphony.dal.util.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.security.auth.login.FailedLoginException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DaliteScreenControllerCommunicator
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 8/14/2023
 * @since 1.0.0
 */
public class DaliteScreenControllerCommunicator extends SshCommunicator implements Monitorable, Controller {

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
            if (!isEmergencyDelivery) {
                convertConfigManagement();
                retrieveMonitoring();
                populateMonitoringAndControlling(stats, controlStats, advancedControllableProperty);
                if (isConfigManagement) {
                    stats.putAll(controlStats);
                    extendedStatistics.setControllableProperties(advancedControllableProperty);
                }
                extendedStatistics.setStatistics(stats);
            }
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
     * Populate monitoring and controlling data
     *
     * @param stats                        the stats are list of statistics
     * @param advancedControllableProperty the advancedControllableProperty are AdvancedControllableProperty instance
     */
    private void populateMonitoringAndControlling(Map<String, String> stats, Map<String, String> controlStats, List<AdvancedControllableProperty> advancedControllableProperty) {
        for (DaLiteCommand command : DaLiteCommand.values()) {
            String data = StringUtils.isNullOrEmpty(cacheKeyAndValue.get(command.getName())) ? DaLiteConstant.NONE : cacheKeyAndValue.get(command.getName());
            switch (command) {
                case NETWORK_INFO:
                    populateStats(data, stats, NetworkInformation.class, DaLiteConstant.NETWORK_SETTINGS);
                    break;
                case VERSION:
                    populateStats(data, stats, VersionInformation.class, "");
                    break;
                case SCREEN_INFO:
                    populateStats(data, stats, ScreenInformation.class, "ScreenInfo");
                    break;
                case FACTORY_RESET:
                    populateStats(data, stats, FactoryResetSoftware.class, "");
                    break;
                case SERIAL_NUMBER:
                    data = handleResponse(command.getCommand(), data);
                    if (DaLiteConstant.NONE.equals(data) || data.contains("not set")) {
                        stats.put(command.getName(), DaLiteConstant.NONE);
                    } else {
                        stats.put(command.getName(), data);
                    }
                    break;
                case SCREEN_POSITION:
                    if ("None".equals(data)) {
                        continue;
                    }
                    data = handleResponse(command.getCommand(), data);
                    addAdvancedControlProperties(advancedControllableProperty, controlStats,
                            createSlider(controlStats, "ScreenInfo#Position(%)", "0", "100", 0f, 100f, Float.valueOf(data)), data);
                    controlStats.put("ScreenInfo#PositionCurrentValue(%)", data);
                    break;
                default:
                    logger.debug(String.format("the command %s doesn't support", command.getName()));
                    break;
            }
        }
        addAdvancedControlProperties(advancedControllableProperty, controlStats,
                createButton(DaLiteConstant.SYSTEM_REBOOT, DaLiteConstant.REBOOT, DaLiteConstant.REBOOTING, 0L), "");
    }

    private <E extends Enum<E>> void populateStats(String response, Map<String, String> stats, Class<E> enumClass, String prefix) {
        try {
            for (E item : enumClass.getEnumConstants()) {
                Method methodValue = item.getClass().getMethod("getValue");
                Method methodName = item.getClass().getMethod("getName");
                String value = methodValue.invoke(item).toString();
                String name = methodName.invoke(item).toString();
                String key = (StringUtils.isNotNullOrEmpty(prefix) ? prefix + DaLiteConstant.HASH : "") + name;
                try {
                    stats.put(key, uppercaseFirstCharacter(extractResponseValue(response, value)));
                } catch (Exception e) {
                    stats.put(key, DaLiteConstant.NONE);
                }
            }
        } catch (Exception e) {
            logger.error("Error when populate " + enumClass.toString());
        }
    }

    /**
     * Extract value received from device
     *
     * @param response the response is response of device
     * @param regex    the regex is regex to extract the response value
     * @return String is value of the device
     */
    private static String extractResponseValue(String response, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return DaLiteConstant.NONE;
    }

    /**
     * capitalize the first character of the string
     *
     * @param input input string
     * @return string after fix
     */
    private String uppercaseFirstCharacter(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    private String handleResponse(String command, String response) {
        return response.replaceAll(command, "")
                .replaceAll("OK", "").replaceAll(">", "").trim();
    }

    /**
     * Retrieve monitoring data
     *
     * @throws FailedLoginException if get the FailedLoginException
     */
    private void retrieveMonitoring() throws FailedLoginException {
        for (DaLiteCommand command : DaLiteCommand.values()) {
            if (command.isMonitoring() || isConfigManagement) {
                sendCommandDetails(command.getCommand(), command.getName());
            }
        }
    }

    /**
     * Send command detail to get the data from device
     *
     * @param command the command is command to get data
     * @param name    the group is name of properties
     * @throws FailedLoginException if authentication fails
     */
    private void sendCommandDetails(String command, String name) throws FailedLoginException {
        try {
            String response = send(command.contains("\r") ? command : command.concat("\r"));
            cacheKeyAndValue.put(name, response.replaceAll(DaLiteConstant.REGEX_RESPONSE, DaLiteConstant.EMPTY));
        } catch (FailedLoginException e) {
            throw new FailedLoginException("Login failed: " + e);
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
                stats.put(property.getName(), "");
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
     * @param name         name of the button
     * @param label        label of the button
     * @param labelPressed label of the button after pressing it
     * @param gracePeriod  grace period of button
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