package ru.m210projects.Build.Net;

import java.io.IOException;
import java.net.InetAddress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;

public class GdxServer implements ISocket {
	ServerSocket server;
	SocketAddr recieve;
	Socket fromclient = null;

	public GdxServer(int port) throws Exception
	{
		recieve = new SocketAddr();
		recieve.port = port;
		recieve.address = InetAddress.getLocalHost().getHostAddress();

		server = Gdx.net.newServerSocket(Protocol.TCP, port, null);
		System.out.print("Waiting for a client...");
		
		fromclient = server.accept(null);
		System.out.println("Client connected");
	}

	@Override
	public SocketAddr recvfrom(byte[] dabuf, int bufsiz) {
		try {
			int i = fromclient.getInputStream().available();
			if(i > 0) {
				fromclient.getInputStream().read(dabuf, 0, bufsiz);
				fromclient.getInputStream().skip(i - bufsiz);
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
			fromclient.getOutputStream().write(dabuf, 0, bufsiz);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		
	}
}
