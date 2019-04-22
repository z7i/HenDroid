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
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Wolfterro on 05/09/2017.
 */

public class GetInformationThread extends Thread {
    // Propriedades privadas
    // ---------------------
    private EDownloaderInfo info = null;

    private Context c = null;
    private ProgressDialog pd = null;
    private String url = "";

    private String errorMsg = "";

    // Construtor da classe
    // --------------------
    public GetInformationThread(Context c, ProgressDialog pd, String url) {
        this.c = c;
        this.pd = pd;
        this.url = url;
    }

    // ================
    // Métodos Públicos
    // ================

    // Iniciando resgate de informações do álbum
    // -----------------------------------------
    @Override
    public void run() {
        info = new EDownloaderInfo(url);
        info.getInfo();

        if(info.isSuccessful()) {
            infoSuccessful.sendEmptyMessage(0);
        }
        else {
            errorMsg = info.getError();
            infoFailure.sendEmptyMessage(0);
        }
    }

    // Em caso de sucesso na recuperação das informações
    // -------------------------------------------------
    private Handler infoSuccessful = new Handler() {
        @Override
        public void handleMessage(Message m) {
            pd.dismiss();

            Intent i = new Intent(c, InfoActivity.class);
            i.putStringArrayListExtra("IMAGES", info.getImages());
            i.putExtra("ALBUMNAME", info.getAlbumName());
            i.putExtra("ALBUMSIZE", info.getAlbumSize());
            i.putExtra("ALBUMLANGUAGE", info.getAlbumLanguage());
            i.putExtra("UPLOADER", info.getUploader());
            i.putExtra("UPLOADDATE", info.getUploadDate());
            i.putExtra("SIZE", info.getFileSize());

            // Caso haja algo mais a adicionar, adicione aqui!
            // -----------------------------------------------

            c.startActivity(i);
        }
    };

    // Em caso de falha na recuperação das informações
    // -------------------------------------------------
    private Handler infoFailure = new Handler() {
        @Override
        public void handleMessage(Message m) {
            pd.dismiss();
            String err = String.format("%s %s",
                    c.getString(R.string.errorCouldNotObtainInformations),
                    errorMsg);

            Toast.makeText(c, err, Toast.LENGTH_LONG).show();
        }
    };
}
