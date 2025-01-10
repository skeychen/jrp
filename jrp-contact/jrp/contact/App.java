package jrp.contact;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class App extends Thread implements Runnable
{
	public static InputStream getFileStream(String name) throws FileNotFoundException
	{
		if(name.toLowerCase().startsWith("classpath:"))
		{
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name.substring("classpath:".length()));
			if(is == null)
			{
				throw new FileNotFoundException(name);
			}
			return is;
		}
		else if(name.toLowerCase().startsWith("classpath*:"))
		{
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name.substring("classpath*:".length()));
			if(is == null)
			{
				throw new FileNotFoundException(name);
			}
			return is;
		}
		return new FileInputStream(name);
	}

	public static String readTextStream(InputStream is) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		int len;
		char[] buf = new char[1024];
		while((len = br.read(buf)) != -1)
		{
			sb.append(new String(buf, 0, len));
		}
		return sb.toString();
	}

	public static String readTextFile(String name) throws IOException
	{
		return readTextStream(getFileStream(name));
	}

	public static void waitForQuit()
	{
		// 以下代码用于防止main进程执行后结束，可用于捕获监听输入，输入quit或exit可退出
		try
		{
			java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
			for(;;)
			{
				String msg = reader.readLine();
				if(msg != null)
				{
					if(msg.equals("quit") || msg.equals("exit"))
					{
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
		}
	}
	
	public static ContactSocket remote = null;
	public static ContactSocket target = null;
	
	public void run()
	{
//		while(true)
//		{
//			try
//			{
//				sleep(5000L);// 5秒重连机制
//			}
//			catch(Exception ee)
//			{
//			}
//		}
	}

	public static void main(String[] args) throws Exception
	{
		String filename = args.length > 0 ? args[0] : "classpath:contact.json";
		String json = readTextFile(filename);
		System.out.println(json);
		ContactConfig c = GsonUtil.toBean(json, ContactConfig.class);
		if(c != null)
		{
			ContactSocket remote = new ContactSocket(c.remoteAddr, c.remotePort, c.timeout);
			ContactSocket target = new ContactSocket(c.targetAddr, c.targetPort, c.timeout);
			
			remote.setToSocket(target);
			target.setToSocket(remote);
			remote.start();
			target.start();
			
			//App app = new App();
			//app.start();
			
			waitForQuit();
		}
	}
}
