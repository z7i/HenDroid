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

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Elementos da Activity Principal
    // -------------------------------
    private EditText editTextAlbumURL;
    private Button buttonCheckAlbum;

    // Propriedades privadas
    // ---------------------
    private String AppDirFullPath = "";
    private final int PERMISSION_GRANTED_VALUE = 0;

    // Menu de opções da activity principal
    // ====================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Selecionando opções no menu da activity principal
    // =================================================
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent about = new Intent(MainActivity.this, AboutActivity.class);
                MainActivity.this.startActivity(about);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Criando activity principal do aplicativo
    // ========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAlbumURL = (EditText)findViewById(R.id.editTextAlbumURL);
        buttonCheckAlbum = (Button)findViewById(R.id.buttonCheckAlbum);

        // Pedindo permissão de acesso ao armazenamento do aparelho para o usuário
        // -----------------------------------------------------------------------
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_GRANTED_VALUE);
        }

        // Definindo o local padrão para o salvamento dos arquivos
        // -------------------------------------------------------
        if(Environment.getExternalStorageState() == null) {
            AppDirFullPath = Environment.getDataDirectory().getAbsolutePath() + GlobalVars.AppDir;
            GlobalVars.AppDirFullPath = AppDirFullPath;
        }
        else {
            AppDirFullPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + GlobalVars.AppDir;
            GlobalVars.AppDirFullPath = AppDirFullPath;
        }

        File mainDir = new File(AppDirFullPath);
        if(mainDir.isDirectory()) {
            System.setProperty("user.dir", AppDirFullPath);
        }
        else {
            mainDir.mkdirs();
            System.setProperty("user.dir", AppDirFullPath);
        }

        // Iniciando a verificação do álbum ao clicar no botão
        // ---------------------------------------------------
        buttonCheckAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editTextAlbumURL.getText().toString();

                if(url.equals("") || url.equals(getString(R.string.baseURL))) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.pleaseInsertURL),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Iniciando resgate de valores do álbum
                    // =====================================
                    ProgressDialog pd = new ProgressDialog(MainActivity.this);

                    pd.setTitle(getString(R.string.obtainingInformations));
                    pd.setMessage(String.format("%s\n\n%s",
                            getString(R.string.standBy),
                            getString(R.string.thisCanTakeAWhile)));

                    pd.setCancelable(false);
                    pd.show();

                    GetInformationThread git = new GetInformationThread(MainActivity.this, pd, url);
                    git.start();
                }
            }
        });
    }

    // =============================================================================================

    // Método invocado após o pedido de permissão
    // ------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_GRANTED_VALUE: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Nada por enquanto
                }
                else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.errorNoPermissionGiven),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
