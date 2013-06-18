@ECHO OFF

@REM SET JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4321 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
SET JAVA_OPTIONS=-Xms256m -Xmx256m -da:ca.odell.glazedlists...

SET JAVA_HOME="C:\Program Files (x86)\Java\jre6\bin"

SET CP=./mailster.jar;lib/*

%JAVA_HOME%\javaw %JAVA_OPTIONS% -cp %CP% org.mailster.MailsterSWT
