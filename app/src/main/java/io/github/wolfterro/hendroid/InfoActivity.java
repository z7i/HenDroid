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
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {
    // Elementos da Activity de Informações
    // ------------------------------------
    private TextView albumName;
    private TextView albumSize;
    private TextView outputDir;
    private TextView albumLanguage;
    private TextView uploader;
    private TextView uploadDate;
    private TextView size;

    private ImageView cover;
    private Button downloadAlbum;

    // Propriedades privadas
    // ---------------------
    private ArrayList<String> images = new ArrayList<String>();

    private String albumNameStr = "";
    private String albumLanguageStr = "";
    private String uploaderStr = "";
    private String uploadDateStr = "";
    private String sizeStr = "";

    private int albumSizeInt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        albumName = (TextView)findViewById(R.id.textViewNameValue);
        albumSize = (TextView)findViewById(R.id.textViewNumberValue);
        outputDir = (TextView)findViewById(R.id.textViewOutputDirValue);
        albumLanguage = (TextView)findViewById(R.id.textViewLanguageValue);
        uploader = (TextView)findViewById(R.id.textViewUploaderValue);
        uploadDate = (TextView)findViewById(R.id.textViewUploadDateValue);
        size = (TextView)findViewById(R.id.textViewSizeValue);

        cover = (ImageView)findViewById(R.id.imageViewCover);
        downloadAlbum = (Button)findViewById(R.id.buttonDownloadAlbum);

        Intent i = getIntent();
        images = i.getStringArrayListExtra("IMAGES");
        albumNameStr = i.getStringExtra("ALBUMNAME");
        albumLanguageStr = i.getStringExtra("ALBUMLANGUAGE");
        uploaderStr = i.getStringExtra("UPLOADER");
        uploadDateStr = i.getStringExtra("UPLOADDATE");
        sizeStr = i.getStringExtra("SIZE");
        albumSizeInt = i.getIntExtra("ALBUMSIZE", 0);

        albumName.setText(albumNameStr);
        albumSize.setText(String.format("%d", albumSizeInt));
        albumLanguage.setText(albumLanguageStr);
        uploader.setText(uploaderStr);
        uploadDate.setText(uploadDateStr);
        size.setText(sizeStr);
        outputDir.setText(getOutputDir());

        // Resgatando a primeira imagem (capa) do álbum e mostrando para o usuário
        // -----------------------------------------------------------------------
        Picasso.with(InfoActivity.this).load(images.get(0)).into(cover);
        // -----------------------------------------------------------------------

        // Iniciando download das imagens do álbum
        // ---------------------------------------
        downloadAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog pd = new ProgressDialog(InfoActivity.this);

                pd.setTitle(getString(R.string.downloadingAlbum));
                pd.setMessage(String.format("%s",
                        getString(R.string.pleaseWaitWhileImagesAreBeingDownloaded)));

                pd.setCancelable(false);
                pd.show();

                DownloadImagesThread dit = new DownloadImagesThread(InfoActivity.this,
                        pd, getOutputDir(), images);
                dit.start();
            }
        });
    }

    // Criando e retornando caminho da pasta de destino
    // ------------------------------------------------
    protected String getOutputDir() {
        String pSymbols[] = {"/", "<", ">", ":", "?", "\\", "|", "*", "\""};
        String outputDirName = albumNameStr;

        for(String p : pSymbols) {
            outputDirName = outputDirName.replace(p, "");
        }
        return String.format("%s%s/", GlobalVars.AppDirFullPath, outputDirName);
    }
}
