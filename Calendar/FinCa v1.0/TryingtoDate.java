import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZoneId;

public class TryingtoDate {
    public static void main(String args[]) {
      int gmtHour = LocalTime.now(ZoneId.of("GMT")).getHour();
      int mosHour = LocalTime.now(ZoneId.of("Europe/Moscow")).getHour();
      int timeOffset = mosHour - gmtHour;
      LocalDate mosDate = LocalDate.now(ZoneId.of("Europe/Moscow"));
      LocalDateTime dtGMT = LocalDateTime.parse("2020-11-22 21:45:00".replace(" ","T"));
      dtGMT = dtGMT.plusHours(timeOffset);
      System.out.println(dtGMT.toLocalDate().compareTo(mosDate));
      System.out.println("dtGMT: " + dtGMT);
      System.out.println("mosDate: " + mosDate);
      System.out.println("offset: " + timeOffset);
    }
}
