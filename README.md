# Unscrew
Unscrew is a wrapper for the Spigot server that allows plugin developers to modify the server bytecode using the ASM library. This is the old version of Unscrew, that loads core plugin transformers using Mojang's LaunchWrapper.

The New Unscrew
---------------
You can find the new Unscrew at https://github.com/Hallowizer/Unscrew.

Compiling
---------
Feel free to compile UnscrewLegacy if you want to use it. You will need Lombok to compile this, which shouldn't be a problem if you are using the maven compiler. You will need to include the LaunchWrpper in the classpath, as well as the Spigot jar. Run Unscrew with the main class com.hallowizer.unscrew.coremod.main.UnscrewLauncher.
