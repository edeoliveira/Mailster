#!/bin/sh

#Debug options
#export JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n

export JAVA_HOME=/usr/java/jre1.5.0_11/
export PATH=$PATH:$JAVA_HOME/bin
export CP=./mailster.jar:lib/swt-gtklinux.jar:lib/mailapi.jar:lib/jface.jar:lib/glazedlists-1.7.0_java15.jar:$CLASSPATH

java $JAVA_OPTIONS -cp $CP org.mailster.MailsterSWT 300
