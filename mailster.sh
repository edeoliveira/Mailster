#!/bin/sh

#Debug options
#export JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4321 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
export JAVA_OPTIONS=-server -Xms256m -Xmx256m -da:ca.odell.glazedlists...

export JAVA_HOME=/usr/java/jre1.6.0_01/
export PATH=$PATH:$JAVA_HOME/bin

export CP=./mailster.jar:lib/mail.jar:$CLASSPATH
export CP=lib/glazedlists_java15.jar:$CP
export CP=lib/bcprov-jdk15-141.jar:lib/bcmail-jdk15-141.jar:$CP
export CP=lib/mina-core-2.0.0-M4.jar:lib/MailsterSMTP-1.0.0-RC1.jar:$CP
export CP=lib/slf4j-api-1.5.6.jar:lib/slf4j-log4j12-1.5.6.jar:lib\log4j-1.2.15.jar:$CP
export CP=lib/commons-lang-2.3.jar:$CP

export CP=lib/org.eclipse.jface_3.4.1.M20080827-2000.jar:$CP
export CP=lib/org.eclipse.swt_3.4.1.v3449c.jar:$CP
export CP=lib/org.eclipse.core.runtime_3.4.0.v20080512.jar:$CP
export CP=lib/org.eclipse.core.commands_3.4.0.I20080509-2000.jar:$CP
export CP=lib/org.eclipse.equinox.common_3.4.0.v20080421-2006.jar:$CP
export CP=lib/org.eclipse.osgi_3.4.2.R34x_v20080826-1230.jar:$CP

export CP=lib/org.eclipse.swt.linux.linux.x86_3.4.1.jar:$CP

java $JAVA_OPTIONS -cp $CP org.mailster.MailsterSWT