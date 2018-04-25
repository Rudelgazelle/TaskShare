package android_development.taskshare;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class UniqueIdGenerator {

    public int createID(){
        Date now = new Date();

        //int idDate = Integer.parseInt(new SimpleDateFormat("ddHHmmssSS",  Locale.getDefault()).format(now));

        int idDate = (int) (System.nanoTime() & 0xfffffff);

        int uniqueId = idDate;
        return uniqueId;
    }

}
