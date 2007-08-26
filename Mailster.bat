@ECHO OFF

@REM SET JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n
SET JAVA_OPTIONS=-server

SET CP="mailster.jar;lib\swt-win.jar;lib\mailapi.jar;lib\jface.jar;lib\glazedlists-1.7.0_java15.jar;%CLASSPATH%"
SET CP="lib\bcprov-jdk15-137.jar;lib\mina-core-1.1.2.jar;lib\mina-filter-ssl-1.1.2.jar;lib\slf4j-api-1.4.3.jar;%CP%"
SET CP="lib\slf4j-simple-1.4.3.jar;%CP%"

java %JAVA_OPTIONS% -cp %CP% org.mailster.MailsterSWT 300