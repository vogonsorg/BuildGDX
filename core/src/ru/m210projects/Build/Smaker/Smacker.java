/*
	libsmacker - A C library for decoding .smk Smacker Video files
	Copyright (C) 2012-2013 Greg Kennedy

	See smacker.h for more information.

	smacker.c
		Main implementation file of libsmacker.
		Open, close, query, render, advance and seek an smk
*/

package ru.m210projects.Build.Smaker;

import static ru.m210projects.Build.Smaker.Smk_hufftree.smk_huff_big_reset;
import static ru.m210projects.Build.Smaker.Smk_hufftree.smk_huff_big_safe_build;
import static ru.m210projects.Build.Smaker.Smk_hufftree.smk_get_code;

import java.nio.ByteBuffer;
import java.util.Arrays;

import ru.m210projects.Build.Smaker.Smk.smk_video;
import ru.m210projects.Build.Types.BitStream;
import ru.m210projects.Build.Types.LittleEndian;

public class Smacker {
	
	/* Smacker palette map */
	static final byte palmap[] =
	{
			0x00, 0x04, 0x08, 0x0C, 0x10, 0x14, 0x18, 0x1C,
			0x20, 0x24, 0x28, 0x2C, 0x30, 0x34, 0x38, 0x3C,
			0x41, 0x45, 0x49, 0x4D, 0x51, 0x55, 0x59, 0x5D,
			0x61, 0x65, 0x69, 0x6D, 0x71, 0x75, 0x79, 0x7D,
			(byte) 0x82, (byte) 0x86, (byte) 0x8A, (byte) 0x8E, (byte) 0x92, (byte) 0x96, (byte) 0x9A, (byte) 0x9E,
			(byte) 0xA2, (byte) 0xA6, (byte) 0xAA, (byte) 0xAE, (byte) 0xB2, (byte) 0xB6, (byte) 0xBA, (byte) 0xBE,
			(byte) 0xC3, (byte) 0xC7, (byte) 0xCB, (byte) 0xCF, (byte) 0xD3, (byte) 0xD7, (byte) 0xDB, (byte) 0xDF,
			(byte) 0xE3, (byte) 0xE7, (byte) 0xEB, (byte) 0xEF, (byte) 0xF3, (byte) 0xF7, (byte) 0xFB, (byte) 0xFF
	};

	/* GLOBALS */
	/* tree processing order */
	static final int SMK_TREE_MMAP	= 0;
	static final int SMK_TREE_MCLR	= 1;
	static final int SMK_TREE_FULL	= 2;
	static final int SMK_TREE_TYPE	= 3;
	
	static final int SMK_BLK_MONO = 0;
	static final int SMK_BLK_FULL = 1;
	static final int SMK_BLK_SKIP = 2;
	static final int SMK_BLK_FILL = 3;
	
	static final int SMK_FLAG_Y_NONE =	0x00;
	static final int SMK_FLAG_Y_INTERLACE =	0x01;
	static final int SMK_FLAG_Y_DOUBLE =	0x02;
	
	/* file-processing mode, pass to smk_open_file */
	static final int  SMK_MODE_DISK	= 0x00;
	static final int  SMK_MODE_MEMORY	= 0x01;
	
	static final int SMK_DONE	= 0x00;
	static final int SMK_MORE	= 0x01;
	static final int SMK_LAST	= 0x02;
	static final int SMK_ERROR	= -1;
	
	public static final int SMK_VIDEO_TRACK	= 0x80;
	
	static final int SMACKER_FLAG_RING_FRAME =	0x01;
	static final int SMACKER_PAL =	0x01;

	/* PUBLIC FUNCTIONS */
	/* open an smk (from a generic Source) */

	public static Smk smk_open_generic(int m, ByteBuffer fp, int process_mode) {
		Smk s = new Smk();

		byte[] buf = new byte[4];
		fp.get(buf,0, 4);
		String signature = new String(buf, 0, 4);
		
		if(signature.equals("SMK4")) 
			s.video.v = '4';
		else if(signature.equals("SMK2"))
			s.video.v = '2';
		else System.err.println("smacker::smk_open_generic - ERROR: invalid SMKn signature " + signature);
		System.out.println("\tProcessing will continue as type " + s.video.v);

		/* width, height, total num frames */
		s.video.w = fp.getInt();
		s.video.h  = fp.getInt();
		s.frames = fp.getInt();

		/* frames per second calculation */
		int temp_l = fp.getInt();
		if (temp_l > 0) /* millisec per frame */
			s.pts_inc = temp_l * 1000;
		else if (temp_l < 0) /* 10 microsec per frame */
			s.pts_inc = temp_l * -10;
		else /* defaults to 10 usf (= 100000 microseconds) */
			s.pts_inc = 100000;
		
		/* Video flags follow.
		Ring frame is important to libsmacker.
		Y scale / Y interlace go in the Video flags.
		The user should scale appropriately. */
		int flags  = fp.getInt();
		if ((flags & SMACKER_FLAG_RING_FRAME) != 0)
			s.frames++;
		
		if ((flags & 0x02) != 0)
			s.video.y_scale_mode = SMK_FLAG_Y_DOUBLE;
		if ((flags & 0x04) != 0)
		{
			if (s.video.y_scale_mode == SMK_FLAG_Y_DOUBLE)
			{
				System.out.println("libsmacker::smk_open_generic - Warning: SMK file specifies both Y-Double AND Y-Interlace.");
			}
			s.video.y_scale_mode = SMK_FLAG_Y_INTERLACE;
		}

		/* Max buffer size for each audio track - we don't use
		but calling application might. */
		for(int i = 0; i < 7; i++) {
			
			fp.getInt(); //smk.audio[i]
		}
		/* Read size of "hufftree chunk" - save for later. */

		int tree_size = fp.getInt();
		/* "unpacked" sizes of each huff tree - we don't use
		but calling application might. */
		for (int i = 0; i < 4; i++) {
			/*
			smk.mmap_size = avio_rl32(pb);
			smk.mclr_size = avio_rl32(pb);
			smk.full_size = avio_rl32(pb);
			smk.type_size = avio_rl32(pb);
			*/
			fp.getInt();
		}

		/* read audio rate data */
		for(int i = 0; i < 7; i++) {
			/*
			smk.rates[i]  = avio_rl24(pb); FIXME
        	smk.aflags[i] = avio_r8(pb);
			*/
			fp.getInt();
//			head.audioRate[i]  = fp.smk_read_ul(); 
		}
		
		fp.getInt(); //extra

		/* FrameSizes and Keyframe marker are stored together. */
//		s.keyframe = new byte[s.frames];
		s.frm_size = new int[s.frames];
		s.frm_flags = new byte[s.frames];
		for (int i = 0; i < s.frames; i++)
		{
			s.frm_size[i] = fp.getInt();
//			if ((s.frm_size[i] & 0x01) != 0) XXX
//				s.keyframe[i] = 1; /* Set Keyframe */
//
//			/* Bits 1 is used, but the purpose is unknown. */
			s.frm_size[i] &= 0xFFFFFFFC;
		}
		
		/* That was easy... Now read FrameTypes! */
		for (int i = 0; i < s.frames; i++)
			s.frm_flags[i] = fp.get();
		
		/* HuffmanTrees
		We know the sizes already: read and assemble into
		something actually parse-able at run-time */
	
		byte[] hufftree_chunk = new byte[tree_size];
		fp.get(hufftree_chunk);

		/* set up a Bitstream */
		BitStream.init(hufftree_chunk, 0, tree_size);

		/* create some tables */
		for (int i = 0; i < 4; i ++)
			s.video.tree[i] = smk_huff_big_safe_build(s.video.tree[i]);
		
		/* clean up */
		hufftree_chunk = null;

		/* Handle the rest of the data.
		For MODE_MEMORY, read the chunks and store */
		
		s.source.chunk_data = new byte[s.frames][];
		for (int i = 0; i < s.frames; i ++)
		{
			s.source.chunk_data[i] = new byte[s.frm_size[i]];
			fp.get(s.source.chunk_data[i],0, s.frm_size[i]);	
		}

		s.video.tframe = new byte[s.video.w * s.video.h];
		return s;
	}
	
	/* open an smk (from a memory buffer) */
	public static Smk smk_open_memory(ByteBuffer fp)
	{
		Smk s = null;
		if(fp == null) return null;
		if(fp.capacity() == 0) return null;
		
		/* set up the read union for Memory mode */
		if ((s = smk_open_generic(0,fp,SMK_MODE_MEMORY)) == null)
		{
			System.err.println("libsmacker::smk_open_memory(buffer,  " + fp.capacity() + ") - ERROR: Fatal error in smk_open_generic, returning NULL.");
		}

		return s;
	}
	
	/* open an smk (from a file) */
	public static Smk smk_open_file(String filename, int mode)
	{
		Smk s = null;

		if(filename == null || filename.isEmpty())
			return null;
		
		/*
		ByteBuffer fp;
		if ((fp.file = fopen(filename,"rb")) == -1)
			System.err.println("libsmacker::smk_open_file(" + filename + "," + mode + ") - ERROR: could not open file");

		if ((s = smk_open_generic(1,fp,0,mode)) == null)
		{
			System.err.println("libsmacker::smk_open_file(" + filename + "," + mode + ") - ERROR: Fatal error in smk_open_generic, returning NULL.");
			fclose(fp.file);
		}

		if (mode == SMK_MODE_MEMORY)
			fclose(fp.file);
		*/
//		else
//		{
//			s.source.file.fp = fp.file;
//		}

		return s;
	}

	/* close out an smk file and clean up memory */
	public static void smk_close(Smk s)
	{
		if(s == null)
			return;

		/* free video sub-components */
		if (s.video != null)
		{
			for (int u = 0; u < 4; u ++)
			{
				s.video.tree[u].t = null;
				s.video.tree[u] = null;
			}
			s.video.palette = null;
			s.video.frame = null;
			s.video = null;
		}

		/* free audio sub-components */
		for (int u=0; u<7; u++)
		{
//			if (s.audio[u] != null)
			{
//				smk_free(s.audio[u].buffer);
//				smk_free(s.audio[u]);
			}
		}

//		s.keyframe = null;
		s.frm_flags = null;

		/* mem-mode */
		if (s.source.chunk_data != null)
		{
			for (int u=0; u< s.frames; u++)
			{
				s.source.chunk_data[u] = null;
			}
			s.source.chunk_data = null;
		}
		
		s.frm_size = null;

		s= null;
	}
	
	/* tell some info about the file */
	//smk_info_all XXX

	public static int info_w;
	public static int info_h;
	public static int info_scale;
	public static int smk_info_video(Smk object, boolean w, boolean h, boolean y_scale_mode)
	{
		/* sanity check */
		if(object == null)
			return -1;
		
		if (!w && !h && !y_scale_mode)
		{
			System.err.println("libsmacker::smk_info_all(object,w,h,y_scale_mode) - ERROR: Request for info with all-NULL return references");
			return -1;
		}

		if (w) info_w = object.video.w;
		if (h) info_h = object.video.h;
		if (y_scale_mode) info_scale = object.video.y_scale_mode;
		
		return 0;
	}
	
	//smk_info_audio XXX
	
	/* Enable-disable switches */
	public static int smk_enable_all(Smk object, int mask)
	{
		int i;

		/* sanity check */
		if(object == null)
			return -1;
		if(object.video == null)
			return -1;

		object.video.enable = (short) (mask & 0x80);

		for (i = 0; i < 7; i ++)
		{
//			if (object.audio[i] != 0)
//			{ FIXME
//				object.audio[i].enable = (mask & (0x01 << i));
//			}
		}

		return 0;
	}

	public static int smk_enable_video(Smk object, short enable)
	{
		/* sanity check */
		if(object == null)
			return -1;
		if(object.video == null)
			return -1;

		object.video.enable = enable;
		return 0;
	}
	
	//smk_enable_audio XXX
	
	public static void smk_readpalette(Smk s)
	{
		byte[] buffer = s.source.chunk_data[0];
		int size = 4 * (buffer[0] & 0xFF);
		if (s.video.enable != 0)
			smk_render_palette(s.video,buffer, 1,size - 1);
	}
	
	public static byte[] smk_get_palette(Smk object)
	{
		if(object == null)
			return null;
		if(object.video == null)
			return null;
		
		if(object.video.palette == null)
			smk_readpalette(object);

		return object.video.palette;
	}
	
	public static byte[] smk_get_video(Smk object)
	{
		if(object == null)
			return null;
	
		return object.video.frame;
	}
	
	//smk_get_audio XXX
	
	//smk_get_audio_size XXX
	
	/* Decompresses a palette-frame. */
	private static byte[] tpalette;
	public static int smk_render_palette(smk_video s, byte[] p, int ptr, int size)
	{
		int i,j,k;

		/* sanity check */
		if(s == null)
			return -1;

		/* Allocate a placeholder for our palette. */
		if(tpalette == null)
			tpalette = new byte[768];
		
		Arrays.fill(tpalette, (byte)0);

		i = 0; /* index into NEW palette */
		j = 0; /* Index into OLD palette */

		while ( (i < 768) && (size > 0) ) /* looping index into NEW palette */
		{
			if ((p[ptr] & 0x80) != 0) /* skip palette entries */
			{
				/* Copy (c + 1) color entries of the previous palette
					to the next entries of the new palette. */
				k = ((p[ptr] & 0x7F) + 1) * 3;
				ptr++; size--;

				/* check for overflow condition */
				if (i + k > 768)
				{
					System.out.println("libsmacker::palette_render(s,p,size)- ERROR: overflow, 0x80 attempt to copy " + k + " bytes from " + i);
					s.palette = tpalette;
					return -1;
				}

				/* if prev palette exists, copy... else memset black */
				if (s.palette != null)
				{
					for(int c = 0; c < k; c++)
						tpalette[c] = s.palette[c];
				}
				i += k;
			}
			else if ((p[ptr] & 0x40) != 0) /* copy with offset */
			{
				/* Copy (c + 1) color entries of the previous palette,
					starting from entry (s)
					to the next entries of the new palette. */
				if (size < 2)
				{
					
					System.out.println("libsmacker::palette_render(s,p,size) - ERROR: 0x40 ran out of bytes for copy");
					s.palette = tpalette;
					return -1;
				}

				/* pick "count" items to copy */
				k = ((p[ptr] & 0x3F) + 1) * 3;  /* count */
				ptr++; size--;

				/* start offset of old palette */
				j = (p[ptr]&0xFF) * 3;
				ptr++; size--;

				if (j + k > 768 || i + k > 768)
				{
					System.out.println("libsmacker::palette_render(s,p,size) - ERROR: overflow, 0x40 attempt to copy " + k + " bytes from " + j + " to " + i);
					s.palette = tpalette;
					return -1;
				}

				if (s.palette != null)
				{
					for(int c = 0; c < k; c++)
						tpalette[c] = s.palette[j];
				}
	
				i += k;
				j += k;
			}
			else /* new entries */
			{
				if (size < 3)
				{
					System.out.println("libsmacker::palette_render - ERROR: 0x3F ran out of bytes for copy, size=" + size);
					s.palette = null;
					s.palette = tpalette;
					return -1;
				}
				/* To be extremely correct we should blow up if (*p) exceeds 0x3F, but I can't be bothered, so just mask it */
				
				tpalette[i++] = (byte) palmap[p[ptr] & 0x3F];
				ptr++; size--;
				tpalette[i++] = (byte) palmap[p[ptr] & 0x3F];
				ptr++; size--;
				tpalette[i++] = (byte) palmap[p[ptr] & 0x3F];
				ptr++; size--;
			}
		}

		if (i < 768)
		{
			System.out.println("libsmacker::palette_render - ERROR: did not completely fill palette (idx=" + i + ")");
			s.palette = null;
			s.palette = tpalette;
			return -1;
		}

		/* free old palette frame if one exists */
		s.palette = null;
		s.palette = tpalette;

		return 0;
	}
	
	private final static short[] block_runs = {
		1,	 2,	3,	4,	5,	6,	7,	8,
		9,	10,   11,   12,   13,   14,   15,   16,
		17,   18,   19,   20,   21,   22,   23,   24,
		25,   26,   27,   28,   29,   30,   31,   32,
		33,   34,   35,   36,   37,   38,   39,   40,
		41,   42,   43,   44,   45,   46,   47,   48,
		49,   50,   51,   52,   53,   54,   55,   56,
		57,   58,   59,  128,  256,  512, 1024, 2048
	};
	
	public static int smk_render_video(smk_video s, byte[] p, int offset, int size) {
		int i, ptr;

		if(s == null) return -1;
		
		if(p == null) {
			s.frame = null;
			return -1;
		}

		/* Set up a bitstream for video unpacking */
		/* We could check the return code but it will only fail if p is null and we already verified that. */
		BitStream.init(p, offset, size);

		/* Reset the cache on all bigtrees */
		smk_huff_big_reset(s.tree[SMK_TREE_MMAP]);
		smk_huff_big_reset(s.tree[SMK_TREE_MCLR]);
		smk_huff_big_reset(s.tree[SMK_TREE_FULL]);
		smk_huff_big_reset(s.tree[SMK_TREE_TYPE]);

		int blk = 0;
	    int bw = s.w >> 2;
	    int bh = s.h >> 2;
	    int blocks = bw * bh;
	    int stride = s.w;
	    while(blk < blocks) {
	    	int type = smk_get_code(s.tree[SMK_TREE_TYPE]);
	          
	        int run = block_runs[(type >> 2) & 0x3F];
	        switch(type & 3){
	        case SMK_BLK_MONO:
	        	while(run-- != 0 && blk < blocks) {
	        		int clr = smk_get_code(s.tree[SMK_TREE_MCLR]);
	                int map = smk_get_code(s.tree[SMK_TREE_MMAP]);
	                ptr = (blk / bw) * (stride * 4) + (blk % bw) * 4;
	                
	                byte hi = (byte) (clr >> 8);
	                byte lo = (byte) (clr & 0xFF);
                	int shift = 1;
	                for(i = 0; i < 4; i++) {
	                	for (int k = 0; k < 4; k ++) {
	                		if((map & shift) != 0) s.tframe[ptr + k] = hi; else s.tframe[ptr + k] = lo;
	                		shift <<= 1;
	                	}
	                	ptr += stride;    
	                }
	                blk++; 
	        	}
	        	break;
	        case SMK_BLK_FULL:
	        	int mode = 0;
	        	if(s.v == '4') { // In case of Smacker v4 we have three modes
	        		if (BitStream.getBit() != 0) mode = 1;
	        		else if (BitStream.getBit() != 0) mode = 2;
	        	}
	        	while(run-- != 0 && blk < blocks) {
	        		ptr = (blk / bw) * (stride * 4) + (blk % bw) * 4;
	                 
	        		switch(mode) {
	        		case 0:
	        			for(i = 0; i < 4; i++) {
	        				int pix = smk_get_code(s.tree[SMK_TREE_FULL]);
	        				LittleEndian.putShort(s.tframe, ptr + 2, (short) pix);
	        				pix = smk_get_code(s.tree[SMK_TREE_FULL]);
	        				LittleEndian.putShort(s.tframe, ptr, (short) pix);
	        				ptr += stride;
	        			}
	        			break;
	        		case 1:
	        			int pix = smk_get_code(s.tree[SMK_TREE_FULL]);
	        			s.tframe[ptr + 0] = s.tframe[ptr + 1] = (byte) (pix & 0xFF);
	        			s.tframe[ptr + 2] = s.tframe[ptr + 3] = (byte) (pix >> 8);
	        			ptr += stride;
	        			s.tframe[ptr + 0] = s.tframe[ptr + 1] = (byte) (pix & 0xFF);
	        			s.tframe[ptr + 2] = s.tframe[ptr + 3] = (byte) (pix >> 8);
	        			ptr += stride;
	        			pix = smk_get_code(s.tree[SMK_TREE_FULL]);
	        			s.tframe[ptr + 0] = s.tframe[ptr + 1] = (byte) (pix & 0xFF);
	        			s.tframe[ptr + 2] = s.tframe[ptr + 3] = (byte) (pix >> 8);
	        			ptr += stride;
	        			s.tframe[ptr + 0] = s.tframe[ptr + 1] = (byte) (pix & 0xFF);
	        			s.tframe[ptr + 2] = s.tframe[ptr + 3] = (byte) (pix >> 8);
	        			break;
	        		case 2:
	        			for(i = 0; i < 2; i++) {
	        				int pix2 = smk_get_code(s.tree[SMK_TREE_FULL]);
	        				int pix1 = smk_get_code(s.tree[SMK_TREE_FULL]);
	        				LittleEndian.putShort(s.tframe, ptr, (short) pix1);
	        				LittleEndian.putShort(s.tframe, ptr + 2, (short) pix2);
	        				ptr += stride;
	        				LittleEndian.putShort(s.tframe, ptr, (short) pix1);
	        				LittleEndian.putShort(s.tframe, ptr + 2, (short) pix2);
	        				ptr += stride;
	        			}
	        			break;
	        		}
	        		blk++;
	        	}
	        	break;
	        case SMK_BLK_SKIP:
	        	while(run-- != 0 && blk < blocks)
	        		blk++;
	        	break;
	        case SMK_BLK_FILL:
	        	mode = type >> 8;
	        	while(run-- != 0 && blk < blocks){
	        		ptr = (blk / bw) * (stride * 4) + (blk % bw) * 4;
	        		int col = mode * 0x01010101;
	        		for(i = 0; i < 4; i++) {
	        			LittleEndian.putInt(s.tframe, ptr, col);
	        			ptr += stride;
	        		}
	        		blk++;
	        	}
	        	break;
	        }
	    }
		
		/*
		int skip = 0;
		int row = 0;
		int col = 0;
		int unpack, type, blocklen, typedata;
		while ( row < s.h )
		{
			unpack = smk_get_code(s.tree[SMK_TREE_TYPE]);

			type = unpack & 3;
			blocklen = (unpack >> 2) & 0x3F;
			typedata = (unpack & 0xFF00) >> 8;
	    
			// support for v4 full-blocks 
			if (type == 1 && s.v == '4')
			{
				if (BitStream.get_bits1() != 0)
					type = 4;
				else if (BitStream.get_bits1() != 0)
					type = 5;
			}
	
			for (int j = 0; j < block_runs[blocklen] && row < s.h; j++)
			{
				skip = (row * s.w) + col;
				
				switch(type)
				{
					case 0:
		                int clr = smk_get_code(s.tree[SMK_TREE_MCLR]);
		                int map = smk_get_code(s.tree[SMK_TREE_MMAP]);
		                //out = smk.pic.data[0] + (blk / bw) * (stride * 4) + (blk % bw) * 4;
		                byte hi = (byte) (clr >> 8);
		                byte lo = (byte) (clr & 0xFF);
		                int shift = 1;
		                for(i = 0; i < 4; i++) {
		                	for (int k = 0; k < 4; k ++) {
		                		 if((map & shift) != 0) s.tframe[skip + k] = hi; else s.tframe[skip + k] = lo;
		                		 shift <<= 1;
		                	}
		                	skip += s.w;
		                }
						break;
					case 1: // FULL BLOCK
						for (int k = 0; k < 4; k ++)
						{
							unpack = smk_get_code(s.tree[SMK_TREE_FULL]);
							s.tframe[skip + 3] = (byte) ((unpack & 0xFF00) >> 8);
							s.tframe[skip + 2] = (byte) (unpack & 0x00FF);
							unpack = smk_get_code(s.tree[SMK_TREE_FULL]);
							s.tframe[skip + 1] = (byte) ((unpack & 0xFF00) >> 8);
							s.tframe[skip] = (byte) (unpack & 0x00FF);
							skip += s.w;
						}
						break;
					case 2: // VOID BLOCK
						if (s.frame != null)
						{
							for(i = 0; i < 4; i++)
								s.tframe[skip + i] = s.frame[skip + i];
							skip += s.w;
							for(i = 0; i < 4; i++)
								s.tframe[skip + i] = s.frame[skip + i];
							skip += s.w;
							for(i = 0; i < 4; i++)
								s.tframe[skip + i] = s.frame[skip + i];
							skip += s.w;
							for(i = 0; i < 4; i++)
								s.tframe[skip + i] = s.frame[skip + i];
						}
						break;
					case 3: //SOLID BLOCK
						for(i = 0; i < 4; i++)
							s.tframe[skip + i] = (byte) typedata;
						skip += s.w;
						for(i = 0; i < 4; i++)
							s.tframe[skip + i] = (byte) typedata;
						skip += s.w;
						for(i = 0; i < 4; i++)
							s.tframe[skip + i] = (byte) typedata;
						skip += s.w;
						for(i = 0; i < 4; i++)
							s.tframe[skip + i] = (byte) typedata;
						break;
					case 4: //V4 DOUBLE BLOCK
						for (int k = 0; k < 2; k ++)
						{
							unpack = smk_get_code(s.tree[SMK_TREE_FULL]);
							for (i = 0; i < 2; i ++)
							{
								for(int c = 0; c < 2; c++)
									s.tframe[skip + 2 + c] = (byte) ((unpack & 0xFF00) >> 8);
								for(int c = 0; c < 2; c++)
									s.tframe[skip + c] = (byte) ((unpack & 0x00FF));
								skip += s.w;
							}
						}
						break;
					case 5: //V4 HALF BLOCK
						for (int k = 0; k < 2; k ++)
						{
							unpack = smk_get_code(s.tree[SMK_TREE_FULL]);
							s.tframe[skip + 3] = (byte) ((unpack & 0xFF00) >> 8);
							s.tframe[skip + 2] = (byte) (unpack & 0x00FF);
							s.tframe[skip + s.w + 3] = (byte) ((unpack & 0xFF00) >> 8);
							s.tframe[skip + s.w + 2] = (byte) (unpack & 0x00FF);
							unpack = smk_get_code(s.tree[SMK_TREE_FULL]);
							s.tframe[skip + 1] = (byte) ((unpack & 0xFF00) >> 8);
							s.tframe[skip] = (byte) (unpack & 0x00FF);
							s.tframe[skip + s.w + 1] = (byte) ((unpack & 0xFF00) >> 8);
							s.tframe[skip + s.w] = (byte) (unpack & 0x00FF);
							skip += (s.w << 1);
						}
						break;
				}
	
				col += 4;
				if (col >= s.w)
				{
					col = 0;
					row += 4;
				}
			}
		}
		*/

		s.frame = null;
		s.frame = s.tframe;

		return 0;
	}
	
	/* Decompress audio track i. XXX */
	
	/* "Renders" (unpacks) the frame at cur_frame
	   Preps all the image and audio pointers */
	public static int smk_render(Smk s)
	{
		int size, p = 0;
		byte[] buffer = null; 
		byte track;

		/* sanity check */
		if(s == null) 
			return -1;
		
		int frame_size = s.frm_size[s.cur_frame];

		/* Retrieve current frm_size for this frame. */
		if (frame_size == 0)
		{
			System.err.println("libsmacker::smk_render(s) - Warning: frame " + s.cur_frame + ": frm_size is 0.");
			return -1;
		}

		/* Just point buffer at the right place */
		if (s.source.chunk_data[s.cur_frame] == null)
		{
			System.err.println("libsmacker::smk_render(s) - ERROR: frame " + s.cur_frame + " : memory chunk is a NULL pointer.");
			return -1;
		}
		buffer = s.source.chunk_data[s.cur_frame];

		int flags = s.frm_flags[s.cur_frame];
		/* Palette record first */
		if ((flags & SMACKER_PAL) != 0)
		{
			size = 4 * (buffer[p++] & 0xFF) - 1;
			if(size + 1 > frame_size)
			{
				System.err.println("libsmacker::smk_render(s) - ERROR: frame " + s.cur_frame + ": insufficient data for a palette rec.");
				return -1;
			}

			if (s.video.enable != 0)
				smk_render_palette(s.video, buffer, p, size);
			
			p += size;
			frame_size -= size;
			frame_size--;
		}
	
		/* Unpack audio chunks */
		for (track = 0; track < 7; track ++)
		{
			if ((flags & (0x02 << track)) != 0)
			{
				size = LittleEndian.getInt(buffer, p);

				if (size == 0 || size + 4 > frame_size) {
					System.err.println("libsmacker::smk_render(s) - ERROR: frame " + s.cur_frame + ": insufficient data for audio[" + track + "] rec.");
					return -1;
                }

				/* If audio rendering enabled, kick this off for decode. */
//				if (s.audio[track] != null && s.audio[track].enable)
//					smk_render_audio(s.audio[track], p + 4, size - 4);
				
				p += size;
				frame_size -= size;
			}
		}

		if (s.video.enable != 0) 
			smk_render_video(s.video, buffer, p, frame_size);

		return 0;
	}
	
	/* rewind to first frame and unpack */
//	public static int smk_first(Smk s)
//	{
//		if(s == null)
//			return -1;
//
//		s.cur_frame = 0;
//		if ( smk_render(s) < 0)
//		{
//			System.err.println("libsmacker::smk_first(s) - Warning: frame " + s.cur_frame + ": smk_render returned errors.");
//			return -1;
//		}
//
//		if (s.frames == 1) return SMK_LAST;
//		return SMK_MORE;
//	}
	
	/* advance to next frame */
//	public static int smk_next(Smk s)
//	{
//		if(s == null)
//			return -1;
//
//		if (s.cur_frame + 1 < s.frames)
//		{
//			s.cur_frame++;
//			if ( smk_render(s) < 0)
//			{
//				System.err.println("libsmacker::smk_next(s) - Warning: frame " + s.cur_frame +": smk_render returned errors.");
//				return -1;
//			}
//			if (s.cur_frame + 1 == s.frames)
//			{
//				return SMK_LAST;
//			}
//			return SMK_MORE;
//		}
//		return SMK_DONE;
//	}
	
	/* seek to a keyframe in an smk */
//	public static int smk_seek_keyframe(Smk s, int f)
//	{
//		if(s == null)
//			return -1;
//		/* rewind (or fast forward!) exactly to f */
//		s.cur_frame = (int) f;
//
//		/* roll back to previous keyframe in stream, or 0 if no keyframes exist */
//		while (s.cur_frame > 0 && (s.keyframe[s.cur_frame] == 0))
//		{
//			s.cur_frame --;
//		}
//
//		/* render the frame: we're ready */
//		if ( smk_render(s) < 0)
//		{
//			System.err.println("libsmacker::smk_seek_keyframe(s," + f + ") - Warning: frame " + s.cur_frame + ": smk_render returned errors.\n");
//			return -1;
//		}
//
//		return 0;
//	}
	
	/* jump to and render a specific frame */
	public static int smk_render_frame(Smk s, int f)
	{
		if(s == null)
			return -1;
	    
	    /* rewind (or fast forward!) exactly to f */
	    s.cur_frame = f;

	    /* render the frame: we're ready */
	    if ( smk_render(s) < 0)
	    {
	    	System.err.println("libsmacker::smk_render_frame(s," + f + ") - Warning: frame " + s.cur_frame + ": smk_render returned errors.");
	    	return -1;
	    }
	    
	    return 0;
	}
}