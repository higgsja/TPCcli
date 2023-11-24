package com.hpi.appcontrollers;

import com.etrade.exampleapp.v1.exception.*;
import com.etrade.exampleapp.v1.oauth.model.*;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.entities.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.swing.JOptionPane;

public class StockDataEtradeController
    extends StockDataEtradeBase
{

    private static ArrayList<String> fileList;

    static
    {
        StockDataEtradeController.symbols = new ArrayList<>();
        StockDataEtradeController.fileList = new ArrayList<>();
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

    public static void doAllStocksOneDay()
    {
        try
        {
            getEquityInfoList();
            getEquityDataFromList();
            doHistoricalSQL();
            doUtil_LastDailyOption();
        } catch (Exception e)
        {
            String s = e.getMessage();

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
    }

    /**
     * Query equityInfo for list of stock symbols
     */
    private static void getEquityInfoList()
    {
        String s;

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
     */
    private static void getEquityDataFromList()
        throws ApiException
    {
        Integer i;

        i = 0;
        for (String symbol : StockDataEtradeController.symbols)
        {
            progress("\t\t=======\t" + symbol + "\t=======");

            getEquityData(symbol);

//            if (getEquityData(symbol))
//            {
//                //too many requests exception
//                try
//                {
//                    progress("\t\t===\t429 sleep: " + i);
//                    Thread.sleep(1000 * 60 / 4);
//                } catch (InterruptedException ex)
//                {
//                    int j = 0;
//                }
//
//                this.getEquityData(symbol);
//            }
//
//            i++;
//            if (i == 50)
//            {
//                try
//                {
//                    progress("\t\t===\tsleep: " + i);
//                    Thread.sleep(1000 * 60 / 4);
//                } catch (InterruptedException ex)
//                {
//                    int k = 0;
//                }
//
//                i = 0;
//            }
        }
    }

    /**
     * Request symbol info from eTrade
     *
     * @param symbol
     * @return String, response from eTrade
     * @throws ApiException
     */
    private static String getEquityData(String symbol)
        throws ApiException
    {
        Message message = new Message();

        //delayed quotes without oauth handshake
        if (oauthManager.getContext().isIntialized())
        {
            message.setOauthRequired(OauthRequired.YES);
        } else
        {
            message.setOauthRequired(OauthRequired.NO);
        }

        message.setHttpMethod(getHttpMethod());
        message.setUrl(getURL(symbol));
        message.setContentType(ContentType.APPLICATION_JSON);

        return oauthManager.invoke(message);
    }

    private static void progress(String outText)
    {
        if (CmdLineController.getsCLIProgressBar().equalsIgnoreCase("true"))
        {
            System.out.println(outText);
        }
    }

    /*
     * Process files from defined location
     */
    private static void doHistoricalSQL()
    {
        String sql;
        File folder;
        DateTimeFormatter dtf;

        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        // get list of files to process
        StockDataEtradeController.fileList = new ArrayList<>();

        folder = new File(CMDirectoriesModel.getInstance().
            getProps().getProperty("OptionHistory"));

        File[] files = folder.listFiles();

        if (files.length == 0)
        {
            return;
        }

        for (File file : files)
        {
            if (file.isFile())
            {
                StockDataEtradeController.fileList.add(file.getAbsolutePath());
            }
        }

        if (StockDataEtradeController.fileList.isEmpty())
        {
            return;
        }

        // loop through files
        for (String sPath : StockDataEtradeController.fileList)
        {
            System.out.println(dtf.format(LocalDateTime.now()));

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                sPath,
                JOptionPane.INFORMATION_MESSAGE);

            // process the file
            sql
                = "load data local infile '%s' replace into table hlhtxc5_dmOfx.StockHistory fields terminated by ',' lines terminated by '\\n' ignore 1 lines (OptionKey, Symbol, ExpirationDate, AskPrice, AskSize, BidPrice, BidSize, LastPrice, PutCall, StrikePrice, Volume, OpenInterest, UnderlyingPrice, DataDate);";
            sql = String.format(sql, sPath);
            CMDBController.updateSQLNoCommit(sql);

            try
            {
                Files.deleteIfExists(Paths.get(sPath));
            } catch (IOException ex)
            {
                // not important
            }

            System.out.println(dtf.format(LocalDateTime.now()));
        }

//        doUtil_LastDailyOption();
    }

    /*
     * Refresh table of active products with latest prices
     */
    private static void doUtil_LastDailyOption()
    {
        String sql;

        sql = "truncate hlhtxc5_dmOfx.Util_LastDailyOption;";
        CMDBController.executeSQL(sql);

        //set Price to average of bid and ask
        sql
            = "insert ignore into hlhtxc5_dmOfx.Util_LastDailyOption select oof.EquityId, max(oh.DataDate) as DataDate, oh.BidPrice, oh.AskPrice, oh.LastPrice, round((oh.AskPrice + oh.BidPrice) / 2, 2) as Price, oh.PutCall, oh.StrikePrice from hlhtxc5_dmOfx.OpenOptionFIFO as oof left join hlhtxc5_dmOfx.OptionHistory as oh on oh.EquityId = oof.EquityId where oh.DataDate >= subdate(now(), interval 4 day) group by oof.EquityId";

        CMDBController.executeSQL(sql);
    }
}
