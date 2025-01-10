package jrp.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TestPool extends Thread
{
	public static int serverPort = 0;
	private final static Map<String, TestClient> pool = new ConcurrentHashMap<>();
	
	public static void put(TestClient client)
	{
		pool.put(client.getIndex(), client);
	}
	
	public static String getTime()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA);
		return sdf.format(cal.getTime());
	}
	

	@SuppressWarnings("deprecation")
	public void run()
	{
		try
		{
			while(true)// 定时给每个客户端发消息
			{
				if(pool.size() > 0)
				{
					String msg = "{\"cmd\":\"charge\",\"par\":\"test\",\"code\":\"00000\",\"data\":\"test\"}\n";
					System.out.println("         " + "[" + pool.size() + "] " + msg.replaceAll("\\n", "\\\\n"));
					for(Map.Entry<String, TestClient> item : pool.entrySet())
					{
						TestClient client = item.getValue();
						if(client.isClosed())
						{
							pool.remove(client.getIndex());// 已退出直接移除
							client.stop();
						}
						else
						{
							try
							{
								client.send(msg);
							}
							catch(Exception ex)
							{
								System.err.println(ex.getMessage());
								pool.remove(client.close());// 关闭并移除
								client.stop();
							}
						}
					}
					// 客户端关闭过多或过快，导致不知道对方已关闭
//					if(pool.size() > 1)
//					{
//						StringBuilder sb = new StringBuilder();
//						for(Map.Entry<String, TestClient> item : pool.entrySet())
//						{
//							TestClient client = item.getValue();
//							sb.append(" " + client.isClosed());
//						}
//						System.err.println(sb.toString());
//						sb.setLength(0);
//					}
				}
				Thread.sleep(5000L);
			}
		}
		catch(Exception ex)
		{
		}
	}
}
