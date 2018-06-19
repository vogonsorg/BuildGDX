package ru.m210projects.Build.Net;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
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
			socket = Gdx.net.newClientSocket(Protocol.TCP, servAddress, port, new SocketHints());
			break; 	
		}
	}
	
	@Override
	public SocketAddr recvfrom(byte[] dabuf, int bufsiz) {
		try {
			int i = socket.getInputStream().available();
			if(i > 0) {
				socket.getInputStream().read(dabuf, 0, bufsiz);
				socket.getInputStream().skip(i - bufsiz);
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
			socket.getOutputStream().write(dabuf, 0, bufsiz);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
		
	}

}
