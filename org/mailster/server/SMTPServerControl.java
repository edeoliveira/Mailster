package org.mailster.server;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.swt.widgets.Display;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.smtp.SimpleSmtpServer;
import org.mailster.smtp.SmtpMessage;
import org.mailster.smtp.events.SMTPServerListener;


/**
 * ---<br>
 * Mailster (C) 2007 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * SMTPServerControl.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class SMTPServerControl
{
    public final static long DEFAULT_REFRESH_TIMEOUT = 5;

    private Hashtable<String, SmtpMessage> retrievedMessages = new Hashtable<String, SmtpMessage>();
    private MailQueueControl updater = new MailQueueControl();
    private SimpleSmtpServer server;

    // Options
    private String defaultOutputDirectory = System.getProperty("user.home");
    private long timeout = DEFAULT_REFRESH_TIMEOUT;
    private boolean autoStart = false;

    private MailsterSWT main;

    class MailQueueControl
    {
        private final ScheduledExecutorService scheduler = Executors
                .newScheduledThreadPool(1);

        private final Runnable mq = new Runnable() {
            public void run()
            {
                try
                {
                    Display.getDefault().asyncExec(new MailQueueObserver());
                }
                catch (RuntimeException e)
                {
                    e.printStackTrace();
                }
            }
        };

        private ScheduledFuture handle = null;

        public void start()
        {
            handle = scheduler.scheduleWithFixedDelay(mq, timeout, timeout,
                    SECONDS);
        }

        public void stop()
        {
            handle.cancel(false);
            main.getMailView().getTableData().clear();
        }
    }

    class MailQueueObserver implements Runnable
    {
        public void run()
        {
            if (main.getMailView().getTable().isDisposed())
                return;

            int queueSize = main.getMailView().getTable().getItemCount();

            if (server == null || server.isStopped())
                main.log(Messages
                        .getString("MailsterSWT.log.server.notStarted")); //$NON-NLS-1$
            else
            {
                int nb = server.getReceivedEmailSize() - queueSize;
                main
                        .log(MessageFormat
                                .format(
                                        Messages
                                                .getString("MailsterSWT.log.server.updated.emailQueue"),
                                        new Object[] {
                                                new Integer(nb),
                                                new Integer(server
                                                        .getReceivedEmailSize()) }));
                Iterator it = server.getReceivedEmail();

                while (it.hasNext())
                {
                    SmtpMessage msg = (SmtpMessage) it.next();
                    String id = msg.getMessageID();
                    if (retrievedMessages.get(id) == null)
                    {
                        retrievedMessages.put(id, msg);
                        main.getMailView().getTableData().add(msg);
                    }
                }
                main.getMailView().getTable().setItemCount(
                        main.getMailView().getTableData().size());
                main.getMailView().getTable().clearAll();
            }
        }
    }

    public SMTPServerControl(MailsterSWT main)
    {
        this.main = main;
        server = new SimpleSmtpServer();
    }

    public void addSMTPServerListener(SMTPServerListener listener)
    {
        if (server != null)
            server.addSMTPServerListener(listener);
    }

    public void removeSMTPServerListener(SMTPServerListener listener)
    {
        if (server != null)
            server.removeSMTPServerListener(listener);
    }

    public void refreshEmailQueue()
    {
        (new MailQueueObserver()).run();
    }

    public void startServer(boolean debug)
    {
        retrievedMessages.clear();
        main.getMailView().getTable().removeAll();
        server.setDebug(debug);
        server.start();
        updater.start();

        main.log(MailsterSWT.MAILSTER_VERSION
                + MessageFormat.format(Messages.getString(debug
                        ? "MailsterSWT.log.server.started.debugmode"
                        : "MailsterSWT.log.server.started"),
                        new Object[] { new Long(timeout) }));
    }

    public void shutdownServer(boolean force)
    {
        try
        {
            updater.stop();
            if (!server.isStopped())
            {
                server.stop();
                main.log(Messages.getString("MailsterSWT.log.server.stopped")); //$NON-NLS-1$
            }
        }
        catch (Exception ex)
        {
            main.log("MailsterSWT.log.error.stopping.server"); //$NON-NLS-1$
        }
    }

    public boolean isStopped()
    {
        return server == null || server.isStopped();
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public String getDefaultOutputDirectory()
    {
        return defaultOutputDirectory;
    }

    public void setDefaultOutputDirectory(String defaultOutputDirectory)
    {
        this.defaultOutputDirectory = defaultOutputDirectory;
    }

    public void setAutoStart(boolean autoStart)
    {
        this.autoStart = autoStart;
    }

    public boolean isAutoStart()
    {
        return autoStart;
    }
}
