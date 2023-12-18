import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.sql.SQLOutput;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CODE {
    static class TeamInfo {
        String[] team1;
        String[] team2;
        static String[] time;

        String[] leagues;


        static String[] timeZone;
        String[] preferredTeams;
        static Duration[] timeUntil;
    }
    public static void main(String[] args) throws IOException {
        readAll();
    }




    //scrape
    public static void readAll() throws IOException {
        Document doc = Jsoup.connect("https://liquipedia.net/dota2/Liquipedia:Upcoming_and_ongoing_matches").get();
        Elements allTables = doc.select("table");
        TeamInfo info = new TeamInfo();
        info.team1 = new String[allTables.size()];
        info.team2 = new String[allTables.size()];
        TeamInfo.time = new String[allTables.size()];
        info.leagues = new String[allTables.size()];
        info.timeUntil = new Duration[allTables.size()];
        TeamInfo.timeZone = new String[allTables.size()];
        int i = 0;
        for (Element element: allTables){
            if (element.text().contains("TBD")){
                continue;
            }
            info.team1[i] = extractTeam1(element);
            info.team2[i] = extractTeam2(element);
            TeamInfo.time[i] = extractTime(element);
            info.leagues[i] = extractLeague(element);
            TeamInfo.timeZone[i] = extractTimezone(element);
            TeamInfo.timeUntil[i] = timeUntilGame(TeamInfo.time[i], i);

            System.out.println(info.team1[i]);
            System.out.println(info.team2[i]);
            System.out.println(TeamInfo.time[i]);
            System.out.println(info.leagues[i]);
            System.out.println(TeamInfo.timeZone[i]);
            System.out.println(TeamInfo.timeUntil[i]);

            System.out.println("*****");
            i++;
        }
    }
    //extracting info from scrape
    private static String extractTeam1(Element element){
        return element.select("tr").get(0).select("td").get(0).text();
    }

    private static String extractTeam2(Element element){
        return element.select("tr").get(0).select("td").get(2).text();
    }

    private static String extractTime(Element element){
        String text = element.select("tr").get(1).select("td").text();
        int space = 0;
        String finalText = "";
        for (int i = 0; i < text.length(); i++){
            if (text.charAt(i) == ' '){
                space++;
            }
            if (space <5){
                finalText += text.charAt(i);
            }
            if (space >= 5){
                break;
            }
        }
        return finalText;
    }

    private static String extractLeague(Element element){
        String text = element.text();
        int start = 0;
        for (int i = text.length()-1; i > 0; i--){
            if (text.charAt(i-2) == ':' && text.charAt(i-1) < 58 && text.charAt(i-1) > 47){
                start = i+6;
                break;
            }
        }
        return text.substring(start);
    }

    private static String extractTimezone(Element element){
        return element.select("tr").get(1).select("td").select("abbr").attr("data-tz");
    }
    //time
   private static int[] cleanDate(String date){
        // input: "month day, year - hour:minute", return [month, day, year, hour, minute]
       String[] dateArray = date.split(" ");
        int[] timeArray = new int[5];
        int month = 0;
        switch (dateArray[0]) {
           case "January" -> month = 1;
           case "February" -> month = 2;
           case "March" -> month = 3;
           case "April" -> month = 4;
           case "May" -> month = 5;
           case "June" -> month = 6;
           case "July" -> month = 7;
           case "August" -> month = 8;
           case "September" -> month = 9;
           case "October" -> month = 10;
           case "November" -> month = 11;
           case "December" -> month = 12;
        }
        timeArray[0] = month;
        timeArray[1] = Integer.parseInt(dateArray[1].replace(",", ""));
        timeArray[2] = Integer.parseInt(dateArray[2]);
        timeArray[3] = Integer.parseInt(dateArray[4].substring(0, 2));
        timeArray[4] = Integer.parseInt(dateArray[4].substring(3, 5));
        return timeArray;
    }

    //return [month, day, year, hour, minute]
    //zoneddatetime requires [year, month, day, hour, minute]
    //

    private static Duration timeUntilGame(String timeString, int i){
        int[] initialString = cleanDate(timeString);
        ZonedDateTime timeOfGame = ZonedDateTime.of(initialString[2], initialString[0], initialString[1], initialString[3], initialString[4], 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.of(TeamInfo.timeZone[i])));
        ZonedDateTime localtimeOfGame = timeOfGame.withZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();
        Duration timeUntilGame = Duration.between(now, localtimeOfGame);
        return timeUntilGame;
    }

}
