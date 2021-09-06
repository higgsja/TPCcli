package com.hpi.appcontrollers;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.entities.LastDailyOptionModel;
import com.hpi.hpiUtils.*;
import com.studerw.tda.client.*;
import com.studerw.tda.model.option.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.time.*;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

public class TDAmeritradeFetchOptionsController2 {

    private static final ArrayList<String> OPEN_TICKERS = new ArrayList<>();
    private static OptionChain optionChain;
    private static final ArrayList<Option> OPTIONS_LIST = new ArrayList<>();

    public static final void doPrices(String universe) {
        HttpTdaClient httpTdaClient;
        Properties props;
        Integer i;

        System.out.println("\t\t----tdAmeritrade options starting");

        //get the list of tickers
        TDAmeritradeFetchOptionsController2.getOpenTickerList(universe);

        //add any special
        TDAmeritradeFetchOptionsController2.OPEN_TICKERS.add("BRK/B");
        TDAmeritradeFetchOptionsController2.OPEN_TICKERS.add("VXX");
//        TDAmeritradeFetchOptionsController2.OPEN_TICKERS.add("ge");

        props = new Properties();

        props.setProperty(
                "tda.client_id", CMOfxDirectModel.getFIMODELS().
                        get(0).getClientId());
        props.setProperty(
                "tda.token.refresh", CMOfxDirectModel.getFIMODELS().
                        get(0).getTokenRefresh());
        props.setProperty(
                "tda.account.id", CMOfxDirectModel.getFIMODELS().
                        get(0).getAccountModels().get(0).getAcctNumber());
//        props.setProperty("tda.url", CMOfxDirectModel.getFIMODELS().
//            get(0).getHttpHost());
//        props.setProperty("tda.debug.bytes.length", CMOfxDirectModel.
//            getFIMODELS().get(0).getDebugBytes());

        httpTdaClient = new HttpTdaClient(props);

        i = 0;
        for (String tkr : TDAmeritradeFetchOptionsController2.OPEN_TICKERS) {
            TDAmeritradeFetchOptionsController2.optionChain = httpTdaClient.getOptionChain(tkr);

            //do calls
            doCallChainDetail();

            //have calls in the optionsList
            doOptionChainSQL();

            //do puts
            doPutChainDetail();

            //have puts in the optionsList
            doOptionChainSQL();

            //throttling
            i++;
            if (i == 30) {
                try {
                    System.out.println("\t\t===\tsleep: " + i);
                    Thread.sleep(1000 * 60 / 4);
                } catch (InterruptedException ex) {
                    int k = 0;
                }

                i = 0;
            }
        }

//        TDAmeritradeFetchOptionsController2.optionChain = httpTdaClient.getOptionChain("VXX");
////        TDAmeritradeFetchOptionsController.optionChain = httpTdaClient.getOptionChain("MSFT");
//
//        doCallChainDetail();
//
//        //have all in the optionsList
//        doOptionChainSQL();
//
//        doPutChainDetail();
//
//        //have all in the optionsList
//        doOptionChainSQL();
//        System.out.println("VXX complete");
//
//        optionChain = httpTdaClient.getOptionChain("BRK/B");
//
//        doCallChainDetail();
//
//        //have all in the optionsList
//        doOptionChainSQL();
//
//        doPutChainDetail();
//
//        //have all in the optionsList
//        doOptionChainSQL();
        System.out.println(
                "\t\t----tdAmeritrade options complete");

        //do the Util_LastDailyOption table
        System.out.println(
                "Util_LastDailyOption update started");

        // table would be 1.4 million rows, too big to rebuild daily
        // do only those in OpenOptionsFIFO
        TDAmeritradeFetchOptionsController2.doUtil_LastDailyOption();

        System.out.println(
                "Util_LastDailyOption update completed");
    }

    /**
     * pull distinct list of tickers from the existing optionHistory and
     * fifoTransactions tables
     */
    private static void getOpenTickerList(String universe) {
        String sql, s;

        sql = "";

        if (universe.equalsIgnoreCase("open")) {
            //all tickers from the open option table and all tickers already in the optionHistory table
            sql
                    = "select distinct Ticker from hlhtxc5_dmOfx.FIFOOpenTransactions where Ticker <> 'cash' "
                    + "union select distinct Symbol from hlhtxc5_dmOfx.OptionHistory order by Ticker";
        }

        if (universe.equalsIgnoreCase("active")) {
            //all tickers active 
            sql
                    = "select distinct Ticker from hlhtxc5_dmOfx.ClientEquityAttributes where Active = 'yes' ";
//                    + "union select Symbol from hlhtxc5_dmOfx.optionHistory order by Ticker";
        }

        try (Connection con = CMDBController.getConnection();
                PreparedStatement pStmt = con.prepareStatement(sql);
                ResultSet rs = pStmt.executeQuery(sql)) {

            while (rs.next()) {
                //dis, ge, run problem
//                if (rs.getString("Ticker").equalsIgnoreCase("dis")
//                        || rs.getString("Ticker").equalsIgnoreCase("ge")
//                        || rs.getString("Ticker").equalsIgnoreCase("run")
//                        || rs.getString("Ticker").equalsIgnoreCase("cvx")
//                        || rs.getString("Ticker").equalsIgnoreCase("ms")
//                        || rs.getString("Ticker").equalsIgnoreCase("cvx")
//                        || rs.getString("Ticker").equalsIgnoreCase("tdoc")){
//                continue;
//                }

                OPEN_TICKERS.add(rs.getString("Ticker"));
            }
        } catch (SQLException e) {
            s = String.format(CMLanguageController.
                    getErrorProps().getProperty("Formatted14"),
                    System.lineSeparator() + e.toString()
                    + System.lineSeparator());

            CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getErrorProps().
                            getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void doCallChainDetail() {
        OPTIONS_LIST.clear();

        optionChain.getCallExpDateMap().forEach((k1, v1) -> {
            //iterates the expiry dates, k1

            v1.forEach((k2, v2) -> {
                //iterates the strikes and only one entry per
                OPTIONS_LIST.add(v2.get(0));
            });
        });
    }

    private static void doPutChainDetail() {
        OPTIONS_LIST.clear();

        optionChain.getPutExpDateMap().forEach((k1, v1) -> {
            //iterates the expiry dates, k1

            v1.forEach((k2, v2) -> {
                //iterates the strikes and only one entry per
                OPTIONS_LIST.add(v2.get(0));
            });
        });
    }

    private static void doOptionChainSQL() {
        String sqlString;

        sqlString
                = "insert ignore into hlhtxc5_dmOfx.OptionHistory (OptionKey, Symbol, ExpirationDate, AskPrice, AskSize, BidPrice, BidSize, LastPrice, PutCall, StrikePrice, Volume, OpenInterest, UnderlyingPrice, DataDate, EquityId) values ";

        if (OPTIONS_LIST.isEmpty()) {
            return;
        }

        for (Option option : OPTIONS_LIST) {
            sqlString += "(";
            sqlString += "'" + option.getSymbol() + "', ";
            sqlString += "'" + option.getSymbol().substring(0,
                    option.getSymbol().indexOf("_")) + "', ";
            sqlString
                    += "'" + format(option.getExpirationDate(), "yyyy-MM-dd") + "', ";
            sqlString += "'" + option.getAskPrice().toString() + "', ";
            sqlString += "'" + option.getAskSize().toString() + "', ";
            sqlString += "'" + option.getBidPrice().toString() + "', ";
            sqlString += "'" + option.getBidSize().toString() + "', ";
            sqlString += "'" + option.getLastPrice().toString() + "', ";
            sqlString += "'" + option.getPutCall().
                    toString().toLowerCase() + "', ";
            sqlString += "'" + option.getStrikePrice().toString() + "', ";
            sqlString += "'" + option.getTotalVolume().toString() + "', ";
            sqlString += "'" + option.getOpenInterest().toString() + "', ";
            sqlString += "'0.0" + "', ";    //underlying price
            sqlString += "'" + DateFormatUtils.format(new Date(
                    option.getQuoteTimeInLong()), "yyyy-MM-dd") + "', ";
            sqlString += "'" + CMHPIUtils.getOCCTicker(
                    option.getSymbol().substring(0,
                            option.getSymbol().indexOf("_")),
                    new Date(option.getExpirationDate()),
                    option.getPutCall().toString(),
                    option.getStrikePrice().doubleValue()) + "'";
            sqlString += "),";
        }

        sqlString = sqlString.substring(0, sqlString.lastIndexOf(",") - 1);
        sqlString += ");";

        CMDBController.executeSQL(sqlString);
    }

    /*
     * Refresh table of active products with latest prices
     */
    private static void doUtil_LastDailyOption() {

        CMDBController.executeSQL(LastDailyOptionModel.TRUNCATE);

        CMDBController.executeSQL(LastDailyOptionModel.UPDATE);
    }
}
