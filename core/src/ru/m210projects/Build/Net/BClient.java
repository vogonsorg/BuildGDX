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
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class BClient extends Listener implements ISocket {
	
	private Client client;
	SocketAddr recieve;

	Object[] list = new Object[256];
	int head, trail = 0;
	
	public BClient(String servAddress, int port) throws Exception
	{
		recieve = new SocketAddr();
		recieve.port = port;
		recieve.address = servAddress;

		client = new Client();
		client.getKryo().register(byte[].class);
		client.start(); 
		while(true) {
			try {
				client.connect(5000, servAddress, port);
				break;
			} catch (IOException e) {} 	
		}
		client.addListener(this); 
	}
	
	@Override
	public void received(Connection c, Object p) { 				
		if(p instanceof byte[]) { 
			list[trail] = p;
			trail = (trail + 1) & 255;
		}
	}

	@Override
	public SocketAddr recvfrom(byte[] dabuf, int bufsiz) {
		
		if(head != trail)
		{
			byte[] resbuf = (byte[]) list[head];
			System.arraycopy(resbuf, 0, dabuf, 0, bufsiz);
			head = (head + 1) & 255;
			return recieve;
		}
		
		return null;
	}

	@Override
	public void sendto(SocketAddr sockaddr, byte[] dabuf, int bufsiz) {
		client.sendTCP(dabuf); //java.lang.IllegalArgumentException
	}
	
	@Override
	public void dispose() {

	}
}
