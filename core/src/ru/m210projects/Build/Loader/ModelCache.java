package ru.m210projects.Build.Loader;

import static ru.m210projects.Build.FileHandle.Cache1D.kGetBytes;
import static ru.m210projects.Build.FileHandle.Compat.BfileExtension;
import static ru.m210projects.Build.Strhandler.Bstrcasecmp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.m210projects.Build.Loader.MD2.MD2Loader;
import ru.m210projects.Build.Loader.MD3.MD3Loader;
import ru.m210projects.Build.Loader.Voxels.KVXLoader;

public class ModelCache {

	public static Model mdload(String filnam)
	{
		byte[] buf = kGetBytes(filnam, 0); 
		if(buf == null) return null;
	    
	    ByteBuffer bb = ByteBuffer.wrap(buf);
    	bb.order( ByteOrder.LITTLE_ENDIAN);
    	
    	Model vm = null;
    	if (Bstrcasecmp(BfileExtension(filnam),"kvx") == 0) 
		    if ((vm = KVXLoader.load(bb)) != null) 
		    	return(vm);

	    switch (bb.getInt(0))
	    {
		    case 0x32504449: //IDP2
		        vm = MD2Loader.load(bb);
		        break;
		    case 0x33504449: //IDP3
		        vm = MD3Loader.load(bb);
		        break; 
		    default:
		        vm = null; break;
	    }

	    return(vm);
	}
}
