cmake_minimum_required(VERSION 3.22.1)
project("rubberbandimplementation")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Corrected JNI_LIBS_PATH
set(JNI_LIBS_PATH ${CMAKE_SOURCE_DIR}/../jniLibs)

add_library(${CMAKE_PROJECT_NAME} SHARED native-lib.cpp)

# Specify the path to your prebuilt libraries
add_library(rubberband SHARED IMPORTED)
set_target_properties(rubberband PROPERTIES
        IMPORTED_LOCATION "${JNI_LIBS_PATH}/${ANDROID_ABI}/librubberband.so")

add_library(c++_shared SHARED IMPORTED)
set_target_properties(c++_shared PROPERTIES
        IMPORTED_LOCATION "${JNI_LIBS_PATH}/${ANDROID_ABI}/libc++_shared.so")

target_link_libraries(${CMAKE_PROJECT_NAME}
        rubberband
        c++_shared
        android
        log)
