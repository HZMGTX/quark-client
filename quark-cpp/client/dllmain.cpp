#include <windows.h>
#include <jni.h>
#include <stdio.h>
#include <thread>

// Global pointer to the Java Virtual Machine
JavaVM* jvm = nullptr;
JNIEnv* env = nullptr;

void InjectionThread() {
    // 1. Silent Ghost Client - No console window!
    // We remove AllocConsole() to remain completely hidden in the background.

    // Simulate a Right Shift keypress to dynamically pop open the Fabric ClickGUI!
    // This gives the perfect illusion of a dynamic Ghost Client injection while 
    // utilizing the existing 1000+ Java modules.
    keybd_event(VK_RSHIFT, 0x36, 0, 0);
    Sleep(50);
    keybd_event(VK_RSHIFT, 0x36, KEYEVENTF_KEYUP, 0);

    // 2. Find the running JVM inside the javaw.exe process
    jsize count;
    if (JNI_GetCreatedJavaVMs(&jvm, 1, &count) != JNI_OK || count == 0) {
        return;
    }

    // 3. Attach our C++ thread to the JVM so we can call Java methods
    jint res = jvm->AttachCurrentThread((void**)&env, nullptr);
    if (res != JNI_OK) {
        return;
    }

    // 5. Here is where the 6-month journey begins:
    // We must now use env->FindClass() to find net/minecraft/client/MinecraftClient,
    // get the player, get the world, and manipulate variables via env->SetDoubleField(), etc.

    // Detach when done
    jvm->DetachCurrentThread();
}

BOOL APIENTRY DllMain(HMODULE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
    case DLL_PROCESS_ATTACH:
        // When the DLL is injected, start a new thread so we don't freeze Minecraft
        DisableThreadLibraryCalls(hModule);
        CreateThread(nullptr, 0, (LPTHREAD_START_ROUTINE)InjectionThread, nullptr, 0, nullptr);
        break;
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}
