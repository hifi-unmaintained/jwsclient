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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Toni Spets <toni.spets@iki.fi>
 */
public class Cache {
    
    private LoadingWindow lw;
    private String root;
    private Map<URL, Boolean> cached;
    
    public Cache() {
        String systemName = System.getProperty("os.name");
        cached = new HashMap<URL, Boolean>();

        // get a proper path to directory where to save data
        if (systemName.startsWith("Windows")) {
            root = System.getenv("LOCALAPPDATA");
            if (root == null || root.length() == 0) {
                root = System.getenv("APPDATA");
            }
        }

        if (root == null || root.length() == 0) {
            root = System.getProperty("user.home");
        }

        root += File.separator + ".jwscache";

        // try to create our path if it doesn't exist
        File rootFile = new File(root);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
            if (!rootFile.isDirectory()) {
                throw new Error("Cache failed to initialize.");
            }
        } else if (rootFile.exists() && !rootFile.isDirectory()) {
            throw new Error("Cache failed to initialize.");
        }

        System.out.println("Cache initialized at " + root);
    }

    public File fromURL(URL url) {
        StringBuilder path = new StringBuilder();

        path.append(root);
        path.append(File.separator);
        path.append(url.getProtocol());
        path.append(File.separator);
        path.append(url.getHost());

        if (url.getPort() > 0) {
            path.append(":");
            path.append(url.getPort());
        }

        for (String part : url.getPath().split("/")) {
            if (part.length() == 0)
                continue;
            path.append(File.separator);
            path.append(part);
        }

        File localFile = new File(path.toString());
        File localPath = new File(localFile.getParent());

        if (!localPath.exists() && !localPath.mkdirs())
                throw new Error("Failed to create cache path: " + localFile.getParent());

        return localFile;
    }

    public void setWindow(LoadingWindow lw) {
        this.lw = lw;
    }

    public File download(URL url) throws IOException {
        return download(url, false);
    }

    public File download(URL url, boolean force) throws IOException {
        File dst = fromURL(url);
        File newDst = new File(dst.toString() + ".new");

        // no force downloading, will only download if local file is missing
        if (!force) {

            // use .new files automatically, this mechanism is to prevent locking
            if (newDst.exists()) {
                if (dst.exists())
                    dst.delete();
                newDst.renameTo(dst);
            }

            if (dst.exists())
                return dst;
        }

        // force downloading, make sure the file is up-to-date
        if (force && cached.containsKey(url))
            return dst;

        cached.put(url, true);

        if (lw != null) {
            lw.setStatus("Loading " + dst.getName() + "...");
            lw.setProgress(0);
            lw.enableProgress(false);
            lw.setVisible(true);
        }

        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();

        if (dst.exists() && dst.lastModified() > 0) {
            con.addRequestProperty("If-Modified-Since", df.format(new Date(dst.lastModified())));
        }

        con.connect();

        if (con.getResponseCode() == 304) {
            if (lw != null)
                lw.setStatus(null);
            return dst;
        }

        if (con.getResponseCode() != 200) {
            throw new IOException(con.getResponseCode() + " " + con.getResponseMessage());
        }

        if (lw != null) {
            lw.setProgress(0);
            lw.enableProgress(true);
        }

        InputStream in = new BufferedInputStream(con.getInputStream());
        File tmp = new File(dst.toString() + ".part");
        OutputStream out = new FileOutputStream(tmp);

        int length = -1;
        String strLen = con.getHeaderField("Content-Length");
        if (strLen != null) {
            length = Integer.parseInt(strLen);
        }

        byte[] buf = new byte[4096];

        int i, pos = 0;
        while ((i = in.read(buf)) >= 0) {
            out.write(buf, 0, i);
            pos += i;

            if (lw != null)
                lw.setProgress((int)(((double)pos / length) * 1000));
        }

        out.close();
        in.close();

        try {
            tmp.setLastModified(df.parse(con.getHeaderField("Last-Modified")).getTime());
        } catch (Exception e) {
            // Last-Modified might be missing, then use current time
            try {
                tmp.setLastModified(df.parse(con.getHeaderField("Date")).getTime());
            } catch (Exception e2) {
                // use whatever local time we have then
            }
        }

        // use new files if not forcing, background update would possibly lock or crash class loader
        if (!force) {
            if (dst.exists())
                dst.delete();

            tmp.renameTo(dst);
        } else {
            if (newDst.exists())
                newDst.delete();

            tmp.renameTo(newDst);
        }

        if (lw != null) {
            lw.setStatus(null);
            lw.enableProgress(false);
        }

        return dst;
    }

}
