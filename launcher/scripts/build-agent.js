'use strict';
// Builds launcher/agent/quark-agent.jar from the bundled Java sources so
// electron-builder's "agent" extraResources entry has something to copy.
// Skips work if the jar already exists (e.g. built manually via build-agent.sh).
const fs   = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const AGENT_DIR = path.join(__dirname, '..', 'agent');
const OUT_DIR   = path.join(AGENT_DIR, 'out');
const LIB_DIR   = path.join(AGENT_DIR, 'lib');
const JAR_OUT   = path.join(AGENT_DIR, 'quark-agent.jar');
const MANIFEST  = path.join(AGENT_DIR, 'manifest.txt');

function findFiles(dir, ext, skip = []) {
    const out = [];
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        const full = path.join(dir, entry.name);
        if (entry.isDirectory()) {
            if (skip.includes(full)) continue;
            out.push(...findFiles(full, ext, skip));
        } else if (entry.name.endsWith(ext)) {
            out.push(full);
        }
    }
    return out;
}

function buildAgent() {
    if (fs.existsSync(JAR_OUT)) {
        console.log('[build-agent] quark-agent.jar already exists, skipping rebuild.');
        return;
    }

    const asmJars = findFiles(LIB_DIR, '.jar');
    if (asmJars.length === 0) throw new Error('ASM jars not found in launcher/agent/lib');

    const javaFiles = findFiles(AGENT_DIR, '.java', [OUT_DIR]);
    if (javaFiles.length === 0) throw new Error('No agent .java sources found in launcher/agent');

    console.log('[build-agent] Compiling agent sources...');
    fs.rmSync(OUT_DIR, { recursive: true, force: true });
    fs.mkdirSync(OUT_DIR, { recursive: true });

    const classpath = asmJars.join(path.delimiter);
    execFileSync('javac', ['-cp', classpath, '-d', OUT_DIR, '--release', '17', '-Xlint:none', ...javaFiles], { stdio: 'inherit' });

    // Shade ASM classes into the agent so it's self-contained when attached —
    // the target JVM's classpath is only quark-agent.jar itself.
    console.log('[build-agent] Merging ASM runtime classes into agent JAR...');
    for (const jar of asmJars) {
        execFileSync('jar', ['xf', jar], { cwd: OUT_DIR, stdio: 'inherit' });
    }
    fs.rmSync(path.join(OUT_DIR, 'META-INF'), { recursive: true, force: true });
    fs.rmSync(path.join(OUT_DIR, 'module-info.class'), { force: true });

    console.log('[build-agent] Packaging quark-agent.jar...');
    const classFiles = findFiles(OUT_DIR, '.class').map(f => path.relative(OUT_DIR, f));
    execFileSync('jar', ['cfm', JAR_OUT, MANIFEST, ...classFiles], { cwd: OUT_DIR, stdio: 'inherit' });

    console.log(`[build-agent] Built: ${JAR_OUT}`);
}

module.exports = buildAgent;
module.exports.default = async () => buildAgent();

if (require.main === module) {
    try {
        buildAgent();
    } catch (err) {
        console.error('[build-agent] FAILED:', err.message);
        console.error('[build-agent] Make sure a JDK 17+ is installed and javac/jar are on your PATH.');
        process.exit(1);
    }
}
