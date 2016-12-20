package ru.homeproduction.andrey.currencyconverter;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText et_amount;
    private TextView tv_result;
    private Button btn_calculate_sum;
    private Spinner spinner_start_valute,spinner_end_valute;

    private static final String URL =
            "http://www.cbr.ru/scripts/XML_daily.asp";

    private List<XmlParser.Entry> entries = null;

    private String amount, start_value, end_value, start_valute, end_valute;

    private double value_result;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_amount = (EditText) findViewById(R.id.et_amount);
        tv_result = (TextView) findViewById(R.id.tv_result);
        btn_calculate_sum = (Button) findViewById(R.id.btn_calculate_sum);
        spinner_start_valute = (Spinner) findViewById(R.id.spinner_start_valute);
        spinner_end_valute = (Spinner) findViewById(R.id.spinner_end_valute);

        btn_calculate_sum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(et_amount.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), R.string.set_sum, Toast.LENGTH_SHORT).show();
                }
                else {

                    amount = et_amount.getText().toString();
                    start_valute = spinner_start_valute.getSelectedItem().toString();
                    end_valute = spinner_end_valute.getSelectedItem().toString();

                    if (start_valute.equals(end_valute)) {
                        tv_result.setText(amount + " " + start_valute);
                    } else {
                        for (XmlParser.Entry entry : entries) {
                            if (start_valute.equals(entry.name)) {
                                start_value = entry.value.replace(',', '.');
                            } else if (end_valute.equals(entry.name)) {
                                end_value = entry.value.replace(',', '.');
                            }
                        }
                        value_result = Calculator.calculate(amount, start_value, end_value);
                        tv_result.setText(String.format("%.2f", value_result) + " " + end_valute);
                    }
                }

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        if(NetworkUtil.getConnectivityStatus(getApplicationContext()) != 0) {
            new DownloadXmlTask().execute(URL);
        }
        else if(FileCacheUtil.getFile(getApplicationContext()).exists()){
            new DownloadFromCache().execute();
        }
        else{
            btn_calculate_sum.setEnabled(false);
            Toast.makeText(getApplicationContext(), R.string.lost_connection, Toast.LENGTH_SHORT).show();
        }
    }

    //Все необходимое для загрузки данных по сети.
    private class DownloadXmlTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {
            try {
                List<String> mList = new ArrayList<>();
                for (XmlParser.Entry entry : loadXmlFromNetwork(URL)) {
                    mList.add(entry.name);
                }
                return mList;
            } catch (IOException e) {
                return null;
            } catch (XmlPullParserException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> mList) {

            if(mList != null){
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(),   R.layout.spinner_layout, mList);
                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
                spinner_start_valute.setAdapter(spinnerArrayAdapter);
                spinner_end_valute.setAdapter(spinnerArrayAdapter);
            }
        }
    }

    private List<XmlParser.Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {

        InputStream stream = null;
        XmlParser xmlParser = new XmlParser();

        try {
            stream = NetworkLoader.getInputStreamFromUrl(urlString);
            FileCacheUtil.setStreamIntoFile(getApplicationContext(),stream);
            stream = NetworkLoader.getInputStreamFromUrl(urlString);
            entries = xmlParser.parse(stream);
        } finally {
            if (stream != null) {
                 stream.close();
            }
        }
        return entries;
    }


    //Все необходимое для загрузки данных из Cache.
    private class DownloadFromCache extends AsyncTask<Void, Void ,List<String>> {

        @Override
        protected List<String> doInBackground(Void...arg0) {
            try {
                List<String> mList = new ArrayList<>();
                for (XmlParser.Entry entry : loadXmlFromCache()) {
                    mList.add(entry.name);
                }
                return mList;
            } catch (IOException e) {
                return null;
            } catch (XmlPullParserException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> mList) {

            if(mList != null){
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(),   R.layout.spinner_layout, mList);
                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
                spinner_start_valute.setAdapter(spinnerArrayAdapter);
                spinner_end_valute.setAdapter(spinnerArrayAdapter);
            }

        }
    }

    private List<XmlParser.Entry> loadXmlFromCache() throws XmlPullParserException, IOException {

        XmlParser xmlParser = new XmlParser();
        entries = xmlParser.parse(FileCacheUtil.getStreamFromFile(getApplicationContext()));
        return entries;
    }

}
