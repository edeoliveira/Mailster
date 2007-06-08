@ECHO OFF
@REM SET JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n
SET CP="mailster.jar;lib\swt-win.jar;lib\mailapi.jar;lib\jface.jar;lib\glazedlists-1.7.0_java15.jar;%CLASSPATH%"
SET CP="lib\bcprov-jdk15-136.jar;lib\mina-core-1.1.0.jar;lib\mina-filter-ssl-1.1.0.jar;lib\slf4j-api-1.3.1.jar;%CP%"
SET CP="lib\slf4j-simple-1.3.1.jar;%CP%"

java %JAVA_OPTIONS% -cp %CP% org.mailster.MailsterSWT 300