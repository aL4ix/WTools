package gold;

import com.opencsv.exceptions.CsvValidationException;
import main.Main;

import java.io.IOException;

public class GoldMain {
    public static void main(String[] args) throws IOException, CsvValidationException {
        Main.Configuration configuration = Main.getConfiguration();
        Goldmine.logTime(configuration.username(), configuration.password(), configuration.browser(),
                "https://goldmine.com");
    }
}
