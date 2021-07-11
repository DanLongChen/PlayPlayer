cmake_minimum_required(VERSION 3.4.1)#编译本地native library的时候需要的Cmake最小版本

set(jnilibs "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs")#赋值操作

include_directories(#指定目录添加到编译器的头文件搜索路径之下，指定的目录被解释成当前源码路径的相对路径。
        util
        glm#glm是为OPenGL定制的math库
        JNI
)

file(GLOB src-files
        ${CMAKE_CURRENT_SOURCE_DIR}/JNI/*.cpp)#多源文件处理（源文件很多，把所有文件一个个加入很麻烦）

add_library(#添加一个库
    native-render#库的名字
        SHARED#类型（STATIC是静态库）
        ${src-files}#库的源文件
)
set_target_properties(native-render PROPERTIES OUTPUT_NAME "librender")

find_library(#用于查找库
        log-lib
        log)

add_library(assimp SHARED IMPORTED )
set_target_properties(assimp PROPERTIES IMPORTED_LOCATION ${jnilibs}/${ANDROID_ABI}/libassimp.so)
#message(SEND_ERROR "${jnilibs}/${ANDROID_ABI}/libassimp.so")#这里只是打印信息

add_library(opencv_java3 SHARED IMPORTED )
set_target_properties(opencv_java3 PROPERTIES IMPORTED_LOCATION ${jnilibs}/${ANDROID_ABI}/libopencv_java3.so)

add_library(freetype STATIC IMPORTED )
set_target_properties(freetype PROPERTIES IMPORTED_LOCATION ${jnilibs}/${ANDROID_ABI}/libfreetype.a)

set(third-part-libs
        assimp#模型导入库
        opencv_java3#opencv
        freetype#字体引擎
        )

target_link_libraries(
        native-render
        ${log_lib}
        ${third-part-libs}
        GLESv3#引入openGL3.0
        EGL#引入EGL，是openGL API与本地窗口系统的中间接口层
        android
)#该指令的作用为将目标文件与库文件进行链接


