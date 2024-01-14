package tests.games;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SyncMaccabiHaifaGames {

    private static String completeDate;
    private static final String gridURL = "http://localhost:4444/"; //desktop
    public static String db = "u204686394_mishakim"; //REMOTE
    protected static ThreadLocal<RemoteWebDriver> driverContainer = new ThreadLocal<>();

    protected static boolean ENV_TO_TEST = false; //Change to false for local test
    protected static Connection conn = null;


    @Test()
    public static void syncMaccabiHaifa() throws Exception {
        scanGames();
    }

    public static void scanGames() throws Exception {
        try {
            WebDriverManager.chromedriver().setup();
            /**
             * Get read of selenium and chrome logs
             */
            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
            System.setProperty("webdriver.chrome.silentOutput", "true");
            Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
            System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
            /**
             * End of Get read of selenium and chrome logs
             */

            ChromeOptions chromeOptions = new ChromeOptions();
            HashMap<String, Object> chromePref = new HashMap<>();


            chromePref.put("credentials_enable_service", false);
            chromePref.put("profile.password_manager_enabled", false);
            chromeOptions.addArguments("--start-maximized");
            chromeOptions.addArguments("--log-level=3");
            chromeOptions.addArguments("--silent");
//            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--remote-allow-origins=*");
            chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
            chromeOptions.setExperimentalOption("prefs", chromePref);

            if (ENV_TO_TEST) {
                LoggingPreferences logPrefs = new LoggingPreferences();
                logPrefs.enable(LogType.BROWSER, Level.INFO);
                logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
                chromeOptions.setCapability("goog:loggingPrefs", logPrefs.toJson());
                driverContainer.set(new ChromeDriver(chromeOptions));

            } else {
                LoggingPreferences logPrefs = new LoggingPreferences();
                logPrefs.enable(LogType.BROWSER, Level.INFO);
                logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
                chromeOptions.setCapability("goog:loggingPrefs", logPrefs.toJson());
                driverContainer.set(new RemoteWebDriver(new URL(gridURL), chromeOptions));
            }
            driverContainer.get().manage().window().maximize();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


        RemoteWebDriver driver = driverContainer.get();

        driver.get("https://mhaifafc.com/historySearch?cYearComp=1959&CompId=2&lang=he&cMode=COMP#results");


        //Dates:
        List<WebElement> dates = driver.findElements(By.xpath("//tr[@class='data_row youth']//td[2]"));

        for (int i = 0; i < dates.size(); i++) {
            System.out.println(dates.get(i).getText());
        }
        System.out.println("\n\n\n");


        //האם משחק חוץ
        List<WebElement> team1 = driver.findElements(By.xpath("//tr[@class='data_row youth']//td[3]"));
        List<WebElement> team2 = driver.findElements(By.xpath("//tr[@class='data_row youth']//td[4]"));

        for (int i = 0; i < team1.size(); i++) {
            String value = team1.get(i).getText();
            if (team1.get(i).getText().equals("מכבי חיפה")) {
                System.out.println("FALSE");
            } else {
                System.out.println("TRUE");
            }
        }
        System.out.println("\n\n\n");

        for (int i = 0; i < team1.size(); i++) {
            if (team1.get(i).getText().equals("מכבי חיפה")) {
                System.out.println(team2.get(i).getText());
            } else {
                System.out.println(team1.get(i).getText());
            }
        }
        System.out.println("\n\n\n");

        List<WebElement> score = driver.findElements(By.xpath("//tr[@class='data_row youth']//td[5]"));

        //score right
        System.out.println("Maccabi Haifa score: ");
        for (int i = 0; i < score.size(); i++) {
            // Split the string based on ":"
            String scoreMaccabiHaifa = score.get(i).getText(); //the right one
            String[] parts = scoreMaccabiHaifa.split(":");

            if (team1.get(i).getText().equals("מכבי חיפה")) {
                // Extract the second (right) number
                String rightNumber = parts[1].trim();
                System.out.println(rightNumber);
            }else  if (team2.get(i).getText().equals("מכבי חיפה")) {
                // Extract the second (left) number
                String leftNumber = parts[0].trim();
                System.out.println(leftNumber);
            }
        }
        System.out.println("\n\n\n");


        //score left
        System.out.println("Opponent score: ");
        for (int i = 0; i < score.size(); i++) {
            // Split the string based on ":"
            String scoreMaccabiHaifa = score.get(i).getText(); //the right one
            String[] parts = scoreMaccabiHaifa.split(":");

            if (!team1.get(i).getText().equals("מכבי חיפה")) {
                // Extract the second (right) number
                String rightNumber = parts[1].trim();
                System.out.println(rightNumber);
            }else  if (!team2.get(i).getText().equals("מכבי חיפה")) {
                // Extract the second (left) number
                String leftNumber = parts[0].trim();
                System.out.println(leftNumber);
            }
        }
        System.out.println("\n\n\n");



        driverContainer.get().close();
        driverContainer.get().quit();
    }

    public static Date convertStringToDate(String time) throws Exception {
        SimpleDateFormat formatter5 = new SimpleDateFormat("HH:mm");
        Date bewTime = formatter5.parse(time);
        return bewTime;
    }

    public static int getDayNumberOld(String date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(date));
        return c.get(Calendar.DAY_OF_WEEK);
    }

    public static String add1Hour(Date date, int hours) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);

        String originalString = String.valueOf(calendar.getTime());
        Date newDate = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy").parse(originalString);
        String newString = new SimpleDateFormat("HH:mm").format(calendar.getTime()); // 9:00


        System.out.println(newString);
        return newString;
    }

    public static String add1Day(String current, int increaseBy, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(current));
        c.add(Calendar.DATE, increaseBy);  // number of days to add
        current = sdf.format(c.getTime());  // current is now the new date
        return current;
    }

    public String remove1Week(String current, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(current));
        c.add(Calendar.DATE, -7);
        current = sdf.format(c.getTime());
        return current;
    }

    public static String remove1day(String current, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(current));
        c.add(Calendar.DATE, -1);
        current = sdf.format(c.getTime());
        return current;
    }

    public static String getIsoFormat_start(String game_date, String time) throws ParseException {
        //Need to be like this: 20221203T19000000

        String pageDateOnlyDateOppositeYear = null;
        String pageDateOnlyDateOppositeMonth = null;
        String pageDateOnlyDateOppositeDay = null;
        if (game_date.length() == 10) {

            pageDateOnlyDateOppositeYear = game_date.replace("/", "").substring(4);
            pageDateOnlyDateOppositeMonth = game_date.replace("/", "").substring(2, 4);
            pageDateOnlyDateOppositeDay = game_date.replace("/", "").substring(0, 2);

        } else if (game_date.length() == 9) {
            //TODO: if date from page come as 1/12/2022 - need to get length of this date and if its 9 instead of 10 (01/12/2022)

            pageDateOnlyDateOppositeYear = game_date.replace("/", "").substring(3);
            pageDateOnlyDateOppositeMonth = game_date.replace("/", "").substring(1, 3);
            pageDateOnlyDateOppositeDay = game_date.replace("/", "").substring(0, 1);
        }

        completeDate = pageDateOnlyDateOppositeYear + pageDateOnlyDateOppositeMonth + pageDateOnlyDateOppositeDay;


        //Remove ":" from time
        String time_formatted = time.replace(":", "");
        String isoFormat = completeDate + "T" + time_formatted + "0000";
        return isoFormat;
    }

    public static String getIsoFormat_end(String isoFormat_start) throws ParseException {

        //Get HH from combined string (index: 9 + 10)
        char index1 = isoFormat_start.charAt(9);
        char index2 = isoFormat_start.charAt(10);
        String index1_s = "" + index1;
        String index2_s = "" + index2;

        String index12 = index1_s + index2_s;
        int index12_i_upgraded_by_1 = 0;
        String isoFormat_end;
        if (index1_s.equals("0")) {
            //convert to int and increase by 1
            int index2_i = Integer.parseInt(index2_s);
            int index2_upgraded_by_1 = index2_i + 2;
            isoFormat_end = completeDate + "T0" + index2_upgraded_by_1;
        } else if (index12.equals("24")) {
            isoFormat_end = completeDate + "T" + "0100";
        } else {
            int index12_i = Integer.parseInt(index1_s + index2_s);
            index12_i_upgraded_by_1 = index12_i + 1;
            isoFormat_end = completeDate + "T" + index12_i_upgraded_by_1;
        }
        isoFormat_end = isoFormat_end + "000000";
        return isoFormat_end;
    }

    public static Connection mysqlConnect() throws Exception {
        try {

            String url = null;
            String user = null;
            String password = null;
            if (db.equals("mishakim")) {
                //LOCAL
                url = "jdbc:mysql://127.0.0.1:3306/mishakim?useSSL=false&allowLoadLocalInfile=true";
                user = "root";
                password = "root";
            } else if (db.equals("u204686394_mishakim")) {
//            //REMOTE
                url = "jdbc:mysql://191.96.56.154:3306/u204686394_mishakim?useSSL=false&allowLoadLocalInfile=true";
                user = "u204686394_mishakim";
                password = "Mishakim!@#$11";

            }


            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new Exception("Not connected to DB: " + e);
        }
        return conn;
    }

    public static List<String> executeSelectQuery(String selectQuery, String columnLabel) throws SQLException {
        System.out.println("\nDB query: " + selectQuery + ":\n");
        List lines;
        List lines_comp = new ArrayList();
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery);
            while (resultSet.next()) {
                System.out.println(resultSet.getString(columnLabel));
                lines = new ArrayList();
                lines.add(resultSet.getString(columnLabel));
                lines_comp.add(lines.get(0));
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (selectQuery.contains("*")) {
            System.out.println("(For column: " + columnLabel + ")");
        } else {
            System.out.println("\n");
        }
        return lines_comp;
    }

    public static void executeUpdate(String updateQuery) throws Exception {
        try {
            System.out.println(updateQuery);
            Statement statement = conn.createStatement();
            statement.executeUpdate(updateQuery);
        } catch (SQLException e) {
            throw e;
        }
    }
}