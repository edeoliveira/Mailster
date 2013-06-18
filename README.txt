WHAT IS MAILSTER ?

Mailster is a project aimed at testing software mail capabilities. It 
provides a mail server container to test emails sent by your apps 
without rewriting your application code.

DEVELOPPERS

Please note that pom.xml is not fully functionnal cause some libs are 
not available under public maven repositories.

INSTALLATION NOTES for All platforms

- JAVA 1.5 is required.

- Some cryptographic restrictions which apply to your country may 
prevent you to start Mailster. You can either download unrestricted
policy files from sun website or change the config.properties to
use a lower crypto strength (minimum required strength is 512). 

- Correctly set the JAVA_HOME variable in the batch launch file (named
Mailster.bat for Windows systems and Mailster.sh for Linux).

- If you do get a 'No server JVM at ...' error at startup please install
a full JDK or remove the -server command line option in the startup script.

- If you get an OutOfMemory exception please change the value (256) in the 
startup script with a value compatible with your available memory for the 
following parameters : -Xms256m -Xmx256m

INSTALLATION NOTES for Linux

You will need to install xulrunner which is downloadable 
from http://developer.mozilla.org/en/docs/XULRunner

https://developer.mozilla.org/en-US/docs/Getting_started_with_XULRunner
will guide you through installation process

If you're connecting to Internet through a proxy, you'll have to set
your proxy preferences. Some info is available at 
http://www.mozilla.org/unix/customizing.html#prefs
but you can also directly add or set the values  of 
the following lines into <Xulrunner-install-dir>/defaults/pref/xulrunner.js

# Proxy settings to append to xulrunner.js
pref("network.proxy.http", "<hostname>");
pref("network.proxy.http_port", <port>);
pref("network.proxy.no_proxies_on", "localhost,127.0.0.1");
pref("network.proxy.share_proxy_settings", true);
pref("network.proxy.ssl", "<hostname>");
pref("network.proxy.ssl_port", <port>);
pref("network.proxy.type", 1);

IMPORTANT : <...> notation is used to replace a user custom value

You must run Mailster with an appropriate user because binding to SMTP or 
POP3 ports needs privileges.

Currently, i've successfully ran Mailster in the following environments :
- Mandriva 2007 distribution with xulrunner 1.8.0.4 and jdk 1.5.0_11
- Ubuntu 7.0.4 distribution and jdk 1.6.0_01 (in a VMWARE virtual machine) 
