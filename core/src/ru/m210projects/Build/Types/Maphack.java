// This file is part of BuildGDX.
// Copyright (C) 2017-2019  Alexander Makarov-[M210] (m210-2007@mail.ru)
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

package ru.m210projects.Build.Types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ru.m210projects.Build.Engine.*;
import static ru.m210projects.Build.FileHandle.Cache1D.*;
import static ru.m210projects.Build.FileHandle.Compat.toLowerCase;

import ru.m210projects.Build.Render.Types.Spriteext;
import ru.m210projects.Build.Script.Scriptfile;

public class Maphack extends Scriptfile {
	
	private long MapCRC;
	private Spriteext[] spriteext;
	private static enum Token {
		MapCRC, 
		Sprite,
		
		AngleOffset,
		XOffset,
		YOffset,
		ZOffset,
		NoModel,
		
		Error,
		EOF;
	}

	private final static Map<String , Token> basetokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("sprite", Token.Sprite);
			put("crc32", Token.MapCRC);
		}
	};
	
	private final static Map<String , Token> sprite_tokens = new HashMap<String , Token>() {
		private static final long serialVersionUID = 1L;
		{
			put("angoff",     Token.AngleOffset);
			put("mdxoff",	  Token.XOffset);
			put("mdyoff",	  Token.YOffset);
			put("mdzoff",	  Token.ZOffset);
			put("notmd",	  Token.NoModel);
		}
    };

	public Maphack(String filename)
	{
		spriteext = new Spriteext[MAXSPRITES];
		for (int i = 0; i < MAXSPRITES; i++)
			spriteext[i] = new Spriteext();
		
		byte[] data = kGetBytes(filename, 0);
		int flen = data.length;
		byte[] tx = Arrays.copyOf(data, flen + 2);
		tx[flen] = tx[flen + 1] = 0;

		preparse(tx, flen);
		this.filename = filename;
		
		while (true)
        {
			switch(gettoken(basetokens))
			{
				case MapCRC:
					Integer crc32 = getsymbol();
					if(crc32 == null) break;
					
					MapCRC = crc32 & 0xFFFFFFFFL;
					break;
				case Sprite:
					Integer sprnum = getsymbol();
					if(sprnum == null) break;
	
					switch (gettoken(sprite_tokens))
	                {
		            	default: break;
		                case AngleOffset:
		                	spriteext[sprnum].angoff = getsymbol().shortValue();
		                	break;
		                case XOffset:
		                	spriteext[sprnum].xoff = getsymbol();
		                	break;
		                case YOffset:
		                	spriteext[sprnum].yoff = getsymbol();
		                	break;
		                case ZOffset:
		                	spriteext[sprnum].zoff = getsymbol();
		                	break;
		                case NoModel:
		                	spriteext[sprnum].flags |= 1; //SPREXT_NOTMD;
		                	break;	
	                }
					
					break;
				case Error:
					break;
				case EOF:
					return;
				default:
					break;
			}
        }
	}
	
	private Token gettoken(Map<String , Token> list) {
		int tok;
		if ((tok = gettoken()) == -2) 
			return Token.EOF;

		Token out = list.get(toLowerCase(textbuf.substring(tok, textptr)));
		if (out != null)
			return out;

		errorptr = textptr;
		return Token.Error;
	}
	
	public boolean loadHack(long crc32, Spriteext[] dst)
	{
		if(crc32 != MapCRC) return false;
		System.arraycopy(spriteext, 0, dst, 0, MAXSPRITES);
		return true;
	}
}
