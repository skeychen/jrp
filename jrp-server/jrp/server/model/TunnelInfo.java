package jrp.server.model;

import java.net.ServerSocket;

import jrp.socket.SocketHelper;

public class TunnelInfo
{
	private String clientId;
	private ServerSocket tcpServerSocket;

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public ServerSocket getTcpServerSocket()
	{
		return tcpServerSocket;
	}

	public void setTcpServerSocket(ServerSocket tcpServerSocket)
	{
		this.tcpServerSocket = tcpServerSocket;
	}

	public void close()
	{
		SocketHelper.safeClose(tcpServerSocket);
	}
}
