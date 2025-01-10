package jrp.contact;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ContactSocket extends Thread implements Runnable
{
	public static String getTime()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA);
		return sdf.format(cal.getTime());
	}
	static Object mylock = new Object();
	
	private String host = null;
	private int port;
	public int timeout = 3000;
	private Socket client = null;
	private ContactSocket toSocket = null;
	private int index = 0;
	public ContactSocket(String host, int port, int timeout)
	{
		if(host != null)
		{
			host = host.trim();
			if(host.equals("") || host.equals("0.0.0.0") || host.equals("127.0.0.1"))
			{
				host = null;// 本机不需要配置
			}
		}
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}
//	public boolean checkClose()
//	{
//		if(client != null)
//		{
//			try
//			{
//				PrintWriter out = new PrintWriter(client.getOutputStream());
//				out.println(0xFF);
//				out.flush();
//				return true;
//			}
//			catch(SocketTimeoutException e)
//			{
//			}
//			catch(IOException e)// SocketException
//			{
//			}
//		}
//		return false;
//	}
	public Socket getSocket() throws Exception
	{
//		if(client != null)
//		{
//			try
//			{
//				client.sendUrgentData(0xFF);
//			}
//			catch(SocketTimeoutException e)
//			{
//				System.err.println("SocketTimeoutException");
//			}
//			catch(IOException e)// SocketException
//			{
//				System.err.println(e.getMessage());// 已断开
//				try
//				{
//					client.close();
//				}
//				catch(Exception ee)
//				{
//				}
//				client = null;
//				System.out.println("close " + port + " = " + index + " " + getTime());
//			}
//		}
		if(client == null)
		{
			index++;
			System.out.println("connet " + port + " = " + index + " " + getTime());
			client = new Socket(host, port);
			client.setKeepAlive(true);
			client.setSoTimeout(timeout);
		}
		return client;
	}
	
	
	public void setToSocket(ContactSocket toSocket)
	{
		this.toSocket = toSocket;
	}
	
	// 防止双向发送时，相互影响
//	public static void sendMsg(ContactSocket sendSocket) throws Exception
//	{
//		String name = Thread.currentThread().getName();
		//System.err.println(name + " try");
		//lock:while(true)
		//{
		//	synchronized(mylock){
				//System.err.println(name + " locked");
//				sendSocket.send();
				//System.err.println(name + " unlocked");
		//		break lock;
		//	}
		//}
///	}
	
	private void send() throws Exception
	{
//		System.err.println("Send starting");
//		try
//		{
			InputStream in = getSocket().getInputStream();
			//java.io.ByteArrayOutputStream res = new java.io.ByteArrayOutputStream(10240);
			OutputStream out = this.toSocket.getSocket().getOutputStream();
			byte[] buf = new byte[1024];
			int len = in.read(buf);// 读不了会出异常
			while(len != -1)// -1其实就是连接关闭了
			{
				out.write(buf, 0, len);// 没有内容的时候，完全不会写入
				len = in.read(buf);
			}
			
//			byte[] arr = res.toByteArray();
//			System.err.println("arr size = " + arr.length);
//			if(arr.length > 0)
//			{
//				out.write(arr, 0, arr.length);
//				out.flush();
//			} 
//		}
//		finally
//		{
//			System.err.println("Send end");
//		}
	}
	
	public void close()
	{
		Socket clientx = client;
		int indexx = index;
		client = null;
		try
		{
			clientx.shutdownInput();
			clientx.shutdownOutput();
		}
		catch(Exception exclose)
		{
		}
		try
		{
			clientx.close();
		}
		catch(Exception exclose)
		{
		}
		try
		{
			sleep(5000L);// 断网或连不上
			clientx.close();// 再关一次试试
			clientx = null;
		}
		catch(Exception exclose)
		{
		}
		System.out.println("close " + port + " = " + indexx + " " + getTime());
	}

	public void run()
	{
		while(true)
		{
			try
			{
//				if(Thread.currentThread().isInterrupted())
//				{
//					break;
//				}
				// ContactSocket.sendMsg(this);// 多个客户端也需要排队发送
				send();
				// 不需要关闭，因为下一次发送就会失败
//				try
//				{
//					client.close();
//				}
//				catch(Exception exclose)
//				{
//				}
//				client = null;
			}
			catch(SocketTimeoutException ee)
			{
			}
			catch(IOException ee)
			{
				close();// 一起关
				toSocket.close();// 一起关
				try
				{
					sleep(10000L);// 断网或连不上
				}
				catch(Exception e)
				{
				}
				//System.err.println("SocketException");// 已断开
			}
			catch(Exception ee)// ConnectException(断网或连不上)
			{
				try
				{
					sleep(1000L);
				}
				catch(Exception e)
				{
				}
				ee.printStackTrace();
			}
		}
	}
}
