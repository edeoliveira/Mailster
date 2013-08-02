package org.mailster.gui.views.mailbox;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mailster.gui.Messages;
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
 * MailBoxItemCategories.java - Handles the elapsed time related categories of {@link MailBoxItem}.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class MailBoxItemCategories
{
	private String weekDays[];
	
	private long tomorrow;
	private long today;
	private Long yesterday;
	private Long twoDaysAgo;
	private Long threeDaysAgo;
	private Long fourDaysAgo;
	private Long fiveDaysAgo;
	private Long sixDaysAgo;
	private Long oneWeekAgo;
	private Long twoWeeksAgo;
	private Long threeWeeksAgo;
	private Long thisMonth;
	private long lastMonth;
	private long twoMonthsAgo;
	
	private String todayLabel;
	private String yesterdayLabel;
	private String twoDaysAgoLabel;
	private String threeDaysAgoLabel;
	private String fourDaysAgoLabel;
	private String fiveDaysAgoLabel;
	private String sixDaysAgoLabel;
	private String oneWeekAgoLabel;
	private String twoWeeksAgoLabel;
	private String threeWeeksAgoLabel;
	private String thisMonthLabel;
	private String lastMonthLabel;
	private String twoMonthsAgoLabel;
	private String olderLabel;
	
	private final Lock lock = new ReentrantLock();
	
	public MailBoxItemCategories()
	{
		rebuildCategories();
	}
	
	public MailBoxItemCategories(Locale l, Date d)
	{
		rebuildCategories(l, d);
	}
	
	private void setLocale(Locale l)
	{
		if (l != null)
			Messages.setLocale(l);
		weekDays = (new DateFormatSymbols(Messages.getLocale())).getWeekdays();

		todayLabel = Messages.getString("MailBoxItemCategories.label.today"); //$NON-NLS-1$
		yesterdayLabel = Messages.getString("MailBoxItemCategories.label.yesterday"); //$NON-NLS-1$
		oneWeekAgoLabel = Messages.getString("MailBoxItemCategories.label.oneWeekAgo"); //$NON-NLS-1$
		twoWeeksAgoLabel = Messages.getString("MailBoxItemCategories.label.twoWeeksAgo"); //$NON-NLS-1$
		threeWeeksAgoLabel = Messages.getString("MailBoxItemCategories.label.threeWeeksAgo"); //$NON-NLS-1$
		thisMonthLabel = Messages.getString("MailBoxItemCategories.label.thisMonth"); //$NON-NLS-1$
		lastMonthLabel = Messages.getString("MailBoxItemCategories.label.lastMonth"); //$NON-NLS-1$
		twoMonthsAgoLabel = Messages.getString("MailBoxItemCategories.label.twoMonthsAgo"); //$NON-NLS-1$
		olderLabel = Messages.getString("MailBoxItemCategories.label.older"); //$NON-NLS-1$
	}

	public void rebuildCategories()
	{
		rebuildCategories(null, null);
	}
	
	public void rebuildCategories(Locale l, Date date)
	{
		setLocale(l);
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();

		if (date != null)
			gc.setTime(date);

		gc.set(Calendar.HOUR, 0);
		gc.set(Calendar.AM_PM, Calendar.AM);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);

		lock.lock();
		
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
				yesterdayLabel = yesterday == null ? null : yesterdayLabel;
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
			  lock.unlock();
		}
	}
	
	public long getCategory(long d)
	{
		lock.lock();
			
		try
		{
			if (d >= today)
				return today;
			else if (yesterday != null && d >= yesterday)
				return yesterday;
			else if (twoDaysAgo != null && d >= twoDaysAgo)
				return twoDaysAgo;
			else if (threeDaysAgo != null && d >= threeDaysAgo)
				return threeDaysAgo;
			else if (fourDaysAgo != null && d >= fourDaysAgo)
				return fourDaysAgo;
			else if (fiveDaysAgo != null && d >= fiveDaysAgo)
				return fiveDaysAgo;
			else if (sixDaysAgo != null && d >= sixDaysAgo)
				return sixDaysAgo;
			else if (oneWeekAgo != null && d >= oneWeekAgo)
				return oneWeekAgo;
			else if (twoWeeksAgo != null && d >= twoWeeksAgo)
				return twoWeeksAgo;
			else if (threeWeeksAgo != null && d >= threeWeeksAgo)
				return threeWeeksAgo;
			else if (thisMonth != null && d >= thisMonth)
				return thisMonth;
			else if (d >= lastMonth)
				return lastMonth;
			else if (d >= twoMonthsAgo)
				return twoMonthsAgo;
			else
				return 0;
		}
		finally 
		{
			lock.unlock();
		}
	}

	public String getCategoryLabel(long d)
	{
		lock.lock();
		
		try
		{
			if (d >= today)
				return todayLabel;
			else if (yesterday != null && d >= yesterday)
				return yesterdayLabel;
			else if (twoDaysAgo != null && d >= twoDaysAgo)
				return twoDaysAgoLabel;
			else if (threeDaysAgo != null && d >= threeDaysAgo)
				return threeDaysAgoLabel;
			else if (fourDaysAgo != null && d >= fourDaysAgo)
				return fourDaysAgoLabel;
			else if (fiveDaysAgo != null && d >= fiveDaysAgo)
				return fiveDaysAgoLabel;
			else if (sixDaysAgo != null && d >= sixDaysAgo)
				return sixDaysAgoLabel;
			else if (oneWeekAgo != null && d >= oneWeekAgo)
				return oneWeekAgoLabel;
			else if (twoWeeksAgo != null && d >= twoWeeksAgo)
				return twoWeeksAgoLabel;
			else if (threeWeeksAgo != null && d >= threeWeeksAgo)
				return threeWeeksAgoLabel;
			else if (thisMonth != null && d >= thisMonth)
				return thisMonthLabel;
			else if (d >= lastMonth)
				return lastMonthLabel;
			else if (d >= twoMonthsAgo)
				return twoMonthsAgoLabel;
			else
				return olderLabel;
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public long getCategory(String label)
	{
		lock.lock();
		
		try
		{
			if (label.equals(todayLabel))
				return today;
			else if (label.equals(yesterdayLabel))
				return yesterday;
			else if (label.equals(twoDaysAgoLabel))
				return twoDaysAgo;
			else if (label.equals(threeDaysAgoLabel))
				return threeDaysAgo;
			else if (label.equals(fourDaysAgoLabel))
				return fourDaysAgo;
			else if (label.equals(fiveDaysAgoLabel))
				return fiveDaysAgo;
			else if (label.equals(sixDaysAgoLabel))
				return sixDaysAgo;
			else if (label.equals(oneWeekAgoLabel))
				return oneWeekAgo;
			else if (label.equals(twoWeeksAgoLabel))
				return twoWeeksAgo;
			else if (label.equals(threeWeeksAgoLabel))
				return threeWeeksAgo;
			else if (label.equals(thisMonthLabel))
				return thisMonth;
			else if (label.equals(lastMonthLabel))
				return lastMonth;
			else if (label.equals(twoMonthsAgoLabel))
				return twoMonthsAgo;
			else
				return 0;			
		}
		finally 
		{
			  lock.unlock();
		}
	}	
	
	public long getTomorrow()
	{
		return tomorrow;
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

	private String setDayOfWeekLabel(Long l, Calendar gc)
	{
		if (l == null)
			return null;

		int day = gc.get(Calendar.DAY_OF_WEEK);
		return StringUtilities.capitalizeFirstLetter(weekDays[day]);
	}
	
	private static Date toDate(Long l)
	{
		return l == null ? null : new Date(l);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(todayLabel).append('=').append(toDate(today)).append('\n');
		sb.append(yesterdayLabel).append('=').append(toDate(yesterday)).append('\n');
		sb.append(twoDaysAgoLabel).append('=').append(toDate(twoDaysAgo)).append('\n');
		sb.append(threeDaysAgoLabel).append('=').append(toDate(threeDaysAgo)).append('\n');
		sb.append(fourDaysAgoLabel).append('=').append(toDate(fourDaysAgo)).append('\n');
		sb.append(fiveDaysAgoLabel).append('=').append(toDate(fiveDaysAgo)).append('\n');
		sb.append(sixDaysAgoLabel).append('=').append(toDate(sixDaysAgo)).append('\n');
		sb.append(oneWeekAgoLabel).append('=').append(toDate(oneWeekAgo)).append('\n');
		sb.append(twoWeeksAgoLabel).append('=').append(toDate(twoWeeksAgo)).append('\n');
		sb.append(threeWeeksAgoLabel).append('=').append(toDate(threeWeeksAgo)).append('\n');
		sb.append(thisMonthLabel).append('=').append(toDate(thisMonth)).append('\n');
		sb.append(lastMonthLabel).append('=').append(toDate(lastMonth)).append('\n');
		sb.append(twoMonthsAgoLabel).append('=').append(toDate(twoMonthsAgo)).append('\n');
		
		return sb.toString();
	}
}
