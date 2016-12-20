package ru.homeproduction.andrey.currencyconverter;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCacheUtil {

    private static final String TAG = "DEBUG_TAG";
    private static final String FILE_NAME = "ExchangeRates.srl";
    private static final int DATA_BUFFER = 10000;

    public static InputStream getStreamFromFile(Context context){

        File file = new File(context.getCacheDir(), FILE_NAME);
        InputStream inputStreamFromCache = null;

        try {
            inputStreamFromCache = new FileInputStream(file);
            Log.d(TAG,"Считывание файла прошло успешно.");

        } catch (FileNotFoundException e) {
            Log.d(TAG, "Заправшиваемый файл в Cache отсутствует.");
        }
        catch(SecurityException e){
            Log.d(TAG,"Доступ к файлу запрещен.");
        }
        return inputStreamFromCache;
    }

    public static void setStreamIntoFile(Context context,InputStream inputStream) {

        try {
            File file = new File(context.getCacheDir(), FILE_NAME);
            OutputStream outputStream;

            try {
                outputStream = new FileOutputStream(file);

                try {
                    try {
                        byte[] buffer = new byte[DATA_BUFFER];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                        Log.d(TAG,"Данные успешно сохранены.");
                        outputStream.flush();
                    } finally {
                        if(outputStream != null)
                            outputStream.close();
                    }
                } catch (Exception e) {
                    Log.d(TAG,"Ошибка при закрытии выходного потока.");
                }
            } finally {
                try{
                    if(inputStream != null)
                        inputStream.close();
                } catch (Exception e) {
                    Log.d(TAG,"Ошибка при закрытии входного потока.");
                }
            }

            } catch (FileNotFoundException e) {
                Log.d(TAG, "Заправшиваемый файл в Cache отсутствует.");
            }
            catch(SecurityException e){
                Log.d(TAG,"Доступ к файлу запрещен.");
            }
    }

    public static File getFile(Context context){
        return new File(context.getCacheDir(), FILE_NAME);
    }

    public static String getFileName(){
        return FILE_NAME;
    }
}
