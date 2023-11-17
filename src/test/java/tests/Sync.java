package tests;

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

public class Sync {

    private static String completeDate;
    private static final String gridURL = "http://localhost:4444/"; //desktop
    public static String db = "u204686394_mishakim"; //REMOTE
    protected static ThreadLocal<RemoteWebDriver> driverContainer = new ThreadLocal<>();

    protected static boolean ENV_TO_TEST = false; //Change to false for local test
    protected static Connection conn = null;


    @Test()
    public static void sync() throws Exception {
        List<Integer> increaseDayBy = new ArrayList<>();
        increaseDayBy.add(0);
        increaseDayBy.add(1);
        increaseDayBy.add(2);
        increaseDayBy.add(3);
        increaseDayBy.add(4);
        increaseDayBy.add(5);
        increaseDayBy.add(6);

        List<String> xpathIndex = new ArrayList<>();
        xpathIndex.add("1");
        xpathIndex.add("2");
        xpathIndex.add("3");
        xpathIndex.add("4");
        xpathIndex.add("5");
        xpathIndex.add("6");
        xpathIndex.add("7");

        for (int i = 0; i < increaseDayBy.size(); i++) {
            scanGames(increaseDayBy.get(i), xpathIndex.get(i));
        }

    }

    public static void scanGames(int increaseDayBy, String xpathIndex) throws Exception {
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

        driver.get("https://www.telesport.co.il/%D7%A9%D7%99%D7%93%D7%95%D7%A8%D7%99%20%D7%A1%D7%A4%D7%95%D7%A8%D7%98");

        //First get the day on screen:
        String pageDate1 = driver.findElement(By.className("current")).getText(); //  01/12/22
        String pageDateIncreased1 = add1Day(pageDate1, increaseDayBy, "dd/MM/yy");

        String pageDate2 = pageDate1.replace("/22", "/2022").replace("/23", "/2023"); //  01/12/2022
        String pageDateIncreased2 = add1Day(pageDate2, increaseDayBy, "dd/MM/yyyy");

        mysqlConnect();

        //First delete all the past records of last week
        String yesterday = remove1day(pageDate2, "dd/MM/yyyy");
        executeUpdate("DELETE from `" + db + "`.`games` where game_date = '" + yesterday + "'");


        //Click on the date
        try {
            driver.findElement(By.linkText(pageDateIncreased1)).click();
//            driver.findElement(By.xpath("//*[text()='" + pageDateIncreased1 + "']")).click();
        } catch (Exception e) {
            System.out.println(pageDateIncreased1 + " wasn't found.");
            throw new Exception(pageDateIncreased1 + " wasn't found. Opps we have a problem.");
        }

        //day of the week:
        int day_int = getDayNumberOld(pageDateIncreased2, "dd/MM/yyyy");
        String day = null;
        switch (day_int) {
            case 1:
                day = "sunday";
                break;
            case 2:
                day = "monday";
                break;
            case 3:
                day = "tuesday";
                break;
            case 4:
                day = "wednesday";
                break;
            case 5:
                day = "thursday";
                break;
            case 6:
                day = "friday";
                break;
            case 7:
                day = "saturday";
                break;
        }


        //Delete all records for this day
        executeUpdate("DELETE from `" + db + "`.`games` where day = '" + day + "'");


        List<WebElement> channels = driver.findElements(By.xpath("//div[@id='bigContext']/div[1]/div[" + xpathIndex + "]/div/div[2]")); //Need to remove first object
        List<WebElement> games = driver.findElements(By.xpath("//div[@id='bigContext']/div[1]/div[" + xpathIndex + "]/div/div[4]"));
        List<WebElement> times = driver.findElements(By.xpath("//div[@id='bigContext']/div[1]/div[" + xpathIndex + "]/div/div[3]")); //Need to remove first object

        try {
            channels.remove(0);
        } catch (Exception e) {
            throw new Exception("Opps we have a problem. ");
        }
        times.remove(0);


        for (int i = 0; i < games.size(); i++) {
            System.out.println(channels.get(i).getText());
            System.out.println(games.get(i).getText());
            System.out.println(times.get(i).getText());


            String value = games.get(i).getText();

            if (value.contains("כדורגל")) {
                if (value.contains("ישראל") || value.contains("הכוכב האדום") || value.contains("ריאל מדריד") || value.contains("מכבי חיפה") || value.contains("מכבי תל אביב") || value.contains("הפועל תל אביב") || value.contains("ברצלונה")) {
                    //increase time by 1 hour
                    Date newTime = convertStringToDate(times.get(i).getText());
                    String time_end = add1Hour(newTime, 2);

                    //trim game name
                    String game_name_trim = games.get(i).getText().replace("'", "").replace("\"", "");

                    //Convert date to ISO format for web:
                    String isoFormat_start = getIsoFormat_start(pageDateIncreased2, times.get(i).getText());
                    String isoFormat_end = getIsoFormat_end(isoFormat_start);

                    //First see if record already saved:
                    try {
                        List<String> id = executeSelectQuery("SELECT * FROM `u204686394_mishakim`.`games` where game_name = '" + game_name_trim + "' and game_date = '" + pageDateIncreased2 + "' and time = '" + times.get(i).getText() + "'", "id");
                        if (id.size() == 0) {
                            System.out.println("Record not exists");

                            executeUpdate("INSERT INTO `" + db + "`.`games`" +
                                    "(\n" +
                                    "`game_date`,\n" +
                                    "`cal_start`,\n" +
                                    "`cal_end`,\n" +
                                    "`time`,\n" +
                                    "`channel`,\n" +
                                    "`game_name`,\n" +
                                    "`color`,\n" +
                                    "`day`,\n" +
                                    "`isoFormat_start`,\n" +
                                    "`isoFormat_end`)\n" +
                                    "VALUES\n" +
                                    "(\n" +
                                    "'" + pageDateIncreased2 + "',\n" +
                                    "'" + pageDateIncreased2 + " " + times.get(i).getText() + "',\n" +
                                    "'" + pageDateIncreased2 + " " + time_end + "',\n" +
                                    "'" + times.get(i).getText() + "',\n" +
                                    "'" + channels.get(i).getText() + "',\n" +
                                    "'" + game_name_trim + "',\n" +
                                    "'white',\n" +
                                    "'" + day + "',\n" +
                                    "'" + isoFormat_start + "',\n" +
                                    "'" + isoFormat_end + "');");

                        } else {
                            System.out.println("Record already exists");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            if (value.contains("כדורסל")) {
                if (value.contains("מכבי תל אביב") || value.contains("הפועל ירושלים") || value.contains("ישראל")) {
                    //increase time by 1 hour
                    Date newTime = convertStringToDate(times.get(i).getText());
                    String time_end = add1Hour(newTime, 2);

                    //trim game name
                    String game_name_trim = games.get(i).getText().replace("'", "").replace("\"", "");

                    //Convert date to ISO format for web:
                    String isoFormat_start = getIsoFormat_start(pageDateIncreased2, times.get(i).getText());
                    String isoFormat_end = getIsoFormat_end(isoFormat_start);

                    //First see if record already saved:
                    try {
                        List<String> id = executeSelectQuery("SELECT * FROM `u204686394_mishakim`.`games` where game_name = '" + game_name_trim + "' and game_date = '" + pageDateIncreased2 + "' and time = '" + times.get(i).getText() + "'", "id");
                        if (id.size() == 0) {
                            System.out.println("Record not exists");

                            executeUpdate("INSERT INTO `" + db + "`.`games`" +
                                    "(\n" +
                                    "`game_date`,\n" +
                                    "`cal_start`,\n" +
                                    "`cal_end`,\n" +
                                    "`time`,\n" +
                                    "`channel`,\n" +
                                    "`game_name`,\n" +
                                    "`color`,\n" +
                                    "`day`,\n" +
                                    "`isoFormat_start`,\n" +
                                    "`isoFormat_end`)\n" +
                                    "VALUES\n" +
                                    "(\n" +
                                    "'" + pageDateIncreased2 + "',\n" +
                                    "'" + pageDateIncreased2 + " " + times.get(i).getText() + "',\n" +
                                    "'" + pageDateIncreased2 + " " + time_end + "',\n" +
                                    "'" + times.get(i).getText() + "',\n" +
                                    "'" + channels.get(i).getText() + "',\n" +
                                    "'" + game_name_trim + "',\n" +
                                    "'white',\n" +
                                    "'" + day + "',\n" +
                                    "'" + isoFormat_start + "',\n" +
                                    "'" + isoFormat_end + "');");

                        } else {
                            System.out.println("Record already exists");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
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