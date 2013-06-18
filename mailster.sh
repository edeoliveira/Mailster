#!/bin/sh

#Debug options
#export JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4321 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
export JAVA_OPTIONS=-server -Xms256m -Xmx256m -da:ca.odell.glazedlists...

export JAVA_HOME=/usr/java/jre1.6.0_01/

export CP=./mailster.jar:lib/*
$JAVA_HOME/javaw $JAVA_OPTIONS -cp $CP org.mailster.MailsterSWT