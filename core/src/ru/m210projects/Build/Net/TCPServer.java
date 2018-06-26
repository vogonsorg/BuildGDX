package ru.m210projects.Build.Net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.m210projects.Build.Input.BInput;
import ru.m210projects.Build.Net.WaifUPnp.UPnP;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;

public class TCPServer implements ISocket {
	private ServerSocket server;
	private List<Socket> clist;
	private int port;
	private boolean useUPnP;
	
	public TCPServer(final int numplayers, final int port, final boolean useUPnP) throws Exception
	{
		this.port = port;
		this.useUPnP = useUPnP;
		server = Gdx.net.newServerSocket(Protocol.TCP, port, null);
		System.out.println("Waiting for a client...");
		
		clist = new ArrayList<Socket>();
		Thread clientListener = new Thread(new Runnable() 
		{
			public void run()
			{
				if(useUPnP) {
					System.out.println("Attempting UPnP port forwarding...");
			        if (UPnP.isUPnPAvailable()) { //is UPnP available?
			            if (UPnP.isMappedTCP(port)) { //is the port already mapped?
			                System.out.println("UPnP port forwarding not enabled: port is already mapped");
			            } else if (UPnP.openPortTCP(port)) { //try to map port
			                System.out.println("UPnP port forwarding enabled");
			            } else {
			                System.out.println("UPnP port forwarding failed");
			            }
			        } else {
			            System.out.println("UPnP is not available");
			        }
				}
				
				while(!Thread.currentThread().isInterrupted() && clist.size() < numplayers - 1)
				{
					try {
						clist.add(server.accept(null));
					} catch(Exception e) { return; } //because libgdx.net haven't server.close()
					
					System.out.println("Client " + clist.size() + " connected");
					Iterator<Socket> it = clist.iterator();
				    while(it.hasNext()) {
				    	Socket client = (Socket)it.next();
				    	if(!client.isConnected()) {
				    		it.remove();
				    	}
				    }
				}
			}
		});
		clientListener.start();

		while(clientListener.isAlive())
		{
			if(Gdx.input != null) {
				((BInput) Gdx.input).updateRequest();
				if(Gdx.input.isKeyPressed(Keys.ESCAPE)) {
					clientListener.interrupt();
					dispose();
					throw new Exception("Canceled");
				}
			}
		}
	}

	@Override
	public Socket recvfrom(byte[] dabuf, int bufsiz) {
		for(Socket client : clist) {
			try {
				int i = client.getInputStream().available();
				if(i > 0) {
					client.getInputStream().read(dabuf, 0, bufsiz);
					client.getInputStream().skip(i - bufsiz);
					return client;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void sendto(Object address, byte[] dabuf, int bufsiz) {
		try {
			Socket sockaddr = (Socket) address;
			sockaddr.getOutputStream().write(dabuf, 0, bufsiz);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		server.dispose();
		clist.clear();
		if(useUPnP)
			UPnP.closePortTCP(port);
	}
}
