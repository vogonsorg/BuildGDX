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
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class BServer extends Listener implements ISocket {

	private Server server;
	SocketAddr recieve;
	int id;
	List<Object> lastRecieved = new ArrayList<Object>();

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
			lastRecieved.add(0, p);
			//recieve.address = c.getRemoteAddressTCP().getAddress().getHostAddress();
		}
	}

	@Override
	public SocketAddr recvfrom(byte[] dabuf, int bufsiz) {
		int size = lastRecieved.size();
		if(size > 0) {
			byte[] resbuf = (byte[]) lastRecieved.get(size - 1);
			if(resbuf != null) {
				if(bufsiz > resbuf.length) bufsiz = resbuf.length;
				System.arraycopy(resbuf, 0, dabuf, 0, bufsiz);
				lastRecieved.remove(size - 1);
				return recieve;
			}
		}  
			
		return null;
	}

	@Override
	public void sendto(SocketAddr sockaddr, byte[] dabuf, int bufsiz) {
//		System.err.println("try send to " + sockaddr.address + " " + id);
		server.sendToTCP(id, dabuf);
	}
}
