package jrp.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class TestClient extends Thread implements Runnable
{
	public static String getTime()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA);
		return sdf.format(cal.getTime());
	}
	private Socket client = null;
	private String index = "";
	private boolean closed = false;
	public String getIndex()
	{
		return index;
	}
	
	public TestClient(Socket c, String idx)
	{
		this.client = c;
		this.index = idx;
		try
		{
			this.client.setKeepAlive(true);
			this.client.setSoTimeout(5000);// 读的时候等待时间
		}
		catch(Exception e)
		{
		}
	}
	
	public void send(String msg) throws Exception
	{
		if(closed)
		{
			throw new Exception("socket is closeed");
		}
		PrintWriter out = new PrintWriter(client.getOutputStream());
		out.println(msg);
		out.flush();
	}
	
	public boolean isClosed()
	{
		return closed;
	}
	
	public String close()
	{
		try
		{
			client.shutdownInput();
			client.shutdownOutput();
			client.close();
			System.out.println("client close " + index + " " + getTime());
		}
		catch(Exception ee)
		{
		}
		try
		{
			client.close();
		}
		catch(Exception ee)
		{
		}
		closed = true;
		return index;
	}

	public void run()
	{
		try
		{
			System.out.println("client connect " + index + " " + getTime());
			close:while(true)
			{
				try
				{
					InputStream is = client.getInputStream();// 读这个才会出错
					BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));// 读这个永远只会显示SocketTimeoutException Read timed out
					while(true)
					{
						try
						{
							String res = in.readLine();
							if(res != null)
							{
								System.out.println("                   " + res);
							}
						}
						catch(SocketTimeoutException ee)
						{
							break;// 没有数据了，就跳了去重新获取流
						}
					}
				}
				catch(SocketTimeoutException ee)
				{
					System.err.println("SocketTimeoutException " + ee.getMessage());
				}
				catch(IOException ex)
				{
					break close;
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		close();
	}
}
