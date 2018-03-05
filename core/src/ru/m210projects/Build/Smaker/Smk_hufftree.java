package ru.m210projects.Build.Smaker;


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
	private static Smk_hufftree smk_huff_safe_build(Smk_hufftree t) {
		if ((t = smk_huff_build()) == null) 
		{ 
			System.out.println("libsmacker::smk_huff_safe_build(" + "," +t+ ") - ERROR (file: %s, line: %lu)");
			return null;
		}
		return t; 
	}
	
	/* This macro interrogates return code from smk_huff_lookup and
	jumps to error label if problems occur. */
	public static int smk_huff_safe_lookup(Smk_hufftree t, int s) 
	{ 
		if ((s = smk_huff_lookup(t)) < 0) 
		{ 
			System.out.println("libsmacker::smk_huff_safe_lookup(" +","+ t +"," +s +") - ERROR (file: %s, line: %lu)");
			return -1;
		} 
		return s;
	}
	
	public static int smk_get_code(smk_huff_big big)
	{
		int s = smk_huff_big_lookup_rec(big.cache,big.t);
		if( s < 0) {
			System.out.println("error smk_huff_big_safe_lookup");
			return -1;
		}
		return s;
	}
	
	public static smk_huff_big smk_huff_big_safe_build(smk_huff_big t) 
	{ 
		if ((t = smk_huff_big_build()) == null) 
		{ 
			System.out.println("libsmacker::smk_huff_big_safe_build(" + "," +t+ ") - ERROR (file: %s, line: %lu)");
			return null;
		} 
		return t;
	}

	/* safe build with built-in error jump */
	private static Smk_hufftree smk_huff_safe_build_rec(Smk_hufftree p) 
	{ 
		if ((p = smk_huff_build_rec()) == null) 
		{ 

			System.out.println("libsmacker::smk_huff_safe_build_rec(" +p +") - ERROR (file: %s, line: %lu)");
			return null;
		} 
		return p;
	}
	
	/* Recursive tree-building function. */
	public static Smk_hufftree smk_huff_build_rec()
	{
		Smk_hufftree ret = null;
		int bit = 0;


		/* Read the bit */
		bit = BitStream.getBit();

		/* Malloc a structure. */
		ret = new Smk_hufftree();
		
		if (bit != 0)
		{
			/* Bit set: this forms a Branch node. */
			/* Recursively attempt to build the Left branch. */
			ret.b0 = smk_huff_safe_build_rec(ret.b0);

			/* Everything is still OK: attempt to build the Right branch. */
			ret.u.b1 = smk_huff_safe_build_rec(ret.u.b1);

			/* return branch pointer here */
			return ret;
		}

		/* Bit unset signifies a Leaf node. */
		/* Attempt to read value */
		ret.u.value = BitStream.get_bits8();

		/* smk_malloc sets entries to 0 by default */
		/* ret.b0 = NULL; */
		ret.u.escapecode = 0xFF;

		return ret;
	}
	
	/*
	Entry point for huff_build.  Basically just checks the start/end tags
	and calls smk_huff_build_rec recursive function.
	*/
	public static Smk_hufftree smk_huff_build()
	{
		Smk_hufftree ret = null;
		int bit = 0;
		/* Smacker huff trees begin with a set-bit. */
		bit = BitStream.getBit();
	
		if (bit == 0)
		{
			/* Got a bit, but it was not 1. In theory, there could be a smk file
				without this particular tree. */
			System.out.println("libsmacker::smk_huff_build(bs) - Warning: initial get_bit returned 0");
			return null;
		}
	
		/* Begin parsing the tree data. */
		ret = smk_huff_safe_build_rec(ret);
	
		/* huff trees end with an unset-bit */
		bit = BitStream.getBit();
	
		if (bit != 0)
		{
			System.out.println("libsmacker::smk_huff_build(bs) - ERROR: final get_bit returned 1");
			return null;
		}
	
		return ret;
	}
	
	/* Look up an 8-bit value from a basic huff tree.
	Return -1 on error. */
	static int smk_huff_lookup (Smk_hufftree t)
	{
		int bit = 0;
	
		/* sanity check */
		if(t==null) return -1;
	
		if (t.b0 == null)
		{
			/* Reached a Leaf node.  Return its value. */
			return t.u.value;
		}
	
		bit = BitStream.getBit();
	
		if (bit != 0)
		{
			/* get_bit returned Set, follow Right branch. */
			return smk_huff_lookup(t.u.b1);
		}
	
		/* follow Right branch */
		return smk_huff_lookup(t.b0);
	}
	
	private static Smk_hufftree smk_huff_big_safe_build_rec(int[] cache,
			Smk_hufftree low8, Smk_hufftree hi8, Smk_hufftree p) {
		if ((p = smk_huff_big_build_rec(cache,low8,hi8))==null) 
		{ 
			System.out.println("libsmacker::smk_huff_big_safe_build_rec("+cache +"," +low8 +"," +hi8+ "," +p+ ") - ERROR (file: %s, line: %lu)"); 
			return null;
		} 
		return p;
	}
	
	/* Recursively builds a Big tree. */
	public static Smk_hufftree smk_huff_big_build_rec(int[] cache, Smk_hufftree low8, Smk_hufftree hi8)
	{
		Smk_hufftree ret = null;

		int bit = 0;
		short lowval = 0;

		/* sanity check */
		if(cache==null) return null;
		if(low8==null) return null;
		if(hi8==null) return null;

		/* Get the first bit */
		bit = BitStream.getBit();

		/* Malloc a structure. */
		ret = new Smk_hufftree();

		if (bit != 0)
		{
			/* Recursively attempt to build the Left branch. */
			ret.b0 = smk_huff_big_safe_build_rec(cache,low8,hi8,ret.b0);

			/* Recursively attempt to build the Left branch. */
			ret.u.b1 = smk_huff_big_safe_build_rec(cache,low8,hi8,ret.u.b1);

			/* return branch pointer here */
			return ret;
		}

		/* Bit unset signifies a Leaf node. */
		lowval = (short) smk_huff_safe_lookup(low8,lowval);
		ret.u.value = smk_huff_safe_lookup(hi8,ret.u.value);

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
	public static smk_huff_big smk_huff_big_build()
	{
		smk_huff_big big;

		Smk_hufftree low8 = null, hi8 = null;

		short lowval = 0;

		int bit = 0;
		int i;

		/* Smacker huff trees begin with a set-bit. */
		bit = BitStream.getBit();

		if (bit == 0)
		{
//			fputs("libsmacker::smk_huff_big_build(bs) - ERROR: initial get_bit returned 0\n",stderr);
			System.out.println("error smk_huff_big_build" );
			return null;
		}

		/* build low-8-bits tree */
		low8 = smk_huff_safe_build(low8);
		/* build hi-8-bits tree */
		hi8 = smk_huff_safe_build(hi8);

		/* Everything looks OK so far.  Time to malloc structure. */
		big = new smk_huff_big();
		
		/* Init the escape code cache. */
		for (i = 0; i < 3; i ++)
		{
			lowval = (short) BitStream.get_bits8();
			big.cache[i] = BitStream.get_bits8();
			big.cache[i] = lowval | (big.cache[i] << 8);
		}

		/* Finally, call recursive function to retrieve the Bigtree. */
		big.t = smk_huff_big_safe_build_rec(big.cache,low8,hi8,big.t);

		/* Done with 8-bit hufftrees, free them. */
		hi8 = null;
		low8 = null;

		/* Check final end tag. */
		bit = BitStream.getBit();

		if (bit != 0)
		{
			System.out.println("libsmacker::smk_huff_big_build(bs) - ERROR: final get_bit returned 1");
			big = null;
			return null;
		}

		return big;
	}
	
	public static int smk_huff_big_lookup_rec (int cache[], Smk_hufftree t)
	{
		if(cache == null || t == null) return -1;

		/* Reached a Leaf node */
		if (t.b0 == null)
		{
			int val = t.u.value;
			if (t.u.escapecode != 0xFF)
			{
				/* Found escape code. Retrieve value from Cache. */
				val = cache[t.u.escapecode];
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

		if (BitStream.getBit() != 0)
		{
			/* get_bit returned Set, follow Right branch. */
			return smk_huff_big_lookup_rec(cache,t.u.b1);
		}
		return smk_huff_big_lookup_rec(cache,t.b0);
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
