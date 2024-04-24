package rongxchen.socialmedia.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * @author CHEN Rongxin
 */
public class DateUtil {

	private DateUtil() {}

	public static String convertToDisplayTime(String localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		LocalDateTime dateTime = LocalDateTime.parse(localDateTime, formatter);
		return convertToDisplayTime(dateTime);
	}

	public static String convertToDisplayTime(LocalDateTime localDateTime) {
		long MINUTE = 60 * 1000;
		long HOUR = MINUTE * 60;
		long DAY = HOUR * 24;
		long MONTH = DAY * 30;
		LocalDateTime now = LocalDateTime.now();
		long duration = ChronoUnit.MILLIS.between(localDateTime, now);
		if (duration < MINUTE) {
			return "just now";
		}
		if (duration < HOUR) {
			long minutes = duration / MINUTE;
			return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
		}
		if (duration < DAY) {
			long hours = duration / HOUR;
			return hours + (hours == 1 ? " hour ago" : " hours ago");
		}
		if (duration < MONTH) {
			long days = duration / DAY;
			return days + (days == 1 ? " day ago" : " days ago");
		}
		return convertDateTimeToString(localDateTime);
	}

	public static String convertDateToString(LocalDate localDate) {
		return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	public static String convertDateTimeToString(LocalDateTime localDateTime) {
		return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

}
