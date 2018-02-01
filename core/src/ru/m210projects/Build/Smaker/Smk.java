package ru.m210projects.Build.Smaker;

public class Smk {

	public double pts_inc;
	public int frames;
	public int	cur_frame;

	class source {
		byte[][] chunk_data;
	}
	source source;

	int[]	frm_size;
	byte[]	frm_flags; //Holds per-frame type mask (e.g. 'audio track 3, 2, and palette swap')

	public class smk_video {
		byte[] tframe;
		
		short enable;

		public int	w;
		public int	h;
		/* Y scale mode (constants defined in smacker.h)
			0: unscaled
			1: doubled
			2: interlaced */
		short	y_scale_mode;

		byte version; //version ('2' or '4')

		smk_huff_big[] tree = new smk_huff_big[4];

		public byte[] palette;
		byte[] frame;
		
		public smk_video() {
			for(int i = 0; i < 4; i++)
				tree[i] = new smk_huff_big();
		}
	}
	public smk_video video;

	/* audio structure */
	//Smk_audio audio[7]; XXX
	
	public Smk()
	{
		video = new smk_video();
		source = new source();
	}
}
