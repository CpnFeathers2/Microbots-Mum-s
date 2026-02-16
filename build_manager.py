import tkinter as tk
from tkinter import messagebox, scrolledtext
import subprocess
import threading
import os
import shutil
import time
from datetime import datetime
import json
import platform
import sys
import argparse

# =============================================================================
# === CONFIGURATION & OS DETECTION ===
# =============================================================================

DEFAULT_CONFIG = {
    "nvme_drive_letter": "H",
    "maven_executable": "",
    "min_artifact_size_bytes": 57671680,
    "skip_tests": False
}

def load_config():
    config = DEFAULT_CONFIG.copy()

    if os.path.exists("config.json"):
        try:
            with open("config.json", "r") as f:
                config.update(json.load(f))
        except Exception as e:
            print(f"Error loading config.json: {e}")

    elif os.path.exists("config.example.json"):
        try:
            # We don't load example config by default to avoid overwriting defaults with placeholders
            pass
        except:
            pass

    return config

CONFIG = load_config()

IS_WINDOWS = platform.system() == "Windows"

# 1. Determine Maven Executable
MAVEN_EXECUTABLE = CONFIG.get("maven_executable", "")

if not MAVEN_EXECUTABLE:
    if IS_WINDOWS:
        common_paths = [
            r"C:\Program Files\apache-maven-3.9.11\bin\mvn.cmd",
            r"C:\Program Files\Maven\bin\mvn.cmd"
        ]
        found = False
        for p in common_paths:
            if os.path.exists(p):
                MAVEN_EXECUTABLE = p
                found = True
                break

        if not found:
            MAVEN_EXECUTABLE = "mvn.cmd"
    else:
        if shutil.which("mvn"):
            MAVEN_EXECUTABLE = "mvn"
        elif os.path.exists("/usr/bin/mvn"):
            MAVEN_EXECUTABLE = "/usr/bin/mvn"
        else:
            MAVEN_EXECUTABLE = "mvn"

# 2. Determine Build Root
if IS_WINDOWS:
    drive_letter = CONFIG.get("nvme_drive_letter", "H")
    NVME_ROOT = f"{drive_letter}:\\Automation_Empire"
else:
    NVME_ROOT = os.path.join(os.path.expanduser("~"), "Automation_Empire")

# 3. Artifact Size
MIN_ARTIFACT_SIZE_BYTES = CONFIG.get("min_artifact_size_bytes", 57671680)

# 4. Max Retries
MAX_RETRIES = 3

# =============================================================================

class NvmeAutomationEmpire:
    def __init__(self, root=None, headless=False, branches_to_build=None):
        self.root = root
        self.headless = headless
        self.branches_to_build = branches_to_build or []
        self.source_path = os.getcwd()
        
        # --- READABLE PATH GENERATION ---
        current_date = datetime.now().strftime("%Y-%m-%d")
        current_time = datetime.now().strftime("%H-%M-%S")
        
        self.nvme_root = NVME_ROOT
        
        self.backup_dest_parent = os.path.join(self.nvme_root, "Backups", current_date)
        self.backup_dest = os.path.join(self.backup_dest_parent, f"Snap_{current_time}")
        
        self.artifacts_dest_parent = os.path.join(self.nvme_root, "Builds", current_date)
        self.artifacts_dest = os.path.join(self.artifacts_dest_parent, f"Builds_{current_time}")
        # ------------------------------------

        if not self.headless:
            self.setup_gui()
        else:
            print(f"Build Manager initialized in HEADLESS mode.")
            print(f"Source: {self.source_path}")
            print(f"Target: {self.backup_dest_parent}")
            print(f"Maven: {MAVEN_EXECUTABLE}")
            self.run_process_headless()

    def setup_gui(self):
        self.root.title("Automation Empire: Cross-Platform Build Manager")
        self.root.geometry("750x700")

        # --- UI HEADER ---
        tk.Label(self.root, text="Build Commander", font=("Segoe UI", 16, "bold")).pack(pady=(15, 5))
        
        info_frame = tk.Frame(self.root)
        info_frame.pack(pady=5)
        tk.Label(info_frame, text=f"Source: {self.source_path}", fg="gray").pack()
        tk.Label(info_frame, text=f"Target: {self.backup_dest_parent}", fg="blue").pack()
        tk.Label(info_frame, text=f"Maven: {MAVEN_EXECUTABLE}", fg="green").pack()

        # --- OPTIONS ---
        opts_frame = tk.Frame(self.root)
        opts_frame.pack(pady=5)
        
        default_skip = CONFIG.get("skip_tests", False)
        self.var_skip_tests = tk.BooleanVar(value=default_skip)
        tk.Checkbutton(opts_frame, text="Skip Tests (-DskipTests)", variable=self.var_skip_tests).pack(side="left", padx=10)

        # --- BRANCH LIST ---
        tk.Label(self.root, text="Select Branches:", font=("Segoe UI", 10, "bold")).pack(anchor="w", padx=20, pady=(10,0))

        self.canvas = tk.Canvas(self.root, borderwidth=0, background="#ffffff")
        self.frame = tk.Frame(self.canvas, background="#ffffff")
        self.vsb = tk.Scrollbar(self.root, orient="vertical", command=self.canvas.yview)
        self.canvas.configure(yscrollcommand=self.vsb.set)

        self.vsb.pack(side="right", fill="y")
        self.canvas.pack(side="top", fill="both", expand=True, padx=20, pady=5)
        self.canvas.create_window((4,4), window=self.frame, anchor="nw")

        self.frame.bind("<Configure>", lambda event, canvas=self.canvas: canvas.configure(scrollregion=canvas.bbox("all")))

        # Load Branches
        self.checkbox_vars = {}
        branches = self.get_git_branches()
        for branch in branches:
            var = tk.BooleanVar()
            chk = tk.Checkbutton(self.frame, text=branch, variable=var, bg="#ffffff", anchor='w')
            chk.pack(fill='x', expand=True)
            self.checkbox_vars[branch] = var

        # --- CONTROLS ---
        btn_frame = tk.Frame(self.root)
        btn_frame.pack(pady=10)
        
        tk.Button(btn_frame, text="Select All", command=self.select_all).pack(side="left", padx=10)
        self.btn_run = tk.Button(btn_frame, text="ENGAGE AUTOMATION", bg="#007acc", fg="white", font=("Segoe UI", 10, "bold"), command=self.start_thread)
        self.btn_run.pack(side="left", padx=10)

        # --- LOG ---
        self.log_text = scrolledtext.ScrolledText(self.root, height=15, state='disabled', bg="#2b2b2b", fg="#00ff00", font=("Consolas", 9))
        self.log_text.pack(fill="x", padx=20, pady=(0, 20))

    def log(self, message):
        if self.headless:
            print(f"[{datetime.now().strftime('%H:%M:%S')}] {message}")
        else:
            # Ensure log updates happen on main thread
            self.log_text.after(0, self._log_internal, message)

    def _log_internal(self, message):
        self.log_text.config(state='normal')
        self.log_text.insert(tk.END, f"[{datetime.now().strftime('%H:%M:%S')}] {message}\n")
        self.log_text.see(tk.END)
        self.log_text.config(state='disabled')

    def get_git_branches(self):
        try:
            res = subprocess.run(["git", "branch", "--format=%(refname:short)"], capture_output=True, text=True)
            return [b.strip() for b in res.stdout.split('\n') if b.strip()]
        except:
            return ["Error: Not a git repo"]

    def select_all(self):
        for var in self.checkbox_vars.values():
            var.set(True)

    def start_thread(self):
        threading.Thread(target=self.run_process_gui).start()

    def run_process_gui(self):
        selected = [b for b, var in self.checkbox_vars.items() if var.get()]
        if not selected:
            messagebox.showwarning("Wait", "No branches selected!")
            return

        self.btn_run.config(state="disabled", text="Processing...")
        self.process_logic(selected)
        self.btn_run.config(state="normal", text="ENGAGE AUTOMATION")

    def run_process_headless(self):
        if not self.branches_to_build:
            self.log("No branches specified for headless build.")
            return

        # Handle 'all' keyword if passed literally or via logic
        if 'all' in self.branches_to_build or (len(self.branches_to_build) == 1 and self.branches_to_build[0] == 'all'):
             self.branches_to_build = self.get_git_branches()

        self.process_logic(self.branches_to_build)

    def process_logic(self, branches):
        # 1. Setup Folders
        try:
            if not os.path.exists(self.artifacts_dest): os.makedirs(self.artifacts_dest)
            if not os.path.exists(self.backup_dest): os.makedirs(self.backup_dest)
        except Exception as e:
            self.log(f"Error creating folders: {e}")
            return

        # 2. Clone
        self.log("Phase 1: Cloning to Build Root...")
        self.log(f"Target Backup Folder: {self.backup_dest}")
        
        clone_res = subprocess.run(["git", "clone", self.source_path, self.backup_dest], capture_output=True, text=True)
        
        if clone_res.returncode != 0:
            self.log("CRITICAL: Clone Failed.")
            self.log(f"GIT ERROR: {clone_res.stderr}") 
            return
        self.log("Clone Complete.")

        # DIAGNOSTIC: Check if pom.xml exists
        pom_path = os.path.join(self.backup_dest, "pom.xml")
        if not os.path.exists(pom_path):
            self.log("CRITICAL ERROR: pom.xml not found in backup folder!")
            self.log(f"Looking for: {pom_path}")
            return

        # 3. Build Loop with Retry
        base_mvn = f'"{MAVEN_EXECUTABLE}" clean install'

        skip_tests = False
        if not self.headless:
            skip_tests = self.var_skip_tests.get()
        else:
            skip_tests = CONFIG.get("skip_tests", False)

        if skip_tests:
            base_mvn += " -DskipTests"

        self.log(f"Phase 2: Building {len(branches)} branches...")
        
        for branch in branches:
            self.log(f"--> Target: {branch}")
            
            # Checkout
            checkout_res = subprocess.run(["git", "checkout", branch], cwd=self.backup_dest, capture_output=True, text=True)
            if checkout_res.returncode != 0:
                self.log(f"    Could not switch branch: {checkout_res.stderr}")
                continue
            
            # RETRY LOOP
            success = False
            for attempt in range(1, MAX_RETRIES + 1):
                # Use shell=True for Windows command execution and complex strings on Linux
                # On Linux, base_mvn is a string, so shell=True executes it as a shell command.
                build_res = subprocess.run(base_mvn, cwd=self.backup_dest, shell=True, capture_output=True, text=True)
                
                if build_res.returncode == 0:
                    success = True
                    self.log(f"    SUCCESS.")
                    break
                else:
                    if attempt == 1:
                        err_msg = build_res.stderr
                        if not err_msg: err_msg = build_res.stdout
                        
                        self.log(f"    FAIL REASON (Snippet): {err_msg[-300:]}")
                        if "is not recognized" in err_msg or "command not found" in err_msg:
                            self.log(f"    !! ACTION REQUIRED: Python cannot find '{MAVEN_EXECUTABLE}'. Check config.json.")
                    
                    if attempt < MAX_RETRIES:
                        time.sleep(2) 
            
            if success:
                # Move Artifact - FIND LARGEST JAR OVER SIZE THRESHOLD
                target_root = os.path.join(self.backup_dest) # Start search from the repo root
                found_artifact = None
                max_size = 0
                
                # os.walk traverses directory tree
                for root_dir, _, files in os.walk(target_root):
                    # Skip searching standard hidden/dependency folders
                    if 'target' in root_dir and 'classes' not in root_dir and 'test-classes' not in root_dir:
                        for f in files:
                            f_lower = f.lower()
                            
                            if f_lower.endswith(".jar") or f_lower.endswith(".war"):
                                artifact_path = os.path.join(root_dir, f)
                                current_size = os.path.getsize(artifact_path)
                                
                                # Check if size is over the minimum threshold AND larger than the largest found so far
                                if current_size >= MIN_ARTIFACT_SIZE_BYTES and current_size > max_size:
                                    max_size = current_size
                                    found_artifact = artifact_path
                
                if found_artifact:
                    # Use the original filename
                    artifact_filename = os.path.basename(found_artifact)
                    new_name = artifact_filename
                    
                    shutil.copy2(found_artifact, os.path.join(self.artifacts_dest, new_name))
                    self.log(f"    Artifact copied: {new_name} ({round(max_size / 1048576, 2)} MB)")
                else:
                    self.log(f"    Warning: Build passed, but no JAR/WAR artifact over {round(MIN_ARTIFACT_SIZE_BYTES / 1048576, 2)} MB found.")
            else:
                self.log(f"    ERROR: {branch} failed.")

        self.log("Phase 3: Operations Complete.")
        if not self.headless:
            messagebox.showinfo("Empire Notification", f"Process Complete.\n\nSaved to:\n{self.artifacts_dest}")

def main():
    parser = argparse.ArgumentParser(description="Automation Empire Build Manager")
    parser.add_argument("--headless", action="store_true", help="Run in headless mode (no GUI)")
    parser.add_argument("--branch", action="append", help="Branch to build (can be specified multiple times)")
    parser.add_argument("--all", action="store_true", help="Build all branches")
    args = parser.parse_args()

    # Detect if we have a display
    has_display = True
    try:
        # Check for DISPLAY env var on Linux/Unix
        if os.name != 'nt' and not os.environ.get('DISPLAY'):
            has_display = False
    except:
        has_display = False

    # Force headless if arg is present OR no display found
    is_headless = args.headless or not has_display

    branches = []
    if args.all:
        branches = ['all']
    elif args.branch:
        branches = args.branch

    # If headless and no branches specified, try to default to something sensible or error out?
    # For now, let's default to 'all' if running headless and nothing specified,
    # to mimic "automating everything".
    if is_headless and not branches:
        print("Headless mode detected and no branches specified. Defaulting to building ALL branches.")
        branches = ['all']

    if is_headless:
        NvmeAutomationEmpire(headless=True, branches_to_build=branches)
    else:
        try:
            root = tk.Tk()
            app = NvmeAutomationEmpire(root, headless=False)
            root.mainloop()
        except Exception as e:
            print(f"Error initializing GUI: {e}")
            print("Falling back to headless mode...")
            NvmeAutomationEmpire(headless=True, branches_to_build=branches if branches else ['all'])

if __name__ == "__main__":
    main()
