package gp.wagner.backend.infrastructure;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

@Component("Utils")
public class Utils {

    //Генерация значений
    private static Random rand = new Random();


    //Формат вывода вещественных чисел
    public static DecimalFormat doubleFormatter = new DecimalFormat("#0.00");

    //Формат вывода даты
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    //Убрать из url лишние префиксы и слэши - через регулярку
    public static String cleanUrl(String fileUri) {
        return fileUri.replaceAll("(URL|URL=|url=)|[\\[\\]]", "").trim();
    }

    //NullPointerException безопасный метод конвертации строки в integer
    public static Integer TryParseInt(String str) {
        int result;

        try {
            result = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
        return result;
    }
    public static Long TryParseLong(String str) {
        long result;

        try {
            result = Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
        return result;
    }

    public Utils() {
    }

    //Получение случайных значений
    public static double getRandom(double lo, double hi) {
        return lo + rand.nextDouble() * (hi - lo);
    }

    public static int getRandom(int lo, int hi) {
        return lo + rand.nextInt(hi - lo);
    }
    public static long getRandom(long lo, long hi) {
        return lo + rand.nextLong(hi - lo);
    }

    //Найти индекс последнего символа из заданного в строке
    public static int findLastIndex(String str, String ch){

        char[] chars = str.toCharArray();
        char[] checkingChars = ch.toCharArray();

        //Если текущий символ входит хотя бы в 1 символ из ch, тогда возвращаем индекс элемента
        for (int i = chars.length - 1; i >= 0; i--) {
            for (char c : checkingChars) {
                if (c == chars[i])
                    return i;
            }//for
        }//for

        return 0;
    }

    public static String[] splitPrices(String str){

        return new String[0];
    }

    //Проверка типа кодировки строки
    public static boolean checkCharset(String str, Charset charset){

        byte[] bytes = str.getBytes();

        //Перезаписать байты строки в CharBuffer для последующей проверки байт, которые не сконвертировались
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(bytes));

        return charBuffer.remaining() == 0;
    }

    //Record-класс для задания id Товара и категории
    public record CategoryAndProductIds(long categoryId, long productId) {
    }

    // Генерация кода заказа
    public static long generateOrderCode(long customerId){

        String formedNumber = String.format("%d%d",customerId , getRandom(10000L, 99_999L));

        Long resultNumber = TryParseLong(formedNumber);

        return resultNumber != null ? resultNumber : getRandom(10_000_000L, 99_999_999L);
    }

}
