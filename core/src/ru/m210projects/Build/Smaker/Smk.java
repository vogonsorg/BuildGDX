package ru.m210projects.Build.Smaker;

/* SMACKER DATA STRUCTURES */
public class Smk {
	
	/* meta-info */
	/* file mode: see flags, smacker.h */
	int	mode;
	
	/* microsec per frame, total frames
	stored as a double to handle scaling (large positive millisec / frame values may
	overflow a ul */
	public double usf;
	
	public int frames;
	/* does file contain a ring frame? */
	short	ring_frame;

	/* Index of current frame */
	public int	cur_frame;
	
	/* SOURCE union.
	Where the data is going to be read from (or be stored),
	depending on the file mode. */
	class source {
		//File fp  - DISK MODE
		byte[][]	chunk_data;
	}
	source source;
	
	/* shared
	array of "chunk sizes"*/
	int[]	chunk_size;
	
	/* Holds per-frame flags (i.e. 'keyframe') */
	byte[]	keyframe;
	/* Holds per-frame type mask (e.g. 'audio track 3, 2, and palette swap') */
	byte[]	frame_type;
	
	/* pointers to video and audio structures */
	/* Video data type: enable/disable decode switch,
	video info and flags,
	pointer to last-decoded-palette */
	public class smk_video {
		byte[] tframe;
		
		/* enable/disable decode switch */
		short enable;

		/* video info */
		int	w;
		int	h;
		/* Y scale mode (constants defined in smacker.h)
			0: unscaled
			1: doubled
			2: interlaced */
		short	y_scale_mode;

		/* version ('2' or '4') */
		char v;

		// Huffman trees 
		smk_huff_big[] tree = new smk_huff_big[4];

		/* Palette data type: pointer to last-decoded-palette */
		byte[] palette;
		/* Last-unpacked frame */
		byte[] frame;
		
		public smk_video() {
			for(int i = 0; i < 4; i++)
				tree[i] = new smk_huff_big();
		}
	}
	smk_video video;

	/* audio structure */
	//Smk_audio audio[7]; XXX
	
	public Smk()
	{
		video = new smk_video();
		source = new source();
	}
}
