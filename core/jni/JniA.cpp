#include <iostream>
#include "JniA.h"

jint *reciptable;
jobject palookup;

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jniinit
  (JNIEnv * env, jobject, jobjectArray jpalookup, jintArray jreciptable) {
	reciptable = env->GetIntArrayElements(jreciptable, NULL);
	palookup = env->GetObjectArrayElement(jpalookup, 1);
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetframeplace
  (JNIEnv * env, jobject jo, jbyteArray newframeplace) {
   printf("newframeplace %d \n", newframeplace);
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetvlinebpl
  (JNIEnv * env, jobject jo, jint dabpl) {
  printf("dabpl %d \n", dabpl);
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnifixtransluscence
  (JNIEnv *, jobject, jbyteArray datrans) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisettransnormal
  (JNIEnv * env, jobject o) {
  printf("jnisettransnormal\n");
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisettransreverse
  (JNIEnv *, jobject) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnidrawpixel
  (JNIEnv *, jobject, jint ptr, jbyte col) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisethlinesizes
  (JNIEnv *, jobject, jint logx, jint logy, jbyteArray bufplc) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetpalookupaddress
  (JNIEnv *, jobject, jint paladdr) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetuphlineasm4
  (JNIEnv *, jobject, jint bxinc, jint byinc) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnihlineasm4
  (JNIEnv *, jobject, jint cnt, jint skiploadincs, jint paloffs, jint by, jint bx, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupslopevlin
  (JNIEnv *, jobject, jint logylogx, jbyteArray bufplc, jint pinc, jint bzinc) {
  
  }
  
JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnislopevlin
  (JNIEnv *, jobject, jint p, jint pal,  jint slopaloffs, jint cnt, jint bx, jint by, jint x3, jint y3, jintArray slopalookup, jint bz)
{

}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupvlineasm
  (JNIEnv *, jobject, jint neglogy) {
  
}

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnivlineasm1
  (JNIEnv *, jobject, jint vinc, jint pal, jint shade, jint cnt, jint vplc, jbyteArray bufplc, jint bufoffs, jint p) {
  
 }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupmvlineasm
  (JNIEnv *, jobject, jint neglogy) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimvlineasm1
  (JNIEnv *, jobject, jint vinc, jint pal, jint shade, jint cnt, jint vplc, jbyteArray bufplc, jint bufoffs, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetuptvlineasm
  (JNIEnv *, jobject, jint neglogy) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitvlineasm1
  (JNIEnv *, jobject, jint vinc, jint pal, jint shade, jint cnt, jint vplc, jbyteArray  bufplc, jint bufoffs, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisethlineincs
  (JNIEnv *, jobject, jint x, jint y) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetuphline
  (JNIEnv *, jobject, jint pal, jint shade) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimsethlineshift
  (JNIEnv *, jobject, jint logx, jint logy) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimhline
  (JNIEnv *, jobject, jbyteArray bufplc, jint bx, jint cntup16, jint junk, jint by, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitsethlineshift
  (JNIEnv *, jobject, jint logx, jint logy) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnithline
  (JNIEnv *, jobject, jbyteArray bufplc, jint bx, jint cntup16, jint junk, jint by, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupspritevline
  (JNIEnv *, jobject, jint pal, jint shade, jint bxinc, jint byinc, jint ysiz) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnispritevline
  (JNIEnv *, jobject, jint bx, jint by, jint cnt, jbyteArray bufplc, jint bufoffs, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimsetupspritevline
  (JNIEnv *, jobject, jint pal, jint shade, jint bxinc, jint byinc, jint ysiz) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnimspritevline
  (JNIEnv *, jobject, jint bx, jint by, jint cnt, jbyteArray bufplc, jint bufoffs, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitsetupspritevline
  (JNIEnv *, jobject, jint pal, jint shade, jint bxinc, jint byinc, jint ysiz) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnitspritevline
  (JNIEnv *, jobject, jint bx, jint by, jint cnt, jbyteArray bufplc, jint bufoffs, jint p) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnisetupdrawslab
  (JNIEnv *, jobject, jint dabpl, jint pal, jint shade, jint trans) {
  
  }

JNIEXPORT void JNICALL Java_ru_m210projects_Build_Render_Software_JniA_jnidrawslab
  (JNIEnv *, jobject, jint dx, jint v, jint dy, jint vi, jbyteArray data, jint vptr, jint p) {
  
  }