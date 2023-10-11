/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_ns_thesis_wordindex_jni_JNIWordIndexBindings */

#ifndef _Included_org_ns_thesis_wordindex_jni_JNIWordIndexBindings
#define _Included_org_ns_thesis_wordindex_jni_JNIWordIndexBindings
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_ns_thesis_wordindex_jni_JNIWordIndexBindings
 * Method:    wordIndexOpen
 * Signature: (Ljava/lang/String;JJZ)J
 */
JNIEXPORT jlong JNICALL Java_org_ns_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexOpen
  (JNIEnv *, jclass, jstring, jlong, jlong, jboolean);

/*
 * Class:     org_ns_thesis_wordindex_jni_JNIWordIndexBindings
 * Method:    wordIndexClose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_ns_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexClose
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_ns_thesis_wordindex_jni_JNIWordIndexBindings
 * Method:    wordIndexReadWithContextBuffered
 * Signature: (JLjava/nio/ByteBuffer;JLjava/lang/String;IIJ)J
 */
JNIEXPORT jlong JNICALL Java_org_ns_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexReadWithContextBuffered
  (JNIEnv *, jclass, jlong, jobject, jlong, jstring, jint, jint, jlong);

/*
 * Class:     org_ns_thesis_wordindex_jni_JNIWordIndexBindings
 * Method:    wordIndexCloseIterator
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_ns_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexCloseIterator
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
