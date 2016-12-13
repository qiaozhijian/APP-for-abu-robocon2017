#include <jni.h>
//#include "com_action_ndktool_JniShareUtils.h
extern "C"
{
JNIEXPORT jbyteArray JNICALL
Java_com_action_app_actionctr_com_action_app_jnitool_JniShareUtils_floatToByte(JNIEnv *env,
                                                                               jobject instance,
                                                                               jfloat data) {

    jbyteArray array;
    jbyte *temp;

    temp = (jbyte *) &data;

    array = env->NewByteArray(4);
    env->SetByteArrayRegion(array, 0, 4, temp);

    return array;

}

}
