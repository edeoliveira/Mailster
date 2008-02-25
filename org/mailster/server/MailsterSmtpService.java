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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.smtp.SimpleSmtpServer;
import org.mailster.smtp.events.SMTPServerAdapter;
import org.mailster.smtp.events.SMTPServerEvent;
import org.mailster.smtp.events.SMTPServerListener;
import org.mailster.util.DateUtilities;

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
 * @version $Revision$, $Date$
 */
public class MailsterSmtpService
{
    public final static long DEFAULT_QUEUE_REFRESH_TIMEOUT = 120;

    private List<StoredSmtpMessage> receivedMessages = new ArrayList<StoredSmtpMessage>();
    private MailQueueControl updater = new MailQueueControl();
    private SimpleSmtpServer server;

    // Options
    private long queueRefreshtimeout = DEFAULT_QUEUE_REFRESH_TIMEOUT;
    private boolean autoStart = false;

    private MailsterPop3Service pop3Service;
    private MailsterSWT main;
    
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

        private ScheduledFuture<?> handle = null;

        public void updateDelay()
        {
            if (handle == null)
                return;
            
            stop();
            long initialDelay = (System.currentTimeMillis() - lastExecutionTime) / 1000;
            if (initialDelay >= queueRefreshtimeout)
                initialDelay = 0;
            handle = scheduler.scheduleWithFixedDelay(this, initialDelay, queueRefreshtimeout,
                    SECONDS);            
        }
        
        public void start()
        {
            handle = scheduler.scheduleWithFixedDelay(this, queueRefreshtimeout, queueRefreshtimeout,
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
            if (main.getMailView().isTableDisposed())
                return;
            if (server == null || server.isStopped())
                main.log(Messages
                        .getString("MailsterSWT.log.server.notStarted")); //$NON-NLS-1$
            else
            {
            	int nb = 0;
                
                synchronized(receivedMessages)
                {
                    main.getMailView().getDataList().getReadWriteLock().writeLock().lock();
                    try
                    {
                    	main.getMailView().getDataList().addAll(receivedMessages);
                    }
                    finally
                    {
                    	main.getMailView().getDataList().getReadWriteLock().writeLock().unlock();
                    }
                    
	                nb = receivedMessages.size();
	                receivedMessages.clear();
                }

                main.log(MessageFormat.format(
                        Messages.getString("MailsterSWT.log.server.updated.emailQueue"), //$NON-NLS-1$
                        new Object[] { new Integer(nb) }));
                
                if (nb>0 && ConfigurationManager.CONFIG_STORE.
                		getBoolean(ConfigurationManager.NOTIFY_ON_NEW_MESSAGES_RECEIVED_KEY))
                	main.showTrayItemTooltipMessage(Messages.getString("MailView.trayTooltip.title") //$NON-NLS-1$
                			+DateUtilities.hourDateFormat.format(new Date())+")",  //$NON-NLS-1$
                			nb+Messages.getString("MailView.trayTooltip.newMessages")); //$NON-NLS-1$                
            }
        }
    }

    public MailsterSmtpService()
    {
        this.main = MailsterSWT.getInstance();
        
        try 
        {        	
        	pop3Service = new MailsterPop3Service();
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
        
        server = new SimpleSmtpServer();
        server.setInternalStoreActivated(false);
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

    public void clearQueue()
    {
    	if (!ConfigurationManager.CONFIG_STORE.
                getBoolean(ConfigurationManager.ASK_ON_REMOVE_MAIL_KEY) 
            || MessageDialog.openConfirm(main.getShell(), 
                    Messages.getString("MailView.dialog.confirm.deleteMails"), 
                    Messages.getString("MailView.dialog.confirm.clear")))
        {
        	main.getMailView().closeTabs(true);
	        receivedMessages.clear();
	        main.getMailView().clearDataList();
	        main.getSMTPService().getPop3Service().getUserManager().
	        	getMailBoxManager().removeAllMessagesFromSpecialAccount();
	        main.getOutlineView().setMessage(null);
        }
    }
    
    public void startServer(boolean debug)
    {
        try
        {            
            server.setDebug(debug);
            server.start();
            
            if (!server.isStopped())
            {
                main.log(ConfigurationManager.MAILSTER_VERSION
                        + MessageFormat.format(Messages.getString(debug
                                ? "MailsterSWT.log.server.started.debugmode"  //$NON-NLS-1$
                                : "MailsterSWT.log.server.started"), //$NON-NLS-1$
                                new Object[] { Messages.getString((queueRefreshtimeout*1000)+"") }));
                
                Integer pop3Port = new Integer(pop3Service.getPort());
                try 
                {
        			pop3Service.startService(debug);
        	        main.log(ConfigurationManager.MAILSTER_VERSION
        	                + MessageFormat.format(Messages.getString("MailsterSWT.log.pop3.started"), //$NON-NLS-1$
                                    new Object[] { pop3Port })); 
        		} 
                catch (IOException e) 
        		{
        			e.printStackTrace();
                    main.log(ConfigurationManager.MAILSTER_VERSION
                            + MessageFormat.format(Messages.getString("MailsterSWT.log.pop3.failed"), //$NON-NLS-1$
                                    new Object[] { pop3Port }));
        		}             
    
    	        updater.start();
            }
            else
            	main.log(ConfigurationManager.MAILSTER_VERSION
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
            if (!server.isStopped())
            {
                Integer pop3Port = pop3Service.getListeningPort();

                if (!force)
                {
                	refreshEmailQueue(true && !force);
                	pop3Service.stopService();
                }
                else
            		pop3Service.shutdownService();
            	
                main.log(ConfigurationManager.MAILSTER_VERSION
                        + MessageFormat.format(Messages.getString("MailsterSWT.log.pop3.stopped"), //$NON-NLS-1$
                                new Object[] { pop3Port }));
                
                updater.stop();
                
                server.stop();
                main.log(ConfigurationManager.MAILSTER_VERSION
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

    /**
     * Set the timeout between queue refreshs in seconds.
     * 
     * @param timeout the new timeout in seconds
     */
    public void setQueueRefreshTimeout(long timeout)
    {
        this.queueRefreshtimeout = timeout <= 0 ? 1 : timeout;
        updater.updateDelay();
    }

    public long getQueueRefreshTimeout()
    {
        return queueRefreshtimeout;
    }

    public String getOutputDirectory()
    {
        return ConfigurationManager.CONFIG_STORE.
            getString(ConfigurationManager.DEFAULT_ENCLOSURES_DIRECTORY_KEY);
    }

    public void setAutoStart(boolean autoStart)
    {
        this.autoStart = autoStart;
    }

    public boolean isAutoStart()
    {
        return autoStart;
    }

    public MailsterPop3Service getPop3Service()
    {
        return pop3Service;
    }
    
    public void setHostName(String hostName)
    {
        server.setHostName(hostName);
    }

    public int getPort()
    {
        return server.getPort();
    }
    
    public void setPort(int port)
    {
        server.setPort(port);
    }
    
    public void setConnectionTimeout(int timeout)
    {
    	server.setConnectionTimeout(timeout);
    }
}
