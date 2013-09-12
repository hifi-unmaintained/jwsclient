/*
 * Copyright (c) 2013 Toni Spets <toni.spets@iki.fi>
 * 
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package fi.iki.hifi.jwsclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Toni Spets <toni.spets@iki.fi>
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            JOptionPane.showMessageDialog(null, "No JNLP file specified.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LoadingWindow lw = new LoadingWindow();
        Cache cache = new Cache();
        JNLP jnlp = null;
        URL jnlpURL = null;
        URL splashURL = null;

        try {
            jnlpURL = new URL(args[0]);
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lw.setImage(new ImageIcon(Main.class.getResource("res/splash.png")));

        try {
            // get local path for jnlp file
            File jnlpFile = cache.fromURL(jnlpURL);

            // show loading window immediately if needed
            if (!jnlpFile.exists())
                cache.setWindow(lw);

            // make sure we have a jnlp file
            cache.download(jnlpURL);

            // try to parse it, if parsing fails redownload to be double sure
            try {
                jnlp = new JNLP(jnlpFile);
            } catch(IOException e) {
                jnlpFile = cache.download(jnlpURL, true);
                jnlp = new JNLP(jnlpFile);
            }

            // second, download our real splash image so we can replace the temp one
            splashURL = jnlp.getSplash();
            if (splashURL != null) {
                try {
                    lw.setImage(new ImageIcon(ImageIO.read(cache.download(splashURL))));
                } catch (Exception e) { }
            }
            cache.setWindow(lw);

            // finally, download all jar files
            List<URL> jarList = jnlp.getJarList();
            URL[] localJars = new URL[jarList.size()];
            int i = 0;
            for (URL jar : jarList) {
                cache.download(jar);
                localJars[i++] = cache.fromURL(jar).toURI().toURL();
            }

            Class client = new URLClassLoader(localJars).loadClass(jnlp.getMainClass());
            Method mainMethod = client.getMethod("main", (new String[1]).getClass());

            // prevent download window when doing background dl
            cache.setWindow(null);

            lw.setVisible(false);
            lw.dispose();
            lw = null;

            System.out.println("Invoking main() on " + client.getName());
            mainMethod.invoke(null, new Object[]{ jnlp.getMainArguments() });

            // shortcut creation using .url files on Windows
            File jarFile = new File(URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));

            System.out.println("Checking if we are not running from cache, wrapped and shortcut exists...");
            if (!jarFile.toString().startsWith(cache.getRoot()) && jarFile.toString().endsWith(".exe")) {
                String operatingSystem = System.getProperty("os.name");

                if (operatingSystem.startsWith("Windows")) {
                    File desktopFile = FileSystemView.getFileSystemView().getHomeDirectory();
                    File shortcutFile = new File(desktopFile + File.separator + jnlp.getTitle() + ".url");

                    if (!shortcutFile.exists()) {

                        File cacheFile = cache.fromFile(jarFile);
                        if (!cacheFile.exists()) {
                            cache.copy(jarFile);
                        }

                        System.out.println("Creating shortcut to " + cacheFile);

                        FileWriter fw = new FileWriter(shortcutFile);
                        fw.append("[InternetShortcut]\r\n");
                        fw.append("URL=" + cacheFile.toURI() + "\r\n");
                        fw.append("WorkingDirectory=" + cacheFile.getParent() + "\r\n");
                        fw.append("IconIndex=0\r\n");
                        fw.append("IconFile=" + cacheFile + "\r\n");
                        fw.close();
                    }
                }
            }

            // after main returns, do our background updating
            System.out.println("Doing background update...");

            try {
                cache.download(jnlpURL, true);
                if (splashURL != null)
                    cache.download(splashURL, true);

                for (URL jar : jarList)
                    cache.download(jar, true);

                System.out.println("Done! Quitting.");
            } catch (IOException e) {
                System.out.println("Background update failed: " + e.getMessage());
            }


        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (lw != null) {
            lw.setVisible(false);
            lw.dispose();
        }
    }
}
