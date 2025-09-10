/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.avdevices.avcontrollers.dalite.screen.controller.common;

import com.avispl.symphony.dal.util.StringUtils;

/**
 * Utility class for this adapter. This class includes helper methods to extract and convert properties.
 * <p>This class is non-instantiable and provides only static utility methods.</p>
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class DaLiteUtil {
	private DaLiteUtil() {
		// Prevent instantiation
	}

	/**
	 * Converts the given value to a formatted String:
	 * <ul>
	 *   <li>If the value is a String equal to {@code "true"} or {@code "false"}, returns it as-is.</li>
	 *   <li>If the value is a non-null, non-empty String, returns its {@code toTitleCase()} representation.</li>
	 *   <li>If the value is a Boolean or Integer, returns its {@code toString()} representation.</li>
	 *   <li>Returns {@link DaLiteConstant#NA} if the value is {@code null} or does not match the above cases.</li>
	 * </ul>
	 *
	 * @param value input value to convert
	 * @return formatted String or {@link DaLiteConstant#NA} if not applicable
	 */
	public static String mapToValue(Object value) {
		if (value == null) {
			return DaLiteConstant.NA;
		}
		if (value instanceof String) {
			String str = (String) value;
			if ("true".equals(str) || "false".equals(str)) {
				return str;
			}
			return StringUtils.isNotNullOrEmpty(str) ? toTitleCase(str) : DaLiteConstant.NA;
		}
		if (value instanceof Boolean || value instanceof Integer) {
			return value.toString();
		}

		return DaLiteConstant.NA;
	}

	/**
	 * Returns the elapsed uptime between the current system time and the given timestamp in milliseconds.
	 * <p>
	 * The input timestamp represents the start time in milliseconds (typically from {@link System#currentTimeMillis()}).
	 * The returned string represents the absolute duration in the format:
	 * "X day(s) Y hour(s) Z minute(s) W second(s)", omitting any zero-value units except seconds.
	 *
	 * @param uptime the start time in milliseconds as a string (e.g., "1717581000000")
	 * @return a formatted duration string like "2 day(s) 3 hour(s) 15 minute(s) 42 second(s)", or null if parsing fails
	 */
	public static String mapToUptime(String uptime) {
		try {
			if (StringUtils.isNullOrEmpty(uptime)) {
				return null;
			}

			long uptimeSecond = (System.currentTimeMillis() - Long.parseLong(uptime)) / 1000;
			long seconds = uptimeSecond % 60;
			long minutes = uptimeSecond % 3600 / 60;
			long hours = uptimeSecond % 86400 / 3600;
			long days = uptimeSecond / 86400;
			StringBuilder rs = new StringBuilder();
			if (days > 0) {
				rs.append(days).append(" day(s) ");
			}
			if (hours > 0) {
				rs.append(hours).append(" hour(s) ");
			}
			if (minutes > 0) {
				rs.append(minutes).append(" minute(s) ");
			}
			rs.append(seconds).append(" second(s)");

			return rs.toString().trim();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the elapsed uptime in **whole minutes** between the current system time and the given timestamp in milliseconds.
	 * <p>
	 * The input timestamp represents the start time in milliseconds (typically from {@link System#currentTimeMillis()}).
	 * The returned string is the total number of minutes that have elapsed, excluding seconds.
	 *
	 * @param uptime the start time in milliseconds as a string (e.g., "1717581000000")
	 * @return a string representing the total number of elapsed minutes (e.g., "125"), or null if parsing fails
	 */
	public static String mapToUptimeMin(String uptime) {
		try {
			if (StringUtils.isNullOrEmpty(uptime)) {
				return null;
			}

			long uptimeSecond = (System.currentTimeMillis() - Long.parseLong(uptime)) / 1000;
			long minutes = uptimeSecond / 60;

			return String.valueOf(minutes);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Capitalizes the first character of the input string.
	 * <p>
	 * If the input is {@code null}, empty, or the literal string {@code "null"}, this method returns {@code null}.
	 * If the input is {@code "true"} or {@code "false"}, the method returns the input unchanged.
	 * Otherwise, it returns the input string with its first character converted to uppercase.
	 * </p>
	 *
	 * @param value the input string to convert
	 * @return a string with the first character capitalized, or {@code null} if the input is invalid
	 */
	private static String toTitleCase(String value) {
		if (StringUtils.isNullOrEmpty(value) || value.equals("null")) {
			return null;
		}
		if (value.equals("true") || value.equals("false")) {
			return value;
		}

		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
