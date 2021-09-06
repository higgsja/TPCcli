package com.hpi.appcontrollers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hpi.entities.IEXChartModel;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import javax.swing.JOptionPane;

public class IEXTradingController {

    private static IEXTradingController instance;
    private static Boolean bMin;
    private static String sDate;
    private static final ArrayList<String> EQUITY_INFOS;
    private static final ArrayList<EquityHistory> EQUITY_HISTORYS;
    private static ArrayList<IEXChartModel> tkrChartList;
    private static final CMProgressBarCLI PROGRESS_BAR_CLI;
    private static Boolean bMessagesExceeded;

    static {
        bMin = false;
        EQUITY_INFOS = new ArrayList<>();
        EQUITY_HISTORYS = new ArrayList<>();
        PROGRESS_BAR_CLI = new CMProgressBarCLI(
            CmdLineController.getsCLIProgressBar());
        bMessagesExceeded = false;
    }

    /*
     * Singleton
     */
    protected IEXTradingController() {
        // protected prevents instantiation outside of package
    }

    public synchronized static IEXTradingController getInstance() {
        if (IEXTradingController.instance == null) {
            IEXTradingController.instance = new IEXTradingController();
        }
        return IEXTradingController.instance;
    }

    /**
     * Use equityInfo table for tickers; update price data in equityHistory
     * Both tables are global tables
     *
     * @param aDate
     * @param bMin
     */
    public static void doHistorical(String aDate, Boolean bMin) {
        String s;

        IEXTradingController.bMin = bMin;
        IEXTradingController.sDate = aDate;

        getEquityInfoList();
        //equityInfo could be empty
        if (EQUITY_INFOS.isEmpty()) {
            s = String.format(CMLanguageController.
                getEquity_info_SqlProp("No_Tkrs"));

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        getEquityHistoryList();

        //history list can be empty if this is a first pass
        if (EQUITY_HISTORYS.isEmpty()) {
            s = String.format(CMLanguageController.
                getErrorProp("EquityHistoryNoData"));

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        getHistory();

        doSQL();

        doUtil_LastDailyStock();
    }

    /**
     * pull distinct list of tickers from the equityInfo table
     */
    private static void getEquityInfoList() {
        String sql, s, tempTkr;

        if (bMin) {
            sql =
                "select distinct Ticker from hlhtxc5_dmOfx.ClientEquityAttributes order by Ticker;";
        }
        else {
            sql =
                "select distinct Ticker from hlhtxc5_dmOfx.EquityInfo where `Date` = (select max(`Date`) as `Date` from hlhtxc5_dmOfx.EquityInfo) order by Ticker;";
        }

        try (Connection con = CMDBController.getConnection();
             PreparedStatement pStmt = con.prepareStatement(sql);
             ResultSet rs = pStmt.executeQuery(sql)) {

            while (rs.next()) {
                tempTkr = rs.getString("Ticker");

                EQUITY_INFOS.add(tempTkr);
            }
            rs.close();
            pStmt.close();
            con.close();
        }
        catch (SQLException e) {
            s = String.format(CMLanguageController.
                getErrorProps().getProperty("Formatted14"),
                System.lineSeparator() + e.toString() +
                System.lineSeparator());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void getEquityHistoryList() {
        String sql, s;
        EquityHistory tempEquityHistory;

        if (bMin) {
            sql =
                //                "select CEA.Ticker, CEA.TickerIEX, max(`Date`) as `Date` from hlhtxc5_dmOfx.EquityHistory as eH left join hlhtxc5_dmOfx.ClientEquityAttributes as CEA on eH.TickerIEX = CEA.TickerIEX where CEA.TickerIEX = eH.TickerIEX and `Date` > '2019-10-01' group by CEA.TickerIEX order by CEA.Ticker;";
                "select CEA.Ticker, CEA.TickerIEX, max(`Date`) as `Date` from hlhtxc5_dmOfx.EquityHistory as eH right join hlhtxc5_dmOfx.ClientEquityAttributes as CEA on eH.TickerIEX = CEA.TickerIEX group by CEA.TickerIEX order by CEA.Ticker;";
        }
        else {
            // need to order by Ticker, not TickerIEX
            sql =
                "select Ticker, TickerIEX, max(`Date`) as `Date` from hlhtxc5_dmOfx.EquityHistory where `Date` > date_sub(now(), interval 2 month) group by Ticker order by Ticker";
        }

        try (Connection con = CMDBController.getConnection();
             PreparedStatement pStmt = con.prepareStatement(sql);
             ResultSet rs = pStmt.executeQuery(sql)) {

            while (rs.next()) {
                tempEquityHistory = new EquityHistory(
                    rs.getString("Ticker"),
                    rs.getString("TickerIEX"),
                    rs.getString("Date"));

                EQUITY_HISTORYS.add(tempEquityHistory);
            }
            rs.close();
            pStmt.close();
            con.close();
        }
        catch (SQLException e) {
            s = String.format(CMLanguageController.
                getErrorProps().getProperty("Formatted14"),
                System.lineSeparator() + e.toString() +
                System.lineSeparator());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * Process ticker list for historical data
     */
    private static void getHistory() {
        int row, rows;
        String tkrIEX;
        Boolean bRemoveEh;

        row = 0;
        rows = EQUITY_INFOS.size();
        bRemoveEh = false;

        PROGRESS_BAR_CLI.barLabel(CMLanguageController.
            getEquity_info_SqlProp("retrieve_history"));
        PROGRESS_BAR_CLI.barUpdate(0, rows);

        // loop through equityInfo tickers
        //  equityInfo is our data of record for present day ticker data
        for (String tkrEquityInfo : EQUITY_INFOS) {
            // message limit may be exceeded during run. Put whatever we get
            //  to the database.
            if (bMessagesExceeded) {
                break;
            }

            if (bRemoveEh) {
                /*
                 * We have removed one equityHistory; need to consider that
                 * more must be removed to regain sync with equityInfo
                 */
                while (!tkrEquityInfo.equalsIgnoreCase(EQUITY_HISTORYS.get(0).
                    getTkr())) {
                    /*
                     * equityInfo and equityHistory are not the same equity
                     *
                     * Initially, equityInfo > equityHistory indicating
                     * equityHistory has equities equityInfo does not.
                     *
                     * equityInfo is our source of record. Remove equityHistory
                     * objects until re-sync.
                     *
                     * Could actually go too far to where equityInfo is now
                     * less than equityHistory. Need to let the code process
                     * what may be a new equityHistory equity.
                     */
                    if (tkrEquityInfo.compareTo(
                        EQUITY_HISTORYS.get(0).getTkr()) < 0) {
                        /*
                         * reached the point where equityHistory is ahead of
                         * equityInfo
                         */
                        break;
                    }

//                    System.out.println("Removed equityHistory ticker: " +
//                                       EQUITY_HISTORYS.get(0).getTkr());
                    EQUITY_HISTORYS.remove(0);
                }
                bRemoveEh = false;
            }

            // This ticker is finViz ticker
            row++;
            PROGRESS_BAR_CLI.barUpdate(row, rows);

            // finViz uses '-'; iex uses '.'
            tkrIEX = tkrEquityInfo.replace("-", ".");

            for (EquityHistory equityHistory : EQUITY_HISTORYS) {
                // compare eh.getTkr (equityHistory) to tkr (equityInfo)
                if (equityHistory.getTkr().toUpperCase()
                    .compareTo(tkrEquityInfo) > 0) //if (eh.getTkr().compareTo(tkr) > 0)
                {
                    int i = 0;
                    // in alpha order, equityInfo has a new one not in equityHistory
                    
                    // this is case where equityInfo has it but equityHistory does not.
                    //  add to equityHistory 30 days of data
                    getTkrHistory(tkrEquityInfo, tkrIEX, LocalDate.now().minusDays(30));

                    // do not remove eh as have not dealt with it yet
//                    EQUITY_HISTORYS.remove(eh);
                    break;
//                    continue;
                }

                if (equityHistory.getTkr().compareTo(tkrEquityInfo) < 0) {
                    /*
                     * we are in alpha order; equityHistory precedes our
                     * equityInfo ticker so we passed it without removing it
                     *
                     * equityInfo is our data of record so ignore equityHistory
                     *
                     * do the equityInfo item; knowing nothing else, use
                     * maxDate from the equityHistory item
                     */
                    getTkrHistory(tkrEquityInfo, tkrIEX, LocalDate.parse(
                        equityHistory.getMaxDate()));

                    //remove equityHistory item
//                    System.out.println("Removed equityHistory ticker: " +
//                                       equityHistory.getTkr());
                    EQUITY_HISTORYS.remove(equityHistory);

                    //setup to resync equityInfo with equityHistory
                    bRemoveEh = true;
                    break;
                }

                if (equityHistory.getTkr().equalsIgnoreCase(tkrEquityInfo)) {
                    if (IEXTradingController.sDate.isEmpty()) {
                        if (equityHistory.getMaxDate() == null) {
                            //there was no data in equityHistory, just get 1 day
                            getTkrHistory(tkrEquityInfo, tkrIEX,
                                LocalDate.now().minusDays(1));
                        }
                        else {
                            //ok
                            getTkrHistory(tkrEquityInfo, tkrIEX, LocalDate.
                                parse(equityHistory.getMaxDate()));
                        }
                    }
                    else {
                        //get data based on command line provided date
                        getTkrHistory(tkrEquityInfo, tkrIEX, LocalDate.parse(
                            IEXTradingController.sDate));
                    }

                    //remove equityHistory item
//                    System.out.println("Removed equityHistory ticker: " +
//                         equityHistory.getTkr());
                    EQUITY_HISTORYS.remove(equityHistory);

                    //setup to resync equityInfo with equityHistory
                    bRemoveEh = true;

                    break;
                }
                
                //never hit
                System.out.println("Getting one day of ticker: " +
                     tkrEquityInfo);
                getTkrHistory(tkrEquityInfo, tkrIEX, LocalDate.now().
                    minusDays(1));
            }
        }
    }

    private static void getTkrHistory(String tkr, String tkrIEX,
        LocalDate startDate) {
        LocalDate quoteDate;
        String url1, url2;
        StringBuilder responseBuilder;
        String quoteDateString;
        Gson gSon;
        Type type;

        // use tkrIEX, not tkr to query IEX
        tkrIEX = tkrIEX.toUpperCase();

        if (IEXTradingController.sDate.isEmpty()) {
            quoteDate = startDate.plusDays(1);
        }
        else {
            quoteDate = LocalDate.parse(IEXTradingController.sDate);
        }

        url1 = buildRequestBase(tkrIEX);

        while (quoteDate.isBefore(LocalDate.now()) ||
               quoteDate.isEqual(LocalDate.now())) {
            // on Sundays and Saturdays their logic realizes its a 
            //weekend or whatever and delivers the Friday quote
            //So, need to disallow that.
            if (quoteDate.getDayOfWeek() == DayOfWeek.SUNDAY ||
                quoteDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                quoteDate = quoteDate.plusDays(1);
                continue;
            }

            quoteDateString = (quoteDate.toString()).replace("-", "");

            // one day at a time
            url2 = url1;
            url2 += quoteDateString;
            url2 += "?chartByDay=true";
            // sandbox
//            url2 += "&token=Tsk_a30bd4d8c9254881bf64ca4f4c4eebee";
            // production
            url2 += "&token=sk_e3796cd74a544842ad956430aaeee6b3";

            responseBuilder = doTkrUrlRequest(tkrIEX, url2);

            if (responseBuilder == null) {
                // means IEX does not have the ticker
                return;
            }

            // if doing a single date this should really return
            // do not expect a call for a stock without data and a single date
            // and, if no data for this date, unlikely there is data for another
            if (responseBuilder.length() <= 2) {
                // means a quote for this day is not available
                quoteDate = quoteDate.plusDays(1);
                continue;
            }

            // parse the JSon string
            gSon = new Gson();
            type = new TypeTokenImpl().getType();

            tkrChartList = gSon.fromJson(responseBuilder.toString(), type);

            // add to array
            doIEXData(tkr, tkrIEX, tkrChartList);

            // when doing a single date, need to leave
            if (IEXTradingController.sDate.isEmpty()) {
                quoteDate = quoteDate.plusDays(1);
            }
            else {
                return;
            }
        }
    }

    private static String buildRequestBase(String tkr) {
        String url1;

        // sandbox
//        url1 = "https://sandbox.iexapis.com/stable/stock/";
        // production
        url1 = "https://cloud.iexapis.com/stable/stock/";
        url1 += tkr;
        url1 += "/chart/date/";

        return url1;
    }

    private static StringBuilder doTkrUrlRequest(String tkr, String url2) {
        int timeOut;
        StringBuilder responseBuilder;
        String line, s;
        int status;

        timeOut = 15000;
        responseBuilder = new StringBuilder();

        try {
            URL request = new URL(url2);
            HttpURLConnection connection =
                (HttpURLConnection) request.openConnection();

            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);

            status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                switch (status) {
                    // 402 over limit on message quota
                    case 402:
                        s = String.format(CMLanguageController.
                            getErrorProps().getProperty("IEX402"),
                            tkr);

                        CMHPIUtils.showDefaultMsg(
                            CMLanguageController.
                                getErrorProps().
                                getProperty("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
                        bMessagesExceeded = true;
                        return null;
                    // cannot do this or lose data that did come through
                    //System.exit(1);
                    // 404 unknown symbol
                    case 404:
                        s = String.format(CMLanguageController.
                            getErrorProps().getProperty("IEX404"),
                            tkr);

                        CMHPIUtils.showDefaultMsg(
                            CMLanguageController.
                                getErrorProps().
                                getProperty("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
                        return null;
                    // 429 too many request too quickly
                    case 429:
//                        s = String.format(CMLanguageController.
//                            getErrorProps().getProperty("IEX429"),
//                            tkr);

//                        //Logger.getLogger(CmdLineController.class.getName()).info(s);

//                        CMHPIUtils.showDefaultMsg(
//                              CMLanguageController.
//                                    getErrorProps().
//                                    getProperty("Title"),
//                              Thread.currentThread().getStackTrace()[1].
//                                    getClassName(),
//                              Thread.currentThread().getStackTrace()[1].
//                                    getMethodName(),
//                              s,
//                              JOptionPane.ERROR_MESSAGE);
                        return null;
                    // 500 system error
                    case 500:
                        s = String.format(CMLanguageController.
                            getErrorProps().getProperty("IEX500"),
                            tkr);

                        CMHPIUtils.showDefaultMsg(
                            CMLanguageController.
                                getErrorProps().
                                getProperty("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
                        return null;
                    default:
//                        BufferedReader bufferedError
//                              = new BufferedReader(new InputStreamReader(
//                                    connection.getErrorStream(), "UTF-8"));
                        s = String.format(CMLanguageController.
                            getErrorProps().getProperty("GeneralError"),
                            Integer.toString(status) + ";\n" +
                            url2 + "\n");

                        CMHPIUtils.showDefaultMsg(
                            CMLanguageController.
                                getErrorProps().
                                getProperty("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
                        return null;
                }
            }

            try (BufferedReader bufferedInput =
                new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));) {
                while ((line = bufferedInput.readLine()) != null) {
                    responseBuilder.append(line);
                }

                bufferedInput.close();
                connection.disconnect();
            }
            catch (FileNotFoundException e) {
                // file not found exception means IEX does not have the ticker
                s = String.format(CMLanguageController.
                    getErrorProp("IEXTickerNotFound"),
                    tkr);

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getAppProp("Title") +
                    CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
            catch (IOException e) {
                // this might happen if too many requests per second
                s = String.format(CMLanguageController.
                    getErrorProp("GeneralError"),
                    e.toString());

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getAppProp("Title") +
                    CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        catch (IOException e) {
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") +
                CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].
                    getClassName(),
                Thread.currentThread().getStackTrace()[1].
                    getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            return null;
        }
        return responseBuilder;
    }

    private static void doIEXData(String tkr, String tkrIEX,
        ArrayList<IEXChartModel> chartList) {
        if (chartList.isEmpty()) {
            return;
        }
        for (IEXChartModel chartModel : chartList) {
            chartModel.setTkrIEX(tkrIEX);
            chartModel.setTkr(tkr);
            IEXChartModel.IEXCHARTMODEL_LIST.add(chartModel);
        }

        // todo: consider sending data to equityHistory every x00 symbols
        //  to reduce the message cost of a failure
//         if (IEXChartModel.IEXCHARTMODEL_LIST.size() >= 100)
//         {
//             doSQL();
//             
//             IEXChartModel.IEXCHARTMODEL_LIST.clear();
//         }
    }

    /*
     * put data into the database
     */
    private static void doSQL() {
        String sql, sql1, s;
        sql =
            "insert ignore into hlhtxc5_dmOfx.EquityHistory (TickerIEX, `Date`, Ticker, `Open`, `High`, `Low`, `Close`, `Volume` ) values (";

        //todo: very inefficient to send one at a time
        //otoh: expect to run this daily so mostly only one record
        //  except for new entities
        for (IEXChartModel iModel : IEXChartModel.IEXCHARTMODEL_LIST) {
            if (iModel.getVolume().equals(0)) {
                s = String.format(CMLanguageController.
                    getErrorProp("Formatted22"),
                    iModel.getTkrIEX());

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getAppProp("Title") +
                    CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].
                        getClassName(),
                    Thread.currentThread().getStackTrace()[1].
                        getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
            }
            sql1 = sql + "'";
            sql1 += iModel.getTkrIEX().toUpperCase() + "', '";
            sql1 += iModel.getDate() + "', '";
            sql1 += iModel.getTkr() + "', '";
            sql1 += iModel.getOpen() + "', '";
            sql1 += iModel.getHigh() + "', '";
            sql1 += iModel.getLow() + "', '";
            sql1 += iModel.getClose() + "', '";
            sql1 += iModel.getVolume() + "'";
            sql1 += ") ";
            sql1 += "on duplicate key update ";
            sql1 += "`Open` = '" + iModel.getOpen() + "', ";
            sql1 += "`High` = '" + iModel.getHigh() + "', ";
            sql1 += "`Low` = '" + iModel.getLow() + "', ";
            sql1 += "`Close` = '" + iModel.getClose() + "', ";
            sql1 += "`Volume` = '" + iModel.getVolume();
            sql1 += "';";

            CMDBController.executeSQL(sql1);
        }
    }

    private static void doUtil_LastDailyStock() {
        String sql;

        sql = "truncate hlhtxc5_dmOfx.Util_LastDailyStock;";
        CMDBController.executeSQL(sql);

        sql =
            "insert into hlhtxc5_dmOfx.Util_LastDailyStock select B.Ticker, B.`Date`, B.Open, B.High, B.Low, B.Close, B.Volume from (select Ticker, max(`Date`) `Date` from hlhtxc5_dmOfx.EquityHistory where `Date` > subdate(now(), interval 10 day) group by Ticker) A inner join hlhtxc5_dmOfx.EquityHistory B using (Ticker, `Date`) order by B.Ticker";
        // pull the latest data from openHistory for each
        CMDBController.executeSQL(sql);
        
        //todo: should post an error if this does not deliver
    }

    private static class TypeTokenImpl extends TypeToken<List<IEXChartModel>> {

        public TypeTokenImpl() {
        }
    }
}

class EquityHistory {

    private final String tkr;
    private final String tkrIEX;
    private final String maxDate;

    public EquityHistory(String tkr, String tickerIEX, String maxDate) {
        this.tkr = tkr;
        this.tkrIEX = tickerIEX;
        this.maxDate = maxDate;
    }

    public String getMaxDate() {
        return maxDate;
    }

    public String getTkr() {
        return tkr;
    }

    public String getTkrIEX() {
        return tkrIEX;
    }
}
