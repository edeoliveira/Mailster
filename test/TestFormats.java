package test;


import java.util.Calendar;
import java.util.Date;

public class TestFormats {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(2011, 4, 12);
		Date d = c.getTime();
		System.out.println(d.getTime());
		System.out.println(d);
//		System.out.println(DateFormatUtils.ISO_DATETIME_FORMAT.format(d));
//		System.out.println(DateFormatUtils.ISO_DATE_FORMAT.format(d));
//		System.out.println(DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(d));
//		System.out.println(DateFormatUtils.ISO_TIME_FORMAT.format(d));
//		System.out.println(DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(d));
//		System.out.println(DateFormatUtils.ISO_TIME_NO_T_TIME_ZONE_FORMAT.format(d));
//		System.out.println(DateFormatUtils.ISO_TIME_TIME_ZONE_FORMAT.format(d));
//		System.out.println(DateFormatUtils.SMTP_DATETIME_FORMAT.format(d));
	}

}
