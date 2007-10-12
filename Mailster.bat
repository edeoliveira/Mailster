@ECHO OFF

@REM SET JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n
SET JAVA_OPTIONS=-server

SET JAVA_HOME=C:\Program Files\Java\jdk1.6.0_01

SET CP=mailster.jar;lib\swt.jar;lib\swt-win.jar;lib\mailapi.jar;lib\glazedlists-1.7.0_java15.jar;%CLASSPATH%
SET CP=lib\bcprov-jdk15-137.jar;lib\bcmail-jdk15-137.jar;%CP%
SET CP=lib\mina-core-1.1.2.jar;lib\mina-filter-ssl-1.1.2.jar;%CP%
SET CP=lib\slf4j-api-1.4.3.jar;lib\slf4j-simple-1.4.3.jar;%CP%
set CP=lib\org.eclipse.core.commands_3.3.0.I20070605-0010.jar;lib\org.eclipse.equinox.common_3.3.0.v20070426.jar;%CP%
set CP=lib\org.eclipse.jface_3.3.0.I20070606-0010.jar;%CP%
SET CP="%CP%"

java %JAVA_OPTIONS% -cp %CP% org.mailster.MailsterSWT