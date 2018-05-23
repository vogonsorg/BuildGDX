/*
 * File Tokeniser/Parser/Whatever
 * by Jonathon Fowler
 * Remixed completely by Ken Silverman
 * See the included license file "BUILDLIC.TXT" for license info.
 * 
 * This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)
 */

package ru.m210projects.Build;

import static ru.m210projects.Build.FileHandle.Compat.*;
import static ru.m210projects.Build.FileHandle.Cache1D.*;

import java.io.File;
import java.util.Arrays;


public class Scriptfile {

	public String textbuf;
	public int errorptr;
	public int textlength;
	public int ltextptr;		// pointer to start of the last token fetched (use this for line numbers)
	public int textptr;
	public int eof;
	public String filename;
	public int linenum;
	public int[] lineoffs;
	public String path;
	
	public Scriptfile() {} //temp constructor

	private void skipovertoken() { 
		while ((textptr < eof) && (textbuf.charAt(textptr)) != 0) textptr++;  
	}
	
	private void skipoverws() { 
		if ((textptr < eof) && (textbuf.charAt(textptr)) == 0) textptr++; 
	}
	
	public String getstring()
	{
	    int out = gettoken();
	    if (out == -1)
	    {
	        //initprintf("Error on line %s:%d: unexpected eof\n",sf.filename,scriptfile_getlinum(sf,sf.textptr));
	        return null;
	    }

	    String txt = textbuf.substring(out, textptr);
	    txt = txt.replace("/", File.separator);
	    return toLowerCase(txt);
	}
	
	
	public int gettoken()
	{
	    int start;

	    skipoverws();
	    if (textptr >= eof) return -2;
	    start = ltextptr = textptr;
	    skipovertoken();

	    return start;
	}
	
	public Double getdouble()
	{
	    int t = gettoken();
	    if (t == -2) return null;
	    
	    try {
	    	return Double.parseDouble(textbuf.substring(t, textptr));
	    } catch(Exception e) {
	        return null;   // not found
	    }
	}

	public Integer getsymbol()
	{
	    int t = gettoken();
	    if (t == -2) return null;
	    
	    try {
	    	return Integer.parseInt(textbuf.substring(t, textptr), 10);
	    } catch(Exception e) {
	    	// looks like a string, so find it in the symbol table
//	        if (scriptfile_getsymbolvalue(t, num)) return 0;
//	        initprintf("Error on line %s:%d: expecting symbol, got \"%s\"\n",sf.filename,scriptfile_getlinum(sf,sf.ltextptr),t);
	        return null;   // not found
	    }
	}
	
	public int getbraces()
	{
	    int bracecnt;
	    int bracestart;

	    skipoverws();
	    if (textptr >= eof)
	    {
	        //initprintf("Error on line %s:%d: unexpected eof\n",sf.filename,scriptfile_getlinum(sf,sf.textptr));
	        return -1;
	    }

	    if (textbuf.charAt(textptr) != '{')
	    {
	        //initprintf("Error on line %s:%d: expecting '{'\n",sf.filename,scriptfile_getlinum(sf,sf.textptr));
	        return -1;
	    }
	    bracestart = ++textptr; bracecnt = 1;
	    while (true)
	    {
	        if (textptr >= eof) return(0);

	        if (textbuf.charAt(textptr) == '{') bracecnt++;
	        if (textbuf.charAt(textptr) == '}') { bracecnt--; if (bracecnt == 0) break; }
	        textptr++;
	    }
	    int braceend = textptr - 1;
	    textptr = bracestart;
	    return braceend;
	}
	
	public boolean eof()
	{
	    skipoverws();
	    if (textptr >= eof) return true;
	    return false;
	}
	
	public int getlinum(int ptr)
	{
	    int i, stp;
	    
	    int ind = ptr;

	    for (stp=1; stp+stp<linenum; stp+=stp); //stp = highest power of 2 less than linenum
	    for (i=0; stp != 0; stp>>=1)
	        if ((i+stp < linenum) && (lineoffs[i+stp] < ind)) i += stp;
	    return i+2; //i = index to highest lineoffs which is less than ind; convert to 1-based line numbers
	}
	
	public Scriptfile(byte[] tx, int flen) //preparse
	{
		//Count number of lines
	    int numcr = 1;
	    for (int i=0; i<flen; i++)
	    {
	        //detect all 4 types of carriage return (\r, \n, \r\n, \n\r :)
	        int cr=0; if (tx[i] == '\r') { i += ((tx[i+1] == '\n')?1:0); cr = 1; }
	        else if (tx[i] == '\n') { i += ((tx[i+1] == '\r')?1:0); cr = 1; }
	        if (cr != 0) { numcr++; continue; }
	    }
	    
	    linenum = numcr;
	    lineoffs = new int[linenum];
	    
	    
	    //Preprocess file for comments (// and /*...*/, and convert all whitespace to single spaces)
	    int nflen = 0, ws = 0, cs = 0, inquote = 0;
	    numcr = 0;
	    for (int i=0; i<flen; i++)
	    {
	        //detect all 4 types of carriage return (\r, \n, \r\n, \n\r :)
	        int cr=0; if (tx[i] == '\r') { i += ((tx[i+1] == '\n')?1:0); cr = 1; }
	        else if (tx[i] == '\n') { i += ((tx[i+1] == '\r')?1:0); cr = 1; }
	        if (cr != 0)
	        {
	            //Remember line numbers by storing the byte index at the start of each line
	            //Line numbers can be retrieved by doing a binary search on the byte index :)
	            lineoffs[numcr++] = nflen;
	            if (cs == 1) cs = 0;
	            ws = 1; continue; //strip CR/LF
	        }

	        if ((inquote == 0) && ((tx[i] == ' ') || (tx[i] == '\t'))) { ws = 1; continue; } //strip Space/Tab
	        if ((tx[i] == '/') && (tx[i+1] == '/') && (cs == 0)) cs = 1;
	        if ((tx[i] == '\\') && (tx[i+1] == '\\') && (cs == 0)) cs = 1;
	        if ((tx[i] == '/') && (tx[i+1] == '*') && (cs == 0)) { ws = 1; cs = 2; }
	        if ((tx[i] == '*') && (tx[i+1] == '/') && (cs == 2)) { cs = 0; i++; continue; }
	        if (cs != 0) continue;

	        if (ws != 0) { tx[nflen++] = 0; ws = 0; }

	        //quotes inside strings: \"
	        if ((tx[i] == '\\') && (tx[i+1] == '\"')) { i++; tx[nflen++] = '\"'; continue; }
	        if (tx[i] == '\"') { inquote ^= 1; continue; }
	        tx[nflen++] = tx[i];
	    }
	    tx[nflen++] = 0; lineoffs[numcr] = nflen;
	    tx[nflen++] = 0;
	    
	    
	    flen = nflen;

	    textbuf = new String(tx);
	    textptr = 0;
	    textlength = nflen;
	    eof = nflen-1;
	}
	
	public static Scriptfile scriptfile_fromstring(String string)
	{
	    if (string == null || string.isEmpty()) return null;

	    int flen = string.length();

	    byte[] tx = Arrays.copyOf(string.getBytes(), flen + 2);
	    tx[flen] = tx[flen+1] = 0;

	    Scriptfile sf = new Scriptfile(tx,flen);
	    sf.filename = null;

	    return sf;
	}
	
	public static Scriptfile scriptfile_fromfile(String fn)
	{
		int fp = kOpen(fn, 0);
	    if (fp<0) return null;

	    int flen = kFileLength(fp);
	    byte[] tx = new byte[flen + 2];
	    kRead(fp, tx, flen);
	    tx[flen] = tx[flen+1] = 0;

	    kClose(fp);

	    Scriptfile sf = new Scriptfile(tx,flen);
	    sf.filename = fn;

	    return sf;
	}
}
