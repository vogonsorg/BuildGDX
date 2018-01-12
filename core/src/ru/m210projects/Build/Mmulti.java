package ru.m210projects.Build;

import static ru.m210projects.Build.Engine.MAXPLAYERS;
import static ru.m210projects.Build.Engine.totalclock;

public class Mmulti {
	//ENGINE CONTROLLED MULTIPLAYER VARIABLES:
	public static short numplayers, myconnectindex;
	public static short connecthead, connectpoint2[] = new short[MAXPLAYERS];   //Player linked list variables (indeces, not connection numbers)
	public static int timeoutcount = 60, resendagaincount = 4, lastsendtime[] = new int[MAXPLAYERS];
	
	//MULTI.OBJ sync state variables
	public static char syncstate;
		
	public static void initmultiplayers(int damultioption, int dacomrateoption, int dapriority)
	{
		int i;
//		String parm;
//		char[] delims = {'\\','-','/','\0'};

//		initcrc();
//		for(i=0;i<MAXPLAYERS;i++)
//		{
//			incnt[i] = 0;
//			outcntplc[i] = 0;
//			outcntend[i] = 0;
//			bakpacketlen[i][255] = -1;
//		}
//
//		for(i=_argc-1;i>0;i--)
//			if ((parm = strtok(_argv[i],delims[0])) != null)
//				if (Bstrcmp("net",parm) == 0) break;
//		if (i == 0)
//		{
//			numplayers = 1; myconnectindex = 0;
//			connecthead = 0; connectpoint2[0] = -1;
//			return;
//		}
//		gcom = (gcomtype)atol(_argv[i+1]);
		
		numplayers = 1; //gcom.numplayers;
		myconnectindex = 0; //gcom.myconnectindex-1;

		connecthead = 0;
		for(i=0;i<numplayers-1;i++) connectpoint2[i] = (short) (i+1);
		connectpoint2[numplayers-1] = -1;

		for(i=0;i<numplayers;i++) lastsendtime[i] = totalclock;
	}

	public static int getoutputcirclesize() {
		return 0;
	}
}
