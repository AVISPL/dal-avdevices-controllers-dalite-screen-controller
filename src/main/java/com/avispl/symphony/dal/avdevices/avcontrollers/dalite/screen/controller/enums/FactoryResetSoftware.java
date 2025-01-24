/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums;

/**
 * Enum representing factory reset related information with display names and corresponding response patterns.
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 11/6/2023
 * @since 1.0.0
 */
public enum FactoryResetSoftware {

    SOFTWARE("FactoryResetSoftware", "factory-reset \\(software\\):(.*?)\r\n"),
    HARDWARE("FactoryResetHardware", "factory-reset \\(hardware\\):(.*?)\r\n"),
    ;

    /**
     * Constructor Instance
     *
     * @param name  of {@link #name}
     * @param value of {@link #value}
     */
    FactoryResetSoftware(String name, String value) {
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