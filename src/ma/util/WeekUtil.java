package ma.util;

import java.util.Calendar;

public class WeekUtil {
	public static boolean isNormalDay() {
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		return dow > Calendar.SUNDAY && dow < Calendar.SATURDAY;
	}
	public static boolean isSunday() {
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		return dow == Calendar.SUNDAY;
	}
	public static boolean isSaturday() {
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		return dow == Calendar.SATURDAY;
	}

}
