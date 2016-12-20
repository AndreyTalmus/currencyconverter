package ru.homeproduction.andrey.currencyconverter;

import android.util.Log;

public class Calculator {

    private static final String TAG = "DEBUG_TAG";

    public static Double calculate(String number,String input,String output){

        Double result = null;

        try {
            Double inputValue = Double.parseDouble(input);
            Double outputValue = Double.parseDouble(output);
            Double amount = Double.parseDouble(number);
            result = (amount * inputValue / outputValue);
            Log.d(TAG,"Расчет произведен успешно.");

        } catch (NullPointerException e) {
            Log.d(TAG, "Входные данные является null.");
        }
        catch(NumberFormatException e){
            Log.d(TAG,"Неверный формат строки для преобразования.");
        }
        return result;
    }
}
