package es.incaser.apps.stockcontrol;

import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sergio on 2/10/14.
 */
public class Tools {
    public static String UUID_EMPTY = "00000000-0000-0000-0000-000000000000";

    public static float getNumber(String text) {
        if (text == null) {
            return 0;
        } else if (text.length() == 0) {
            return 0;
        } else if (text.matches(".*\\\\D+.*")) {
            return 0;
        } else {
            //TODO Controlar separador de miles
            return Float.valueOf(text.replace(",", "."));
        }
    }

    public static float getNumber(EditText txt) {
        return getNumber(txt.getText().toString());
    }

    public static String importeStr(Float importe) {
        DecimalFormat nf = new DecimalFormat();
        nf.applyPattern("#0.00");
        return nf.format(importe);
    }

    public static String importeStr(String importe) {
        return importeStr(getNumber(importe));
    }


    public static int getInt(String text) {
        return Math.round(getNumber(text));
    }

    public static int getInt(EditText txtView) {
        return getInt(txtView.getText().toString());
    }

    public static String enteroStr(Integer x) {
        return x.toString();
    }

    public static String importeStr(EditText txt) {
        return importeStr(txt.getText().toString());
    }

    public static String getToday() {
        return getToday("yyyy-MM-dd 00:00:00.0");
    }

    public static String getToday(String format) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String millis2String(Long miliseconds) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(miliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        return sdf.format(date.getTime());
    }

//    public static String getActualHour(){
//        Date date = Calendar.getInstance().getTime();
//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
//        return sdf.format(date);
//    }

    public static double getActualHour() {
        Date date = Calendar.getInstance().getTime();
        return getHourFractionDay(date.getTime());
    }

    public static double getHourFractionDay(long timelong) {
        double milisecondsDay = 86400000.00;
        return (timelong % (milisecondsDay)) / milisecondsDay;
    }

    public static String getTimeStamp() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
        return sdf.format(date);
    }

    public static Date str2date(String myTimestamp, String strFormat) {
        SimpleDateFormat formato = new SimpleDateFormat(strFormat);
        Date convertedDate = null;
        try {
            convertedDate = formato.parse(myTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    public static Date str2date(String myTimestamp) {
        return str2date(myTimestamp, "yyyy-MM-dd HH:mm:ss.S");
    }

    public static String date2str(Date date, String strFormat) {
        SimpleDateFormat formato = new SimpleDateFormat(strFormat);
        return formato.format(date);
    }

    public static String date2str(Date date) {
        return date2str(date, "dd-MM-yyyy 00:00:00.0");
    }

    public static String dateStr2str(String date) {
        return date2str(str2date(date), "dd/MM/yyyy");
    }
    
    public static String date2Sql(String date){
        return "CONVERT(DATETIME,'" + date + "', 102)";
    }
}
