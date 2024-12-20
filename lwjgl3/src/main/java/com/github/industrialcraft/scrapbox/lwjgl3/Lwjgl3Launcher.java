package com.github.industrialcraft.scrapbox.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.industrialcraft.scrapbox.client.ScrapBox;
import com.github.industrialcraft.scrapbox.client.SoundStateChecker;
import com.github.industrialcraft.scrapbox.server.SaveFile;
import com.github.industrialcraft.scrapbox.server.Server;
import org.lwjgl.openal.AL10;

import java.io.*;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) throws IOException {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.

        if((args.length >= 2 && args.length <= 4) && args[0].equals("host")){
            File saveFile = null;
            if(args.length > 2){
                saveFile = new File(args[2]);
            }
            Server server = new Server(saveFile);
            int port = Integer.parseInt(args[1]);
            server.startNetwork(port);
            if(args.length > 3){
                server.password = args[3];
            }
            if(saveFile != null) {
                try {
                    server.loadSaveFile(new SaveFile(new DataInputStream(new BufferedInputStream(new FileInputStream(saveFile)))));
                } catch(FileNotFoundException e){
                    System.out.println("couldn't load savefile");
                }
            }
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } else {
            createApplication();
        }
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new ScrapBox(new SoundStateChecker() {
            @Override
            public boolean isPlaying(int id) {
                return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
            }
        }), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("ScrapBox");
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        configuration.setWindowedMode(640, 480);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
