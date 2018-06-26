package ru.m210projects.Build.Net;

import static ru.m210projects.Build.Net.Mmulti.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPSocket implements ISocket {

	public DatagramChannel sock;
	public int port;
	private ByteBuffer buf = ByteBuffer.allocate(MAXPAKSIZ);
	
	public UDPSocket(int port) throws IOException
	{
		sock = DatagramChannel.open();
		sock.configureBlocking(false);
		this.port = port;
	}
	
	@Override
	public InetSocketAddress recvfrom(byte[] dabuf, int bufsiz) {
		try {
			buf.clear();
			InetSocketAddress recieve = (InetSocketAddress) sock.receive(buf);
			if(recieve != null) {
				buf.rewind();
				buf.get(dabuf, 0, bufsiz);
				return recieve;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void sendto(Object address, byte[] dabuf, int bufsiz) {
		buf.clear();
		buf.put(dabuf, 0, bufsiz);
		buf.flip();
		try {
			sock.send(buf, (InetSocketAddress) address);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
		try {
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
