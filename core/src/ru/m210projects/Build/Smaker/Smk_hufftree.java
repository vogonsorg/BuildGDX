package ru.m210projects.Build.Smaker;

import static ru.m210projects.Build.Smaker.Smk_bitstream.smk_bs_safe_read_1;
import static ru.m210projects.Build.Smaker.Smk_bitstream.smk_bs_safe_read_8;

public class Smk_hufftree {
	
	Smk_hufftree b0;
	union u;
	
	public Smk_hufftree() {
		u = new union();
	}

	public class union {
		Smk_hufftree b1;
		int value;
		short escapecode;
	}
	/* This macro interrogates return code from smk_huff_lookup and
	jumps to error label if problems occur. */
	private static Smk_hufftree smk_huff_safe_build(smk_bit bs, Smk_hufftree t) {
		if ((t = smk_huff_build(bs)) == null) 
		{ 
			System.out.println("libsmacker::smk_huff_safe_build(" +bs+ "," +t+ ") - ERROR (file: %s, line: %lu)");
			return null;
		}
		return t; 
	}
	
	/* This macro interrogates return code from smk_huff_lookup and
	jumps to error label if problems occur. */
	public static int smk_huff_safe_lookup(smk_bit bs,Smk_hufftree t, int s) 
	{ 
		if ((s = smk_huff_lookup(bs,t)) < 0) 
		{ 
			System.out.println("libsmacker::smk_huff_safe_lookup(" +bs +","+ t +"," +s +") - ERROR (file: %s, line: %lu)");
			return -1;
		} 
		return s;
	}
	
	public static int smk_huff_big_safe_lookup (smk_bit bs, smk_huff_big big, int s)
	{
		if( (s = smk_huff_big_lookup(bs,big)) < 0) {
			System.out.println("error smk_huff_big_safe_lookup");
			return -1;
		}
		return s;
	}
	
	public static smk_huff_big smk_huff_big_safe_build(smk_bit bs, smk_huff_big t) 
	{ 
		if ((t = smk_huff_big_build(bs)) == null) 
		{ 
			System.out.println("libsmacker::smk_huff_big_safe_build(" +bs+ "," +t+ ") - ERROR (file: %s, line: %lu)");
			return null;
		} 
		return t;
	}

	/* safe build with built-in error jump */
	private static Smk_hufftree smk_huff_safe_build_rec(smk_bit bs, Smk_hufftree p) 
	{ 
		if ((p = smk_huff_build_rec(bs)) == null) 
		{ 

			System.out.println("libsmacker::smk_huff_safe_build_rec(" +bs+ "," +p +") - ERROR (file: %s, line: %lu)");
			return null;
		} 
		return p;
	}
	
	/* Recursive tree-building function. */
	public static Smk_hufftree smk_huff_build_rec(smk_bit bs)
	{
		Smk_hufftree ret = null;
		int bit = 0;

		/* sanity check */
		if(bs == null)
			return null;

		/* Read the bit */
		bit = smk_bs_safe_read_1(bs,bit);

		/* Malloc a structure. */
		ret = new Smk_hufftree();
		
		if (bit != 0)
		{
			/* Bit set: this forms a Branch node. */
			/* Recursively attempt to build the Left branch. */
			ret.b0 = smk_huff_safe_build_rec(bs,ret.b0);

			/* Everything is still OK: attempt to build the Right branch. */
			ret.u.b1 = smk_huff_safe_build_rec(bs,ret.u.b1);

			/* return branch pointer here */
			return ret;
		}

		/* Bit unset signifies a Leaf node. */
		/* Attempt to read value */
		ret.u.value = smk_bs_safe_read_8(bs,ret.u.value);

		/* smk_malloc sets entries to 0 by default */
		/* ret.b0 = NULL; */
		ret.u.escapecode = 0xFF;

		return ret;
	}
	
	/*
	Entry point for huff_build.  Basically just checks the start/end tags
	and calls smk_huff_build_rec recursive function.
	*/
	public static Smk_hufftree smk_huff_build(smk_bit bs)
	{
		Smk_hufftree ret = null;
		int bit = 0;
	
		/* sanity check */
		if(bs == null)
			return null;
	
		/* Smacker huff trees begin with a set-bit. */
		bit = smk_bs_safe_read_1(bs,bit);
	
		if (bit == 0)
		{
			/* Got a bit, but it was not 1. In theory, there could be a smk file
				without this particular tree. */
			System.out.println("libsmacker::smk_huff_build(bs) - Warning: initial get_bit returned 0");
			return null;
		}
	
		/* Begin parsing the tree data. */
		ret = smk_huff_safe_build_rec(bs,ret);
	
		/* huff trees end with an unset-bit */
		bit = smk_bs_safe_read_1(bs,bit);
	
		if (bit != 0)
		{
			System.out.println("libsmacker::smk_huff_build(bs) - ERROR: final get_bit returned 1");
			return null;
		}
	
		return ret;
	}
	
	/* Look up an 8-bit value from a basic huff tree.
	Return -1 on error. */
	static int smk_huff_lookup (smk_bit bs, Smk_hufftree t)
	{
		int bit = 0;
	
		/* sanity check */
		if(bs==null) return -1;
		if(t==null) return -1;
	
		if (t.b0 == null)
		{
			/* Reached a Leaf node.  Return its value. */
			return t.u.value;
		}
	
		bit = smk_bs_safe_read_1(bs,bit);
	
		if (bit != 0)
		{
			/* get_bit returned Set, follow Right branch. */
			return smk_huff_lookup(bs,t.u.b1);
		}
	
		/* follow Right branch */
		return smk_huff_lookup(bs,t.b0);
	}
	
	private static Smk_hufftree smk_huff_big_safe_build_rec(smk_bit bs, int[] cache,
			Smk_hufftree low8, Smk_hufftree hi8, Smk_hufftree p) {
		if ((p = smk_huff_big_build_rec(bs,cache,low8,hi8))==null) 
		{ 
			System.out.println("libsmacker::smk_huff_big_safe_build_rec(" +bs +","+cache +"," +low8 +"," +hi8+ "," +p+ ") - ERROR (file: %s, line: %lu)"); 
			return null;
		} 
		return p;
	}
	
	/* Recursively builds a Big tree. */
	public static Smk_hufftree smk_huff_big_build_rec(smk_bit bs, int[] cache, Smk_hufftree low8, Smk_hufftree hi8)
	{
		Smk_hufftree ret = null;

		int bit = 0;
		short lowval = 0;

		/* sanity check */
		if(bs==null) return null;
		if(cache==null) return null;
		if(low8==null) return null;
		if(hi8==null) return null;

		/* Get the first bit */
		bit = smk_bs_safe_read_1(bs,bit);

		/* Malloc a structure. */
		ret = new Smk_hufftree();

		if (bit != 0)
		{
			/* Recursively attempt to build the Left branch. */
			ret.b0 = smk_huff_big_safe_build_rec(bs,cache,low8,hi8,ret.b0);

			/* Recursively attempt to build the Left branch. */
			ret.u.b1 = smk_huff_big_safe_build_rec(bs,cache,low8,hi8,ret.u.b1);

			/* return branch pointer here */
			return ret;
		}

		/* Bit unset signifies a Leaf node. */
		lowval = (short) smk_huff_safe_lookup(bs,low8,lowval);
		ret.u.value = smk_huff_safe_lookup(bs,hi8,ret.u.value);

		/* Looks OK: we got low and hi values.  Return a new LEAF */
		/* ret.b0 = NULL; */
		ret.u.value = lowval | (ret.u.value << 8);

		/* Last: when building the tree, some Values may correspond to cache positions.
			Identify these values and set the Escape code byte accordingly. */
		if (ret.u.value == cache[0])
		{
			ret.u.escapecode = 0;
		}
		else if (ret.u.value == cache[1])
		{
			ret.u.escapecode = 1;
		}
		else if (ret.u.value == cache[2])
		{
			ret.u.escapecode = 2;
		}
		else
		{
			ret.u.escapecode = 0xFF;
		}

		return ret;
	}
	
	/* Entry point for building a big 16-bit tree. */
	public static smk_huff_big smk_huff_big_build(smk_bit bs)
	{
		smk_huff_big big;

		Smk_hufftree low8 = null, hi8 = null;

		short lowval = 0;

		int bit = 0;
		int i;

		/* sanity check */
		if(bs == null)
			return null;

		/* Smacker huff trees begin with a set-bit. */
		bit = smk_bs_safe_read_1(bs,bit);

		if (bit == 0)
		{
//			fputs("libsmacker::smk_huff_big_build(bs) - ERROR: initial get_bit returned 0\n",stderr);
			System.out.println("error smk_huff_big_build" );
			return null;
		}

		/* build low-8-bits tree */
		low8 = smk_huff_safe_build(bs,low8);
		/* build hi-8-bits tree */
		hi8 = smk_huff_safe_build(bs,hi8);

		/* Everything looks OK so far.  Time to malloc structure. */
		big = new smk_huff_big();
		
		/* Init the escape code cache. */
		for (i = 0; i < 3; i ++)
		{
			lowval = (short) smk_bs_safe_read_8(bs,lowval);
			big.cache[i] = smk_bs_safe_read_8(bs,big.cache[i]);
			big.cache[i] = lowval | (big.cache[i] << 8);
		}

		/* Finally, call recursive function to retrieve the Bigtree. */
		big.t = smk_huff_big_safe_build_rec(bs,big.cache,low8,hi8,big.t);

		/* Done with 8-bit hufftrees, free them. */
		hi8 = null;
		low8 = null;

		/* Check final end tag. */
		bit = smk_bs_safe_read_1(bs,bit);

		if (bit != 0)
		{
			System.out.println("libsmacker::smk_huff_big_build(bs) - ERROR: final get_bit returned 1");
			big = null;
			return null;
		}

		return big;
	}
	
	public static int smk_huff_big_lookup_rec (smk_bit bs, int cache[], Smk_hufftree t)
	{
		int val;
		int bit = 0;

		/* sanity check */
		if(bs == null)
			return -1;
		if(cache == null)
			return -1;
		if(t == null)
			return -1;

		/* Reached a Leaf node */
		if (t.b0 == null)
		{
			if (t.u.escapecode != 0xFF)
			{
				/* Found escape code. Retrieve value from Cache. */
				val = cache[t.u.escapecode];
			}
			else
			{
				/* Use value directly. */
				val = t.u.value;
			}

			if ( cache[0] != val)
			{
				/* Update the cache, by moving val to the front of the queue,
					if it isn't already there. */
				cache[2] = cache[1];
				cache[1] = cache[0];
				cache[0] = val;
			}

			return val;
		}

		bit = smk_bs_safe_read_1(bs,bit);

		if (bit != 0)
		{
			/* get_bit returned Set, follow Right branch. */
			return smk_huff_big_lookup_rec(bs,cache,t.u.b1);
		}
		return smk_huff_big_lookup_rec(bs,cache,t.b0);
	}
	
	/* Convenience call-out for recursive bigtree lookup function */
	public static int smk_huff_big_lookup (smk_bit bs, smk_huff_big big)
	{
		return smk_huff_big_lookup_rec(bs,big.cache,big.t);
	}

	/* Resets a Big hufftree cache */
	public static void smk_huff_big_reset (smk_huff_big big)
	{
		if(big != null) {
			big.cache[0] = 0;
			big.cache[1] = 0;
			big.cache[2] = 0;
		}
	}

}

class smk_huff_big
{
	Smk_hufftree t;
	int[] cache = new int[3];
	
	public smk_huff_big() {
		t = new Smk_hufftree();
	}
};
