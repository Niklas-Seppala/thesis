#include <string.h>

#include "windex.h"
#include "windex/utils.h"
#include "windex/jni/org_nse_thesis_wordindex_jni_JNIWordIndexBindings.h"

/**
 * @brief
 *
 * @param env
 * @param filepath
 * @param capacity
 * @param bufferSize
 * @param compact
 * @return JNIEXPORT
 */
JNIEXPORT jlong JNICALL Java_org_nse_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexOpen(
    JNIEnv *env, jclass class, jstring filepath, jlong capacity, jlong bufferSize,
    jboolean compact) {
    NOT_USED(class);

    const char *fpath = (*env)->GetStringUTFChars(env, filepath, NULL);
    WordIndex *index_handle = file_word_index_open(fpath, capacity, bufferSize, compact);
    (*env)->ReleaseStringUTFChars(env, filepath, fpath);
    return (jlong)index_handle;
}

/**
 * @brief
 *
 * Class:     org_ns_thesis_wordindex_NativeWordIndex
 * Method:    wordIndexClose
 * Signature: (J)V
 *
 * @param env
 * @param handle
 * @return JNIEXPORT
 */
JNIEXPORT void JNICALL Java_org_nse_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexClose(
    JNIEnv *env, jclass class, jlong handle) {
    NOT_USED(class);
    NOT_USED(env);

    file_word_index_close((WordIndex *)handle);
}

/**
 * @brief
 *
 * Class:     org_ns_thesis_wordindex_NativeWordIndex
 * Method:    wordIndexReadWithContextBuffered
 * Signature: (JLjava/nio/ByteBuffer;JLjava/lang/String;IJ)J
 *
 * @param env
 * @param handle
 * @param jbytebyffer
 * @param readBufferSize
 * @param word
 * @param context
 * @param previous
 * @return JNIEXPORT
 */
JNIEXPORT jlong JNICALL
Java_org_nse_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexReadWithContextBuffered(
    JNIEnv *env, jclass class, jlong handle, jobject jbytebyffer, jlong readBufferSize,
    jstring jword, jint word_len, jint context, jlong iter) {
    NOT_USED(class);

    char *buffer = (*env)->GetDirectBufferAddress(env, jbytebyffer);
    WordIndex *index = (WordIndex *)handle;

    char *word = NULL;
    if ((void *)iter == NULL) {
        word = (char *)(*env)->GetStringUTFChars(env, jword, NULL);
    }

    void *result_iter = file_word_index_read_with_context_buffered(
        index, buffer, readBufferSize, word, word_len, context, (void *)iter);

    if (word != NULL) {
        (*env)->ReleaseStringUTFChars(env, jword, word);
    }

    return (long)result_iter;
}

/*
 * Class:     org_ns_thesis_wordindex_NativeWordIndex
 * Method:    wordIndexCloseIterator
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_org_nse_thesis_wordindex_jni_JNIWordIndexBindings_wordIndexCloseIterator(JNIEnv *env,
                                                                    jclass class,
                                                                    jlong iterator) {
    NOT_USED(env);
    NOT_USED(class);
    file_word_index_close_iterator((void *)iterator);
}
