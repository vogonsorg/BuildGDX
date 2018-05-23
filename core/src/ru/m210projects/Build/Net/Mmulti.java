/*
 *  "Mmulti" code originally written by Ken Silverman
 *	Ken Silverman's official web site: http://www.advsys.net/ken
 *
 *  See the included license file "BUILDLIC.TXT" for license info.
 *
 *  This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build.Net;

import static ru.m210projects.Build.Engine.MAXPLAYERS;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import ru.m210projects.Build.OnSceenDisplay.Console;
import ru.m210projects.Build.Types.LittleEndian;

public class Mmulti {
	
	public static final int NETPORT = 0x5bd9;
	public static final int MAXPAKSIZ = 256;
	public static final int PAKRATE = 40;
	
	public static byte[] pakbuf = new byte[MAXPAKSIZ];
	
	//ENGINE CONTROLLED MULTIPLAYER VARIABLES:
	public static short numplayers, myconnectindex;
	public static short connecthead, connectpoint2[] = new short[MAXPLAYERS];   //Player linked list variables (indeces, not connection numbers)
	public static int timeoutcount = 60, resendagaincount = 4, lastsendtime[] = new int[MAXPLAYERS];
	

	public static ISocket mysock;
	public static SocketAddr ip;
	public static int myport = NETPORT, otherport[] = new int[MAXPLAYERS];
	public static int snatchport = 0, danetmode = 255, netready = 0;
	
	public static String snatchip, myip;
	public static String otherip[] = new String[MAXPLAYERS];
	
	
	public static final int FIFSIZ = 512; //16384/40 = 6min:49sec
	static int[] ipak[] = new int[MAXPLAYERS][FIFSIZ], icnt0 = new int[MAXPLAYERS];
	static int[] opak[] = new int[MAXPLAYERS][FIFSIZ], ocnt0 = new int[MAXPLAYERS], ocnt1 = new int[MAXPLAYERS];
	
	public static byte pakmem[] = new byte[4194304];
	public static int pakmemi = 1;

	//MULTI.OBJ sync state variables
	public static char syncstate;
	public static long tims, lastsendtims[] = new long[MAXPLAYERS];
	
	private static long GetTickCount()
	{
		return System.currentTimeMillis();
	}
	
	private static int[] crctab16 = new int[256];
	static void initcrc16 ()
	{
		int a;
		for(int j=0;j<256;j++)
		{
			a = 0;
			for(int i=7,k=(j<<8);i>=0;i--,k=((k<<1)&65535))
			{
				if (((k^a)&0x8000) != 0)
					a = ((a<<1)&65535)^0x1021;
				else a = ((a<<1)&65535);
			}
			crctab16[j] = (a&65535);
		}
	}

	private static int updatecrc16(int crc, byte dat) {
		return crc = (((crc<<8)&65535)^crctab16[(((crc & 0xFFFF)>>8)&65535)^(dat&0xFF)]);
	}
			
	private static int getcrc16 (byte[] buffer, int bufleng)
	{
		int j = 0;
		for(int i = bufleng-1; i >= 0; i--) j = updatecrc16(j, buffer[i]);
		return j & 65535;
	}
	
	private static int netinit (int index, int portnum)
	{
		if(index == 0)
			mysock = new BServer(portnum);
		else 
			mysock = new BClient(portnum);	
		
		if (mysock == null) return 0;
		ip = new SocketAddr();
		
		ip.port = portnum;
		ip.address = null;
		
		//if (bind(mysock, ip) != SOCKET_ERROR)
		{
			myport = portnum;
			try {
				myip =  InetAddress.getByName("localhost").getHostAddress();
				Console.Println("mmulti: This machine's IP is " + myip);
				return 1;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	private static void netsend (int other, byte[] dabuf, int bufsiz)
	{
		if (otherip[other] == null) return;
		
//		ip.address = otherip[other]; Nullpointer from netread, FIXME
//		ip.port = otherport[other];
		
		mysock.sendto(ip, dabuf, bufsiz);
	}
	
	private static int netread (byte[] dabuf, int bufsiz)
	{
		if ((ip = mysock.recvfrom(dabuf,bufsiz)) == null) 
			return -1;
		
		snatchip = ip.address;
		snatchport = ip.port;
		
		int other = myconnectindex;
		for(int i = 0; i < MAXPLAYERS; i++) {
			if (otherip[i] != null && (otherip[i].equals(snatchip)) && (otherport[i] == snatchport))
				{ other = i; break; }
		}
		
		return other;
	}
	
	private static int htons(int i)
	{
		int b0,b1;

	    b0 = (i&0x00ff)>>0;
	    b1 = (i&0xff00)>>8;

	    return ((b0<<8)|(b1<<0));
	}
	
	private static boolean isvalidipaddress(String st)
	{
		int bcnt = 0, num = 0;
		for(int i=0; i < st.length() && st.charAt(i) != 0; i++)
		{
			if (st.charAt(i) == '.') { bcnt++; num = 0; continue; }
			if (st.charAt(i) == ':')
			{
				if (bcnt != 3) return false;
				num = 0;
				for(i++; st.charAt(i) != 0;i++)
				{
					if ((st.charAt(i) >= '0') && (st.charAt(i) <= '9'))
						{ num = num*10+st.charAt(i)-'0'; if (num >= 65536) return false; }
					else return false;
				}
				return true;
			}
			if ((st.charAt(i) >= '0') && (st.charAt(i) <= '9'))
				{ num = num*10+st.charAt(i)-'0'; if (num >= 256) return false; }

		}
		return(bcnt == 3);
	}
	
	private static void initmultiplayers_reset()
	{
		initcrc16();
		Arrays.fill(icnt0, 0);
		Arrays.fill(ocnt0, 0);
		Arrays.fill(ocnt1, 0);
		for(int i = 0; i < MAXPLAYERS; i++)
			Arrays.fill(ipak[i], 0);

		lastsendtims[0] = GetTickCount();
		for(int i = 1; i < MAXPLAYERS; i++) 
			lastsendtims[i] = lastsendtims[0];
		numplayers = 1; myconnectindex = 0;

		Arrays.fill(otherip, null);
		for(int i = 0; i < MAXPLAYERS; i++)
			otherport[i] = htons(NETPORT); 
	}
	
	public static int initmultiplayerscycle()
	{
		getpacket(null);

		tims = GetTickCount();
		if (myconnectindex == connecthead)
		{
			int i;
			for(i = numplayers-1;i>0;i--)
				if (otherip[i] == null) break;
			if (i == 0) {
				netready = 1;
				return 0;
			}
		}
		else
		{
			if (netready != 0) return 0;
			if (tims < lastsendtims[connecthead]) lastsendtims[connecthead] = tims;
			if (tims >= lastsendtims[connecthead]+250) //1000/PAKRATE)
			{
				lastsendtims[connecthead] = tims;
					//   short crc16ofs;       //offset of crc16
					//   long icnt0;           //-1 (special packet for MMULTI.C's player collection)
					//   ...
					//   unsigned short crc16; //CRC16 of everything except crc16
				int k = 2;
				LittleEndian.putInt(pakbuf, k, -1);
				k += 4;
				pakbuf[k++] = (byte) 0xAA;
				LittleEndian.putShort(pakbuf, 0, (short)k);
				LittleEndian.putShort(pakbuf, k, (short) getcrc16(pakbuf,k)); k += 2;

				netsend(connecthead,pakbuf,k);
			}
		}

		return 1;
	}
		
	public static void initmultiplayers(String[] argv, int damultioption, int dacomrateoption, int dapriority)
	{
		if (initmultiplayersparms(argv))
		{
			Console.Println("Waiting for players...");
			while (initmultiplayerscycle() != 0);
		}
		netready = 1;
	}
	
	
	// Multiplayer command line summary. Assume myconnectindex always = 0 for 192.168.1.2
	//
	// (mast/slav) 2 player:               		3 player:
	// 192.168.1.2     game /net                game /n0:3
	// 192.168.1.100   game /net 192.168.1.2    game /n0 192.168.1.2
	// 192.168.1.4                              game /n0 192.168.1.2

	public static boolean initmultiplayersparms(String[] argv)
	{
		int portnum = NETPORT;
		initmultiplayers_reset();
		
		danetmode = 255; 
		int daindex = 0;
		
		if(argv != null) {
			// go looking for the port, if specified
			for (int i = 0;i < argv.length; i++) {
				if(argv[i] == null || argv[i].length() < 3 ) continue;
				
				if (argv[i].charAt(0) != '-' && argv[i].charAt(0) != '/') continue;
				if ((argv[i].charAt(1) == 'p' || argv[i].charAt(1) == 'P') && argv[i].charAt(2) != 0) {
					String port = argv[i].substring(2).trim();
					int j = Integer.parseInt(port);
					if (j > 0 && j<65535) portnum = j;
	
					Console.Println("mmulti: Using port " + portnum);
				}
			}

			for (int i = 0; i < argv.length; i++) {
				if(argv[i] == null) continue;
				
				if ((argv[i].charAt(0) == '-') || (argv[i].charAt(0) == '/')) {
					if ((argv[i].charAt(1) == 'N') || (argv[i].charAt(1) == 'n') || (argv[i].charAt(1) == 'I') || (argv[i].charAt(1) == 'i'))
					{
						numplayers = 2;
						if (argv[i].charAt(2) == '0')
						{
							danetmode = 0;
							if (argv[i].length() > 3 && (argv[i].charAt(3) == ':') && (argv[i].charAt(4) >= '0') && (argv[i].charAt(4) <= '9'))
							{
								numplayers = (short) (argv[i].charAt(4)-'0');
								if (argv[i].length() > 5 && (argv[i].charAt(5) >= '0') && (argv[i].charAt(5) <= '9')) numplayers = (short) (numplayers*10+(argv[i].charAt(5)-'0'));
								Console.Println("mmulti: "+ numplayers + "-player game");
							}
							Console.Println("mmulti: Master-slave mode");
						}
						continue;
					}
					else if ((argv[i].charAt(1) == 'P') || (argv[i].charAt(1) == 'p')) continue;
				}
				
				if (isvalidipaddress(argv[i]))
				{
					if ((danetmode == 1) && (daindex == myconnectindex)) daindex++;
					for(int j = 0; j <  argv[i].length() && argv[i].charAt(j) != 0; j++)
						if (argv[i].charAt(j) == ':') { 
							String port = argv[i].substring(j+1).trim();
							otherport[daindex] = htons(Integer.parseInt(port)); 
							break; 
						}
	
					otherip[daindex] = argv[i];
					Console.Println("mmulti: Player " + daindex + " at " + argv[i] + ":" + htons(otherport[daindex]));
					daindex++;
					continue;
				}
				else
				{
					int pt = htons(NETPORT);
					char[] st = argv[i].toCharArray();
	
					int pos;
					for(pos = 0; pos < argv[i].length() && st[pos] != 0; pos++) {
						if (argv[i].charAt(pos) == ':')
						{ 
							pt = htons(Integer.parseInt(argv[i].substring(pos+1))); 
							break; 
						}
					}
					try {
						InetAddress addr = InetAddress.getByName(argv[i].substring(0, pos));
						if ((danetmode == 1) && (daindex == myconnectindex)) daindex++;
						otherip[daindex] = addr.getHostAddress();
						otherport[daindex] = pt;
						Console.Println("mmulti: Player " + daindex + " at " + otherip[daindex] + ":" + htons(pt));
						daindex++;
					} catch (Exception e) {
						Console.Println("mmulti: Failed resolving " + argv[i]);
					}
					continue;
				}
			}
			
			netinit(daindex, portnum);
		}
		
		if ((danetmode == 255) && (daindex != 0)) { numplayers = 2; danetmode = 0; } //an IP w/o /n# defaults to /n0
		if ((numplayers >= 2) && (daindex != 0) && (danetmode == 0)) myconnectindex = 1;
		if (daindex > numplayers) numplayers = (short) daindex;

		connecthead = 0;
		for(int i=0; i < numplayers-1; i++) connectpoint2[i] = (short) (i+1);
		connectpoint2[numplayers-1] = -1;

		return (((danetmode == 0) && (numplayers >= 2)) || (numplayers == 2));
	}

	public static int getoutputcirclesize() {
		return 0;
	}
	
	public static void dosendpackets(int other)
	{
		if (otherip[other] == null) return;

			//Packet format:
			//   short crc16ofs;       //offset of crc16
			//   long icnt0;           //earliest unacked packet
			//   char ibits[32];       //ack status of packets icnt0<=i<icnt0+256
			//   while (short leng)    //leng: !=0 for packet, 0 for no more packets
			//   {
			//      long ocnt;         //index of following packet data
			//      char pak[leng];    //actual packet data :)
			//   }
			//   unsigned short crc16; //CRC16 of everything except crc16


		tims = GetTickCount();
		if (tims < lastsendtims[other]) lastsendtims[other] = tims;
		if (tims < lastsendtims[other]+1000/PAKRATE) return;
		lastsendtims[other] = tims;

		int k = 2;
		LittleEndian.putInt(pakbuf, k, icnt0[other]);k += 4;

		Arrays.fill(pakbuf, k, k + 32, (byte)0);
		for(int i=icnt0[other]; i < icnt0[other]+256; i++)
			if (ipak[other][i&(FIFSIZ-1)] != 0)
				pakbuf[((i-icnt0[other])>>3)+k] |= (1<<((i-icnt0[other])&7));
		k += 32;

		while ((ocnt0[other] < ocnt1[other]) && (opak[other][ocnt0[other]&(FIFSIZ-1)]) == 0) ocnt0[other]++;
		for(int i=ocnt0[other];i<ocnt1[other];i++)
		{
			int j = LittleEndian.getShort(pakmem, opak[other][i&(FIFSIZ-1)]);
			if (j == 0) continue; //packet already acked
			if (k+6+j+4 > pakbuf.length) break;

			LittleEndian.putShort(pakbuf, k, (short) j); k += 2;
			LittleEndian.putInt(pakbuf, k, i); k += 4;

			
			System.arraycopy(pakmem, opak[other][i&(FIFSIZ-1)]+2, pakbuf, k, j); k += j;
		}
		LittleEndian.putShort(pakbuf, k, (short) 0); k += 2;
		LittleEndian.putShort(pakbuf, 0, (short) k);
		LittleEndian.putShort(pakbuf, k, (short) getcrc16(pakbuf,k)); k += 2;

		netsend(other,pakbuf,k);
	}
	
	public static void sendpacket (int other, byte[] bufptr, int messleng)
	{
		if (numplayers < 2) return;

		if (pakmemi+messleng+2 > pakmem.length) pakmemi = 1;
		opak[other][ocnt1[other]&(FIFSIZ-1)] = pakmemi;
		LittleEndian.putShort(pakmem, pakmemi, (short) messleng);

		System.arraycopy(bufptr, 0, pakmem, pakmemi+2, messleng);

		pakmemi += messleng+2;
		ocnt1[other]++;

		dosendpackets(other);
	}
	
	public static int otherpacket;
	public static int getpacket(byte[] bufptr)
	{
		int j, k, ic0, crc16ofs, messleng, other = 0;

		if (numplayers < 2) return(0);

		if (netready != 0)
		{
			for(int i=connecthead;i>=0;i=connectpoint2[i])
			{
				if (i != myconnectindex) dosendpackets(i);
				if ((danetmode == 0) && (myconnectindex != connecthead)) break; //slaves in M/S mode only send to master
			}
		}

		while ((other = netread(pakbuf, pakbuf.length)) != -1)
		{
				//Packet format:
				//   short crc16ofs;       //offset of crc16
				//   long icnt0;           //earliest unacked packet
				//   char ibits[32];       //ack status of packets icnt0<=i<icnt0+256
				//   while (short leng)    //leng: !=0 for packet, 0 for no more packets
				//   {
				//      long ocnt;         //index of following packet data
				//      char pak[leng];    //actual packet data :)
				//   }
				//   unsigned short crc16; //CRC16 of everything except crc16
			k = 0;
			crc16ofs = LittleEndian.getUShort(pakbuf); k += 2;

			if ((crc16ofs+2 <= pakbuf.length) && (getcrc16(pakbuf,crc16ofs) == LittleEndian.getUShort(pakbuf, crc16ofs)))
			{
				ic0 = LittleEndian.getInt(pakbuf, k); k += 4;
				if (ic0 == -1)
				{
						 //Slave sends 0xaa to Master at initmultiplayers() and waits for 0xab response
						 //Master responds to slave with 0xab whenever it receives a 0xaa - even if during game!
					if (((pakbuf[k]&0xFF) == 0xAA) && (myconnectindex == connecthead))
					{
						for(other=1;other<numplayers;other++)
						{
								//Only send to others asking for a response
							if ((otherip[other] != null) && ((!otherip[other].equals(snatchip)) || (otherport[other] != snatchport))) continue;
							otherip[other] = snatchip;
							otherport[other] = snatchport;

								//   short crc16ofs;       //offset of crc16
								//   long icnt0;           //-1 (special packet for MMULTI.C's player collection)
								//   ...
								//   unsigned short crc16; //CRC16 of everything except crc16
							k = 2;
							LittleEndian.putInt(pakbuf, k, -1); k += 4;
							pakbuf[k++] = (byte) 0xAB;
							pakbuf[k++] = (byte)other;
							pakbuf[k++] = (byte)numplayers;
							LittleEndian.putShort(pakbuf, 0, (short)k);
							LittleEndian.putShort(pakbuf, k, (short)getcrc16(pakbuf,k)); k += 2;
							netsend(other,pakbuf,k);
							break;
						}
					}
					else if (((pakbuf[k]&0xFF) == 0xAB) && (myconnectindex != connecthead))
					{
						if (((pakbuf[k+1] & 0xFF) < (pakbuf[k+2] & 0xFF)) &&
							 ((pakbuf[k+2] & 0xFF) < MAXPLAYERS))
						{
							myconnectindex = pakbuf[k+1];
							numplayers = pakbuf[k+2];

							connecthead = 0;
							for(int i=0;i<numplayers-1;i++) connectpoint2[i] = (short) (i+1);
							connectpoint2[numplayers-1] = -1;

							otherip[connecthead] = snatchip;
							otherport[connecthead] = snatchport;
							netready = 1;
						}
					}
				}
				else
				{
					if (ocnt0[other] < ic0) ocnt0[other] = ic0;
					for(int i=ic0;i<Math.min(ic0+256,ocnt1[other]);i++)
						if ((pakbuf[((i-ic0)>>3)+k]&(1<<((i-ic0)&7))) != 0)
							opak[other][i&(FIFSIZ-1)] = 0;
					k += 32;
					
					
					messleng = LittleEndian.getUShort(pakbuf, k); k += 2;
					while (messleng != 0)
					{
						j = LittleEndian.getInt(pakbuf, k); k += 4;
						if ((j >= icnt0[other]) && (ipak[other][j&(FIFSIZ-1)] == 0))
						{
							if (pakmemi+messleng+2 > pakmem.length) pakmemi = 1;
							ipak[other][j&(FIFSIZ-1)] = pakmemi;
							LittleEndian.putShort(pakmem, pakmemi, (short)messleng);
							System.arraycopy(pakbuf, k, pakmem, pakmemi+2, messleng); pakmemi += messleng+2;
						}
						k += messleng;
						messleng = LittleEndian.getUShort(pakbuf, k); k += 2;
					}
				}
			}
		}

			//Return next valid packet from any player
		if (bufptr == null) return(0);
		for(int i=connecthead;i>=0;i=connectpoint2[i])
		{
			if (i != myconnectindex)
			{
				j = ipak[i][icnt0[i]&(FIFSIZ-1)];
				if (j != 0)
				{
					messleng = LittleEndian.getShort(pakmem, j);
					System.arraycopy(pakmem, j+2, bufptr, 0, messleng);
					otherpacket = i; ipak[i][icnt0[i]&(FIFSIZ-1)] = 0; icnt0[i]++;
					return(messleng);
				}
			}
			if ((danetmode == 0) && (myconnectindex != connecthead)) break; //slaves in M/S mode only send to master
		}

		return(0);
	}
}
