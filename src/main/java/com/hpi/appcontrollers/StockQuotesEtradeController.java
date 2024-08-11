package com.hpi.appcontrollers;

import com.etrade.exampleapp.v1.exception.*;
import com.etrade.exampleapp.v1.oauth.model.*;
import com.fasterxml.jackson.databind.*;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.entities.*;
import com.hpi.entities.eTrade.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.swing.JOptionPane;

public class StockQuotesEtradeController
    extends QuotesEtradeBase
{

//    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private final ArrayList<EtradeEquityHistoryModel> etradeEquityHistoryModels
        = new ArrayList<>();

    /*
     * Singleton
     *
     */
    private StockQuotesEtradeController instance;

    protected StockQuotesEtradeController()
    {
//        int i = 0;
        // protected prevents instantiation outside of package
    }

    public synchronized StockQuotesEtradeController getInstance()
    {
        if (this.instance == null)
        {
            this.instance = new StockQuotesEtradeController();
        }
        return this.instance;
    }
    //***

    public void doAllStocksOneDay()
    {
        this.etradeEquityHistoryModels.clear();
//        ctx = new AnnotationConfigApplicationContext();
//        ctx.register(OOauthConfig.class);
//        ctx.refresh();
//
//        securityContext = ctx.getBean(SecurityContext.class);
//        resource = securityContext.getResources();
//        resource.setConsumerKey("a7f522d5dcec23c63a173ad3eb2e93cc");
//        resource.setSharedSecret("22fece510eb9661ccc1469161200e041db7e28ff20e8d84d456bf1e55749906a");
//
//        appController = ctx.getBean(AppController.class);

        getEquityInfoList();
        getEquityDataFromList();
        doHistoricalSQL();
        doUtil_LastDailyStock();
    }

    /**
     * Query equityInfo for list of stock symbols
     */
    private void getEquityInfoList()
    {
        String s;

        symbols.clear();

        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(EquityInfoModel.TICKER);
            ResultSet rs = pStmt.executeQuery())
        {

            while (rs.next())
            {
                this.symbols.add(rs.getString("Ticker"));
            }
        } catch (SQLException e)
        {
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

    /**
     * Loop through symbols, get required data
     * We can put up to 25 symbols per request
     */
    private void getEquityDataFromList()
    {
        StringBuffer symbolsStringBuffer;
        Iterator<String> symbolIterator;

        Integer i = 0;
        String symbol = "";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        symbolsStringBuffer = new StringBuffer();

        symbolIterator = this.symbols.iterator();

        System.out.println("\n\tUpdate getEquityDataFromList start: " + dtf.format(LocalDateTime.now()));

        while (symbolIterator.hasNext())
        {
            symbol = symbolIterator.next();

            if (i++ < 24)
            {
//                progress("\t\t=======\t" + symbol + "\t=======: " + i);
                symbolsStringBuffer.append(symbol);
                symbolsStringBuffer.append(",");

                //limit for testing
//                if (symbol.equalsIgnoreCase("aaau"))
//                {
//                    break;
//                }
                continue;
            }

            //remove trailing comma
            getEquityData(symbolsStringBuffer.substring(0, symbolsStringBuffer.length() - 1));

            symbolsStringBuffer.setLength(0);
            symbolsStringBuffer.append(symbol);
            symbolsStringBuffer.append(",");

            i = 1;
//            progress("\t\t=======\t" + symbol + "\t=======: " + i);
            symbolsStringBuffer.append(symbol);
            symbolsStringBuffer.append(",");

        }

        //process the final symbols in batch < 25
        if (symbolsStringBuffer.length() > 0)
        {
            getEquityData(symbolsStringBuffer.substring(0, symbolsStringBuffer.length() - 1));
        }

        System.out.println("\n\tUpdate getEquityDataFromList end: " + dtf.format(LocalDateTime.now()) + "\n");
    }

    /**
     * Request symbol info from eTrade
     *
     * @param symbol
     * @return String, JSon response from eTrade
     */
    private void getEquityData(String symbol)
    {
        JsonNode nodeRoot;
        JsonNode nodeQuoteResponse;
        JsonNode nodeQuoteData;
        EtradeEquityHistoryModel etradeEquityHistoryModel;

        String response = "";

        Message messageQuote = new Message();
        messageQuote.setOauthRequired(OauthRequired.NO);
        messageQuote.setHttpMethod(getHttpMethod());
        messageQuote.setUrl(getQuoteUrl(symbol));
        messageQuote.setContentType(ContentType.APPLICATION_JSON);

        try
        {
            response = appController.invoke(messageQuote);
            nodeRoot = parse(response);

            nodeQuoteResponse = nodeRoot.get("QuoteResponse");

            /**
             * returns array, [] though only one element as we have it now
             */
            nodeQuoteData = nodeQuoteResponse.get("QuoteData");

            for (JsonNode node : nodeQuoteData)
            {
                etradeEquityHistoryModel = new EtradeEquityHistoryModel();

                etradeEquityHistoryModel.setTickerIEX(node.findValue("Product").findValue("symbol").asText());
                etradeEquityHistoryModel.setTicker(node.findValue("Product").findValue("symbol").asText());

                etradeEquityHistoryModel.setDateString(node.findValue("dateTime").asText().substring(13));
                //convert to LocalDate
                etradeEquityHistoryModel
                    .setDate(LocalDate.parse(etradeEquityHistoryModel.getDateString(), dateFormatter));

                etradeEquityHistoryModel.setOpen(node.findValue("All").findValue("open").asDouble());
                etradeEquityHistoryModel.setHigh(node.findValue("All").findValue("high").asDouble());
                etradeEquityHistoryModel.setLow(node.findValue("All").findValue("low").asDouble());
                etradeEquityHistoryModel.setClose(node.findValue("All").findValue("previousClose").asDouble());
                etradeEquityHistoryModel.setVolume(node.findValue("All").findValue("totalVolume").asInt());

                this.etradeEquityHistoryModels.add(etradeEquityHistoryModel);

            }
        } catch (ApiException ex)
        {
//            Logger.getLogger(this.class
//                .getName()).log(Level.SEVERE, null, ex);
            String s = ex.getMessage();

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getErrorProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].
                    getClassName(),
                Thread.currentThread().getStackTrace()[1].
                    getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    /**
     * Use models list to update the sql table
     */
    private void doHistoricalSQL()
    {
        String sql;
        StringBuffer values;
//        File folder;
        DateTimeFormatter dtf;

        values = new StringBuffer();

        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        System.out.println("doHistoricalSQL start: " + dtf.format(LocalDateTime.now()));

        if (this.etradeEquityHistoryModels.isEmpty())
        {
            return;
        }

        //iterate through the history models and update the database
        for (EtradeEquityHistoryModel eehm : this.etradeEquityHistoryModels)
        {
            //open the sql
            sql = "insert ignore into hlhtxc5_dmOfx.EquityHistory (TickerIEX, Ticker, Date, Open, High, Low, Close, Volume) values (";

            values.append("'");
            values.append(eehm.getTickerIEX());
            values.append("','");
            values.append(eehm.getTicker());
            values.append("','");
            values.append(eehm.getDate());
            values.append("','");
            values.append((eehm.getOpen()));
            values.append("','");
            values.append((eehm.getHigh()));
            values.append("','");
            values.append((eehm.getLow()));
            values.append("','");
            values.append((eehm.getClose()));
            values.append("','");
            values.append((eehm.getVolume()));
            //close the sql
            values.append("');");

            sql = sql + values;
            CMDBController.executeSQL(sql);

            values.setLength(0);
        }

        System.out.println("doHistoricalSQL end: "
            + dtf.format(LocalDateTime.now()));
    }

    /*
     * Refresh utility table with latest prices
     */
    private void doUtil_LastDailyStock()
    {
        String sql;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        System.out.println("doUtil_LastDailyStock start: "
            + dtf.format(LocalDateTime.now()));

        sql = "truncate hlhtxc5_dmOfx.Util_LastDailyStock;";
        CMDBController.executeSQL(sql);

        //set Price to average of bid and ask
        sql
//            = "insert ignore into hlhtxc5_dmOfx.Util_LastDailyStock (EquityId, `Date`, Open, High, Low, Close, Volume) select Ticker, `Date`, `Open`, High, Low, `Close`, Volume from hlhtxc5_dmOfx.EquityHistory eh where eh.`Date` = ((select max(eh2.`Date`) from hlhtxc5_dmOfx.EquityHistory eh2 where eh.Ticker = eh2.Ticker)) order by Ticker, `Date`;";
        = "insert ignore into hlhtxc5_dmOfx.Util_LastDailyStock (EquityId, `Date`, Open, High, Low, Close, Volume) select Ticker, `Date`, `Open`, High, Low, `Close`, Volume from hlhtxc5_dmOfx.EquityHistory as eh where eh.`Date` = ((select max(eh2.`Date`) from hlhtxc5_dmOfx.EquityHistory eh2 where eh.Ticker = eh2.Ticker)) order by Ticker, `Date`;";

        CMDBController.executeSQL(sql);

        System.out.println("doUtil_LastDailyStock end: "
            + dtf.format(LocalDateTime.now()));
    }
}
