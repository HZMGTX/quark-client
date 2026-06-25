cmake_minimum_required(VERSION 3.20)
project(QuarkGhostClient VERSION 1.0)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find Java (JNI is required for hooking into the JVM)
find_package(JNI REQUIRED)

# ==========================================
# 1. The Injector Executable
# ==========================================
add_executable(quark-injector injector/main.cpp)
target_link_libraries(quark-injector PRIVATE user32 kernel32)

# ==========================================
# 2. The Ghost Client DLL Payload
# ==========================================
add_library(quark-client SHARED client/dllmain.cpp)
target_include_directories(quark-client PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(quark-client PRIVATE ${JNI_LIBRARIES} user32)

# Copy the compiled DLL to the injector directory for easy testing
add_custom_command(TARGET quark-client POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy $<TARGET_FILE:quark-client> $<TARGET_FILE_DIR:quark-injector>
)
