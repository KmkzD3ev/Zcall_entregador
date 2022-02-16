package br.com.zenitech.zcallmobile;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class ClassAuxiliar {

    //FORMATAR DATA - INSERIR E EXIBIR
    private SimpleDateFormat inserirDataFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat exibirDataFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat exibirDataFormat_dataHora = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
    private SimpleDateFormat anoMesChaveNFCE = new SimpleDateFormat("yyMM");
    //FORMATAR HORA
    private SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");
    private Date data = new Date();
    private Calendar cal = Calendar.getInstance();


    //ANO E MÊS ATUAL DO SISTEMA - pt-BR / PARA A CHAVE DA NOTA NFC-E
    public String anoMesAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();
        String anoMesAtual = anoMesChaveNFCE.format(data_atual);

        return anoMesAtual;
    }

    //EXIBIR DATA ATUAL DO SISTEMA - pt-BR
    public String exibirDataAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();
        String dataAtual = exibirDataFormat.format(data_atual);

        return dataAtual;
    }

    //INSERIR DATA ATUAL DO SISTEMA
    public String inserirDataAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();
        String dataAtual = inserirDataFormat.format(data_atual);

        return dataAtual;
    }

    //EXIBIR DATA
    public String exibirData(String data) {
        String CurrentString = data;
        String[] separated = CurrentString.split("-");
        data = separated[2] + "/" + separated[1] + "/" + separated[0];

        return data;
    }

    //INSERIR DATA
    public String inserirData(String data) {
        String CurrentString = data;
        String[] separated = CurrentString.split("/");
        data = separated[2] + "-" + separated[1] + "-" + separated[0];

        return data;
    }

    //EXIBIR HORA ATUAL
    public String horaAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();
        String horaAtual = dateFormat_hora.format(data_atual);

        return horaAtual;
    }

    //TIMESTAMP
    public String timeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        return dateFormat.format(new Date());
    }

    //SOMAR VALORES
    public BigDecimal somar(String[] args) {
        BigDecimal valor = new BigDecimal("0.0");

        //
        for (String v : args) {
            valor = new BigDecimal(String.valueOf(valor)).add(new BigDecimal(v));
            //
            Log.e("TOTAL", "SOMAR " + String.valueOf(valor));
        }
        return valor;
    }

    //SUBTRAIR VALORES
    public BigDecimal subitrair(String[] args) {
        BigDecimal valor = new BigDecimal(args[0]).subtract(new BigDecimal(args[1]));

        //
        Log.e("TOTAL", "SUBTRAIR " + String.valueOf(valor));
        return valor;
    }

    //MULTIPLICAR VALORES
    public BigDecimal multiplicar(String[] args) {
        BigDecimal valor = new BigDecimal(args[0]).multiply(new BigDecimal(args[1]));

        //
        Log.e("TOTAL", "MULTIPLICAR " + String.valueOf(valor));
        return valor;
    }

    //DIVIDIR VALORES
    public BigDecimal dividir(String[] args) {
        BigDecimal valor = new BigDecimal(args[0]).divide(new BigDecimal(args[1]), 3, RoundingMode.UP);

        //
        Log.e("TOTAL", "DIVIDIR " + String.valueOf(valor));
        return valor;
    }

    //COMPARAR VALORES
    public int comparar(String[] args) {
        int valor = new BigDecimal(args[0]).compareTo(new BigDecimal(args[1]));
        //
        Log.e("TOTAL", "COMPARAR " + String.valueOf(valor));
        return valor;
    }

    //CONVERTER VALORES PARA CALCULO E INSERÇÃO NO BANCO DE DADOS
    public BigDecimal converterValores(String value) {
        BigDecimal parsed = null;
        try {
            //String cleanString = value.replaceAll("[R,$,.]", "");
            parsed = new BigDecimal(this.soNumeros(value)).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);

            Log.e("TOTAL", "FORAMATAR NUMERO: " + String.valueOf(parsed));
        } catch (Exception e) {
            Log.e("TOTAL", e.getMessage(), e);
        }
        return parsed;
    }

    /*//CONVERTER VALORES PARA CALCULO E INSERÇÃO NO BANCO DE DADOS
    public BigDecimal converterValores(String value) {
        BigDecimal parsed = null;
        try {
            String cleanString = value.replaceAll("[R,$,.]", "");
            parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);

            Log.e("TOTAL", "FORAMATAR NUMERO: " + parsed);
        } catch (Exception e) {
            Log.e("TOTAL", e.getMessage(), e);
        }
        return parsed;
    }*/

    //CONVERTER VALORES PARA CALCULO E INSERÇÃO NO BANCO DE DADOS
    public String converterValoresNota(String value) {
        String text = String.valueOf(this.converterValores(value));
        String cleanString = text.replaceAll(".", "");

        return cleanString;
    }

    //
    public String maskMoney(BigDecimal valor) {
        /*NumberFormat formato1 = NumberFormat.getCurrencyInstance();
        NumberFormat formato2 = NumberFormat.getCurrencyInstance(new Locale("en", "EN"));
        NumberFormat formato3 = NumberFormat.getIntegerInstance();
        NumberFormat formato4 = NumberFormat.getPercentInstance();
        NumberFormat formato5 = new DecimalFormat(".##");
        NumberFormat formato6 = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        //
        String valorFormat = valor;

        valorFormat = formato5.format(valor);*/

        //
        //texto.setText(formato1.format(valor));
        /*Log.i("Moeda atual", formato1.format(valor));
        Log.i("Moeda EUA", formato2.format(valor));
        Log.i("Número inteiro", formato3.format(valor));
        Log.i("Porcentagem", formato4.format(valor));
        Log.i("Decimal", formato5.format(valor));
*/
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) nf).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        ((DecimalFormat) nf).setDecimalFormatSymbols(decimalFormatSymbols);
        ((DecimalFormat) nf).setMinimumFractionDigits(2);
        //System.out.println(nf.format(12345.124).trim());

        return nf.format(valor);
    }

    /*public static void main(String[] args) {
        System.out.println("Subtrai");
        System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.1")));

        System.out.println("");
        System.out.println("Soma");
        System.out.println(new BigDecimal("2.00").add(new BigDecimal("1.2")));

        System.out.println("");
        System.out.println("Compara");
        System.out.println(new BigDecimal("2.00").compareTo(new BigDecimal("1.3")));

        System.out.println("");
        System.out.println("Divide");
        System.out.println(new BigDecimal("2.00").divide(new BigDecimal("2.00")));

        System.out.println("");
        System.out.println("Máximo");
        System.out.println(new BigDecimal("2.00").max(new BigDecimal("1.5")));

        System.out.println("");
        System.out.println("Mínimo");
        System.out.println(new BigDecimal("2.00").min(new BigDecimal("1.6")));

        System.out.println("");
        System.out.println("Potência");
        System.out.println(new BigDecimal("2.00").pow(2));

        System.out.println("");
        System.out.println("Multiplica");
        System.out.println(new BigDecimal("2.00").multiply(new BigDecimal("1.8")));

    }*/

    //DEIXAR A PRIMEIRA LETRA DA STRING EM MAIUSCULO
    public String maiuscula1(String palavra) {
        //betterIdea = Character.toUpperCase(userIdea.charAt(0)) + userIdea.substring(1);
        palavra = palavra.trim();
        palavra = Character.toUpperCase(palavra.charAt(0)) + palavra.substring(1);
        //return palavra.substring(0, 1).toUpperCase() + palavra.substring(1);
        return palavra;
    }

    //SÓ NÚMEROS
    public String soNumeros(String txt) {
        String numero = txt;

        numero = numero.replaceAll("[^0-9]*", "");

        return numero;
    }

    //MASCARA DATA PROTOCOLO
    public String exibirDataProtocolo(String data) {

        String dat = "";
        String dia = "", mes = "", ano = "";
        //
        for (char d : data.toCharArray()) {
            dat = dat + d;

            if (dat.length() <= 4) {
                ano = ano + d;
            } else if (dat.length() <= 6) {
                mes = mes + d;
            } else if (dat.length() <= 8) {
                dia = dia + d;
            }
        }

        dat = dia + "/" + mes + "/" + ano;

        return dat;
    }

    //MASCARA DATA PROTOCOLO
    public String exibirHoraProtocolo(String hora) {

        String hor = "";
        String h = "", m = "", s = "";
        //
        for (char d : hora.toCharArray()) {
            hor = hor + d;

            if (hor.length() <= 2) {
                h = h + d;
            } else if (hor.length() <= 4) {
                m = m + d;
            } else if (hor.length() <= 62) {
                s = s + d;
            }
        }

        hor = h + ":" + m + ":" + s;

        return hor;
    }

    //MODULO 11 PARA GERAR O DIGITO VERIFICADOR DA NOTA NFC-E
    private static int MODULO11 = 11;

    public String digitoVerificado(String chave) {
        int[] pesos = {4, 3, 2, 9, 8, 7, 6, 5};
        int somaPonderada = 0;
        for (int i = 0; i < chave.length(); i++) {
            somaPonderada += pesos[i % 8] * (Integer.parseInt(chave.substring(i, i + 1)));
        }
        int DV = (MODULO11 - somaPonderada % MODULO11);
        if (DV >= 10 || DV == 0 || DV == 1) {
            DV = 0;
        }
        return chave + DV;
    }

    //RETORNA O ID DO ESTADO
    public String idEstado(String uf) {

        String id = null;

        //Codificação da UF definida pelo IBGE:
        switch (uf) {
            //11-Rondônia
            case "RO":
                id = "11";
                break;
            //12-Acre
            case "AC":
                id = "12";
                break;
            //13-Amazonas
            case "AM":
                id = "13";
                break;
            //14-Roraima
            case "RR":
                id = "14";
                break;
            //15-Pará
            case "PA":
                id = "15";
                break;
            //16-Amapá
            case "AP":
                id = "16";
                break;
            //17-Tocantins
            case "TO":
                id = "17";
                break;
            //21-Maranhão
            case "MA":
                id = "21";
                break;
            //22-Piauí
            case "PI":
                id = "22";
                break;
            //23-Ceará
            case "CE":
                id = "23";
                break;
            //24-Rio Grande do Norte
            case "RN":
                id = "24";
                break;
            //25-Paraíba
            case "PB":
                id = "25";
                break;
            //26-Pernambuco
            case "PE":
                id = "26";
                break;
            //27-Alagoas
            case "AL":
                id = "27";
                break;
            //28-Sergipe
            case "SE":
                id = "28";
                break;
            //29-Bahia
            case "BH":
                id = "29";
                break;
            //31-Minas Gerais
            case "MG":
                id = "31";
                break;
            //32-Espírito Santo
            case "ES":
                id = "32";
                break;
            //33-Rio de Janeiro
            case "RJ":
                id = "33";
                break;
            //35-São Paulo
            case "SP":
                id = "35";
                break;
            //41-Paraná
            case "PR":
                id = "41";
                break;
            //42-Santa Catarina
            case "SC":
                id = "42";
                break;
            //43-Rio Grande do Sul
            case "RS":
                id = "43";
                break;
            //50-Mato Grosso do Sul
            case "MS":
                id = "50";
                break;
            //51-Mato Grosso
            case "MT":
                id = "51";
                break;
            //52-Goiás
            case "GO":
                id = "52";
                break;
            //53-Distrito Federal
            case "DF":
                id = "53";
                break;
        }

        return id;
    }

    public static String getSha1Hex(String clearString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static String horaAbreviada(String hora) {
        //hora = hora.substring(0, 3) + "." + hora.substring(3, 6) + "." + hora.substring(6, 9) + "-" + hora.substring(9, 11);
        hora = hora.substring(0, 3) + "." + hora.substring(3, 6) + "." + hora.substring(6, 9) + "-" + hora.substring(9, 11);
        return hora;
    }

    //GERAR CÓDIO RANDOMICO
    public String getRandomNumber(int quatCaracteres, int min, int max) {
        StringBuilder cod = new StringBuilder();
        for (int i = 0; i < quatCaracteres; i++) {
            cod.append(((new Random()).nextInt((max - min) + 1) + min));
        }
        return cod.toString();
    }

    private String unmask(String s) {
        return s.replaceAll("[^0-9]*", "");
    }

    public String mask(String txt) {

        String maskCNPJ = "(##) # ####-####";
        String oldValue = "";

        String str = unmask(txt);

        StringBuilder mascara = new StringBuilder();
        int i = 0;
        for (char m : maskCNPJ.toCharArray()) {
            if ((m != '#' && str.length() > oldValue.length()) || (m != '#' && str.length() < oldValue.length() && str.length() != i)) {
                mascara.append(m);
                continue;
            }

            try {
                mascara.append(str.charAt(i));
            } catch (Exception e) {
                break;
            }
            i++;
        }

        return mascara.toString();
    }
}
