package ru.m210projects.Build.Net;

import java.net.InetSocketAddress;

public class UDPServer extends UDPSocket {
	public UDPServer(int port) throws Exception {
		super(port);
		sock.bind(new InetSocketAddress(port));
	}
}
