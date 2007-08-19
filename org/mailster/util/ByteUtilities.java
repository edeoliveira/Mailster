package org.mailster.util;

public class ByteUtilities 
{   
    /**
     * Returns the integer represented by 4 bytes in network byte order.
     */
    public static int networkByteOrderToInt(byte[] buf, int start, int count) 
    {
    	if (count > 4)
    		throw new IllegalArgumentException("Cannot handle more than 4 bytes");

    	int result = 0;

        for (int i = 0; i < count; i++) 
        {
        	result <<= 8;
        	result |= ((int)buf[start+i] & 0xff);
        }
        
        return result;
    }

    /**
     * Encodes an integer into 4 bytes in network byte order in the buffer
     * supplied.
     */
    public static void intToNetworkByteOrder(int num, byte[] buf, int start, int count) 
    {
    	if (count > 4)
    		throw new IllegalArgumentException("Cannot handle more than 4 bytes");
    
    	for (int i = count-1; i >= 0; i--) 
    	{
    		buf[start+i] = (byte)(num & 0xff);
    		num >>>= 8;
    	}
    }
}
