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

import com.badlogic.gdx.net.SocketHints;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class BClient extends Listener implements ISocket {
	
	private Client client;
	SocketAddr recieve;
	SocketHints hints = new SocketHints();
	byte[] lastRecieved;
	boolean recieved;
	
	public BClient(int port)
	{
		client = new Client();
		client.getKryo().register(byte[].class);
		client.start(); 
		try {
			client.connect(5000, "localhost", port);
		} catch (IOException e) {
			e.printStackTrace();
		} 		 		
		client.addListener(this); 
	
		recieve = new SocketAddr();
		recieve.port = port;
		recieve.address = "localhost";
	}
	
	@Override
	public void received(Connection c, Object p) { 				
		if(p instanceof byte[]) { 
			lastRecieved = (byte[]) p;
			recieved = true;
		}
	}

	@Override
	public SocketAddr recvfrom(byte[] dabuf, int bufsiz) {
		if(recieved) {
			System.arraycopy(lastRecieved, 0, dabuf, 0, bufsiz);
			recieved = false;
			return recieve;
		} else 
			return null;
	}

	@Override
	public void sendto(SocketAddr sockaddr, byte[] dabuf, int bufsiz) {
		client.sendTCP(dabuf);
	}
	
	@Override
	public void dispose() {

	}
}
