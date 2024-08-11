package com.hpi.appcontrollers;

import com.etrade.exampleapp.v1.exception.*;
import com.etrade.exampleapp.v1.oauth.model.*;
import com.fasterxml.jackson.databind.*;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.entities.*;
import static com.hpi.hpiUtils.CMHPIUtils.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.swing.JOptionPane;
import lombok.*;

public class OptionQuotesEtradeController
    extends QuotesEtradeBase
{

    private class OptionModel
    {

        @Setter @Getter public String underlyingString;
        @Setter @Getter public String occString;
        @Setter @Getter public String etradeString;
        @Setter @Getter public Double open;
        @Setter @Getter public Double high;
        @Setter @Getter public Double low;
        @Setter @Getter public Double close;
        @Setter @Getter public LocalDate localDate;
        @Setter @Getter public String dateString;
    }

    //private final ArrayList<EtradeEquityHistoryModel> etradeEquityHistoryModels = new ArrayList<>();
    private final ArrayList<OptionModel> optionModels = new ArrayList<>();


    /*
     * Singleton
     *
     */
    private OptionQuotesEtradeController instance;

    protected OptionQuotesEtradeController()
    {
        // protected prevents instantiation outside of package
    }

    public synchronized OptionQuotesEtradeController getInstance()
    {
        if (this.instance == null)
        {
            this.instance = new OptionQuotesEtradeController();
        }
        return this.instance;
    }
    //***

    public void doAllOptionsOneDay()
    {
        this.optionModels.clear();

        getEquityIdList();
        getEquityDataFromList();
        doHistoricalSQL();
        doUtil_LastDailyOption();
    }

    /**
     * Query openOptionFIFO table for list of symbols
     */
    private void getEquityIdList()
    {
        String s;
        OptionModel optionModel;

        this.optionModels.clear();

        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(OpenOptionFIFOModel.ACTIVE_OPTIONS);
            ResultSet rs = pStmt.executeQuery())
        {

            while (rs.next())
            {
                optionModel = new OptionModel();
                optionModel.setOccString(rs.getString("EquityId"));
                optionModel.setEtradeString(getEtradeOption(optionModel.getOccString()));
                optionModel.setUnderlyingString(
                    optionModel.getEtradeString().substring(0, optionModel.getEtradeString().indexOf(":"))
                );

                this.optionModels.add(optionModel);
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
     * Loop through optionModels, get required data
     */
    private void getEquityDataFromList()
    {
//        StringBuffer symbolsStringBuffer;
        Iterator<OptionModel> omIterator;

//        Integer i = 0;
        OptionModel optionModel = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        symbolsStringBuffer = new StringBuffer();

        omIterator = this.optionModels.iterator();

        System.out.println("\n\tUpdate getOptionDataFromList start: " + dtf.format(LocalDateTime.now()));

        while (omIterator.hasNext())
        {
            optionModel = omIterator.next();

            getEquityData(optionModel);

        }
        System.out.println("\tUpdate getOptionDataFromList end: " + dtf.format(LocalDateTime.now()) + "\n");
    }

    /**
     * Request info from eTrade
     *
     * @param optionModel
     * @return String, JSon response from eTrade
     */
    private void getEquityData(OptionModel optionModel)
    {
        JsonNode nodeRoot;
        JsonNode nodeQuoteResponse;
        JsonNode nodeMessages;
        JsonNode nodeQuoteData;

        String response = "";

        Message messageQuote = new Message();
        messageQuote.setOauthRequired(OauthRequired.NO);
        messageQuote.setHttpMethod(getHttpMethod());
        messageQuote.setUrl(getQuoteUrl(optionModel.getEtradeString()));
        messageQuote.setContentType(ContentType.APPLICATION_JSON);

        try
        {
            response = appController.invoke(messageQuote);
            nodeRoot = parse(response);

            nodeQuoteResponse = nodeRoot.get("QuoteResponse");

            nodeMessages = nodeQuoteResponse.get("Messages");

            if (nodeMessages != null)
            {
                /**
                 * if messages then an error
                 */
                return;
            }

            /**
             * returns array, [] though only one element as we have it now
             */
            nodeQuoteData = nodeQuoteResponse.get("QuoteData");

            for (JsonNode node : nodeQuoteData)
            {
                optionModel.setDateString(node.findValue("dateTime").asText().substring(13));
                //convert to LocalDate
                optionModel.setLocalDate(LocalDate.parse(optionModel.getDateString(), dateFormatter));

                optionModel.setOpen(node.findValue("All").findValue("open").asDouble());
                optionModel.setHigh(node.findValue("All").findValue("high").asDouble());
                optionModel.setLow(node.findValue("All").findValue("low").asDouble());
                optionModel.setClose(node.findValue("All").findValue("previousClose").asDouble());

                if (node.findValue("All").findValue("previousClose").asDouble() == 0.0
                    && node.findValue("All").findValue("averageVolume").asDouble() == 0.0)
                {
                    //no executed trades on the day, use average of bid/ask
                    optionModel.setClose(
                        (node.findValue("All").findValue("bid").asDouble()
                        + node.findValue("All").findValue("ask").asDouble()) / 2
                    );

                }
            }
        } catch (ApiException ex)
        {
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
        DateTimeFormatter dtf;

        values = new StringBuffer();

        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        System.out.println("\tdoHistoricalSQL start: " + dtf.format(LocalDateTime.now()));

        sql = "truncate hlhtxc5_dmOfx.Util_LastDailyOption;";
        CMDBController.executeSQL(sql);

        if (this.optionModels.isEmpty())
        {
            return;
        }

        //iterate through the history models and update the database
        for (OptionModel om : this.optionModels)
        {
            //open the sql
            sql = "insert ignore into hlhtxc5_dmOfx.OptionHistory (EquityId, `Date`, Ticker, `Open`, High, Low, `Close`) values (";

            values.append("\"");
            values.append(om.getOccString());
            values.append("\",\"");
            values.append(om.getLocalDate());
            values.append("\",\"");
            values.append(om.getUnderlyingString());
            values.append("\",");
            values.append((om.getOpen()));
            values.append(",");
            values.append((om.getHigh()));
            values.append(",");
            values.append((om.getLow()));
            values.append(",");
            values.append((om.getClose()));
            //close the sql
            values.append(");");

            sql = sql + values;
            if (om.getClose() != null)
            {
                /**
                 * Only push records with a valid close to the database
                 */
                CMDBController.executeSQL(sql);
            }

            values.setLength(0);
        }

        System.out.println("\tdoHistoricalSQL end: "
            + dtf.format(LocalDateTime.now()) + "\n");
    }

    /*
     * Refresh utility table with latest prices
     */
    private void doUtil_LastDailyOption()
    {
        String sql;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        System.out.println("\tdoUtil_LastDailyOption start: "
            + dtf.format(LocalDateTime.now()));

        sql = "truncate hlhtxc5_dmOfx.Util_LastDailyOption;";
        CMDBController.executeSQL(sql);

        //set Price to average of bid and ask
        sql
            = "insert ignore into hlhtxc5_dmOfx.Util_LastDailyOption (EquityId, `DataDate`, BidPrice, AskPrice, LastPrice, Price, PutCall, StrikePrice) select EquityId, `Date`, 0, 0, `Close`, 0, '', 0 from hlhtxc5_dmOfx.OptionHistory oh where oh.`Date` = ((select max(oh2.`Date`) from hlhtxc5_dmOfx.OptionHistory oh2 where oh.EquityId = oh2.EquityId)) order by Ticker, `Date`;";

        CMDBController.executeSQL(sql);

        System.out.println("\tdoUtil_LastDailyOption end: "
            + dtf.format(LocalDateTime.now()) + "\n");
    }
}
