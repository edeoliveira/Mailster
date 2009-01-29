package test.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

public class VersionCheck 
{
	public static void main(String[] args) throws Exception 
	{
		//String url = "http://tedorg.free.fr/en/main.php";
		String url = "http://tedorg.free.fr/downloads/MailsterSMTP-1.0.0-M1.dist.zip";
		GetMethod method = new GetMethod(url);
		HttpClient client = new HttpClient();
		//method.setRequestHeader("Accept-Encoding", "gzip");
		
		//execute the method
        //String responseBody = null;
        InputStream is = null;
        try{
            client.executeMethod(method);
            //responseBody = method.getResponseBodyAsString();
            is = method.getResponseBodyAsStream();
        } catch (HttpException he) {
            System.err.println("Http error connecting to '" + url + "'");
            System.err.println(he.getMessage());
            System.exit(-4);
        } catch (IOException ioe){
            System.err.println("Unable to connect to '" + url + "'");
            System.exit(-3);
        }

        /*
        //write out the request headers
        System.out.println("*** Request ***");
        System.out.println("Request Path: " + method.getPath());
        System.out.println("Request Query: " + method.getQueryString());
        Header[] requestHeaders = method.getRequestHeaders();
        for (int i=0; i<requestHeaders.length; i++){
            System.out.print(requestHeaders[i]);
        }

        //write out the response headers
        System.out.println("*** Response ***");
        System.out.println("Status Line: " + method.getStatusLine());
        Header[] responseHeaders = method.getResponseHeaders();
        for (int i=0; i<responseHeaders.length; i++){
            System.out.print(responseHeaders[i]);
        }

        //write out the response body
        System.out.println("*** Response Body ***");
        System.out.println(responseBody);*/

        Header contentLength = method.getResponseHeader("Content-Length");
        long size = Long.parseLong(contentLength.getValue());
        FileOutputStream fos = new FileOutputStream(new File("test.zip"));
        
        byte[] buf = new byte[65536];
        int len = 0;
        long total = 0;
        long start = System.currentTimeMillis();
        while ((len = is.read(buf)) != -1)
        {
        	fos.write(buf, 0, len);
        	//total += len;
        	//long perct = (100 * total) / size;
        	//System.err.println("Downloaded "+total+"/"+size+" bytes - "+perct+"%");
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Download speed: "+((size * 1000)/ elapsed)+" bytes/sec");
        fos.flush();
        fos.close();
        is.close();
        
        //clean up the connection resources
        method.releaseConnection();

        System.exit(0);
	}
}
