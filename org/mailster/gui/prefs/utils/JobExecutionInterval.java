/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005 Feed'n Read Development Team                             *
 * http://sourceforge.net/fnr                                                  *
 *                                                                             *
 * All rights reserved.                                                        *
 *                                                                             *
 * This program and the accompanying materials are made available under the    *
 * terms of the Common Public License v1.0 which accompanies this distribution,*
 * and is available at                                                         *
 * http://www.eclipse.org/legal/cpl-v10.html                                   *
 *                                                                             *
 * A copy is found in the file cpl-v10.html and important notices to the       *
 * license from the team is found in the textfile LICENSE.txt distributed      *
 * in this package.                                                            *
 *                                                                             *
 * This copyright notice MUST APPEAR in all copies of the file.                *
 *                                                                             *
 * Contributors:                                                               *
 *    Feed'n Read - initial API and implementation                             *
 *                  (smachhau@users.sourceforge.net)                           *
 *******************************************************************************/

package org.mailster.gui.prefs.utils;

import org.mailster.gui.Messages;

/**
 * Represents an interval for a repeated automatic execution of an
 * <code>IJob</code> within the <code>JobManager</code> scheduler context.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 */
public class JobExecutionInterval
{

    /**
     * Default interval of zero (no execution)
     */
    public final static JobExecutionInterval NO_EXECUTION = new JobExecutionInterval(
            0);

    /**
     * Default interval of 1 (execution at application startup)
     */
    public final static JobExecutionInterval APPLICATION_STARTUP = new JobExecutionInterval(
            1);

    /**
     * Default interval of 1 minute
     */
    public final static JobExecutionInterval ONE_MINUTE = new JobExecutionInterval(
            60000);

    /**
     * Default interval of 2 minutes
     */
    public final static JobExecutionInterval TWO_MINUTES = new JobExecutionInterval(
            120000);

    /**
     * Default interval of 3 minutes
     */
    public final static JobExecutionInterval THREE_MINUTES = new JobExecutionInterval(
            180000);

    /**
     * Default interval of 4 minutes
     */
    public final static JobExecutionInterval FOUR_MINUTES = new JobExecutionInterval(
            240000);

    /**
     * Default interval of 5 minutes
     */
    public final static JobExecutionInterval FIVE_MINUTES = new JobExecutionInterval(
            300000);

    /**
     * Default interval of 10 minutes
     */
    public final static JobExecutionInterval TEN_MINUTES = new JobExecutionInterval(
            600000);

    /**
     * Default interval of 15 minutes
     */
    public final static JobExecutionInterval FIFTEEN_MINUTES = new JobExecutionInterval(
            900000);

    /**
     * Default interval of 30 minutes
     */
    public final static JobExecutionInterval THIRTY_MINUTES = new JobExecutionInterval(
            1800000);

    /**
     * Default interval of 1 hour
     */
    public final static JobExecutionInterval ONE_HOUR = new JobExecutionInterval(
            3600000);

    /**
     * Default interval of 2 hours
     */
    public final static JobExecutionInterval TWO_HOURS = new JobExecutionInterval(
            7200000);

    /**
     * Default interval of 4 hours
     */
    public final static JobExecutionInterval FOUR_HOURS = new JobExecutionInterval(
            14400000);

    /**
     * Default interval of 8 hours
     */
    public final static JobExecutionInterval EIGHT_HOURS = new JobExecutionInterval(
            28800000);

    /**
     * Default interval of 12 hours
     */
    public final static JobExecutionInterval TWELVE_HOURS = new JobExecutionInterval(
            43200000);

    /**
     * Default interval of 1 day
     */
    public final static JobExecutionInterval ONE_DAY = new JobExecutionInterval(
            86400000);

    /**
     * Default interval of 1 week
     */
    public final static JobExecutionInterval ONE_WEEK = new JobExecutionInterval(
            604800000);

    /**
     * The predefined intervals
     */
    public final static JobExecutionInterval[] DEFAULT_INTERVALS = {
            NO_EXECUTION, APPLICATION_STARTUP, ONE_MINUTE, TWO_MINUTES,
            THREE_MINUTES, FOUR_MINUTES, FIVE_MINUTES, TEN_MINUTES,
            FIFTEEN_MINUTES, THIRTY_MINUTES, ONE_HOUR, TWO_HOURS, FOUR_HOURS,
            EIGHT_HOURS, TWELVE_HOURS, ONE_DAY, ONE_WEEK };

    /**
     * The period in milliseconds that separates executions of an
     * <code>IJob</code>.
     */
    private long period;

    /**
     * The localized textual representation of a period
     */
    private String localizedPeriod;

    /**
     * Creates a new <code>JobExceutionInterval</code>.
     * 
     * @param period period the period in milliseconds that separates executions
     *            of an <code>IJob</code>
     */
    public JobExecutionInterval(long period)
    {
        this.setPeriod(period);
    }

    /**
     * Sets the period in milliseconds that separates subsequent executions of
     * an <code>IJob</code>.
     * 
     * @param period the period in milliseconds that separates subsequent
     *            executions of an <code>IJob</code>
     * @throws IllegalArgumentException if a value less than zero is specified
     *             for <code>period</code>
     */
    public void setPeriod(long period)
    {
        if (period >= 0)
        {
            this.period = period;
            this.initializeLocalizedPeriod();
        }
        else
            throw new IllegalArgumentException("Period has to be >= 0");
    }

    /**
     * Gets the period in milliseconds that separates subsequent executions of
     * an <code>IJob</code>.
     * 
     * @return the period in milliseconds that separates subsequent executions
     *         of an <code>IJob</code>
     */
    public long getPeriod()
    {
        return this.period;
    }

    /**
     * Gets the localized textual representation of the currently set period as
     * returned by the {@link #getPeriod()} method, e.g. "5 minutes".
     * 
     * @return the localized textual representation of the currently set period
     *         as returned by the {@link #getPeriod()} method, e.g. "5 minutes"
     */
    public String getLocalizedPeriod()
    {
        return this.localizedPeriod;
    }

    /**
     * Gets the textual representation of this <code>JobExecutionInterval</code>
     * instance. This method returns the localized textual representation of the
     * currently set period as returned by the {@link #getLocalizedPeriod()}
     * method, e.g. "5 minutes".
     * 
     * @return the textual representation of this
     *         <code>JobExecutionInterval</code> instance
     */
    public String toString()
    {
        return this.localizedPeriod;
    }

    /**
     * Gets the <code>JobExecutionInterval</code> that matches the specified
     * <code>period</code>. If none of the predefined
     * <code>JobExecutionIntervals</code> matches the specified
     * <code>period</code> the default {@link #NO_EXECUTION}
     * <code>JobExecutionInterval</code> is returned.
     * 
     * @param period the period to get the <code>JobExecutionIntevral</code>
     *            for
     * @return the <code>JobExecutionInterval</code> matching the specified
     *         <code>period</code>
     */
    public static JobExecutionInterval getInterval(long period)
    {
        for (int i = 0; i < DEFAULT_INTERVALS.length; i++)
        {
            if (DEFAULT_INTERVALS[i].getPeriod() == period)
                return (DEFAULT_INTERVALS[i]);
        }
        
        return NO_EXECUTION;
    }

    /**
     * Initializes the localized textual representation of the currently set
     * period as returned by the {@link #getPeriod()} method, e.g. "5 minutes".
     */
    private void initializeLocalizedPeriod()
    {
        this.localizedPeriod = Messages.getString(String.valueOf(this.period));
    }
}