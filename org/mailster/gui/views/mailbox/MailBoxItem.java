package org.mailster.gui.views.mailbox;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.util.StringUtilities;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster Web Site</a>
 * <br>
 * ---
 * <p>
 * MailBoxItem.java - Decorates the <code>StoredSmtpMessage</code>object with properties used for
 * the <code>MailBoxView</code>.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.9 $, $Date: 2011/05/14 12:08:10 $
 */
public class MailBoxItem
{
	private static final String WEEK_DAYS[] = (new DateFormatSymbols()).getWeekdays();
	private static final String YESTERDAY_LABEL = "Hier";

	static long tomorrow;
	private static long today;
	private static Long yesterday;
	private static Long twoDaysAgo;
	private static Long threeDaysAgo;
	private static Long fourDaysAgo;
	private static Long fiveDaysAgo;
	private static Long sixDaysAgo;
	private static Long oneWeekAgo;
	private static Long twoWeeksAgo;
	private static Long threeWeeksAgo;
	private static Long thisMonth;
	private static long lastMonth;
	private static long twoMonthsAgo;

	private static final String todayLabel = "Aujourd'hui";
	private static String yesterdayLabel;
	private static String twoDaysAgoLabel;
	private static String threeDaysAgoLabel;
	private static String fourDaysAgoLabel;
	private static String fiveDaysAgoLabel;
	private static String sixDaysAgoLabel;
	private static final String oneWeekAgoLabel = "Semaine dernière";
	private static final String twoWeeksAgoLabel = "Il y a deux semaines";
	private static final String threeWeeksAgoLabel = "Il y a trois semaines";
	private static final String thisMonthLabel = "Au début du mois";
	private static final String lastMonthLabel = "Mois dernier";
	private static final String twoMonthsAgoLabel = "Il y a deux mois";
	private static final String olderLabel = "Plus vieux";
	
	private static final Lock LOCK = new ReentrantLock();

	private StoredSmtpMessage message;
	private String categoryLabel;
	private long category = -1;
	private boolean root;

	static
	{
		computeCategories(null);
	}

	private static boolean isPreviousDaySunday(Calendar gc)
	{
		gc.add(Calendar.DAY_OF_YEAR, -1);
		return gc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
	}

	private static Long setIfCurrentMonth(int amount, int currentMonth, Calendar gc)
	{
		if (currentMonth != gc.get(Calendar.MONTH))
			return null;
		else
		{
			gc.add(Calendar.DAY_OF_MONTH, amount);
			if (currentMonth != gc.get(Calendar.MONTH))
			{
				gc.add(Calendar.DAY_OF_MONTH, -amount);
				return null;
			}
			else
				return gc.getTimeInMillis();
		}
	}

	private static Long setIfCurrentWeekAndMonth(boolean previousWeek, int currentMonth, Calendar gc)
	{
		if (previousWeek || currentMonth != gc.get(Calendar.MONTH))
		{
			gc.add(Calendar.DAY_OF_YEAR, 1);
			return null;
		}
		else
			return gc.getTimeInMillis();
	}

	private static String setDayOfWeekLabel(Long l, Calendar gc)
	{
		if (l == null)
			return null;

		int day = gc.get(Calendar.DAY_OF_WEEK);
		return StringUtilities.capitalizeFirstLetter(WEEK_DAYS[day]);
	}

	protected static void computeCategories()
	{
		computeCategories(null);
	}

	private static void computeCategories(Date date)
	{
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();

		if (date != null)
			gc.setTime(date);

		gc.set(Calendar.HOUR, 0);
		gc.set(Calendar.AM_PM, Calendar.AM);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);

		LOCK.lock();
		
		try
		{
			today = gc.getTimeInMillis();

			int currentMonth = gc.get(Calendar.MONTH);
			gc.add(Calendar.DAY_OF_YEAR, 1);
			tomorrow = gc.getTimeInMillis();
			
			gc.add(Calendar.DAY_OF_YEAR, -1);

			boolean previousWeek = isPreviousDaySunday(gc);
			if (previousWeek)
			{
				yesterday = null;
				yesterdayLabel = null;
				gc.add(Calendar.DAY_OF_YEAR, 1);
			}
			else
			{
				yesterday = setIfCurrentWeekAndMonth(previousWeek, currentMonth, gc);
				yesterdayLabel = yesterday == null ? null : YESTERDAY_LABEL;
			}

			if (previousWeek)
			{
				twoDaysAgo = null;
				twoDaysAgoLabel = null;
			}
			else
			{
				previousWeek = isPreviousDaySunday(gc);
				twoDaysAgo = setIfCurrentWeekAndMonth(previousWeek, currentMonth, gc);
				twoDaysAgoLabel = setDayOfWeekLabel(twoDaysAgo, gc);
			}

			if (previousWeek)
			{
				threeDaysAgo = null;
				threeDaysAgoLabel = null;
			}
			else
			{
				previousWeek = isPreviousDaySunday(gc);
				threeDaysAgo = setIfCurrentWeekAndMonth(previousWeek, currentMonth, gc);
				threeDaysAgoLabel = setDayOfWeekLabel(threeDaysAgo, gc);
			}

			if (previousWeek)
			{
				fourDaysAgo = null;
				fourDaysAgoLabel = null;
			}
			else
			{
				previousWeek = isPreviousDaySunday(gc);
				fourDaysAgo = setIfCurrentWeekAndMonth(previousWeek, currentMonth, gc);
				fourDaysAgoLabel = setDayOfWeekLabel(fourDaysAgo, gc);
			}

			if (previousWeek)
			{
				fiveDaysAgo = null;
				fiveDaysAgoLabel = null;
			}
			else
			{
				previousWeek = isPreviousDaySunday(gc);
				fiveDaysAgo = setIfCurrentWeekAndMonth(previousWeek, currentMonth, gc);
				fiveDaysAgoLabel = setDayOfWeekLabel(fiveDaysAgo, gc);
			}

			if (previousWeek)
			{
				sixDaysAgo = null;
				sixDaysAgoLabel = null;
			}
			else
			{
				previousWeek = isPreviousDaySunday(gc);
				sixDaysAgo = setIfCurrentWeekAndMonth(previousWeek, currentMonth, gc);
				sixDaysAgoLabel = setDayOfWeekLabel(sixDaysAgo, gc);
			}

			oneWeekAgo = setIfCurrentMonth(-7, currentMonth, gc);
			twoWeeksAgo = setIfCurrentMonth(-7, currentMonth, gc);
			threeWeeksAgo = setIfCurrentMonth(-7, currentMonth, gc);

			if (gc.get(Calendar.DAY_OF_MONTH) > 1)
			{
				gc.set(Calendar.DAY_OF_MONTH, 1);
				thisMonth = gc.getTimeInMillis();
			}
			else
				thisMonth = null;

			gc.add(Calendar.MONTH, -1);
			lastMonth = gc.getTimeInMillis();
			gc.add(Calendar.MONTH, -1);
			twoMonthsAgo = gc.getTimeInMillis();
		}
		finally 
		{
			  LOCK.unlock();
		}
	}

	public MailBoxItem(String label)
	{
		// root nodes constructor
		this.root = true;
		setCategory(label);
	}

	public MailBoxItem(StoredSmtpMessage msg)
	{
		this.message = msg;
	}

	public boolean isRoot()
	{
		return root;
	}

	private void setCategory(String label)
	{
		LOCK.lock();
		
		try
		{
			categoryLabel = label;
			if (label.equals(todayLabel))
				category = today;
			else if (label.equals(yesterdayLabel))
				category = yesterday;
			else if (label.equals(twoDaysAgoLabel))
				category = twoDaysAgo;
			else if (label.equals(threeDaysAgoLabel))
				category = threeDaysAgo;
			else if (label.equals(fourDaysAgoLabel))
				category = fourDaysAgo;
			else if (label.equals(fiveDaysAgoLabel))
				category = fiveDaysAgo;
			else if (label.equals(sixDaysAgoLabel))
				category = sixDaysAgo;
			else if (label.equals(oneWeekAgoLabel))
				category = oneWeekAgo;
			else if (label.equals(twoWeeksAgoLabel))
				category = twoWeeksAgo;
			else if (label.equals(threeWeeksAgoLabel))
				category = threeWeeksAgo;
			else if (label.equals(thisMonthLabel))
				category = thisMonth;
			else if (label.equals(lastMonthLabel))
				category = lastMonth;
			else if (label.equals(twoMonthsAgoLabel))
				category = twoMonthsAgo;
			else
				category = 0;			
		}
		finally 
		{
			  LOCK.unlock();
		}
	}

	public boolean resetCategory()
	{
		String oldCategoryLabel = categoryLabel;
		categoryLabel = null;
		category = -1;

		return !getCategoryLabel().equals(oldCategoryLabel);
	}

	public long getCategory()
	{
		LOCK.lock();
		
		try
		{
			if (category == -1 && message != null)
			{
				long d = message.getMessageDate().getTime();

				if (d >= today)
					category = today;
				else if (yesterday != null && d >= yesterday)
					category = yesterday;
				else if (twoDaysAgo != null && d >= twoDaysAgo)
					category = twoDaysAgo;
				else if (threeDaysAgo != null && d >= threeDaysAgo)
					category = threeDaysAgo;
				else if (fourDaysAgo != null && d >= fourDaysAgo)
					category = fourDaysAgo;
				else if (fiveDaysAgo != null && d >= fiveDaysAgo)
					category = fiveDaysAgo;
				else if (sixDaysAgo != null && d >= sixDaysAgo)
					category = sixDaysAgo;
				else if (oneWeekAgo != null && d >= oneWeekAgo)
					category = oneWeekAgo;
				else if (twoWeeksAgo != null && d >= twoWeeksAgo)
					category = twoWeeksAgo;
				else if (threeWeeksAgo != null && d >= threeWeeksAgo)
					category = threeWeeksAgo;
				else if (thisMonth != null && d >= thisMonth)
					category = thisMonth;
				else if (d >= lastMonth)
					category = lastMonth;
				else if (d >= twoMonthsAgo)
					category = twoMonthsAgo;
				else
					category = 0;
			}
		}
		finally 
		{
			  LOCK.unlock();
		}

		return category;
	}

	public String getCategoryLabel()
	{
		LOCK.lock();
		
		try
		{
			if (categoryLabel == null && message != null)
			{
				long d = message.getMessageDate().getTime();

				if (d >= today)
					categoryLabel = todayLabel;
				else if (yesterday != null && d >= yesterday)
					categoryLabel = yesterdayLabel;
				else if (twoDaysAgo != null && d >= twoDaysAgo)
					categoryLabel = twoDaysAgoLabel;
				else if (threeDaysAgo != null && d >= threeDaysAgo)
					categoryLabel = threeDaysAgoLabel;
				else if (fourDaysAgo != null && d >= fourDaysAgo)
					categoryLabel = fourDaysAgoLabel;
				else if (fiveDaysAgo != null && d >= fiveDaysAgo)
					categoryLabel = fiveDaysAgoLabel;
				else if (sixDaysAgo != null && d >= sixDaysAgo)
					categoryLabel = sixDaysAgoLabel;
				else if (oneWeekAgo != null && d >= oneWeekAgo)
					categoryLabel = oneWeekAgoLabel;
				else if (twoWeeksAgo != null && d >= twoWeeksAgo)
					categoryLabel = twoWeeksAgoLabel;
				else if (threeWeeksAgo != null && d >= threeWeeksAgo)
					categoryLabel = threeWeeksAgoLabel;
				else if (thisMonth != null && d >= thisMonth)
					categoryLabel = thisMonthLabel;
				else if (d >= lastMonth)
					categoryLabel = lastMonthLabel;
				else if (d >= twoMonthsAgo)
					categoryLabel = twoMonthsAgoLabel;
				else
					categoryLabel = olderLabel;
			}

			return categoryLabel;
		}
		finally 
		{
			  LOCK.unlock();
		}
	}

	public StoredSmtpMessage getMessage()
	{
		return message;
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (category ^ (category >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (root ? 1231 : 1237);
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MailBoxItem other = (MailBoxItem) obj;
		if (root != other.root)
			return false;
		if (category != other.category)
			return false;
		if (message == null)
		{
			if (other.message != null)
				return false;
		}
		else if (!message.equals(other.message))
			return false;
		return true;
	}

	public String toString()
	{
		if (message == null)
			return categoryLabel;
		else
			return message.getMessageSubject();
	}

	private static Date toDate(Long l)
	{
		return l == null ? null : new Date(l);
	}

	private static void printCategories()
	{
		System.out.println(new Date(tomorrow));
		System.out.println(todayLabel + "=" + toDate(today));
		System.out.println(yesterdayLabel + "=" + toDate(yesterday));
		System.out.println(twoDaysAgoLabel + "=" + toDate(twoDaysAgo));
		System.out.println(threeDaysAgoLabel + "=" + toDate(threeDaysAgo));
		System.out.println(fourDaysAgoLabel + "=" + toDate(fourDaysAgo));
		System.out.println(fiveDaysAgoLabel + "=" + toDate(fiveDaysAgo));
		System.out.println(sixDaysAgoLabel + "=" + toDate(sixDaysAgo));
		System.out.println(oneWeekAgoLabel + "=" + toDate(oneWeekAgo));
		System.out.println(twoWeeksAgoLabel + "=" + toDate(twoWeeksAgo));
		System.out.println(threeWeeksAgoLabel + "=" + toDate(threeWeeksAgo));
		System.out.println(thisMonthLabel + "=" + toDate(thisMonth));
		System.out.println(lastMonthLabel + "=" + toDate(lastMonth));
		System.out.println(twoMonthsAgoLabel + "=" + toDate(twoMonthsAgo));
	}

	public static void main(String[] args)
	{
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
		gc.set(Calendar.HOUR, 0);
		gc.set(Calendar.AM_PM, Calendar.AM);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		
		gc.set(Calendar.YEAR, 2012);
		gc.set(Calendar.MONTH, 3);
		gc.set(Calendar.DAY_OF_MONTH, 18);

		for (int i = 0; i < 100; i++)
		{
			gc.add(Calendar.DAY_OF_MONTH, 1);
			Date d = gc.getTime();
			computeCategories(d);
			printCategories();
			System.out.println("--- \n");
		}
	}
}
