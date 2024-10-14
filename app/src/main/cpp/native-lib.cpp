#include <jni.h>
#include <string>
#include "RubberBandStretcher.h"

using namespace RubberBand;

// Helper function to get the RubberBandStretcher instance from the JNI object
RubberBandStretcher* getStretcher(JNIEnv *env, jobject obj) {
    jclass c = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(c, "handle", "J");
    jlong handle = env->GetLongField(obj, fid);
    return reinterpret_cast<RubberBandStretcher*>(handle);
}

// Helper function to set the RubberBandStretcher instance to the JNI object
void setStretcher(JNIEnv *env, jobject obj, RubberBandStretcher *stretcher) {
    jclass c = env->GetObjectClass(obj);
    jfieldID fid = env->GetFieldID(c, "handle", "J");
    jlong handle = reinterpret_cast<jlong>(stretcher);
    env->SetLongField(obj, fid, handle);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_dispose(JNIEnv *env, jobject obj) {
    delete getStretcher(env, obj);
    setStretcher(env, obj, nullptr);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_reset(JNIEnv *env, jobject obj) {
    getStretcher(env, obj)->reset();
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setTimeRatio(JNIEnv *env, jobject obj, jdouble ratio) {
    getStretcher(env, obj)->setTimeRatio(ratio);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setPitchScale(JNIEnv *env, jobject obj, jdouble scale) {
    getStretcher(env, obj)->setPitchScale(scale);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_getChannelCount(JNIEnv *env, jobject obj) {
    return getStretcher(env, obj)->getChannelCount();
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_getTimeRatio(JNIEnv *env, jobject obj) {
    return getStretcher(env, obj)->getTimeRatio();
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_getPitchScale(JNIEnv *env, jobject obj) {
    return getStretcher(env, obj)->getPitchScale();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_getLatency(JNIEnv *env, jobject obj) {
    return getStretcher(env, obj)->getLatency();
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setTransientsOption(JNIEnv *env, jobject obj, jint options) {
    getStretcher(env, obj)->setTransientsOption(options);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setDetectorOption(JNIEnv *env, jobject obj, jint options) {
    getStretcher(env, obj)->setDetectorOption(options);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setPhaseOption(JNIEnv *env, jobject obj, jint options) {
    getStretcher(env, obj)->setPhaseOption(options);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setFormantOption(JNIEnv *env, jobject obj, jint options) {
    getStretcher(env, obj)->setFormantOption(options);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setPitchOption(JNIEnv *env, jobject obj, jint options) {
    getStretcher(env, obj)->setPitchOption(options);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setExpectedInputDuration(JNIEnv *env, jobject obj, jlong duration) {
    getStretcher(env, obj)->setExpectedInputDuration(duration);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_setMaxProcessSize(JNIEnv *env, jobject obj, jint size) {
    getStretcher(env, obj)->setMaxProcessSize(size);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_getSamplesRequired(JNIEnv *env, jobject obj) {
    return getStretcher(env, obj)->getSamplesRequired();
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_study(JNIEnv *env, jobject obj, jobjectArray data, jint offset, jint n, jboolean final_block) {
    int channels = env->GetArrayLength(data);
    float **arr = new float*[channels];
    float **input = new float*[channels];
    for (int c = 0; c < channels; ++c) {
        jfloatArray cdata = (jfloatArray)env->GetObjectArrayElement(data, c);
        arr[c] = env->GetFloatArrayElements(cdata, 0);
        input[c] = arr[c] + offset;
    }

    getStretcher(env, obj)->study(input, n, final_block);

    for (int c = 0; c < channels; ++c) {
        jfloatArray cdata = (jfloatArray)env->GetObjectArrayElement(data, c);
        env->ReleaseFloatArrayElements(cdata, arr[c], 0);
    }

    delete[] input;
    delete[] arr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_process(JNIEnv *env, jobject obj, jobjectArray data, jint offset, jint n, jboolean final_block) {
    int channels = env->GetArrayLength(data);
    float **arr = new float*[channels];
    float **input = new float*[channels];
    for (int c = 0; c < channels; ++c) {
        jfloatArray cdata = (jfloatArray)env->GetObjectArrayElement(data, c);
        arr[c] = env->GetFloatArrayElements(cdata, 0);
        input[c] = arr[c] + offset;
    }

    getStretcher(env, obj)->process(input, n, final_block);

    for (int c = 0; c < channels; ++c) {
        jfloatArray cdata = (jfloatArray)env->GetObjectArrayElement(data, c);
        env->ReleaseFloatArrayElements(cdata, arr[c], 0);
    }

    delete[] input;
    delete[] arr;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_available(JNIEnv *env, jobject obj) {
    return getStretcher(env, obj)->available();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_retrieve(JNIEnv *env, jobject obj, jobjectArray output, jint offset, jint n) {
    RubberBandStretcher *stretcher = getStretcher(env, obj);
    size_t channels = stretcher->getChannelCount();

    float **outbuf = new float*[channels];
    for (size_t c = 0; c < channels; ++c) {
        outbuf[c] = new float[n];
    }

    size_t retrieved = stretcher->retrieve(outbuf, n);

    for (size_t c = 0; c < channels; ++c) {
        jfloatArray cdata = (jfloatArray)env->GetObjectArrayElement(output, c);
        env->SetFloatArrayRegion(cdata, offset, retrieved, outbuf[c]);
    }

    for (size_t c = 0; c < channels; ++c) {
        delete[] outbuf[c];
    }
    delete[] outbuf;

    return retrieved;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_initialise(JNIEnv *env, jobject obj, jint sampleRate, jint channels, jint options, jdouble initialTimeRatio, jdouble initialPitchScale) {
    setStretcher(env, obj, new RubberBandStretcher(sampleRate, channels,options , initialTimeRatio, initialPitchScale));
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_rubberbandimplementation_RubberBandStretcher_getStartDelay(JNIEnv *env,
                                                                            jobject obj) {

    return getStretcher(env, obj)->getStartDelay();

}