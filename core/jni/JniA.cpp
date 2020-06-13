#include <cstdlib>
#include <cstring>
#include "JniA.h"

static int *reciptable;
static char *frameplace;
static int xdim, ydim;

static long bpl, transmode = 0;
static long glogx, glogy, gbxinc, gbyinc, gpinc;
static char *gbuf, *gpal, *ghlinepal, *gtrans;
static char *hlinepal;
static long asm1, asm2, hlineshade, bzinc, gshade;

JNIEXPORT jbyteArray JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnigetframeplace
  (JNIEnv * env, jobject) { 
	jbyteArray jframeplace = env->NewByteArray(xdim * ydim);
	env->SetByteArrayRegion(jframeplace, 0, xdim * ydim, (jbyte *) frameplace);
	return jframeplace;
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jniclearframe
  (JNIEnv *, jobject, jbyte dacol) {
	memset((void *)frameplace,dacol, xdim * ydim);
}
  
JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jniinit
  (JNIEnv * env, jobject, jint width, jint height, jintArray jreciptable) {
	jint length = env->GetArrayLength(jreciptable);

	reciptable = (int *) malloc(length * sizeof(int));
	env->GetIntArrayRegion(jreciptable, 0, length, (jint *) reciptable);
	
	xdim = width;
	ydim = height;

	frameplace = (char *) malloc(xdim * ydim);
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetframeplace
  (JNIEnv * env, jobject jo, jbyteArray newframeplace) {
   
	//frameplace = (char *)env->GetByteArrayElements(newframeplace, NULL); XXX
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetvlinebpl
  (JNIEnv * env, jobject jo, jint dabpl) {
	bpl = dabpl;
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnifixtransluscence
  (JNIEnv * env, jobject, jbyteArray datransoff) {
	
	jint length = env->GetArrayLength(datransoff);
	gtrans = (char *) malloc(length);
	
	env->GetByteArrayRegion(datransoff, 0, length, (jbyte *) gtrans);
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisettransnormal
  (JNIEnv * env, jobject o) { transmode = 0; }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisettransreverse
  (JNIEnv *, jobject) { transmode = 1; }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnidrawpixel
  (JNIEnv *, jobject, jint ptr, jbyte col) {
	frameplace[ptr] = col;
}






JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisethlinesizes
  (JNIEnv * env, jobject, jint logx, jint logy, jbyteArray bufplc) {
	glogx = logx; glogy = logy; 
	
	jint length = env->GetArrayLength(bufplc);
	gbuf = (char *) malloc(length);
	env->GetByteArrayRegion(bufplc, 0, length, (jbyte *) gbuf);
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetpalookupaddress
  (JNIEnv * env, jobject, jbyteArray paladdr) {
	  
	jint length = env->GetArrayLength(paladdr);
	ghlinepal = (char *) malloc(length);
	env->GetByteArrayRegion(paladdr, 0, length, (jbyte *) ghlinepal);
	
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetuphlineasm4
  (JNIEnv *, jobject, jint bxinc, jint byinc) {
  gbxinc = bxinc; gbyinc = byinc;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnihlineasm4
  (JNIEnv *, jobject, jint cnt, jint skiploadincs, jint paloffs, jint by, jint bx, jint p) {

	unsigned char ch;
	char *palptr;
	long fp = (long) frameplace;	
	fp += p;

	//palptr = ( char *)&ghlinepal[paloffs];
	if (!skiploadincs) { gbxinc = asm1; gbyinc = asm2; }
	
	for(;cnt>=0;cnt--)
	{
		//ch = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))];
		*((char *)fp) = 0; //palptr[ch];
		bx -= gbxinc;
		by -= gbyinc;
		fp--;
	}
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupslopevlin
  (JNIEnv * env, jobject, jint logylogx, jbyteArray bufplc, jint pinc, jint bz) {
	glogx = (logylogx&255); glogy = (logylogx>>8);
	
	
	jint length = env->GetArrayLength(bufplc);
	gbuf = (char *) malloc(length);
	env->GetByteArrayRegion(bufplc, 0, length, (jbyte *) gbuf);
	
	gpinc = pinc;
	bzinc = (bz >> 3);

}
  
long krecip(long i)
{ 	// Ken did this
	float f = (float)i; i = *(long *)&f;
	return((reciptable[(i>>12)&2047]>>(((i-0x3f800000)>>23)&31))^(i>>31));
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnislopevlin
  (JNIEnv * env, jobject, jint p, jbyteArray pal,  jint slopaloffs, jint cnt, jint bx, jint by, jint x3, jint y3, jintArray jslopalookup, jint bz)
{
	int index, ch, i;
	long slopalptr;
	long u, v;
	long fp = (long) frameplace;	
	fp += p;

	int * slopalookup = (int *) env->GetPrimitiveArrayCritical(jslopalookup, NULL);

	for(;cnt>0;cnt--)
	{
		i = krecip(bz>>6); bz += bzinc;
		u = bx+x3*i;
		v = by+y3*i;

		index = ((u>>(32-glogx))<<glogy)+(v>>(32-glogy));
		//ch = gbuf[index];

		*((char *)fp) = 0; //*(char *)(slopalookup[slopaloffs]+ch);
		slopaloffs--;
		fp += gpinc;
	}
	
	env->ReleasePrimitiveArrayCritical(jslopalookup, (jint *) slopalookup, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupvlineasm
  (JNIEnv *, jobject, jint neglogy) {
	glogy = neglogy;
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnivlineasm1
  (JNIEnv * env, jobject, jint vinc, jbyteArray pal, jint shade, jint cnt, jint vplc, jbyteArray bufplc, jint buffofs, jint p) {
	unsigned char ch;
	long fp = (long) frameplace;	
	fp += p;
	
	gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL);
	//gpal = (char *) env->GetPrimitiveArrayCritical(pal, NULL);

	for(;cnt>=0;cnt--)
	{
		ch = gbuf[buffofs + (vplc>>glogy)];
		*((char *)fp) = ch; //gpal[ch];
		fp += bpl;
		vplc += vinc;
	}

	//env->ReleasePrimitiveArrayCritical(pal, (jbyte *) gpal, JNI_ABORT);
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
	
 }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupmvlineasm
  (JNIEnv *, jobject, jint neglogy) {
  glogy = neglogy;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimvlineasm1
  (JNIEnv * env, jobject, jint vinc, jbyteArray pal, jint shade, jint cnt, jint vplc, jbyteArray bufplc, jint bufoffs, jint p) {

	unsigned char ch;
	long index;
	long fp = (long) frameplace;	
	fp += p;

	gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL);
	//gpal = (char *) env->GetPrimitiveArrayCritical(pal, NULL);
	
	for(;cnt>=0;cnt--)
	{
		index = bufoffs + (vplc >> glogy);
		ch = gbuf[index]; 
		if (ch != 255) *((char *)fp) = ch; //gpal[ch];
		fp += bpl;
		vplc += vinc;
	}
	
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
	//env->ReleasePrimitiveArrayCritical(pal, (jbyte *) gpal, JNI_ABORT);
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetuptvlineasm
  (JNIEnv *, jobject, jint neglogy) {
  glogy = neglogy;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitvlineasm1
  (JNIEnv * env, jobject, jint vinc, jbyteArray pal, jint shade, jint cnt, jint vplc, jbyteArray  bufplc, jint bufoffs, jint p) {
  
	unsigned char ch;
	unsigned short transch;
	long fp = (long) frameplace;	
	fp += p;
	
	gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL) + bufoffs;
	//gpal = (char *) env->GetPrimitiveArrayCritical(pal, NULL); // + shade; XXX
	
	if (transmode)
	{
		for(;cnt>=0;cnt--)
		{
			ch = gbuf[vplc>>glogy];
			transch = (*((char *)fp))+ch<<8;//(gpal[ch]<<8);
			if (ch != 255) *((char *)fp) = gtrans[transch];
			fp += bpl;
			vplc += vinc;
		}
	}
	else
	{
		for(;cnt>=0;cnt--)
		{
			ch = gbuf[vplc>>glogy];
			transch = ((*((char *)fp))<<8)+ch; //gpal[ch]
			if (ch != 255) *((char *)fp) = gtrans[transch];
			fp += bpl;
			vplc += vinc;
		}
	}
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
	//env->ReleasePrimitiveArrayCritical(pal, (jbyte *) gpal, JNI_ABORT);
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisethlineincs
  (JNIEnv *, jobject, jint x, jint y) {
	asm1 = x;
	asm2 = y;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetuphline
  (JNIEnv * env, jobject, jbyteArray pal, jint shade) {
  
  free(hlinepal);
	jint length = env->GetArrayLength(pal);
	hlinepal = (char *) malloc(length);
	env->GetByteArrayRegion(pal, 0, length, (jbyte *) hlinepal);
    hlineshade = shade;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimsethlineshift
  (JNIEnv *, jobject, jint logx, jint logy) {
	glogx = logx; 
	glogy = logy;
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimhline
  (JNIEnv * env, jobject, jbyteArray bufplc, jint bx, jint cntup16, jint junk, jint by, jint p) {
	unsigned char ch;
	long fp = (long) frameplace;	
	fp += p;
	
	gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL);
	//gpal = hlinepal; // + hlineshade; XXX
	for(cntup16>>=16;cntup16>0;cntup16--)
	{
		ch = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))];
		if (ch != 255) *((char *)fp) = ch; //gpal[ch];
		bx += asm1;
		by += asm2;
		fp++;
	}
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitsethlineshift
  (JNIEnv *, jobject, jint logx, jint logy) {
  glogx = logx; glogy = logy;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnithline
  (JNIEnv * env, jobject, jbyteArray bufplc, jint bx, jint cntup16, jint junk, jint by, jint p) {
  
   unsigned char ch;
   unsigned short transch;
	long fp = (long) frameplace;	
	fp += p;
	
	gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL);
	//gpal = hlinepal; // + hlineshade; XXX
	
	if (transmode)
	{
		for(cntup16>>=16;cntup16>0;cntup16--)
		{
			ch = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))];
			transch = (*((char *)fp))+ch<<8; //(gpal[ch]<<8);
			if (ch != 255) *((char *)fp) = gtrans[transch];
			bx += asm1;
			by += asm2;
			fp++;
		}
	}
	else
	{
		for(cntup16>>=16;cntup16>0;cntup16--)
		{
			ch = gbuf[((bx>>(32-glogx))<<glogy)+(by>>(32-glogy))];
			transch = ((*((char *)fp))<<8)+ch; //gpal[ch];
			if (ch != 255) *((char *)fp) = gtrans[transch];
			bx += asm1;
			by += asm2;
			fp++;
		}
	}
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
  }
  
  
  
  
  
  
  
  
  

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupspritevline
  (JNIEnv * env, jobject, jbyteArray pal, jint shade, jint bxinc, jint byinc, jint ysiz) {
    free(gpal);
	jint length = env->GetArrayLength(pal);
	gpal = (char *) malloc(length);
	env->GetByteArrayRegion(pal, 0, length, (jbyte *) gpal);
	
	gbxinc = bxinc;
	gbyinc = byinc;
	glogy = ysiz;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnispritevline
  (JNIEnv * env, jobject, jint bx, jint by, jint cnt, jbyteArray bufplc, jint bufoffs, jint p) {
	unsigned char ch;
	long fp = (long) frameplace;	
	fp += p;

    gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL);
	for(;cnt>1;cnt--)
	{
		ch = gbuf[bufoffs + (bx>>16)*glogy+(by>>16)];
		(*(char *)fp) = gpal[ch];
		bx += gbxinc;
		by += gbyinc;
		fp += bpl;
	}
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimsetupspritevline
  (JNIEnv * env, jobject, jbyteArray pal, jint shade, jint bxinc, jint byinc, jint ysiz) {
  
    free(gpal);
	jint length = env->GetArrayLength(pal);
	gpal = (char *) malloc(length);
	env->GetByteArrayRegion(pal, 0, length, (jbyte *) gpal);
	
    gshade = shade;
	gbxinc = bxinc;
	gbyinc = byinc;
	glogy = ysiz;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimspritevline
  (JNIEnv * env, jobject, jint bx, jint by, jint cnt, jbyteArray bufplc, jint bufoffs, jint p) {
	unsigned char ch;
	long fp = (long) frameplace;	
	fp += p;
	
    gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL);
	
	for(;cnt>1;cnt--)
	{
		ch = gbuf[bufoffs + (bx>>16)*glogy+(by>>16)];
		if (ch != 255) (*(char *)fp) = gpal[ch + gshade];
		bx += gbxinc;
		by += gbyinc;
		fp += bpl;
	}
	
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitsetupspritevline
  (JNIEnv * env, jobject, jbyteArray pal, jint shade, jint bxinc, jint byinc, jint ysiz) {
    free(gpal);
	jint length = env->GetArrayLength(pal);
	gpal = (char *) malloc(length);
	env->GetByteArrayRegion(pal, 0, length, (jbyte *) gpal);
	
	gbxinc = bxinc;
	gbyinc = byinc;
	glogy = ysiz;
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitspritevline
  (JNIEnv * env, jobject, jint bx, jint by, jint cnt, jbyteArray bufplc, jint bufoffs, jint p) {
	unsigned char ch;
	unsigned short transch;
	unsigned int itransch;
	long fp = (long) frameplace;	
	fp += p;

	gbuf = (char *) env->GetPrimitiveArrayCritical(bufplc, NULL);
	if (transmode) {
		for(;cnt>1;cnt--)
		{
			ch = gbuf[bufoffs + (bx>>16)*glogy+(by>>16)];
			transch = (*((char *)fp))+(gpal[ch]<<8);
			if (ch != 255) 
				(*(char *)fp) = gtrans[transch];
			bx += gbxinc;
			by += gbyinc;
			fp += bpl;
		}
	} else {
		for(;cnt>1;cnt--)
		{
			ch = gbuf[bufoffs + (bx>>16)*glogy+(by>>16)];
			transch = ((*((char *)fp))<<8)+gpal[ch];

			if (ch != 255)
				(*(char *)fp) = gtrans[transch];
			bx += gbxinc;
			by += gbyinc;
			fp += bpl;
		}
	}
	
	env->ReleasePrimitiveArrayCritical(bufplc, (jbyte *) gbuf, JNI_ABORT);
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupdrawslab
  (JNIEnv *, jobject, jint dabpl, jbyteArray pal, jint shade, jint trans) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnidrawslab
  (JNIEnv *, jobject, jint dx, jint v, jint dy, jint vi, jbyteArray data, jint vptr, jint p) {
  
  }