package test.junit;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;

import org.mailster.gui.Messages;
import org.mailster.gui.views.mailbox.MailBoxItemCategories;
import org.mailster.util.StringUtilities;

public class MailBoxItemTest
	extends TestCase
{
	private final GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		synchronized(gc)
		{
			gc.set(Calendar.HOUR, 0);
			gc.set(Calendar.AM_PM, Calendar.AM);
			gc.set(Calendar.MINUTE, 0);
			gc.set(Calendar.SECOND, 0);
			gc.set(Calendar.MILLISECOND, 0);
		}
	}
	
	private long getTime(int day, int month, int year)
	{
		synchronized(gc)
		{
			gc.set(Calendar.YEAR, year);
			gc.set(Calendar.MONTH, month-1);
			gc.set(Calendar.DAY_OF_MONTH, day);
			
			return gc.getTimeInMillis();
		}
	}
	
	public final void testComputeCategories()
	{
		long today = getTime(28, 8, 2013);
		Locale locale = Locale.FRENCH;
		
		MailBoxItemCategories cats = 
			new MailBoxItemCategories(locale, new Date(today));

		assertEquals(Messages.getString("MailBoxItemCategories.label.today"), 
				cats.getCategoryLabel(today));
		
		assertEquals(Messages.getString("MailBoxItemCategories.label.yesterday"), 
				cats.getCategoryLabel(getTime(27, 8, 2013)));
		
		String monday = (new DateFormatSymbols(locale)).getWeekdays()[Calendar.MONDAY];
		
		assertEquals(StringUtilities.capitalizeFirstLetter(monday), 
				cats.getCategoryLabel(getTime(26, 8, 2013)));
		
		assertEquals(Messages.getString("MailBoxItemCategories.label.oneWeekAgo"), 
				cats.getCategoryLabel(getTime(22, 8, 2013)));
		
		assertEquals(Messages.getString("MailBoxItemCategories.label.twoWeeksAgo"), 
				cats.getCategoryLabel(getTime(18, 8, 2013)));
		
		assertEquals(Messages.getString("MailBoxItemCategories.label.threeWeeksAgo"), 
				cats.getCategoryLabel(getTime(5, 8, 2013)));
		
		assertEquals(Messages.getString("MailBoxItemCategories.label.thisMonth"), 
				cats.getCategoryLabel(getTime(3, 8, 2013)));
		
		assertEquals(Messages.getString("MailBoxItemCategories.label.lastMonth"), 
				cats.getCategoryLabel(getTime(10, 7, 2013)));
		
		assertEquals(Messages.getString("MailBoxItemCategories.label.twoMonthsAgo"), 
				cats.getCategoryLabel(getTime(10, 6, 2013)));

		assertEquals(Messages.getString("MailBoxItemCategories.label.older"), 
				cats.getCategoryLabel(getTime(31, 5, 2013)));
		
		System.out.println(cats);	
	}
}
