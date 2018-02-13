package ru.m210projects.Build.Net;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class BServer extends Listener implements ISocket {

	Server server;
	SocketAddr recieve;
	int id;
	byte[] lastRecieved;
	boolean recieved;

	public BServer(int port)
	{
		server = new Server();
		server.getKryo().register(byte[].class);
		server.addListener(this);
		try {
			server.bind(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.start();
		
		recieve = new SocketAddr();
		recieve.port = port;
		recieve.address = "localhost";
	}
	
	@Override
	public void connected(Connection c)
	{ 		
		id = c.getID();
		System.out.println("На сервер подключился "+c.getRemoteAddressTCP().getHostString() + " - " + id); 
	}
	
	
	@Override
	public void dispose() {

	}
	
	@Override
	public void received(Connection c, Object p) { 				
		if(p instanceof byte[]) { 
			lastRecieved = (byte[]) p;
			recieved = true;
		}
	}

	@Override
	public SocketAddr recvfrom(byte[] dabuf, int bufsiz) {
		if(recieved) {
			System.arraycopy(lastRecieved, 0, dabuf, 0, bufsiz);
			recieved = false;
			return recieve;
		} else 
			return null;
	}

	@Override
	public void sendto(SocketAddr sockaddr, byte[] dabuf, int bufsiz) {
		server.sendToTCP(id, dabuf);
	}
}
