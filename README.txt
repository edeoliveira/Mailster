INSTALLATION NOTES for All platforms

- JAVA 1.5 is required.

- Correctly set the JAVA_HOME variable in the batch launch file (named
Mailster.bat for Windows systems and Mailster.sh for Linux).

INSTALLATION NOTES for Linux

You will need to install xulrunner which is downloadable 
from http://developer.mozilla.org/en/docs/XULRunner

http://developer.mozilla.org/en/docs/XULRunner_1.8.0.4_Release_Notes
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

You must run Mailster with an appropriate user because binding to SMTP port 
needs privileges.

Currently, i've successfully ran Mailster in the following environments :
- Mandriva 2007 distribution with xulrunner 1.8.0.4 and jdk 1.5.0_11
- Ubuntu 7.0.4 distribution and jdk 1.6.0_01 (in a VMWARE virtual machine) 
