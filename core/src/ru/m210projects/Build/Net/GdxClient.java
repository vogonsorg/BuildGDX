package ru.m210projects.Build.Net;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

public class GdxClient implements ISocket {
	Socket socket;
	SocketAddr recieve;
	
	public GdxClient(String servAddress, int port) throws Exception
	{
		recieve = new SocketAddr();
		recieve.port = port;
		recieve.address = servAddress;

		while(true) {
			socket = Gdx.net.newClientSocket(Net.Protocol.TCP, servAddress, port, new SocketHints());
			break; 	
		}
	}
	
	@Override
	public SocketAddr recvfrom(byte[] dabuf, int bufsiz) {
		try {
			int i = socket.getInputStream().available();
			if(i > 0) {
				if(i < bufsiz) bufsiz = i;
				socket.getInputStream().read(dabuf, 0, bufsiz);
				return recieve;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void sendto(SocketAddr sockaddr, byte[] dabuf, int bufsiz) {
		try {
			socket.getOutputStream().write(dabuf, 0, 256); //XXX
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
		
	}

}
