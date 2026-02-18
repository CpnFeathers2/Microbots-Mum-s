TO USE THIS PROJECT: 
1. GO HERE: https://download-directory.github.io/
2. Then paste in the official full directory URL (bottom of the README.md) and what this does is the same as doing git clone. So then you just click open project instead of clone repository.

PLEASE USE apache-maven-3.9.11 (system-wide, and when building make sure you make it 4gb capacity MAVEN_OPTS="-Xmx4g" - refer to links at bottom of README-md)

This project comes with some plugins:
1. Redacted from Microbot Project Moss Killer plugin. 
1. An autolooter
2. Screenshot programme (takes 1 screenshot per second of game window only (this is video recording of your bot))
3. BoL (Highly restricted QoL)
4. Breakhandler before it got corrupted with pauseallscripts changes
5. Discord (I just leave this in out of fear my integrated webhooks wont trigger)
6. Highly educative ExampleScript and ExamplePlugin - if you need the "basic" one as a beginner coder just get it from another repository link from my profile at the bottom of the README.md
7. Player Monitor (with LiteMode)
8. Rs2walker (ShortestPath) - #tip ShortestPathPlugin.walkViaPanel() is a great method if you ever get stuck or script walkTo commands don't operate reliably. 

Please PR to improve and add plugins for developer role. Discord link available on youtube channel and i'll put a video up soon on how to get Microbot's Mum up and running (the old gal). 

----------------------------------------- BELOW HERE IS ONLY FOR THE ABSOLUTE GIGACHADS TO BOOT HER UP AND IT DOES NOT SHIP WIH DEPENDENCIES -----------------------------------------------

This is the full setup (will go through it on YouTube video for beginners just enjoy the AI slop below that will half get you there):
## 🛠️ Quick Setup
1. **Clone the repo.**
2. **Create a `config.json`** in the root directory (refer to `config.example.json`).
3. **Define your paths** (Drive letters, Maven location, etc.) in the JSON file.
4. **Run the scripts.** Your personal paths will be ignored by Git.

This project contains a specialized bootstrapper designed to fetch, prune, and locally install the RuneLite source code. It ensures your environment stays synchronized with the latest stable upstream releases while stripping out unnecessary plugins to keep your build lean. Please install maven 11.9

The Python script `bootstrap_runelite.py` automates the heavy lifting of environment setup. It handles everything from GitHub API version checks to Maven/Gradle local installation.

Before running the client for the first time, you **must** update the following paths in the `config.json` in the root of the project (with the readME you're reading now).

### 🛠️ What the Python Script Does
1. **Dynamic Version Tracking**: Queries the GitHub API to find the latest `runelite-parent` tag (excluding SNAPSHOTS).
2. **Automated Pruning**: Deletes all plugin source code **except** for the essential "Keep List" (e.g., `config`, `worldmap`, `fps`).
3. **Source Sanitization**: Rewrites `RuneLiteModule.java` on the fly to prevent compilation errors from deleted plugins.
4. **Local Artifact Injection**: Builds the project and installs `runelite-api` and `runelite-client` into your local `.m2` repository under the version `LOCAL-GRADLE`.

This then means that the runelite-api and runelite-client is external to your maven build, and is processed through gradle commands. This allows for accurate (and possible) api calls. 
However, there is an older version of the runelite-api that ships with the build. Simply Legacy api from pre-gradle. Will likely slowly fade from existence but it has cool modules like Cuboid. 

### 💻 Usage
1. Open `bootstrap_runelite.py`.
2. Edit the `WORK_DIR` and `VERSION_FILE_PATH`.
3. Run the script:
   ```powershell
   python bootstrap_runelite.py

Anyway, once you've set it all up, your m2 got your api, your project is cloned/opened in intelliJ, you can do a normal maven clean install to get your jar, or you can go to your Microbot root project folder and use the python script build_manager. 
This will allow an automatic, background jar build, showing a gui of which branch you would like to build AND making a clone of your current entire setup. So works as a free backup and also updates the pom to the version the bootstrap_runelite.py created in a .txt file. 

MY YOUTUBE CHANNEL:  
https://www.youtube.com/@davidgatward4395

THE "FRESH" EXAMPLE PLUGIN: 
https://github.com/FireStarter-Instigator/example-and-vanilla-plugins

THE OFFICIAL GITHUB LINK TO THE REPO (Just like Microbot's or Runelite's)
https://github.com/FireStarter-Instigator/Microbots-Mum

THIS WAS A HALLUCINATION OF THE AI SO I MADE IT:
https://github.com/FireStarter-Instigator/Microbots-Mum/commit/d96eff5b4f4eed96b2d30f8b4716fc3985789c37
IT DEMONSTRATES THAT apache-maven-3.9.11 (link to download https://maven.apache.org/docs/history.html) IS A COMPATIBLE MAVEN VERSION FOR THE BUILD AND FEATURES. 


