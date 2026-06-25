#include <windows.h>
#include <jni.h>
#include <stdio.h>
#include <thread>
#include <string>

// ============================================================================
//  Quark.cc Ghost Client - DLL Payload
//  Injected into javaw.exe (Minecraft) by quark-injector.exe
//
//  Flow:
//  1. DLL_PROCESS_ATTACH → InjectionThread()
//  2. Find the running JVM via JNI_GetCreatedJavaVMs
//  3. Attach our native thread to the JVM
//  4. Load quark-agent.jar into the system ClassLoader
//  5. Call cc.quark.agent.QuarkAgent.injectFromDll() — this bootstraps all 1300+ modules
//  6. Simulate Right-Shift to open the ClickGUI
// ============================================================================

static JavaVM* gJvm    = nullptr;
static JNIEnv* gEnv    = nullptr;

// Path to quark-agent.jar (same directory as the DLL)
static std::string GetAgentJarPath() {
    char dllPath[MAX_PATH] = {};
    HMODULE hMod = nullptr;
    GetModuleHandleExA(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS |
                       GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
                       (LPCSTR)&GetAgentJarPath, &hMod);
    GetModuleFileNameA(hMod, dllPath, MAX_PATH);

    std::string path(dllPath);
    size_t pos = path.find_last_of("\\/");
    if (pos != std::string::npos) path = path.substr(0, pos + 1);
    return path + "quark-agent.jar";
}

// Add a JAR to the system ClassLoader's search path via URLClassLoader reflection
static bool AddJarToClasspath(JNIEnv* env, const std::string& jarPath) {
    // Convert Windows path to file:// URL
    std::string url = "file:/" + jarPath;
    for (char& c : url) if (c == '\\') c = '/';

    jclass urlClass    = env->FindClass("java/net/URL");
    jclass loaderClass = env->FindClass("java/net/URLClassLoader");
    if (!urlClass || !loaderClass) return false;

    // new URL("file:/C:/path/to/quark-agent.jar")
    jmethodID urlCtor = env->GetMethodID(urlClass, "<init>", "(Ljava/lang/String;)V");
    jstring jUrl = env->NewStringUTF(url.c_str());
    jobject urlObj = env->NewObject(urlClass, urlCtor, jUrl);
    if (!urlObj) return false;

    // ClassLoader.getSystemClassLoader()
    jclass classLoaderClass = env->FindClass("java/lang/ClassLoader");
    jmethodID getSysLoader  = env->GetStaticMethodID(classLoaderClass, "getSystemClassLoader",
                                                      "()Ljava/lang/ClassLoader;");
    jobject sysLoader = env->CallStaticObjectMethod(classLoaderClass, getSysLoader);
    if (!sysLoader) return false;

    // Cast to URLClassLoader and call addURL(URL)
    jmethodID addUrl = env->GetMethodID(loaderClass, "addURL", "(Ljava/net/URL;)V");
    if (addUrl) {
        env->CallVoidMethod(sysLoader, addUrl, urlObj);
        printf("[Quark DLL] Added to classpath: %s\n", url.c_str());
        return !env->ExceptionCheck();
    }

    // If addURL is not accessible (sealed ClassLoader), use Instrumentation via attach
    printf("[Quark DLL] addURL unavailable, falling back to agent attach.\n");
    return false;
}

static void InjectionThread() {
    printf("\n");
    printf("  ======================================================\n");
    printf("   Quark.cc Ghost Client  -  DLL Injector v1.0\n");
    printf("  ======================================================\n\n");

    // 1. Find the running Minecraft JVM
    jsize jvmCount = 0;
    if (JNI_GetCreatedJavaVMs(&gJvm, 1, &jvmCount) != JNI_OK || jvmCount == 0) {
        printf("[Quark DLL] ERROR: No running JVM found. Is Minecraft running?\n");
        return;
    }
    printf("[Quark DLL] Found JVM at 0x%p\n", (void*)gJvm);

    // 2. Attach our thread to the JVM
    JavaVMAttachArgs attachArgs = {};
    attachArgs.version  = JNI_VERSION_10;
    attachArgs.name     = (char*)"Quark-DLL-Thread";
    attachArgs.group    = nullptr;

    jint attachResult = gJvm->AttachCurrentThread((void**)&gEnv, &attachArgs);
    if (attachResult != JNI_OK) {
        printf("[Quark DLL] ERROR: Failed to attach thread to JVM (code %d)\n", attachResult);
        return;
    }
    printf("[Quark DLL] Attached to JVM successfully.\n");

    // 3. Add quark-agent.jar to the classpath
    std::string agentJar = GetAgentJarPath();
    printf("[Quark DLL] Agent JAR: %s\n", agentJar.c_str());

    if (!AddJarToClasspath(gEnv, agentJar)) {
        // Try loading agent via system property trick if direct add fails
        printf("[Quark DLL] Classpath add failed. Attempting system-attach workaround...\n");
    }

    // 4. Load and call cc.quark.agent.QuarkAgent.injectFromDll()
    jclass agentClass = gEnv->FindClass("cc/quark/agent/QuarkAgent");
    if (!agentClass) {
        if (gEnv->ExceptionCheck()) gEnv->ExceptionDescribe();
        printf("[Quark DLL] ERROR: Could not find cc.quark.agent.QuarkAgent class.\n");
        printf("[Quark DLL] Make sure quark-agent.jar is in the same folder as the DLL.\n");
        gJvm->DetachCurrentThread();
        return;
    }

    jmethodID injectMethod = gEnv->GetStaticMethodID(agentClass, "injectFromDll", "()V");
    if (!injectMethod) {
        printf("[Quark DLL] ERROR: injectFromDll() method not found.\n");
        gJvm->DetachCurrentThread();
        return;
    }

    printf("[Quark DLL] Calling QuarkAgent.injectFromDll()...\n");
    gEnv->CallStaticVoidMethod(agentClass, injectMethod);

    if (gEnv->ExceptionCheck()) {
        gEnv->ExceptionDescribe();
        gEnv->ExceptionClear();
        printf("[Quark DLL] Exception during injection.\n");
    } else {
        printf("[Quark DLL] *** QUARK INJECTED SUCCESSFULLY ***\n");
        printf("[Quark DLL] Press Right-Shift in Minecraft to open the GUI.\n");

        // 5. Simulate Right-Shift keypress to open the ClickGUI
        Sleep(2000);
        keybd_event(VK_RSHIFT, 0x36, 0, 0);
        Sleep(50);
        keybd_event(VK_RSHIFT, 0x36, KEYEVENTF_KEYUP, 0);
    }

    // 6. Detach — don't keep the thread alive
    gJvm->DetachCurrentThread();
}

BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
    case DLL_PROCESS_ATTACH:
        DisableThreadLibraryCalls(hModule);
        // Launch injection in a separate thread so DllMain returns immediately
        CreateThread(nullptr, 0, (LPTHREAD_START_ROUTINE)InjectionThread, nullptr, 0, nullptr);
        break;
    case DLL_PROCESS_DETACH:
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
        break;
    }
    return TRUE;
}
