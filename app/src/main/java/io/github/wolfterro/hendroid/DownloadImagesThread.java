/*
The MIT License (MIT)

Copyright (c) 2017 Wolfgang Almeida

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package io.github.wolfterro.hendroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.*;

/**
 * Created by Wolfterro on 06/09/2017.
 */

public class DownloadImagesThread extends Thread {
    // Propriedades privadas
    // ---------------------
    private Context c = null;
    private ProgressDialog pd = null;
    private String fullPath = "";
    private ArrayList<String> images = new ArrayList<String>();

    private String downloadMessage = "";
    private String newMessage = "";
    private String TAG = "DownloadImagesThread";

    // Construtor da classe
    // --------------------
    public DownloadImagesThread(Context c, ProgressDialog pd,
                                String fullPath, ArrayList<String> images) {

        this.c = c;
        this.pd = pd;
        this.fullPath = fullPath;
        this.images = images;

        this.downloadMessage = c.getString(R.string.pleaseWaitWhileImagesAreBeingDownloaded);
    }

    // ================
    // Métodos Públicos
    // ================

    // Iniciando download das imagens
    // ------------------------------
    @Override
    public void run() {
        File fPath = new File(fullPath);
        if(fPath.isDirectory()) {
            System.setProperty("user.dir", fullPath);
        }
        else {
            fPath.mkdirs();
            System.setProperty("user.dir", fullPath);
        }

        for(int i = 0; i < images.size(); i++) {
            String filename = "";

            try {
                URL u = new URL(images.get(i));
                filename = FilenameUtils.getName(u.getPath());
                File downloadedImage = new File(fullPath + filename);

                String percentage = String.format(c.getString(R.string.downloadingImageXFromY),
                        i, images.size());
                newMessage = String.format("%s\n\n%s", downloadMessage, percentage);

                if(!downloadedImage.exists()) {
                    updateDownloadMessage.sendEmptyMessage(0);
                    FileUtils.copyURLToFile(u, downloadedImage, 10000, 10000);
                }
            }
            catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        finished.sendEmptyMessage(0);
    }

    // Atualizando mensagem de download
    // --------------------------------
    private Handler updateDownloadMessage = new Handler() {
        @Override
        public void handleMessage(Message m) {
            pd.setMessage(newMessage);
        }
    };

    // Atualizando mensagem de download
    // --------------------------------
    private Handler finished = new Handler() {
        @Override
        public void handleMessage(Message m) {
            pd.dismiss();

            Toast.makeText(c,
                    c.getString(R.string.downloadFinishedCheckOutputDir),
                    Toast.LENGTH_SHORT).show();
        }
    };
}
