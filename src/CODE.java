import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
public class CODE {
    static class TeamInfo {
        static String[] team1;
        static String[] team2;
        static String[] time;

        static String[] leagues;


        static String[] timeZone;
        static Duration[] timeUntil;
    }

    public static void main(String[] args) throws IOException {
       readAll();
       displayHomepage();
    }

    //scrape and extract all info
    public static void readAll() throws IOException {
        Document doc = Jsoup.connect("https://liquipedia.net/dota2/Liquipedia:Upcoming_and_ongoing_matches").get();
        Elements allTables = doc.select("table");
        TeamInfo.team1 = new String[allTables.size()];
        TeamInfo.team2 = new String[allTables.size()];
        TeamInfo.time = new String[allTables.size()];
        TeamInfo.leagues = new String[allTables.size()];
        TeamInfo.timeUntil = new Duration[allTables.size()];
        TeamInfo.timeZone = new String[allTables.size()];
        int i = 0;
        for (Element element: allTables){
            if (element.text().contains("TBD")){
                continue;
            }
            TeamInfo.team1[i] = extractTeam1(element);
            TeamInfo.team2[i] = extractTeam2(element);
            TeamInfo.time[i] = extractTime(element);
            TeamInfo.leagues[i] = extractLeague(element);
            TeamInfo.timeZone[i] = extractTimezone(element);
            TeamInfo.timeUntil[i] = timeUntilGame(TeamInfo.time[i], i);

            i++;
        }
    }
    //extracting info from scrape
    private static String extractTeam1(Element element){
        return element.select("tr").get(0).select("td").get(0).select("span").get(0).attr("data-highlightingclass");
    }

    private static String extractTeam2(Element element){
        return element.select("tr").get(0).select("td").get(2).select("span").get(0).attr("data-highlightingclass");
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
    private static Duration timeUntilGame(String timeString, int i){
        int[] initialString = cleanDate(timeString);
        ZonedDateTime timeOfGame = ZonedDateTime.of(initialString[2], initialString[0], initialString[1], initialString[3], initialString[4], 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.of(TeamInfo.timeZone[i])));
        ZonedDateTime localtimeOfGame = timeOfGame.withZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();
        Duration timeUntilGame = Duration.between(now, localtimeOfGame);
        return timeUntilGame;
    }


    //ask for preference
    public static void askPreference() throws IOException {
        File allTeams = new File("allTeams");
        Scanner allTeamsScanner = new Scanner(allTeams);
        File preferences = new File ("teamPreferences");
        boolean isRunning = true;
        String[] oldTeamPrefs = new String[countLinesInFile("teamPreferences")];
        if (countLinesInFile("teamPreferences") > 0){
            System.out.println("Here's your current list of preferences.");
            for (int i = 0; i < oldTeamPrefs.length; i++){
                oldTeamPrefs[i] = Files.readAllLines(Paths.get("teamPreferences")).get(i);
                System.out.println(i+1 + "." + oldTeamPrefs[i]);
            }
        }
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter("teamPreferences", false));
        for (String teamPref : oldTeamPrefs){
            outputWriter.write(teamPref);
            outputWriter.newLine();
        }
        while (isRunning){
            System.out.println("Press 2 to add a team, press 1 to remove a team, press 0 to exit.");
            Scanner userInputScanner = new Scanner(System.in);
            int addRemoveOrExit;
            try{
                addRemoveOrExit = userInputScanner.nextInt();
                if (addRemoveOrExit == 2){
                    int i = 1;
                    while (allTeamsScanner.hasNextLine()) {
                        System.out.println(i + "." + allTeamsScanner.nextLine());
                        i++;
                    }
                    System.out.println("Enter the number of the team to add to your preferences.");
                    int numToAdd = userInputScanner.nextInt();
                    String line = Files.readAllLines(Paths.get("allTeams")).get(numToAdd-1);
                    boolean toAdd = true;
                    Scanner scanner = new Scanner("teamPreferences");
                    while (scanner.hasNextLine()){ //needs help
                        String nextLine = scanner.nextLine();
                        if (nextLine.contains(line)){
                            toAdd = false;
                            System.out.println("You already have that in your preferences.");
                        }
                    }
                    if (toAdd){
                        try{
                            outputWriter.write(line);
                            outputWriter.newLine();
                            outputWriter.flush();

                        }
                        catch(Exception e){
                            System.out.println("Dude. Input a number that's within range.");
                        }
                    }
                    System.out.println("Here is your updated list of preferences.");
                    Scanner preferencesScanner = new Scanner(preferences);
                    int j = 1;
                    while (preferencesScanner.hasNextLine()){
                        System.out.println(j + "." + preferencesScanner.nextLine());
                        j++;
                    }
                }
                else if (addRemoveOrExit == 1){
                    if (countLinesInFile("teamPreferences") <= 1){
                        System.out.println("You have to add before you delete, genius.");
                        continue;
                    }
                    String[] teamPrefs = new String[countLinesInFile("teamPreferences")];
                    for (int x = 0; x < teamPrefs.length; x++){
                        teamPrefs[x] = Files.readAllLines(Paths.get("teamPreferences")).get(x);
                    }
                    Scanner preferencesScanner = new Scanner(preferences);
                    int i = 1;
                    while (preferencesScanner.hasNextLine()){
                        System.out.println(i + "." + preferencesScanner.nextLine());
                        i++;
                    }
                    System.out.println("Input the number of the team you want to remove.");
                    BufferedWriter newFileWriter = new BufferedWriter(new FileWriter("updatedPrefs", false));
                    int numToRemove = userInputScanner.nextInt();
                    for (int a = 0; a < teamPrefs.length; a++) {
                        if (a == (numToRemove-1)) continue;
                        newFileWriter.write(teamPrefs[a]);
                        newFileWriter.newLine();
                        newFileWriter.flush();
                    }
                    newFileWriter.close();
                    Files.copy(Path.of("updatedPrefs"), Path.of("teamPreferences"), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Here is your updated list of preferences.");
                    int j = 1;
                    Scanner finalScanner = new Scanner (preferences);
                    while (finalScanner.hasNextLine()){
                        System.out.println(j + "." + finalScanner.nextLine());
                        j++;
                    }
                }
                else if (addRemoveOrExit == 0) isRunning = false;
            }
            catch (Exception e){
                System.out.println("Can you just follow the instructions, thanks.");
            }
        }

    }

    public static int countLinesInFile(String nameOfFile) throws FileNotFoundException {

        File file = new File(nameOfFile);
        Scanner scanner = new Scanner(file);

        int lineCount = 0;

        while (scanner.hasNextLine()) {

            lineCount++;
            scanner.nextLine();

        }

        return lineCount;

    }


    //display all teams
    public static void displayAll(){
        for (int i = 0; i <= 10; i++){
            if (TeamInfo.timeUntil[i].isNegative()){
                continue;
            }
            int minutes = (int) ((int) TeamInfo.timeUntil[i].toMinutes() - 60 * TeamInfo.timeUntil[i].toHours());
            System.out.println("\n" + TeamInfo.team1[i] + "\t vs \t" + TeamInfo.team2[i] + "\n");
            System.out.println(TeamInfo.timeUntil[i].toHours() + "h " + minutes + "m");
            System.out.println(TeamInfo.leagues[i] + "\n");
            System.out.println("*******");
        }
    }

    // display some teams
    public static void displaySome() throws IOException {
        int count = 0;
        if (countLinesInFile("TeamPreferences") < 1){
            System.out.println("\nThere's nothing to display.");
            return;
        }
        String[] teamPrefs = new String[countLinesInFile("teamPreferences")];
        for (int i = 0; i < teamPrefs.length; i++){
            teamPrefs[i] = Files.readAllLines(Paths.get("teamPreferences")).get(i);
        }
        for (int i = 0; i < 30; i++){
            if (TeamInfo.timeUntil[i].isNegative()){
                continue;
            }
            if (TeamInfo.team1[i].isBlank() || TeamInfo.team2[i].isBlank()){
                continue;
            }
            for (String teamPref : teamPrefs) {
                if (TeamInfo.team1[i].contains(teamPref) || TeamInfo.team2[i].contains(teamPref)) {
                    System.out.println("\n" + TeamInfo.team1[i] + "\t vs \t" + TeamInfo.team2[i]);
                    int minutes = (int) ((int) TeamInfo.timeUntil[i].toMinutes() - 60 * TeamInfo.timeUntil[i].toHours());
                    System.out.println(TeamInfo.timeUntil[i].toHours() + "h " + minutes + "m");
                    System.out.println(TeamInfo.leagues[i]);
                    System.out.println("*****");
                    count++;
                }
            }
        }
        if (count == 0){
            System.out.println("\nIt looks like your favourite teams don't love you very much. There's nothing to display.");
        }
    }


    //homepage display things
    public static void displayHomepage() throws IOException {
        boolean isRunning = true;
        while (isRunning){
            System.out.println(DateTimeFormatter.ofPattern("\n \t\t\t\t\t\t\t\t  HH:mm\n \t\t\t\t\t\t\tEEE, MMM dd yyyy").format(ZonedDateTime.now(ZoneId.systemDefault())));
            int hour = ZonedDateTime.now(ZoneId.systemDefault()).getHour();
            if (hour < 12){
                System.out.println("\t\t\t\t\t\t\t  Good morning!");
            }
            else if (hour > 12 && hour < 18){
                System.out.println("\t\t\t\t\t\t\t  Good afternoon");
            }
            else {
                System.out.println("\t\t\t\t\t\t\t  Good evening");
            }
            int musicLine = (int) (Math.random() * (60) + 1);
            int beforeLine = (int) (Math.random() * (13) + 1);
            System.out.println("\n" + Files.readAllLines(Paths.get("beforeMusic")).get(beforeLine - 1) + " " + Files.readAllLines(Paths.get("allSongs")).get(musicLine-1));
            System.out.println("\n\n Press 0 to exit. Press 1 to view all upcoming Dota 2 matches. Press 2 to view upcoming games for your preferred teams. Press 3 to change your preferences.");
            Scanner scanner = new Scanner(System.in);
            int userInput;
            try{
                userInput = scanner.nextInt();
                if (userInput < 0 || userInput > 3){
                    System.out.println("Bestie, that's not a choice.");
                }
                if (userInput == 3) {
                    askPreference();
                }
                if (userInput == 1) displayAll();
                if (userInput ==2) displaySome();
                if (userInput == 0) isRunning = false;
            }
            catch (Exception e){
                System.out.println("Enter a valid option. Use your brain.");
            }
        }

    }




}
