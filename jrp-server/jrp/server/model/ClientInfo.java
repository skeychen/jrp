package jrp.server.model;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jrp.socket.SocketHelper;

public class ClientInfo
{
	private Socket controlSocket;
	private BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>();// LinkedBlockingQueue不同于ArrayBlockingQueue，它如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列。

	public Socket getControlSocket()
	{
		return controlSocket;
	}

	public void setControlSocket(Socket controlSocket)
	{
		this.controlSocket = controlSocket;
	}

	public BlockingQueue<Request> getRequestQueue()
	{
		return requestQueue;
	}

	public void close()
	{
		try
		{
			requestQueue.put(new Request());
		}
		catch(InterruptedException e)
		{
			// ignore
		}
		SocketHelper.safeClose(controlSocket);
	}
}
