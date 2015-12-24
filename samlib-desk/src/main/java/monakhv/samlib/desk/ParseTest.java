package monakhv.samlib.desk;

import monakhv.samlib.db.entity.Author;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.exception.SamlibInterruptException;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.http.HttpClientController;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by monakhv on 21.12.15.
 */
public class ParseTest {

    public ParseTest() throws SQLException, ClassNotFoundException {

    }

    public static void main(String...args) throws SamlibParseException, SamlibInterruptException, IOException {

        System.out.println("Start");
        Settings settings = Settings.getInstance();
        HttpClientController http = HttpClientController.getInstance(settings);

        Author a = new Author();
        //String link ="/d/demchenko_aw/";
        String link ="/m/metelxskij_n_a/";

        a = http.getAuthorByURLNew( link,a);


    }
}
