@ECHO OFF

@REM SET JAVA_OPTIONS=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=999,server=y,suspend=n -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4321 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
SET JAVA_OPTIONS=-Xms256m -Xmx256m -da:ca.odell.glazedlists...

SET JAVA_HOME=C:\Program Files\Java\jdk1.6.0_01

SET CP=mailster.jar;lib\mail.jar;lib\glazedlists_java15.jar;%CLASSPATH%
SET CP=lib\bcprov-jdk15-141.jar;lib\bcmail-jdk15-141.jar;%CP%
SET CP=lib\mina-core-2.0.0-M4.jar;lib\MailsterSMTP-1.0.0-M2.jar;%CP%
SET CP=lib\slf4j-api-1.5.6.jar;lib\slf4j-log4j12-1.5.6.jar;lib\log4j-1.2.15.jar;%CP%
set CP=lib\commons-lang-2.3.jar;%CP%

set CP=lib\org.eclipse.jface_3.4.1.M20080827-2000.jar;%CP%
set CP=lib\org.eclipse.swt_3.4.1.v3449c.jar;%CP%
set CP=lib\org.eclipse.core.runtime_3.4.0.v20080512.jar;%CP%
set CP=lib\org.eclipse.core.commands_3.4.0.I20080509-2000.jar;%CP%
set CP=lib\org.eclipse.equinox.common_3.4.0.v20080421-2006.jar;%CP%
set CP=lib\org.eclipse.osgi_3.4.2.R34x_v20080826-1230.jar;%CP%
set CP=lib\org.eclipse.swt.win32.win32.x86_3.4.1.v3449c.jar;%CP%

SET CP="%CP%"

java %JAVA_OPTIONS% -cp %CP% org.mailster.MailsterSWT