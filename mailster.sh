#!/bin/sh

#Debug options
#export JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4321 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
export JAVA_OPTIONS=-server -Xms256m -Xmx256m

export JAVA_HOME=/usr/java/jre1.6.0_01/
export PATH=$PATH:$JAVA_HOME/bin

export CP=./mailster.jar:lib/swt-gtklinux.jar:lib/mailapi.jar:lib/glazedlists_java15.jar:$CLASSPATH
export CP=lib/bcprov-jdk15-137.jar:lib/bcmail-jdk15-137.jar:$CP
export CP=lib/mina-core-1.1.6.jar:lib/mina-filter-ssl-1.1.6.jar:$CP
export CP=lib/slf4j-simple-1.4.3.jar:lib/slf4j-api-1.4.3.jar:$CP
export CP=lib/org.eclipse.core.commands_3.3.0.I20070605-0010.jar:lib/org.eclipse.equinox.common_3.3.0.v20070426.jar;$CP
export CP=lib/org.eclipse.jface_3.3.0.I20070606-0010.jar;$CP

java $JAVA_OPTIONS -cp $CP org.mailster.MailsterSWT
