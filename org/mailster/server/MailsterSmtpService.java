package org.mailster.server;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.swt.widgets.Display;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.glazedlists.SmtpMessageTableFormat;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.smtp.SimpleSmtpServer;
import org.mailster.smtp.events.SMTPServerAdapter;
import org.mailster.smtp.events.SMTPServerEvent;
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
 * MailsterSmtpService.java - The SMTP service controller.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class MailsterSmtpService
{
    public final static long DEFAULT_REFRESH_TIMEOUT = 120;

    private List<StoredSmtpMessage> receivedMessages = new ArrayList<StoredSmtpMessage>();
    private MailQueueControl updater = new MailQueueControl();
    private SimpleSmtpServer server;

    // Options
    private String defaultOutputDirectory = System.getProperty("user.home");
    private long timeout = DEFAULT_REFRESH_TIMEOUT;
    private boolean autoStart = false;

    private MailsterPop3Service pop3Service;
    private MailsterSWT main;

    // By default ensure the most secured method
    private boolean usingAPOPAuthMethod = true;
    
    class MailQueueControl implements Runnable
    {
        private final ScheduledExecutorService scheduler = Executors
                .newScheduledThreadPool(1);
        private long lastExecutionTime;
        
        public void run()
        {
            try
            {
                lastExecutionTime=System.currentTimeMillis();
                Display.getDefault().asyncExec(new MailQueueObserver());
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
            }
        }

        private ScheduledFuture handle = null;

        public void updateDelay()
        {
            if (handle == null)
                return;
            
            stop();
            long initialDelay = (System.currentTimeMillis() - lastExecutionTime) / 1000;
            if (initialDelay >= timeout)
                initialDelay = 0;
            handle = scheduler.scheduleWithFixedDelay(this, initialDelay, timeout,
                    SECONDS);            
        }
        
        public void start()
        {
            handle = scheduler.scheduleWithFixedDelay(this, timeout, timeout,
                    SECONDS);
        }

        public void stop()
        {
        	if (handle != null)
        		handle.cancel(false);
            handle = null;
        }
    }

    class MailQueueObserver implements Runnable
    {
        public void run()
        {
            if (main.getMailView().getTable().isDisposed())
                return;
            if (server == null || server.isStopped())
                main.log(Messages
                        .getString("MailsterSWT.log.server.notStarted")); //$NON-NLS-1$
            else
            {
            	int nb = 0;
                
                synchronized(receivedMessages)
                {
                    main.getMailView().getDataList().addAll(receivedMessages);
	                nb = receivedMessages.size();
	                receivedMessages.clear();
                }

                main.log(MessageFormat.format(
                        Messages.getString("MailsterSWT.log.server.updated.emailQueue"), //$NON-NLS-1$
                        new Object[] { new Integer(nb) }));
                
                main.getMailView().refreshTable();                
                if (nb>0)
                	main.showTrayItemTooltipMessage(Messages.getString("MailView.trayTooltip.title") //$NON-NLS-1$
                			+SmtpMessageTableFormat.hourDateFormat.format(new Date())+")",  //$NON-NLS-1$
                			nb+Messages.getString("MailView.trayTooltip.newMessages")); //$NON-NLS-1$                
            }
        }
    }

    public MailsterSmtpService(MailsterSWT main)
    {
        this.main = main;
        
        try 
        {        	
        	pop3Service = new MailsterPop3Service();
            pop3Service.setSMTPService(this);
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
        
        server = new SimpleSmtpServer();
        server.addSMTPServerListener(new SMTPServerAdapter() {
            public void emailReceived(SMTPServerEvent event)
            {
            	StoredSmtpMessage stored = pop3Service.storeMessage(event.getMessage());
                synchronized(receivedMessages)
                {
                	receivedMessages.add(stored);
                }
            }
        });
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

    public void refreshEmailQueue(boolean syncExec)
    {
    	if (syncExec)
    		Display.getDefault().syncExec(new MailQueueObserver());
    	else
    		Display.getDefault().asyncExec(new MailQueueObserver());
    }

    public void startServer(boolean debug)
    {
        try
        {            
            receivedMessages.clear();
            main.getMailView().clearDataList();
            server.clearQueue();
            server.setDebug(debug);
            server.start();
            
            if (!server.isStopped())
            {
                main.log(MailsterSWT.MAILSTER_VERSION
                        + MessageFormat.format(Messages.getString(debug
                                ? "MailsterSWT.log.server.started.debugmode"  //$NON-NLS-1$
                                : "MailsterSWT.log.server.started"), //$NON-NLS-1$
                                new Object[] { new Long(timeout) }));
                
                Integer pop3Port = new Integer(pop3Service.getPort());
                try 
                {
        			pop3Service.startService(usingAPOPAuthMethod);
        	        main.log(MailsterSWT.MAILSTER_VERSION
        	                + MessageFormat.format(Messages.getString("MailsterSWT.log.pop3.started"), //$NON-NLS-1$
                                    new Object[] { pop3Port })); 
        		} 
                catch (IOException e) 
        		{
        			e.printStackTrace();
                    main.log(MailsterSWT.MAILSTER_VERSION
                            + MessageFormat.format(Messages.getString("MailsterSWT.log.pop3.failed"), //$NON-NLS-1$
                                    new Object[] { pop3Port }));
        		}             
    
    	        updater.start();
            }
            else
            	main.log(MailsterSWT.MAILSTER_VERSION
                        + Messages.getString("MailsterSWT.log.server.notStarted")); //$NON-NLS-1$
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void shutdownServer(boolean force)
    {
        try
        {
            Integer pop3Port = pop3Service.getListeningPort();

            if (!force)
            {
            	refreshEmailQueue(true);
            	pop3Service.stopService();
            }
            else
        		pop3Service.shutdownService();
        	
            main.log(MailsterSWT.MAILSTER_VERSION
                    + MessageFormat.format(Messages.getString("MailsterSWT.log.pop3.stopped"), //$NON-NLS-1$
                            new Object[] { pop3Port }));
            
            updater.stop();
            if (!server.isStopped())
            {
                server.stop();
                main.log(MailsterSWT.MAILSTER_VERSION
                        + Messages.getString("MailsterSWT.log.server.stopped")); //$NON-NLS-1$
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
        this.timeout = timeout <= 0 ? 1 : timeout;
        updater.updateDelay();
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

	public boolean isUsingAPOPAuthMethod() 
	{
		return usingAPOPAuthMethod;
	}

	public void setUsingAPOPAuthMethod(boolean usingAPOPAuthMethod) 
	{
		this.usingAPOPAuthMethod = usingAPOPAuthMethod;
        pop3Service.setUsingAPOPAuthMethod(usingAPOPAuthMethod);
	}

    public MailsterPop3Service getPop3Service()
    {
        return pop3Service;
    }
}
