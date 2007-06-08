#!/bin/sh

#Debug options
#export JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n

export JAVA_HOME=/usr/java/jre1.5.0_11/
export PATH=$PATH:$JAVA_HOME/bin
export CP=./mailster.jar:lib/swt-gtklinux.jar:lib/mailapi.jar:lib/jface.jar:lib/glazedlists-1.7.0_java15.jar:$CLASSPATH
export CP=lib/bcprov-jdk15-136.jar:lib/mina-core-1.1.0.jar:lib/mina-filter-ssl-1.1.0.jar:lib/slf4j-api-1.3.1.jar:$CP
export CP=lib/slf4j-simple-1.3.1.jar:$CP

java $JAVA_OPTIONS -cp $CP org.mailster.MailsterSWT 300
