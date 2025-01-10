/**
 * 与Ngrokd建立代理连接，并将接收到的流量转发给本地服务器
 */
package jrp.client.connect;

import java.net.Socket;

import jrp.Protocol;
import jrp.client.Context;
import jrp.client.Message;
import jrp.client.model.Tunnel;
import jrp.log.Logger;
import jrp.socket.PacketReader;
import jrp.socket.SocketHelper;
import jrp.util.GsonUtil;

public class ProxyConnect implements Runnable
{
	private String clientId;
	private Socket socket;
	private Context context;
	private Logger log = Logger.getLogger();

	public ProxyConnect(Socket socket, String clientId, Context context)
	{
		this.socket = socket;
		this.clientId = clientId;
		this.context = context;
	}

	@Override
	public void run()
	{
		try (Socket socket = this.socket)
		{
			SocketHelper.sendpack(socket, Message.RegProxy(clientId));
			PacketReader pr = new PacketReader(socket);
			String msg = pr.read();
			if(msg == null)
			{
				return;
			}
			log.info("收到服务器信息：" + msg);
			Protocol protocol = GsonUtil.toBean(msg, Protocol.class);
			if("StartProxy".equals(protocol.Type))
			{
				Tunnel tunnel = getTunnelByUrl(protocol.RemotePort);
				if(tunnel == null)
				{
					String html = "没有找到对应的管道：" + context.serverHost + ":" + protocol.RemotePort;
					log.error(html);
					String header = "HTTP/1.0 404 Not Found\r\n";
					header += "Content-Length: " + html.getBytes().length + "\r\n\r\n";
					header = header + html;
					SocketHelper.sendbuf(socket, header.getBytes());
					return;
				}
				log.info("建立本地连接：[host]=%s [port]=%s", tunnel.getLocalHost(), tunnel.getLocalPort());
				try (Socket localSocket = SocketHelper.newSocket(tunnel.getLocalHost(), tunnel.getLocalPort()))
				{
					Thread thread = new Thread(new LocalConnect(localSocket, socket));
					thread.setDaemon(true);
					thread.start();
					try
					{
						SocketHelper.forward(socket, localSocket);
					}
					catch(Exception e)
					{
						// ignore
					}
				}
				catch(Exception e)
				{
					log.error("本地连接建立失败：[host]=%s [port]=%s", tunnel.getLocalHost(), tunnel.getLocalPort());
					String html = "<html><body style=\"background-color: #97a8b9\"><div style=\"margin:auto; width:400px;padding: 20px 60px; background-color: #D3D3D3; border: 5px solid maroon;\"><h2>Tunnel ";
					html += context.serverHost + ":" + protocol.RemotePort;
					html += " unavailable</h2><p>Unable to initiate connection to <strong>";
					html += tunnel.getLocalHost() + ":" + String.valueOf(tunnel.getLocalPort());
					html += "</strong>. This port is not yet available for web server.</p>";
					String header = "HTTP/1.0 502 Bad Gateway\r\n";
					header += "Content-Type: text/html\r\n";
					header += "Content-Length: " + html.getBytes().length;
					header += "\r\n\r\n" + html;
					SocketHelper.sendbuf(socket, header.getBytes());
				}
			}
		}
		catch(Exception e)
		{
			log.error(e.toString());
		}
	}

	private Tunnel getTunnelByUrl(int remotePort)
	{
		for(Tunnel tunnel : context.tunnelList)
		{
			if(remotePort == tunnel.getRemotePort())
			{
				return tunnel;
			}
		}
		return null;
	}
}
