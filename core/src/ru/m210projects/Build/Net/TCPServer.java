// This file is part of BuildGDX.
// Copyright (C) 2017-2018  Alexander Makarov-[M210] (m210-2007@mail.ru)
//
// BuildGDX is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// BuildGDX is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with BuildGDX.  If not, see <http://www.gnu.org/licenses/>.

package ru.m210projects.Build.Net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.m210projects.Build.Architecture.BuildGdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;

public class TCPServer implements ISocket {
	private ServerSocket server;
	private List<Socket> clist;

	public TCPServer(final int numplayers, final int port) throws Exception
	{
		server = Gdx.net.newServerSocket(Protocol.TCP, port, null);
		System.out.println("Waiting for a client...");
		
		clist = new ArrayList<Socket>();
		Thread clientListener = new Thread(new Runnable() 
		{
			public void run()
			{
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
			if(BuildGdx.input != null) {
				BuildGdx.input.processMessages();
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
	}
}
