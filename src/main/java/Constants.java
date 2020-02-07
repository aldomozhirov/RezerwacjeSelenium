import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String TERMS_PAGE_URL_TEMPLATE = "http://rezerwacje.duw.pl/reservations/pol/queues/%d/%d";

    public static final Map<Integer, String> MONTHS = new HashMap<Integer, String>() {{
        put(1, "Styczeń");
        put(2, "Luty");
        put(3, "Marzec");
        put(4, "Kwiecień");
        put(5, "Maj");
        put(6, "Czerwiec");
        put(7, "Lipiec");
        put(8, "Sierpień");
        put(9, "Wrzesień");
        put(10, "Październik");
        put(11, "Listopad");
        put(12, "Grudzień");
    }};

}