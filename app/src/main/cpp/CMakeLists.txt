cmake_minimum_required(VERSION 3.22.1)
project(edgeai_llama)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(LLAMA_CPP_SOURCE_DIR "D:/edgeai_vendor/llama.cpp" CACHE PATH "Path to local llama.cpp source")
set(EDGEAI_WITH_LLAMA_CPP OFF)

if (EXISTS "${LLAMA_CPP_SOURCE_DIR}/CMakeLists.txt")
    set(EDGEAI_WITH_LLAMA_CPP ON)
    set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
    set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
    add_subdirectory("${LLAMA_CPP_SOURCE_DIR}" "${CMAKE_BINARY_DIR}/llama_cpp_build" EXCLUDE_FROM_ALL)
endif()

add_library(
    edgeai_llama
    SHARED
    edgeai_llama.cpp
)

find_library(log-lib log)

if(EDGEAI_WITH_LLAMA_CPP)
    target_compile_definitions(edgeai_llama PRIVATE EDGEAI_WITH_LLAMA_CPP=1)
    target_include_directories(edgeai_llama PRIVATE "${LLAMA_CPP_SOURCE_DIR}/include")
endif()

target_link_libraries(
    edgeai_llama
    $<$<BOOL:${EDGEAI_WITH_LLAMA_CPP}>:llama>
    ${log-lib}
)
