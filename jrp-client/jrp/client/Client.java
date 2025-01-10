package jrp.client;

import java.net.Socket;
import java.util.List;

import jrp.client.connect.ControlConnect;
import jrp.client.model.Tunnel;
import jrp.log.Logger;
import jrp.log.LoggerImpl;
import jrp.socket.SocketHelper;
import jrp.util.FileUtil;
import jrp.util.GsonUtil;

public class Client
{
	private Context context = new Context();
	private Logger log = Logger.getLogger();
	private long pingTime = 60000;// 心跳包周期默认为一分钟

	public void setServerHost(String serverHost)
	{
		context.serverHost = serverHost;
	}

	public void setServerPort(int serverPort)
	{
		context.serverPort = serverPort;
	}

	public void setAuthToken(String authToken)
	{
		context.authToken = authToken;
	}

	public void setTunnelList(List<Tunnel> tunnelList)
	{
		context.tunnelList = tunnelList;
	}

	public void setLog(Logger log)
	{
		Logger.setLogger(log);
		this.log = log;
	}

	public void setPingTime(long pingTime)
	{
		this.pingTime = pingTime;
	}

	public void start()
	{
		boolean first = true;
		while(true)
		{
			try
			{
				if(first)
				{
					first = false;
				}
				else
				{
					try
					{
						Thread.sleep(10000);
					}
					catch(InterruptedException e)
					{
					}
				}
				Socket socket = SocketHelper.newSocket(context.serverHost, context.serverPort);
				Thread thread = new Thread(new ControlConnect(socket, context));
				thread.setDaemon(true);
				thread.start();
				while(true)
				{
					try
					{
						Thread.sleep(this.pingTime);
					}
					catch(InterruptedException e)
					{
					}
					try
					{
						if(thread.isAlive())
						{
							SocketHelper.sendpack(socket, Message.Ping());
						}
						else
						{
							socket.close();
							break;
						}
					}
					catch(Exception e)
					{
						log.error(e.toString());
					}
				}
			}
			catch(Exception e)
			{
				log.error(e.toString());
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		String filename = args.length > 0 ? args[0] : "classpath:client.json";
		String json = FileUtil.readTextFile(filename);
		Config config = GsonUtil.toBean(json, Config.class);
		Client client = new Client();
		client.setTunnelList(config.tunnelList);
		client.setServerHost(config.serverHost);
		client.setServerPort(config.serverPort);
		client.setAuthToken(config.authToken);
		client.setPingTime(config.pingTime);
		client.setLog(new LoggerImpl().setEnableLog(config.enableLog));
		client.start();
	}
}
