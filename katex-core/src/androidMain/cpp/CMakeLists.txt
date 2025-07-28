# androidMain/cpp/CMakeLists.txt

cmake_minimum_required(VERSION 3.22)
project(freetypejni)

add_subdirectory(${CMAKE_SOURCE_DIR}/../../../../external/freetype freetype_build)

add_library(freetypejni SHARED freetype_android.cpp)
target_include_directories(freetypejni PRIVATE
        ${CMAKE_SOURCE_DIR}/../../../../external/freetype/include
        ${CMAKE_SOURCE_DIR}/../../../../external/freetype/src
)
target_link_libraries(freetypejni freetype)