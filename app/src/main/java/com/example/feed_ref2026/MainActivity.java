package com.example.feed_ref2026;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feed_ref2026.databinding.ActivityMainBinding;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    // #1.0 - Primeiramente vamos criar TextView  para exibir o conteúdo baixado.
    // #Vamos criar uma variável TextView para receber o conteúdo baixado
    //private TextView xmlTexView;

    //1.1 - Vamos criar uma variável String que receberá o conteúdo do site.
    private String mFileContents;
    // Parte 3# Parte 3# Declarando a variável RecyclerView
    private RecyclerView mRecyclerView;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

//        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.nav_host_fragment_content_main);
//
//        if (navHostFragment != null) {
//            NavController navController = navHostFragment.getNavController();
//
//            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        }
//
//        binding.fab.setOnClickListener(
//                view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAnchorView(R.id.fab)
//                        .setAction("Action", null).show()
//        );
        //1.2 - associando a variável à view no XML.
        //xmlTexView = (TextView) findViewById(R.id.textViewInicial);

        //1.5 chamada do metódo executeTask(url )para execução da task paralela
        executeTask("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");

        //    Recycler block
        //3.1 Associando a a variável à RecyclerView no XML
        mRecyclerView = findViewById(R.id.myreclycer);
        //3.2 é interessante caso os itens de layout tenham tamanho fixo. Otimizar a implementação
        mRecyclerView.setHasFixedSize(true);
        //3.3 Associação de um gerenciador de Layout para a RecyclerView.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    // 1.3 - Vamos criar o metodo para realizar o download do feed.
    private String downloadXMLFile(String theUrl){
        // precisamos usar um try pois as operações de abertura de conexão e leitura do stream de dados podem lançar exeções de IO.
        try {
//            //cria um instância da classe URL com a url que será utilizada na conexão.
            URL myUrl = new URL(theUrl);
//
//            //abre a conexão
            HttpURLConnection myconnection = (HttpURLConnection) myUrl.openConnection();
//
//            //verifica se foi bem sucedida a conexão; se sim código 200
            int response = myconnection.getResponseCode();
            Log.d("DownloadData", "The response code is " + response);
//
//            // cria a variável "data" que irá receber o stream de bytes
            InputStream data = myconnection.getInputStream();
//
//            //usa a classe InputStreamReader para converter bytes em chars
            InputStreamReader caracteres = new InputStreamReader(data);
//
//            //variável para contagem de número de caracteres lidos
            int charRead;
//
//            //criação de um array de char para leitura de 500 em 500 caracteres
            char[] inputBuffer = new char[500];
//
//            //Criação de uma instância StringBuilder para formar a String final de interesse
            StringBuilder tempBuffer = new StringBuilder();
//
//            //laço de leitura e formação da string
            while (true) {
//                //lê os caracteres com tamanho máximo de inputBuffer(500) e informa o número lido
                charRead = caracteres.read(inputBuffer);
//
//                //se -1 não há caracteres lidos e sai do laço.
                if (charRead <= 0) {
                    break;
                }
//
//                //Enquanto charRead>0, vai "appendando" as leituras de 500 em 500 caracteres do inputBuffer
//
                tempBuffer.append(String.copyValueOf(inputBuffer, 0, charRead));
            }
//            //Retorna o conteúdo final do tempBuffer, convertido para String
            return tempBuffer.toString();
        }catch(IOException e)
        {
            Log.d("DownloadData", "IO Expection reading data: " + e.getMessage());
        }catch (SecurityException e){
            Log.e("DownloadData","Security exception. Needs permission?"+e.getMessage());
        }
//
        return null;
    }

//1.4 - Vamos criar o metodo executeTask responsável pela execução da thread paralela

    //Executor responsável pela thread paralela
    private void executeTask(String url) {
        //Cria um executor com uma única thread para gerenciar tarefas.
        // Executors.newSingleThreadExecutor() retorna uma instância de
        // uma classe classe que implementa ExecutorService.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        //o executor  submete a tarefa de donwload via metodo downloadXMLFile() escrito anteriormente
        // à classe anônima Callable através do metodo call()
        Future<String> future = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return downloadXMLFile(url);
            }
        });

        //o executor submete a tarefa de processar o resultado obtido pela Callable através da classe anônima Runnable e do metodo run().
        // Neste caso,ela espera que o download termine para processar o resultado, fazendo uma chamada ao future.get().
        executor.submit(new Runnable() {
            @Override
            public void run() {
              try{
                  mFileContents = future.get();
                  if (mFileContents==null){
                      Log.d("DownloadData", "Erro downloading");
                  }else {
                      Log.d("DownloadData","Baixou =>"+ mFileContents);
                  }
              }catch(Exception e){
                  Log.e("DownloadData", "Error executing task", e);
              }
            }
        });

        executor.shutdown();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //1.6 - Inserindo o conteúdo baixado na caixa de texto.
            // Obs.Deve ser comentado na parte 2, quando é ativada a RecyclerView

            //xmlTexView.setText(mFileContents);
            //Parte 2 parse application
            //criando uma instância da classe parseApplication e disparando a mineração e vamos conferir no logcat

            ParseApplications parseApplications = new ParseApplications(mFileContents);
            parseApplications.process();

            // Recycler block
            //3.4 criação de uma instância do nosso adapter e passando o resultado da mineração do parse Application
            //através do metodo getApplication que já retorna um array de application1
            MyFirstAdapter mAdapter = new MyFirstAdapter(parseApplications.getApplications());
            //3.5 linkando nossa RecyclerView com o adapter
            mRecyclerView.setAdapter(mAdapter);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.nav_host_fragment_content_main);
//        boolean handled = false;
//        if (navHostFragment != null) {
//            NavController navController = navHostFragment.getNavController();
//            handled = NavigationUI.navigateUp(navController, appBarConfiguration);
//        }
//        return handled || super.onSupportNavigateUp();
//    }
}