/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.enums;

/**
 * Enum representing different types of screen information with their corresponding display names and response patterns.
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 11/6/2023
 * @since 1.0.0
 */
public enum ScreenInformation {

    CONNECTED("Connected", "connected:(.*?)\r\n"),
    PRESET_SUPPORT("PresetSupport", "preset support:(.*?)\r\n"),
    POSITION_SUPPORT("PositionSupport", "position support:(.*?)\r\n"),
    FINE_POSITION_SUPPORT("FinePositionSupport", "fine position support:(.*?)\r\n"),
    SWITCH_1_CONNECTED("Switch1Connected", "switch 1 connected:(.*?)\r\n"),
    SWITCH_2_CONNECTED("Switch2Connected", "switch 2 connected:(.*?)\r\n"),
    ;

    /**
     * Constructor Instance
     *
     * @param name  of {@link #name}
     * @param value of {@link #value}
     */
    ScreenInformation(String name, String value) {
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