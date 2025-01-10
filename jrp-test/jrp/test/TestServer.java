package jrp.test;

import java.net.ServerSocket;

public final class TestServer
{
	public static void start(int port) throws Exception
	{
		@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(port);
		System.out.println(port + " start");
		TestPool pool = new TestPool();
		TestPool.serverPort = port;
		pool.start();
		
		int i = 1;
		while(true)
		{
			try
			{
				String index = "" + i++;
				TestClient client = new TestClient(server.accept(), index);
				TestPool.put(client);
				client.start();
				// System.out.println("Wait for next");
			}
			catch(Exception e)
			{
			}
		}
	}
}
