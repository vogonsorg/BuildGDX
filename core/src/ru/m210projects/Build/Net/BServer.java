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

import java.net.InetAddress;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class BServer extends Listener implements ISocket {

	private Server server;
	SocketAddr recieve;
	int id;
	
	Object[] list = new Object[256];
	int head, trail = 0;

	public BServer(int port) throws Exception
	{
		recieve = new SocketAddr();
		recieve.port = port;
		recieve.address = InetAddress.getLocalHost().getHostAddress();

		server = new Server();
		server.getKryo().register(byte[].class);
		server.addListener(this);

		server.bind(port);

		server.start();
	}
	
	@Override
	public void connected(Connection c)
	{ 		
		id = c.getID(); //XXX
		System.out.println("На сервер подключился "+c.getRemoteAddressTCP().getHostString() + " - " + id); 
	}
	
	
	@Override
	public void dispose() {

	}
	
	@Override
	public void received(Connection c, Object p) { 				
		if(p instanceof byte[]) { 
			list[trail] = p;
			trail = (trail + 1) & 255;
			//recieve.address = c.getRemoteAddressTCP().getAddress().getHostAddress();
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
		server.sendToTCP(id, dabuf);
	}
}
