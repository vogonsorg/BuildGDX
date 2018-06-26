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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

public class TCPClient implements ISocket {
	private Socket socket;

	public TCPClient(String servAddress, int port) throws Exception
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
	public void sendto(Object address, byte[] dabuf, int bufsiz) {
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
