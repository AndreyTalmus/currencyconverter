package ru.homeproduction.andrey.currencyconverter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText et_quantity;
    private TextView tv_result;
    private Button btn_calculate_sum;
    private Spinner spinner_start_valute,spinner_end_valute;

    //Ссылка на xml файл.
    private static final String URL =
            "http://www.cbr.ru/scripts/XML_daily.asp";

    List<XmlParser.Entry> entries = null;
    FileInputStream InputStreamFromCache;

    //Value1 - value выбранной валюты из spinner_start_valute
    //Value2 - value выбранной валюты из spinner_end_valute
    //Value3 - введенная сумма в editText
    //Value4 - конечный результат
    private double value1,value2, value_sum, value_result;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_quantity = (EditText) findViewById(R.id.et_quantity);
        tv_result = (TextView) findViewById(R.id.tv_result);
        btn_calculate_sum = (Button) findViewById(R.id.btn_calculate_sum);
        spinner_start_valute = (Spinner) findViewById(R.id.spinner_start_valute);
        spinner_end_valute = (Spinner) findViewById(R.id.spinner_end_valute);

        //В случае увеличения количества записей в xml, необходимо создать отдельный AsyncTask для
        //этих вычислений, чтобы не блокировать главный поток.
        btn_calculate_sum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {




                //Проверяем, есть ли информация в EditText
                if(et_quantity.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), R.string.set_sum, Toast.LENGTH_SHORT).show();
                }
                else {

                    //Получаем выбранные String из Spinner
                    //Ищем в записи с соответствующими валютами
                    //Получаем value данных записей
                    //Выполняем математическое преобразование.
                    //Выводим в соответствующем формате.
                    String start_valute;
                    String end_valute;
                    String entry_name;
                    String string_value1;
                    String string_value2;



                    value_sum = Double.parseDouble(et_quantity.getText().toString());
                    start_valute = spinner_start_valute.getSelectedItem().toString();
                    end_valute = spinner_end_valute.getSelectedItem().toString();
                    if (start_valute.equals(end_valute)) {

                        tv_result.setText(String.format("%.2f", value_sum) + " " + start_valute);

                    } else {
                        for (XmlParser.Entry entry : entries) {
                            entry_name = entry.name;
                            if (start_valute.equals(entry_name)) {
                                string_value1 = entry.value.replace(',', '.');
                                value1 = Double.parseDouble(string_value1);
                            } else if (end_valute.equals(entry_name)) {
                                string_value2 = entry.value.replace(',', '.');
                                value2 = Double.parseDouble(string_value2);
                            }
                        }

                        value_result = (value_sum * value1 / value2);
                        tv_result.setText(String.format("%.2f", value_result) + " " + end_valute);
                    }
                }

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        boolean isConnected;

        //Проверяем если активная сеть, если есть скачиваем xml файл. В случае отсутствия
        //проверяем существует ли файл в кеше, если есть скачиваем данные из него, если нет
        // блокируем кнопку и выводим сообщение об отсутствии интернет соединения.
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        File file = new File(getCacheDir(), "ExchangeRates.srl");


        if(activeNetwork != null) {
           isConnected = activeNetwork.isConnectedOrConnecting();
        }
        else{
            isConnected = false;
        }

        if(isConnected){
            new DownloadXmlTask().execute(URL);
        }
        else if(file.exists()){
            new DownloadFromCache().execute();
        }
        else{
            btn_calculate_sum.setEnabled(false);
            Toast.makeText(getApplicationContext(), R.string.lost_connection, Toast.LENGTH_SHORT).show();
        }

    }

    //AsyncTaks для загрузки XML файла по сети.
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //Обновляем данные в spinners.Так как исходная и конечная валюта взята из одного документа
            //устанавливаем один адаптер для 2 spinner.
            String[] separated = result.split("//");
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),   R.layout.spinner_layout, separated);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout); // The drop down view
            spinner_start_valute.setAdapter(spinnerArrayAdapter);
            spinner_end_valute.setAdapter(spinnerArrayAdapter);
        }
    }

    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {

        InputStream stream = null;
        XmlParser xmlParser = new XmlParser();
        String name = "";

        File file = new File(getCacheDir(), "ExchangeRates.srl");

        try {
            //Загружаем наш xml документ
            stream = downloadUrl(urlString);
            //Сохраняем его в cache
            copyInputStreamToFile(stream, file);
            //Производим повторную загрузку xml
            stream = downloadUrl(urlString);
            //Производим обработку.
            entries = xmlParser.parse(stream);
        } finally {
            if (stream != null) {
                 stream.close();
            }
        }

        for (XmlParser.Entry entry : entries) {
            name = name + entry.name +"//";
        }
        return name;
    }


    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }


    //Все необходимое для загрузки Валют из кеша.Копируем AsyncTask выше, за исключением того,
    //что InputStream получаем из файла.
    private class DownloadFromCache extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                return loadXmlFromCache();
            } catch (IOException e) {
                return getResources().getString(R.string.xml_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {

            String[] separated = result.split("//");
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),   R.layout.spinner_layout, separated);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
            spinner_start_valute.setAdapter(spinnerArrayAdapter);
            spinner_end_valute.setAdapter(spinnerArrayAdapter);
        }
    }
    //Метод загрузки xml файла из кеша.
    private String loadXmlFromCache() throws XmlPullParserException, IOException {

        XmlParser xmlParser = new XmlParser();
        String name = "";

        try {
            File file = new File(getCacheDir(), "ExchangeRates.srl");
            InputStreamFromCache = new FileInputStream(file);
            entries = xmlParser.parse(InputStreamFromCache);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        if (InputStreamFromCache != null) {
            InputStreamFromCache.close();
        }
    }
        for (XmlParser.Entry entry : entries) {
            name = name + entry.name +"//";
        }
        return name;
    }

    //Для копирования входящего поток в файл.
    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[10000];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
