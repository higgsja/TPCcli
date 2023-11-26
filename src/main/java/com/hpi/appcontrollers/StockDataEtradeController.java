package com.hpi.appcontrollers;

import com.etrade.exampleapp.config.*;
import com.etrade.exampleapp.v1.exception.*;
import com.etrade.exampleapp.v1.oauth.*;
import com.etrade.exampleapp.v1.oauth.model.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.entities.*;
import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JOptionPane;
import org.springframework.context.annotation.*;

public class StockDataEtradeController
    extends StockDataEtradeBase
{

    private static AnnotationConfigApplicationContext ctx;
    private static SecurityContext securityContext;
    private static Resource resource;
    private static AppController appController;

    private static final ObjectMapper objectMapper = getDefaultObjectMapper();

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private static final ArrayList<EtradeEquityHistoryModel> etradeEquityHistoryModels
         = new ArrayList<>();

    static
    {
        StockDataEtradeController.symbols = new ArrayList<>();
    }

    /*
     * Singleton
     *
     */
    private static StockDataEtradeController instance;

    protected StockDataEtradeController()
    {
        // protected prevents instantiation outside of package
    }

    public synchronized static StockDataEtradeController getInstance()
    {
        if (StockDataEtradeController.instance == null)
        {
            StockDataEtradeController.instance = new StockDataEtradeController();
        }
        return StockDataEtradeController.instance;
    }
    //***

    private static ObjectMapper getDefaultObjectMapper()
    {
        ObjectMapper defaultObjectMapper = new ObjectMapper();

        // ignore attributes we do not care about rather than fail
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // load module for java time 
//        defaultObjectMapper.registerModule(new JavaTimeModule());
        return defaultObjectMapper;
    }

    public static JsonNode parse(String src)
    {
        try
        {
            return objectMapper.readTree(src);
        } catch (IOException e)
        {
            System.exit(-1);
        }
        return null;
    }

    public static <A> A fromJson(JsonNode node, Class<A> clazz)
    {
        try
        {
            return objectMapper.treeToValue(node, clazz);
        } catch (IllegalArgumentException | JsonProcessingException ex)
        {
            java.util.logging.Logger.getLogger(StockDataEtradeController.class.getName())
                .log(java.util.logging.Level.SEVERE, null, ex);
            System.exit(-1);
        }
        return null;
    }

    public static void doAllStocksOneDay()
    {
        StockDataEtradeController.etradeEquityHistoryModels.clear();
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(OOauthConfig.class);
        ctx.refresh();

        securityContext = ctx.getBean(SecurityContext.class);
        resource = securityContext.getResources();
        resource.setConsumerKey("a7f522d5dcec23c63a173ad3eb2e93cc");
        resource.setSharedSecret("22fece510eb9661ccc1469161200e041db7e28ff20e8d84d456bf1e55749906a");

        appController = ctx.getBean(AppController.class);

        getEquityInfoList();
        getEquityDataFromList();
        doHistoricalSQL();
        doUtil_LastDailyOption();
    }

    /**
     * Query equityInfo for list of stock symbols
     */
    private static void getEquityInfoList()
    {
        String s;

        StockDataEtradeController.symbols.clear();

        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(EquityInfoModel.TICKER);
            ResultSet rs = pStmt.executeQuery())
        {

            while (rs.next())
            {
                StockDataEtradeController.symbols.add(rs.getString("Ticker"));
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
     * We can put up to 25 sybols per request
     */
    private static void getEquityDataFromList()
    {
        StringBuffer symbolsStringBuffer;
        Iterator<String> symbolIterator;
        Integer i = 0;
        String symbol = "";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        symbolsStringBuffer = new StringBuffer();

        symbolIterator = StockDataEtradeController.symbols.iterator();

        System.out.println("Update getEquityDataFromList start: " + dtf.format(LocalDateTime.now()));

        while (symbolIterator.hasNext())
        {
            symbol = symbolIterator.next();

            if (i++ < 24)
            {
//                progress("\t\t=======\t" + symbol + "\t=======: " + i);
                symbolsStringBuffer.append(symbol);
                symbolsStringBuffer.append(",");
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

            //limit for testing
//            if (symbol.equalsIgnoreCase("aau"))
//            {
//                break;
//            }
        }

        //process the final symbols in batch < 25
        if (symbolsStringBuffer.length() > 0)
        {
            getEquityData(symbolsStringBuffer.substring(0, symbolsStringBuffer.length() - 1));
        }

        System.out.println("Update getEquityDataFromList end: " + dtf.format(LocalDateTime.now()));

    }

    /**
     * Request symbol info from eTrade
     *
     * @param symbol
     * @return String, JSon response from eTrade
     */
    private static void getEquityData(String symbol)
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

                StockDataEtradeController.etradeEquityHistoryModels.add(etradeEquityHistoryModel);

            }

        } catch (ApiException ex)
        {
            Logger.getLogger(StockDataEtradeController.class
                .getName()).log(Level.SEVERE, null, ex);
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

    private static void progress(String outText)
    {
        if (CmdLineController.getsCLIProgressBar().equalsIgnoreCase("true"))
        {
            System.out.println(outText);
        }
    }

    private static String getQuoteUrl()
    {
        //return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getQuoteUri());
        return String.format("%s%s", "https://api.etrade.com", "/v1/market/quote/");
    }

    private static String getQuoteUrl(String symbol)
    {
        return String.format("%s%s", getQuoteUrl(), symbol);
    }

    /**
     * Use models list to update the sql table
     */
    private static void doHistoricalSQL()
    {
        String sql;
        StringBuffer values;
        File folder;
        DateTimeFormatter dtf;

        values = new StringBuffer();

        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        System.out.println("doHistoricalSQL start: " + dtf.format(LocalDateTime.now()));

        if (StockDataEtradeController.etradeEquityHistoryModels.isEmpty())
        {
            return;
        }

        //iterate through the history models and update the database
        for (EtradeEquityHistoryModel eehm : StockDataEtradeController.etradeEquityHistoryModels)
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
            //+ "Symbols: "); + StockDataEtradeController.etradeEquityHistoryModels);
    }

    /*
     * Refresh utility table with latest prices
     */
    private static void doUtil_LastDailyOption()
    {
        String sql;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

                System.out.println("doUtil_LastDailyOption start: "
            + dtf.format(LocalDateTime.now()));

        sql = "truncate hlhtxc5_dmOfx.Util_LastDailyStock;";
        CMDBController.executeSQL(sql);

        //set Price to average of bid and ask
        sql
            = "insert ignore into hlhtxc5_dmOfx.Util_LastDailyStock (EquityId, `Date`, Open, High, Low, Close, Volume) select Ticker, `Date`, `Open`, High, Low, `Close`, Volume from hlhtxc5_dmOfx.EquityHistory eh where eh.`Date` = ((select max(eh2.`Date`) from hlhtxc5_dmOfx.EquityHistory eh2 where eh.Ticker = eh2.Ticker)) order by Ticker, `Date`;";

//            "insert ignore into hlhtxc5_dmOfx.Util_LastDailyStock select oof.EquityId, max(oh.DataDate) as DataDate, oh.BidPrice, oh.AskPrice, oh.LastPrice, round((oh.AskPrice + oh.BidPrice) / 2, 2) as Price, oh.PutCall, oh.StrikePrice from hlhtxc5_dmOfx.OpenOptionFIFO as oof left join hlhtxc5_dmOfx.OptionHistory as oh on oh.EquityId = oof.EquityId where oh.DataDate >= subdate(now(), interval 4 day) group by oof.EquityId";
        CMDBController.executeSQL(sql);
        
        System.out.println("doUtil_LastDailyOption end: "
            + dtf.format(LocalDateTime.now()));
    }
}
