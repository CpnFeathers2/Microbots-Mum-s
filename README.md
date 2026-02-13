Follow the youtube video on my channel on how to setup your runelite maven project (similar as before just uses Microbot's mum and not Microbot).

This project comes with some plugins:
1. An autolooter
2. Screenshot programme (takes 1 screenshot per second (this is video recording of your bot))
3. BoL (Highly restricted QoL)
4. Breakhandler before it got corrupted
5. Discord (I just leave this in out of fear my integrated webhooks wont trigger)
6. Highly educative ExampleScript and ExamplePlugin - if you need the "basic" one just get it from another repository on my profile I made for it.
7. Player Monitor (with LiteMode)
8. Rs2walker (ShortestPath) - #tip ShortestPathPlugin.walkViaPanel() is a great method if you ever get stuck or script walkTo commands don't operate reliably. 

Please PR to improve and add plugins for developer role. Discord link available on youtube channel.

This is the full setup (will go through it on YouTube video for beginners):
## 🛠️ Quick Setup
1. **Clone the repo.**
2. **Create a `config.json`** in the root directory (refer to `config.example.json`).
3. **Define your paths** (Drive letters, Maven location, etc.) in the JSON file.
4. **Run the scripts.** Your personal paths will be ignored by Git.

This project contains a specialized bootstrapper designed to fetch, prune, and locally install the RuneLite source code. It ensures your environment stays synchronized with the latest stable upstream releases while stripping out unnecessary plugins to keep your build lean.

The Python script `bootstrap_runelite.py` automates the heavy lifting of environment setup. It handles everything from GitHub API version checks to Maven/Gradle local installation.

Before running the client for the first time, you **must** update the following paths in the `config.json` in the root of the project (with the readME you're reading now).

### 🛠️ What the Python Script Does
1. **Dynamic Version Tracking**: Queries the GitHub API to find the latest `runelite-parent` tag (excluding SNAPSHOTS).
2. **Automated Pruning**: Deletes all plugin source code **except** for the essential "Keep List" (e.g., `config`, `worldmap`, `fps`).
3. **Source Sanitization**: Rewrites `RuneLiteModule.java` on the fly to prevent compilation errors from deleted plugins.
4. **Local Artifact Injection**: Builds the project and installs `runelite-api` and `runelite-client` into your local `.m2` repository under the version `LOCAL-GRADLE`.

### 💻 Usage
1. Open `bootstrap_runelite.py`.
2. Edit the `WORK_DIR` and `VERSION_FILE_PATH`.
3. Run the script:
   ```powershell
   python bootstrap_runelite.py

MY YOUTUBE CHANNEL:  
https://www.youtube.com/@davidgatward4395
