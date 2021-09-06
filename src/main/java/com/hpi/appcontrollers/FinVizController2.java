package com.hpi.appcontrollers;

import com.hpi.entities.FinVizEquityInfoModel;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FinVizController2 {

    private static int iRow;
    private static int iTotalRows;
    private static Element dataTable;
    private static final ArrayList<FinVizEquityInfoModel> EQUITY_INFO_MODELS;
    private static final CMProgressBarCLI BAR_CLI;

    static {
        BAR_CLI = new CMProgressBarCLI(CmdLineController.getsCLIProgressBar());
        EQUITY_INFO_MODELS = new ArrayList<>();
    }

    public static final void doEquityInfo() {
        String s, sql, sql1;
        int rowCount;

        // first time through will get us the total row count
        iRow = 0;
        iTotalRows = 0;

        getDataTable();

        BAR_CLI.barUpdate(0, iTotalRows * 2);

        iRow++;
        while (iRow <= iTotalRows) {
            //finViz tables have 20 rows; do 4 tables before wait
            //point is to not trigger the 'too many requests' fail
            if (iRow > 1 && ((iRow - 1) / 80.0) == ((iRow - 1) / 80)) {
                try {
                    Thread.sleep(3000);
                }
                catch (InterruptedException ex) {
                    //Logger.getLogger(FinVizController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            getDataTable();
            BAR_CLI.barUpdate(iRow, iTotalRows * 2);

            doEquity();
        }

//        sql = "";
        sql1
                = "insert ignore into hlhtxc5_dmOfx.EquityInfo (Ticker, Company, Sector, Industry, Country, `MktCap(B)`, PE, FwdPE, PEG, `Div`, PayoutRatio, EPS, `EPS/CY`, `EPS/NY`, `EPS/P5Y`, `EPS/N5Y`, ATR, SMA20, SMA50, SMA200, `50dHi`, `50dLo`, `52wHi`, `52wLo`, RSI, AnRec, Price, Volume, EarnDate, TgtPrice, `Date`, Beta) VALUES ";
        sql1 += "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        rowCount = 0;
        try (Connection con = CMDBController.getConnection();
                //Statement stmt = con.createStatement();
                PreparedStatement ps = con.prepareStatement(sql1);) {
            con.setAutoCommit(false);
            
            for (FinVizEquityInfoModel eim : EQUITY_INFO_MODELS) {
                iRow++;
                rowCount++;
                BAR_CLI.barUpdate(iRow, iTotalRows * 2);

                ps.setString(1, eim.getTkr());
                ps.setString(2, eim.getCompany());
                ps.setString(3, eim.getSector());
                ps.setString(4, eim.getIndustry());
                ps.setString(5, eim.getCountry());

                ps.setString(6, eim.getMktCap());
                ps.setString(7, eim.getPE());
                ps.setString(8, eim.getFwdPE());
                ps.setString(9, eim.getPEG());
                ps.setString(10, eim.getDiv());

                ps.setString(11, eim.getPayoutRatio());
                ps.setString(12, eim.getEPS());
                ps.setString(13, eim.getEPSCY());
                ps.setString(14, eim.getEPSNY());
                ps.setString(15, eim.getEPSP5Y());

                ps.setString(16, eim.getEPSN5Y());
                ps.setString(17, eim.getATR());
                ps.setString(18, eim.getSMA20());
                ps.setString(19, eim.getSMA50());
                ps.setString(20, eim.getSMA200());

                ps.setString(21, eim.getHi50d());
                ps.setString(22, eim.getLo50d());
                ps.setString(23, eim.getHi52w());
                ps.setString(24, eim.getLo52w());
                ps.setString(25, eim.getRSI());

                ps.setString(26, eim.getAnRec());
                ps.setString(27, eim.getPrice());
                ps.setString(28, eim.getVolume());
                ps.setString(29, eim.getEarnDt());
                ps.setString(30, eim.getTgtPrice());

                String st = CMHPIUtils.getShortDateISO();
                LocalDate ld = LocalDate.parse(st);
                ps.setDate(31, java.sql.Date.valueOf(ld));

                ps.setString(32, eim.getBeta());

                ps.addBatch();

                rowCount = 0;
//                sql = "";
            }

            ps.executeBatch();
        }
        catch (SQLException e) {
            s = String.format(CMLanguageController.
                    getErrorProps().getProperty("GeneralError"),
                    e.toString());

            CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getErrorProps().
                            getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].
                            getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                            getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
        }

//        if (rowCount > 0) {
//            try (Connection con = CMDBController.getConnection();
//                    Statement stmt = con.createStatement();) {
//                sql = sql.substring(0, (sql.length() - 2)) + ";";
//                stmt.execute(sql1 + sql);
//                stmt.close();
//                con.close();
//            }
//            catch (SQLException e) {
//                s = String.format(CMLanguageController.
//                        getErrorProps().getProperty("GeneralError"),
//                        e.toString());
//
//                CMHPIUtils.showDefaultMsg(
//                        CMLanguageController.getErrorProps().
//                                getProperty("Title"),
//                        Thread.currentThread().getStackTrace()[1].
//                                getClassName(),
//                        Thread.currentThread().getStackTrace()[1].
//                                getMethodName(),
//                        s,
//                        JOptionPane.ERROR_MESSAGE);
//            }
//        }

        // report number of rows added
        s = String.format(CMLanguageController.
                getAppProp("EquityInfoReturn"),
                Integer.toString(iTotalRows));

        CMHPIUtils.showDefaultMsg(
                "\r\n" + CMLanguageController.getAppProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void getDataTable() {
        // &r= establishes first row to display; iterate through those at 20 each
        // read this from file
        //String sCustom = "http://finviz.com/screener.ashx?v=152&c=0,1,2,3,4,5,"
        //    + "6,7,8,9,11,14,15,16,17,18,19,20,22,26,28,29,31,35,36,39,40,41,"
        //    + "42,43,44,45,46,47,48,50,51,52,53,54,55,56,57,58,59,61,62,63,64,"
        //    + "65,66,67,68&r=";
        // append the row count

        String sCustom, charset, sUrlQuery, sTxt, s;
        Boolean finVizError;
        // StringBuilder sb;
//        int MAX_REDIRECTS;
        int intStatus, i;
        URL url;
        HttpsURLConnection connection;
        Document doc;
        int iTotalRows2;
        Iterator<Element> iteratorT;

        finVizError = true;
        iteratorT = null;

//        sCustom = "v=152&#038;c=0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70&#038;f=sh_opt_option";
//        sCustom = "v=152&f=sh_opt_option";
//        sCustom = "v=152&c=1,2,3,4,5,6,7,8,9,14,15,16,17,18,19,20,49,52,53,54,55,56,57,58,59,62,65,67,68,69&f=sh_opt_option";
        sCustom
                = "v=152&c=1,2,3,4,5,6,7,8,9,14,15,16,17,18,19,20,49,52,53,54,55,56,57,58,59,62,65,67,68,69,48";
        sCustom += "&r=";

        sUrlQuery = "https://finviz.com/screener.ashx";
        sUrlQuery += "?" + sCustom;

//        MAX_REDIRECTS = 5;
        charset = "UTF-8";

        try {
            while (finVizError) {
                sUrlQuery = sUrlQuery + Integer.toString(iRow);
                url = new URL(sUrlQuery);
                connection = (HttpsURLConnection) url.openConnection();

                ((HttpsURLConnection) connection).
                        setHostnameVerifier(new MyHostnameVerifier());
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=" + charset);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept-Charset", charset);
                connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                connection.addRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9"
                        + ".2.3) Gecko/20100401");
                connection.setDoOutput(false);
                intStatus = connection.getResponseCode();
                if (intStatus != HttpURLConnection.HTTP_OK) {
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("GeneralError"),
                            Integer.toString(intStatus));

                    CMHPIUtils.showDefaultMsg(
                            CMLanguageController.getErrorProps().
                                    getProperty("Title"),
                            Thread.currentThread().getStackTrace()[1].getClassName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                /*
             * If the HTTP response code is 4nn (Client Error) or 5nn (Server
             * Error),
             * then you may want to read the HttpURLConnection#getErrorStream()
             * to see
             * if the server has sent any useful error information.
             *
             * InputStream error = ((HttpURLConnection)
             * connection).getErrorStream();
             *
             * If the HTTP response code is -1, then something went wrong with
             * connection and response handling. The HttpURLConnection
             * implementation
             * is somewhat buggy with keeping connections alive. You may want to
             * turn
             * it off by setting the http.keepAlive system property to false.
             * You can
             * do this programmatically in the beginning of your application by:
             * System.setProperty("http.keepAlive", "false");
                 */
                // we specified UTF-8, so byte sized characters
//            sb = new StringBuilder();
//
//            try (BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream())))
//            {
//
//                while ((s = reader.readLine()) != null)
//                {
//                    sb.append(s);
//                }
//            }
//            catch (IOException e)
//            {
//
//            }
                doc = Jsoup.parse(connection.getInputStream(), charset, "");

                iteratorT = doc.select("table").iterator();
                //finVizError = (Jsoup.parse(connection.getInputStream(), charset, "")).body().text();
                if (doc.body().text().equalsIgnoreCase("Too many requests.")) {
                    try {
                        Thread.sleep(15000);
                        System.out.println("Too many requests triggered; rows: " + iRow);
                    }
                    catch (InterruptedException ex) {
                        //Logger.getLogger(FinVizController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    finVizError = false;
                }

            }

            if (!iteratorT.hasNext()) {
                s = String.format(CMLanguageController.
                        getErrorProps().getProperty("Formatted16"),
                        sUrlQuery);

                CMHPIUtils.showDefaultMsg(
                        CMLanguageController.getErrorProps().
                                getProperty("Title"),
                        Thread.currentThread().getStackTrace()[1].getClassName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        s,
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            i = 0;
            sTxt = "";
            while (iteratorT.hasNext()) {
                if (++i > 9) {
                    //get past the header info
                    break;
                }
                sTxt = iteratorT.next().text();
            }

            // At table with total row count
            // sTxt: Total: 7031 #1 save as portfolio
            if (iRow == 0) {
                sTxt = sTxt.substring(sTxt.indexOf(' ') + 1);
                sTxt = sTxt.substring(0, sTxt.indexOf(' '));
                iTotalRows = Integer.parseInt(sTxt);
            } else {
                // each page call will give a new total row number.
                // so long as it is the same as the original, fine.
                // otherwise, it's an error as the data changed.
                // easiest out is to show an error and stop.
                sTxt = sTxt.substring(sTxt.indexOf(' ') + 1);
                sTxt = sTxt.substring(0, sTxt.indexOf(' '));
                iTotalRows2 = Integer.parseInt(sTxt);
                if (iTotalRows2 != iTotalRows) {
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted17"),
                            sUrlQuery);

                    CMHPIUtils.showDefaultMsg(
                            CMLanguageController.getErrorProps().
                                    getProperty("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                    getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                    getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // next table has data
            dataTable = iteratorT.next();
        }
        catch (UnknownHostException e) {
            s = String.format(CMLanguageController.
                    getErrorProps().getProperty("Formatted18"),
                    sUrlQuery);

            CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getErrorProps().
                            getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e) {
            s = String.format(CMLanguageController.
                    getErrorProps().getProperty("General"),
                    e.getMessage());

            CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getErrorProps().
                            getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void doEquity() {
        String sCell;
        Iterator<Element> iteratorR;
        Element aElement;
        int intC;
        FinVizEquityInfoModel equityInfoModel;

        iteratorR = dataTable.select("tr").iterator();
        int intR = 0;

        while (iteratorR.hasNext()) {
            // iterate through rows, keep No. to use on next request
            intR++;
            if (intR == 1) {
                // first row is headers
                iteratorR.next();
                continue;
            }

            iRow++;
            BAR_CLI.barUpdate(iRow, iTotalRows * 2);

            // have a row
            equityInfoModel = new FinVizEquityInfoModel();

            aElement = iteratorR.next();
            Iterator<Element> iteratorC
                    = aElement.select("td").iterator();

            intC = 0;

            while (iteratorC.hasNext()) {
                intC++;
                // iterate through cells of row
                sCell = iteratorC.next().text();

                if (sCell.contains("<")) {
                    sCell = sCell.substring(0, sCell.indexOf('<'));
                }

                switch (intC) {
                    case 1: // Ticker
                        sCell = sCell.replaceAll(" ", "");
                        equityInfoModel.setTkr(sCell);
                        break;
                    case 2: // Company
                        equityInfoModel.setCompany(sCell);
                        break;
                    case 3: // Sector
                        equityInfoModel.setSector(sCell);
                        break;
                    case 4: // Industry
                        equityInfoModel.setIndustry(sCell);
                        break;
                    case 5: // Country
                        equityInfoModel.setCountry(sCell);
                        break;
                    case 6: // Mkt Cap
                        sCell = sCell.replaceAll(" ", "");
                        sCell = toBillion(sCell);
                        equityInfoModel.setMktCap(sCell);
                        break;
                    case 7: // PE
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPE(sCell);
                        break;
                    case 8: // FwdPE
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setFwdPE(sCell);
                        break;
                    case 9: // PEG
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPEG(sCell);
                        break;
                    case 10: // Div
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setDiv(sCell);
                        break;
                    case 11: // Payout Ratio
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPayoutRatio(sCell);
                        break;
                    case 12: // EPS
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPS(sCell);
                        break;
                    case 13: // EPS this year
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSCY(sCell);
                        break;
                    case 14: // EPS next year
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSNY(sCell);
                        break;
                    case 15: // EPS past 5
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSP5Y(sCell);
                        break;
                    case 16: // EPS next 5
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setEPSN5Y(sCell);
                        break;
                    case 17: // ATR
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setATR(sCell);
                        break;
                    case 18: // sma20
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setSMA20(sCell);
                        break;
                    case 19: // sma50
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setSMA50(sCell);
                        break;
                    case 20: // sma200
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setSMA200(sCell);
                        break;
                    case 21: // 50d hi
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setHi50d(sCell);
                        break;
                    case 22: // 50d lo
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setLo50d(sCell);
                        break;
                    case 23: // 52w hi
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setHi52w(sCell);
                        break;
                    case 24: // 52w lo
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll("%", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setLo52w(sCell);
                        break;
                    case 25: // RSI
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setRSI(sCell);
                        break;
                    case 26: // Analyst recommendation
                        sCell = sCell.replaceAll(" ", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setAnRec(sCell);
                        break;
                    case 27: // Price
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setPrice(sCell);
                        break;
                    case 28: // Volume
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setVolume(sCell);
                        break;
                    case 29: // earnings: Aug 17, Aug 17/a, Aug 17/b
                        if (sCell.endsWith("a")) {
                            sCell = sCell.replace("/a", "");
                            sCell = earningsDate(sCell);
                            sCell += " ";
                            sCell += "16:16:16";
                            equityInfoModel.setEarnDt(sCell);
                        } else if (sCell.endsWith("b")) {
                            sCell = sCell.replace("/b", "");
                            sCell = earningsDate(sCell);
                            sCell += " ";
                            sCell += "05:05:05";
                            equityInfoModel.setEarnDt(sCell);
                        } else {
                            sCell = earningsDate(sCell);
                            sCell += " ";
                            sCell += "12:12:12";
                            equityInfoModel.setEarnDt(sCell);
                        }
                        break;
                    case 30: // Target Price
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setTgtPrice(sCell);
                        break;
                    case 31: // Beta
                        sCell = sCell.replaceAll(" ", "");
                        sCell = sCell.replaceAll(",", "");
                        if (sCell.equalsIgnoreCase("-")) {
                            sCell = "-999.999";
                        }
                        equityInfoModel.setBeta(sCell);
                        break;
                    default:
                }
            }

            // put it into the database
            EQUITY_INFO_MODELS.add(equityInfoModel);
        }
    }

    private static String toBillion(String sString) {
        Double dDouble;
        if (sString.equalsIgnoreCase("-")) {
            return "-999.999";
        }
        // can have M, B. Make all be in billions
        if (sString.endsWith("B")) {
            sString = sString.replace("B", "");
            return sString;
        }

        if (sString.endsWith("M")) {
            sString = sString.replace("M", "");
            dDouble = Double.parseDouble(sString) / 1000.00;
            sString = dDouble.toString();
            return sString;
        }

        return sString;
    }

    private String toMillion(String sString) {
        Double dDouble;
        if (sString.equalsIgnoreCase("-")) {
            return "-999.999";
        }
        // can have M, K. Make all M
        if (sString.endsWith("M")) {
            sString = sString.replace("M", "");
            return sString;
        }

        if (sString.endsWith("K")) {
            sString = sString.replace("K", "");
            dDouble = Double.parseDouble(sString) / 1000.00;
            sString = dDouble.toString();
            return sString;
        }

        return sString;
    }

    private static String earningsDate(String sString) {
        Calendar calNow;
        Calendar calEarnings;
        Date dateNow;
        Date dateConvert;
        DateFormat dateFormat;
        // Given Aug 16
        // get current year

        if (sString.equalsIgnoreCase("-")) {
            dateConvert = new Date();
            dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            // no earnings date given, put it well into the future
            calEarnings = Calendar.getInstance();
            calEarnings.setTime(dateConvert);
            calEarnings.add(Calendar.YEAR, 5);
            dateConvert = calEarnings.getTime();

            sString = dateFormat.format(dateConvert);

            return sString;
        }

        calNow = Calendar.getInstance();
        String sYear = String.valueOf(calNow.get(Calendar.YEAR));

        // Turn date object assuming this year
        sString += " " + sYear;
        dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss",
                Locale.ENGLISH);
        try {
            dateNow = new Date();
            dateConvert = dateFormat.parse(sString + " 23:59:59");

            // Check if that put the earnings before today
            if (dateNow.after(dateConvert)) {
                // earnings are actually in the following year
                //  or, still referring to the last one
                //  either way, add a year
                calEarnings = Calendar.getInstance();
                calEarnings.setTime(dateConvert);
                calEarnings.add(Calendar.YEAR, 1);
                dateConvert = calEarnings.getTime();
            }

            // Convert to string MM/dd/yyyy
            dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            sString = dateFormat.format(dateConvert);
        }
        catch (ParseException ex) {
            //todo: is this an issue
//            ex.printStackTrace();
        }
        return sString;

    }

    public static class MyHostnameVerifier
            implements HostnameVerifier {

        /**
         *
         * @param hostname
         * @param session
         *
         * @return
         */
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // verification off
            return true;
        }
    }
}
