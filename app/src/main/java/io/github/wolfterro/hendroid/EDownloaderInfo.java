package io.github.wolfterro.hendroid;

import android.util.Log;

import java.io.*;
import java.util.*;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class EDownloaderInfo {
    // Private properties
    // =====================

    // Incoming properties
    // ----------------------
    private String albumURL = "";

    // Properties sent or stored
    // ----------------------------------
    private Document doc = null;

    private Elements links = null;

    private List<String> imagePages = new ArrayList<String>();
    private List<String> albumPages = new ArrayList<String>();

    private String TAG = "EDownloaderInfo.java";

    // The properties below may return
    // ---------------------------------------
    private ArrayList<String> imageLinks = new ArrayList<String>();

    private String albumName = "";
    private String albumLanguage = "";
    private String uploadDate = "";
    private String uploader = "";
    private String fileSize = "";
    private String error = "";

    private int pages = 0;

    private boolean isTranslated = false;
    private boolean isOk = false;

    // Class Builder
    // ====================
    public EDownloaderInfo(String albumURL) {
        this.albumURL = albumURL;
    }

    // ================
    // Private methods
    // ================

    // Retrieving Album Information
    // -------------------------------------
    private boolean pGetInfo() {
        if(!checkURLDomain()) {
            error = "INVALID_URL_ERROR";
            Log.e(TAG, error);
            return false;
        }

        try {
            doc = Jsoup.connect(albumURL).cookie("nw", "1").get();
            albumName = String.format("%s", doc.title()).replace(" - E-Hentai Galleries", "");

            pGetAlbumLanguage();
            pGetUploader();
            pGetUploadDate();
            pGetFileSize();
            pGetAlbumPages();
            pGetImagePages();
            pGetImageLinks();

            pages = imageLinks.size();

            if(pages == 0 || albumName.equals("")) {
                error = "NO_ALBUM_FOUND";
                Log.e(TAG, error);
                return false;
            }

            return true;
        }
        catch(IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            error = "NOT_FOUND_OR_INACCESSIBLE";
            Log.e(TAG, error);
            return false;
        }
    }

    // Verifying that the URL belongs to the site
    // =====================================
    private boolean checkURLDomain() {
        try {
            URL u = new URL(albumURL);
            if(u.getAuthority().equals("e-hentai.nhent.ai")) {
                // If the user enters something beyond the first page
                // --------------------------------------------------
                albumURL = albumURL.replaceAll("\\?page=.*", "");
                return true;
            }
            else {
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    // Resuming album language
    // --------------------------
    private void pGetAlbumLanguage() {
        links = doc.select("div[id=gdd]");

        for(Element e : links) {
            Elements table = e.getElementsByTag("table");

            for(Element t : table) {
                Elements row = t.getElementsByTag("tr");

                for(Element l : row) {
                    Elements lang = l.getElementsByClass("gdt2");
                    Elements langDescr = l.getElementsByClass("gdt1");

                    int i = 0;
                    for(Element ll : langDescr) {
                        if(ll.text().contains("Language")) {
                            if(lang.get(i).text().contains("TR")) {
                                isTranslated = true;
                            }

                            albumLanguage = lang.get(i)
                                    .text()
                                    .replace(" &nbsp;", "")
                                    .replace("TR", "");
                            return;
                        }

                        i += 1;
                    }
                }
            }
        }
    }

    // Rescuing uploader name
    // ---------------------------
    private void pGetUploader() {
        links = doc.select("div[id=gdn]");

        for(Element e : links) {
            Elements uplName = e.getElementsByTag("a");
            uploader = uplName.text();
        }
    }

    // Redeeming upload date
    // -------------------------
    private void pGetUploadDate() {
        links = doc.select("div[id=gdd]");

        for(Element e : links) {
            Elements table = e.getElementsByTag("table");

            for(Element t : table) {
                Elements row = t.getElementsByTag("tr");

                for(Element d : row) {
                    Elements date = d.getElementsByClass("gdt2");

                    for(Element dd : date) {
                        uploadDate = dd.text()
                                .replace("-", "/")
                                .replace(" ", " - ");
                        return;
                    }
                }
            }
        }
    }

    // Resizing album size
    // ---------------------------
    private void pGetFileSize() {
        links = doc.select("div[id=gdd]");
        String bytes[] = {"B", "KB", "MB", "GB", "TB"};

        for(Element e : links) {
            Elements table = e.getElementsByTag("table");

            for(Element t : table) {
                Elements row = t.getElementsByTag("tr");

                for(Element f : row) {
                    Elements fs = f.getElementsByClass("gdt2");

                    for(String b : bytes) {
                        if(fs.text().endsWith(b)) {
                            fileSize = fs.text();
                        }
                    }
                }
            }
        }
    }

    // Rescuing pages from the album, as it is shown on the site
    // -------------------------------------------------------------
    private void pGetAlbumPages() {
        links = doc.select("a[href]");

        for(Element e : links) {
            if(e.absUrl("href").contains("")) {
                if(!albumPages.contains(e.absUrl("href"))) {
                    albumPages.add(e.absUrl("href"));
                }
            }
        }
    }

    // Redeeming link of images as they are shown on the site
    // -------------------------------------------------------
    private void pGetImagePages() {
        for(Element e : links) {
            if(e.absUrl("href").startsWith("https://e-hentai.nhent.ai/s/")) {
                imagePages.add(e.absUrl("href"));
            }
        }

        if(albumPages.size() >= 1) {
            for(int i = 0; i < albumPages.size(); i++) {
                try {
                    doc = Jsoup.connect(albumPages.get(i)).cookie("nw", "1").get();
                    links = doc.select("a[href]");

                    for(Element e : links) {
                        if(e.absUrl("href").startsWith("https://e-hentai.nhent.ai/s/")) {
                            imagePages.add(e.absUrl("href"));
                        }
                    }
                }
                catch(IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    // Rescuing absolute link from images through pages
    // --------------------------------------------------------
    private void pGetImageLinks() {
        for(int i = 0; i < imagePages.size(); i++) {
            try {
                doc = Jsoup.connect(imagePages.get(i)).cookie("nw", "1").get();
                links = doc.select("img[id=img]");

                for(Element img : links) {
                    imageLinks.add(img.absUrl("src"));
                }
            }
            catch(IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    // ================
    // Public methods
    // ================

    // Starting process of recovering album values
    // -------------------------------------------------
    public void getInfo() {
        isOk = pGetInfo();
    }

    // Checking if the process completed successfully
    // ---------------------------------------------------
    public boolean isSuccessful() {
        return isOk;
    }

    // Rescuing absolute link from images
    // ------------------------------------
    public ArrayList<String> getImages() {
        return imageLinks;
    }

    // Redeeming number of pages (images) from the album
    // -----------------------------------------------
    public int getAlbumSize() {
        return pages;
    }

    // Resuming album language
    // --------------------------
    public String getAlbumLanguage() {
        return albumLanguage;
    }

    // Checking if the album is a translation
    // -------------------------------------
    public boolean isTranslated() {
        return isTranslated;
    }

    // Rescuing uploader name
    // ---------------------------
    public String getUploader() {
        return uploader;
    }

    // Redeeming upload date
    // -------------------------
    public String getUploadDate() {
        return uploadDate;
    }

    // Resizing album size
    // ---------------------------
    public String getFileSize() {
        return fileSize;
    }

    // Resuming album name
    // ------------------------
    public String getAlbumName() {
        return albumName;
    }

    // Rescuing error message
    // ---------------------------
    public String getError() {
        return error;
    }
}