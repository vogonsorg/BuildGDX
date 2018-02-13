package ru.m210projects.Build.Net;

import com.badlogic.gdx.utils.Disposable;

public interface ISocket extends Disposable {

	public SocketAddr recvfrom (byte[] dabuf, int bufsiz);
	public void sendto (SocketAddr sockaddr, byte[] dabuf, int bufsiz);
	public void dispose();
	
}
