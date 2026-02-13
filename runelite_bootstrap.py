import os
import subprocess
import shutil
import json
import urllib.request
import re
import platform
import stat
from datetime import datetime

# =============================================================================
# CONFIGURATION
# =============================================================================

# Detect OS and set paths dynamically
IS_WINDOWS = os.name == 'nt'
USER_HOME = os.path.expanduser("~")

# Set Base Directory
# If on Windows and H: drive exists, use it (preserving your old workflow)
# Otherwise, default to the user's home folder (Works on Ubuntu & Windows C drive)
if IS_WINDOWS and os.path.exists(r"H:\\"):
    BASE_DIR = r"H:\Automation_Empire"
else:
    BASE_DIR = os.path.join(USER_HOME, "Automation_Empire")

# Define operational paths using os.path.join for cross-platform compatibility
WORK_DIR = os.path.join(BASE_DIR, "Upstream_RuneLite")
VERSION_FILE_PATH = os.path.join(BASE_DIR, "target_version.txt")

MAVEN_GROUP_ID = "net.runelite"
MAVEN_VERSION = "LOCAL-GRADLE"

# PLUGINS TO KEEP
KEEP_PLUGINS = {
    "account",
    "config",
    "devtools",
    "fps",
    "grounditems",
    "itemcharges",
    "logouttimer",
    "lowmemory",
    "skillcalculator",
    "worldmap",
    "screenmarkers", # Required by Config Panel
}

# =============================================================================

class RuneLiteBootstrapper:
    def __init__(self):
        self.repo_url = "https://github.com/runelite/runelite.git"
        self.mvn_cmd = "mvn.cmd" if IS_WINDOWS else "mvn"
        
        # Determine Gradle command path
        gradle_script = "gradlew.bat" if IS_WINDOWS else "gradlew"
        self.gradle_cmd = os.path.join(WORK_DIR, gradle_script)
        
        self.latest_tag = ""
        self.latest_version = ""

    def log(self, msg):
        print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}")

    def get_latest_version(self):
        self.log("Fetching latest tags from GitHub...")
        try:
            # We use /tags because /releases/latest often returns 404 for this repo
            url = "https://api.github.com/repos/runelite/runelite/tags"
            
            req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req) as response:
                data = json.loads(response.read().decode())
                
                found_tag = None
                
                # Iterate through tags to find the first "Stable" one
                for tag_obj in data:
                    name = tag_obj.get("name", "")
                    
                    # Filter Logic:
                    # 1. Must start with "runelite-parent-"
                    # 2. Must NOT contain "SNAPSHOT"
                    if name.startswith("runelite-parent-") and "SNAPSHOT" not in name:
                        found_tag = name
                        break
                
                if not found_tag:
                    raise ValueError("Could not find a valid stable runelite-parent tag.")
                
                self.latest_tag = found_tag

                # Extract "1.12.16" from "runelite-parent-1.12.16"
                match = re.search(r'(\d+\.\d+\.\d+)', self.latest_tag)
                if match:
                    self.latest_version = match.group(1)
                else:
                    raise ValueError(f"Could not parse version from tag: {self.latest_tag}")
                
                self.log(f"Detected Stable Version: {self.latest_version} (Tag: {self.latest_tag})")
                
                # === SAVE THE VERSION ARTIFACT ===
                # Ensure directory exists before writing
                target_dir = os.path.dirname(VERSION_FILE_PATH)
                if target_dir: # Only make dirs if path is not empty
                    os.makedirs(target_dir, exist_ok=True)
                    
                with open(VERSION_FILE_PATH, "w") as f:
                    f.write(self.latest_version)
                self.log(f"Saved version target to: {VERSION_FILE_PATH}")

        except Exception as e:
            self.log(f"Error fetching version: {e}")
            raise

    def setup_env(self):
        env = os.environ.copy()
        # Set JAVA_HOME if needed here, or rely on system path
        return env

    def run(self, cmd, cwd, env):
        self.log(f"EXEC: {cmd}")
        subprocess.check_call(cmd, cwd=cwd, env=env, shell=True)

    def make_gradle_executable(self):
        """Ensures gradlew has execution permissions on Linux/Mac"""
        if not IS_WINDOWS and os.path.exists(self.gradle_cmd):
            st = os.stat(self.gradle_cmd)
            os.chmod(self.gradle_cmd, st.st_mode | stat.S_IEXEC)
            self.log("Made gradlew executable.")

    def install_jar(self, file_path, artifact_id, env):
        self.log(f"Installing {os.path.basename(file_path)}...")
        # Quote file path to handle spaces in Linux or Windows paths
        cmd = (
            f'{self.mvn_cmd} install:install-file '
            f'-Dfile="{file_path}" '
            f'-DgroupId={MAVEN_GROUP_ID} '
            f'-DartifactId={artifact_id} '
            f'-Dversion={MAVEN_VERSION} '
            f'-Dpackaging=jar'
        )
        self.run(cmd, WORK_DIR, env)

    def find_jar(self, directory, prefix=""):
        if not os.path.exists(directory): return None
        candidates = [
            f for f in os.listdir(directory) 
            if f.endswith(".jar") and prefix in f 
            and "sources" not in f and "javadoc" not in f 
            and "plain" not in f and "shaded" not in f
        ]
        if not candidates: return None
        candidates.sort(key=len)
        return os.path.join(directory, candidates[0])

    def prune_plugins(self):
        self.log("--- PRUNING PLUGINS ---")
        base_path = os.path.join(WORK_DIR, "runelite-client")
        # Use os.path.join to handle slashes correctly
        main_plugins = os.path.join(base_path, "src", "main", "java", "net", "runelite", "client", "plugins")
        test_plugins = os.path.join(base_path, "src", "test", "java", "net", "runelite", "client", "plugins")
        module_file = os.path.join(base_path, "src", "main", "java", "net", "runelite", "client", "RuneLiteModule.java")

        deleted_count = 0

        # 1. Delete Main Plugin Source Code
        if os.path.exists(main_plugins):
            for folder in os.listdir(main_plugins):
                folder_path = os.path.join(main_plugins, folder)
                if os.path.isdir(folder_path) and folder not in KEEP_PLUGINS:
                    shutil.rmtree(folder_path)
                    deleted_count += 1
        
        # 2. Delete Test Plugin Source Code
        if os.path.exists(test_plugins):
            for folder in os.listdir(test_plugins):
                folder_path = os.path.join(test_plugins, folder)
                if os.path.isdir(folder_path) and folder not in KEEP_PLUGINS:
                    shutil.rmtree(folder_path)

        self.log(f"Deleted {deleted_count} plugins and their tests.")

        # 3. Sanitize RuneLiteModule.java
        if os.path.exists(module_file):
            self.log("Sanitizing RuneLiteModule.java...")
            with open(module_file, 'r', encoding='utf-8') as f:
                lines = f.readlines()

            new_lines = []
            for line in lines:
                should_delete = False
                for p in KEEP_PLUGINS:
                    # Logic: if line matches a kept plugin, break (keep it)
                    if f"plugins.{p}" in line or f"{p.capitalize()}Plugin.class" in line or f"{p.upper()}" in line:
                        break 
                else:
                    # Logic: if loop finished without breaking, it's not a kept plugin.
                    # check if it is a plugin definition line
                    if "net.runelite.client.plugins." in line or "Plugin.class" in line:
                         if "PluginManager" not in line and "ExternalPluginManager" not in line:
                             should_delete = True

                if not should_delete:
                    new_lines.append(line)
            
            with open(module_file, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
        
        self.log("--- PRUNE COMPLETE ---")

    def build_project(self, env):
        is_maven = os.path.exists(os.path.join(WORK_DIR, "pom.xml"))
        
        if is_maven:
            self.log("Detected MAVEN build system")
            self.run(f"{self.mvn_cmd} clean install -DskipTests -Dmaven.test.skip=true -Dmaven.javadoc.skip=true", WORK_DIR, env)
        else:
            self.log("Detected GRADLE build system")
            self.make_gradle_executable() # FIX: Ensure gradlew is +x on Linux
            self.run(f'"{self.gradle_cmd}" :runelite-api:assemble', WORK_DIR, env)
            self.run(f'"{self.gradle_cmd}" :client:assemble -x javadoc', WORK_DIR, env)

    def execute(self):
        self.log("=== RuneLite Binary Factory ===")

        # 1. FETCH VERSION & WRITE TO TXT FILE
        self.get_latest_version()

        if not os.path.exists(WORK_DIR):
            os.makedirs(WORK_DIR)
            self.run(f"git clone {self.repo_url} .", WORK_DIR, os.environ.copy())

        env = self.setup_env()
        self.run("git fetch --all", WORK_DIR, env)
        
        # 2. USE DYNAMIC TAG
        self.run(f"git checkout {self.latest_tag} -f", WORK_DIR, env)
        
        self.run("git reset --hard", WORK_DIR, env)
        self.run("git clean -fd", WORK_DIR, env)

        self.prune_plugins()
        self.build_project(env)

        # Install Artifacts
        api_dir_gradle = os.path.join(WORK_DIR, "runelite-api", "build", "libs")
        api_dir_maven = os.path.join(WORK_DIR, "runelite-api", "target")
        api_lib = api_dir_maven if os.path.exists(api_dir_maven) else api_dir_gradle
        
        api_jar = self.find_jar(api_lib, prefix="runelite-api")
        if not api_jar: raise RuntimeError(f"runelite-api JAR not found")
        self.install_jar(api_jar, "runelite-api", env)

        client_dir_gradle = os.path.join(WORK_DIR, "runelite-client", "build", "libs")
        client_dir_maven = os.path.join(WORK_DIR, "runelite-client", "target")
        client_lib = client_dir_maven if os.path.exists(client_dir_maven) else client_dir_gradle
        
        client_jar = self.find_jar(client_lib)
        if not client_jar: raise RuntimeError(f"client JAR not found")
        self.install_jar(client_jar, "client", env)

        self.log(f"SUCCESS: Built and Installed Version {self.latest_version}")

if __name__ == "__main__":
    try:
        RuneLiteBootstrapper().execute()
        # On Linux servers, input() hangs automation. Only pause if interactive.
        if sys.stdin.isatty():
            input("Bootstrap Complete. Press Enter to exit...")
    except Exception as e:
        print(f"CRITICAL FAILURE: {e}")
        # Only pause on error if interactive
        import sys
        if sys.stdin.isatty():
             input("Error occurred. Press Enter to exit...")
