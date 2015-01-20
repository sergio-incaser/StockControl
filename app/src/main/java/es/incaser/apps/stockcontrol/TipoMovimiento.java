package es.incaser.apps.stockcontrol;

/**
 * Created by sergio on 20/01/15.
 */
public class TipoMovimiento {
    public static String ENTRADA = "1";
    public static String SALIDA = "2";
    public static String ORIGEN_ENTRADA = "10";
    public static String ORIGEN_SALIDA = "11";

    public static String origenMov(String tipoMov){
        String res = "";
        if (tipoMov.equals("1")){
            res = ORIGEN_ENTRADA;
        }else{
            res = ORIGEN_SALIDA;
        }
        return res;
    }
}
