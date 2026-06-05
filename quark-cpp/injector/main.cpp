#include <windows.h>
#include <tlhelp32.h>
#include <iostream>
#include <string>

// Function to find the PID of javaw.exe
DWORD GetProcessIdByName(const std::wstring& processName) {
    DWORD pid = 0;
    HANDLE snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if (snapshot != INVALID_HANDLE_VALUE) {
        PROCESSENTRY32W pe32;
        pe32.dwSize = sizeof(PROCESSENTRY32W);
        if (Process32FirstW(snapshot, &pe32)) {
            do {
                if (processName == pe32.szExeFile) {
                    pid = pe32.th32ProcessID;
                    break;
                }
            } while (Process32NextW(snapshot, &pe32));
        }
        CloseHandle(snapshot);
    }
    return pid;
}

int main() {
    std::cout << "======================================\n";
    std::cout << " Quark Ghost Client Injector (C++) \n";
    std::cout << "======================================\n\n";

    // 1. Find Minecraft (javaw.exe)
    DWORD pid = GetProcessIdByName(L"javaw.exe");
    if (pid == 0) {
        std::cerr << "[!] Could not find running Minecraft process (javaw.exe)!" << std::endl;
        return 1;
    }
    std::cout << "[+] Found Minecraft running on PID: " << pid << std::endl;

    // 2. Get the full path to our DLL Payload
    char dllPath[MAX_PATH];
    GetFullPathNameA("quark-client.dll", MAX_PATH, dllPath, NULL);
    std::cout << "[+] DLL Path: " << dllPath << std::endl;

    // 3. Open the Minecraft Process
    HANDLE hProcess = OpenProcess(PROCESS_ALL_ACCESS, FALSE, pid);
    if (!hProcess) {
        std::cerr << "[!] Failed to open process. Try running as Administrator." << std::endl;
        return 1;
    }
    std::cout << "[+] Handle opened to target process." << std::endl;

    // 4. Allocate memory inside the Minecraft Process for the DLL path
    LPVOID pDllPath = VirtualAllocEx(hProcess, 0, strlen(dllPath) + 1, MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
    if (!pDllPath) {
        std::cerr << "[!] Memory allocation failed in target process." << std::endl;
        CloseHandle(hProcess);
        return 1;
    }

    // 5. Write the DLL path into that allocated memory
    WriteProcessMemory(hProcess, pDllPath, (LPVOID)dllPath, strlen(dllPath) + 1, 0);

    // 6. Create a remote thread inside Minecraft that calls LoadLibraryA on our DLL path
    HANDLE hThread = CreateRemoteThread(hProcess, 0, 0,
        (LPTHREAD_START_ROUTINE)GetProcAddress(GetModuleHandleA("kernel32.dll"), "LoadLibraryA"),
        pDllPath, 0, 0);

    if (!hThread) {
        std::cerr << "[!] Failed to create remote thread (Injection failed)." << std::endl;
        VirtualFreeEx(hProcess, pDllPath, 0, MEM_RELEASE);
        CloseHandle(hProcess);
        return 1;
    }

    std::cout << "[+] Injection successful! The DLL is now executing inside Minecraft." << std::endl;

    // 7. Cleanup
    WaitForSingleObject(hThread, INFINITE);
    VirtualFreeEx(hProcess, pDllPath, 0, MEM_RELEASE);
    CloseHandle(hThread);
    CloseHandle(hProcess);

    return 0;
}
