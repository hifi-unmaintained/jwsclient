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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Toni Spets <toni.spets@iki.fi>
 */
final public class JNLP {

    private Document doc;

    private Node information;
    private Node resources;
    private Node applicationDesc;

    public JNLP(File file) throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch (Exception e) {
            throw new IOException(e);
        }

        NodeList nl = doc.getDocumentElement().getChildNodes();
        Node node;
        int i = 0;
        while ((node = nl.item(i++)) != null) {
            String tag = node.getNodeName().toLowerCase();
            if (tag.equalsIgnoreCase("information")) {
                information = node;
            } else if (tag.equalsIgnoreCase("resources")) {
                resources = node;
            } else if (tag.equalsIgnoreCase("application-desc")) {
                applicationDesc = node;
            }
        }
    }

    public String getCodebase() {
        NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
        Node attr = attributes.getNamedItem("codebase");
        return attr != null ? attr.getNodeValue() : null;
    }

    public URL getSplash() {
        String codebase = getCodebase();
        NodeList nl = information.getChildNodes();
        Node node;
        int i = 0;
        while ((node = nl.item(i++)) != null) {
            String tag = node.getNodeName().toLowerCase();
            if (tag.equalsIgnoreCase("icon")) {
                NamedNodeMap attributes = node.getAttributes();
                Node kind = attributes.getNamedItem("kind");
                Node href = attributes.getNamedItem("href");
                if (kind != null && kind.getNodeValue().equalsIgnoreCase("splash") && href != null) {
                    try {
                        return new URL(codebase + (codebase.endsWith("/") ? "" : "/") + href.getNodeValue());
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    public String getTitle() {
        NodeList nl = information.getChildNodes();
        Node node;
        int i = 0;
        while ((node = nl.item(i++)) != null) {
            String tag = node.getNodeName().toLowerCase();
            if (tag.equalsIgnoreCase("title")) {
                return node.getFirstChild().getNodeValue();
            }
        }

        return null;
    }

    public String getMainClass() {
        NamedNodeMap attributes = applicationDesc.getAttributes();
        Node attr = attributes.getNamedItem("main-class");
        return attr != null ? attr.getNodeValue() : null;
    }

    public String[] getMainArguments() {
        NodeList nl = applicationDesc.getChildNodes();
        Node node;
        int i = 0;
        List<String> argumentList = new ArrayList<String>();

        while ((node = nl.item(i++)) != null) {
            String tag = node.getNodeName().toLowerCase();
            if (tag.equalsIgnoreCase("argument")) {
                Node textChild = node.getFirstChild();
                if (textChild != null)
                    argumentList.add(textChild.getNodeValue());
            }
        }

        return argumentList.toArray(new String[argumentList.size()]);
    }

    public List<URL> getJarList() {
        String codebase = getCodebase();
        NodeList nl = resources.getChildNodes();
        Node node;
        int i = 0;
        List<URL> jarList = new ArrayList<URL>();

        while ((node = nl.item(i++)) != null) {
            String tag = node.getNodeName().toLowerCase();
            if (tag.equalsIgnoreCase("jar")) {
                NamedNodeMap attributes = node.getAttributes();
                Node attr = attributes.getNamedItem("href");
                try {
                    URL jar = new URL(codebase + (codebase.endsWith("/") ? "" : "/") + attr.getNodeValue());
                    jarList.add(jar);
                } catch (MalformedURLException e) {
                    // ignore?
                }
            }
        }

        return jarList;
    }

    public URL getMainJar() {
        String codebase = getCodebase();
        NodeList nl = resources.getChildNodes();
        Node node;
        int i = 0;
        URL mainJar = null;

        while ((node = nl.item(i++)) != null) {
            String tag = node.getNodeName().toLowerCase();
            if (tag.equalsIgnoreCase("jar")) {
                NamedNodeMap attributes = node.getAttributes();
                Node attr = attributes.getNamedItem("href");
                Node main = attributes.getNamedItem("main");
                if (mainJar == null || (main != null && !main.getNodeValue().equalsIgnoreCase("true"))) {
                    try {
                        mainJar = new URL(codebase + (codebase.endsWith("/") ? "" : "/") + attr.getNodeValue());
                    } catch (MalformedURLException e) {
                        //
                    }
                }
            }
        }

        return mainJar;
    }
}
