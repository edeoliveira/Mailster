package test.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class BASICVersionCheck 
{
	public static void main(String[] args) throws Exception 
	{
		URL url = new URL("http://tedorg.free.fr/downloads/MailsterSMTP-1.0.0-M1.dist.zip");
		System.out.println(url.getUserInfo());
		InputStream is = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File("test.zip"));
        
        byte[] buf = new byte[65536];
        int len = 0;
        long total = 0;
        long start = System.currentTimeMillis();
        while ((len = is.read(buf)) != -1)
        {
        	fos.write(buf, 0, len);
        	total += len;
        	//long perct = (100 * total) / size;
        	//System.err.println("Downloaded "+total+"/"+size+" bytes - "+perct+"%");
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Download speed: "+((1388498 * 1000)/ elapsed)+" bytes/sec");
        fos.flush();
        fos.close();
        is.close();
        
        //clean up the connection resources
        //method.releaseConnection();

        System.exit(0);
	}
}
