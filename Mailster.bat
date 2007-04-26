@ECHO OFF
@REM SET JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n
SET CP="mailster.jar;lib\swt-win.jar;lib\mailapi.jar;lib\jface.jar;lib\glazedlists-1.7.0_java15.jar;%CLASSPATH%"

java %JAVA_OPTIONS% -cp %CP% org.mailster.MailsterSWT 300