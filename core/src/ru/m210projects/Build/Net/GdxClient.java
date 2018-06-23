package ru.m210projects.Build.Net;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

public class GdxClient implements ISocket {
	private Socket socket;

	public GdxClient(String servAddress, int port) throws Exception
	{
		socket = Gdx.net.newClientSocket(Protocol.TCP, servAddress, port, new SocketHints());
	}
	
	@Override
	public Socket recvfrom(byte[] dabuf, int bufsiz) {
		try {
			int i = socket.getInputStream().available();
			if(i > 0) {
				socket.getInputStream().read(dabuf, 0, bufsiz);
				socket.getInputStream().skip(i - bufsiz);
				return socket;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void sendto(Socket sockaddr, byte[] dabuf, int bufsiz) {
		try {
			socket.getOutputStream().write(dabuf, 0, bufsiz);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
		socket.dispose();
	}
}
