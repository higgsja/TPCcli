package com.hpi.appcontrollers;

import com.higgstx.schwabapi.config.*;
import com.higgstx.schwabapi.service.MarketDataService;
import com.higgstx.schwabapi.service.TokenManager;
import com.higgstx.schwabapi.model.market.DailyPriceData;
import com.higgstx.schwabapi.exception.SchwabApiException;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.config.*;
import com.hpi.entities.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility controller for retrieving stock data from Schwab API and updating database
 */
@Slf4j
@RequiredArgsConstructor
public class StockQuotesSchwabController
{

    private final MarketDataService marketDataService;
    private final TokenManager tokenManager;
    private final ArrayList<String> equitySymbolList = new ArrayList<>();
    private final ArrayList<DailyPriceData> schwabDailyPriceData = new ArrayList<>();
    
    private final CMOfxDLFIModel baseModel = CMOfxDirectModel
            .getFIMODELS().get(0);

    // Singleton pattern
    private static StockQuotesSchwabController instance;

    protected StockQuotesSchwabController()
    {
        Properties props = new Properties();

        props.setProperty("tda.client_id", this.baseModel.getClientId());
        props.setProperty("tda.client_secret", this.baseModel.getClientSecret());
        props.setProperty("tda.token.refresh", this.baseModel.getTokenRefresh());
        props.setProperty("tda.account.id", this.baseModel.getAccountModels()
                .get(0).getAcctNumber());
        props.setProperty("tda.debug.bytes.length", this.baseModel.getDebugBytes());
        props.setProperty("tda.redirect_url", this.baseModel.getRedirectUrl());
        props.setProperty("tda.httpTimeout", this.baseModel.getHttpTimeout());
        props.setProperty("tda.scope", "readonly");
        props.setProperty("tda.auth_url", this.baseModel.getAuthUrl());
        props.setProperty("tda.auth_token_url", this.baseModel.getAuthTokenUrl());
        props.setProperty("tda.market_url", this.baseModel.getMarketUrl());
        props.setProperty("tda.trader_url", this.baseModel.getTraderUrl());

        try
        {

            // Create services manually without Spring proxies to avoid module issues
            SchwabTestConfig config = new SchwabTestConfig();
            // Manually set properties from application.yml
            config.setAppKey("y5eXVg33MBOWWyAOkDTuNRFr35Ml1Y5p");
            config.setAppSecret("hy9B5A0tf7KcYE1A");
            config.setTokenPropertiesFile("schwab-api.json");

            // Set URLs
            SchwabTestConfig.Urls urls = new SchwabTestConfig.Urls();
            urls.setAuth("https://api.schwabapi.com/v1/oauth/authorize");
            urls.setToken("https://api.schwabapi.com/v1/oauth/token");
            urls.setMarketData("https://api.schwabapi.com/marketdata/v1");
            config.setUrls(urls);

            // Set defaults
            SchwabTestConfig.Defaults defaults = new SchwabTestConfig.Defaults();
            defaults.setRedirectUri("https://127.0.0.1:8182");
            defaults.setScope("readonly");
            defaults.setHttpTimeoutMs(30000);
            config.setDefaults(defaults);

            // Create services directly
            this.tokenManager = new TokenManager(
                    config.getTokenPropertiesFile(),
                    config.getAppKey(),
                    config.getAppSecret()
            );

            SchwabApiProperties apiProperties = new SchwabApiProperties(
                    config.getUrls().getAuth(),
                    config.getUrls().getToken(),
                    config.getUrls().getMarketData(),
                    config.getDefaults().getRedirectUri(),
                    config.getDefaults().getScope(),
                    config.getDefaults().getHttpTimeoutMs()
            );

            this.marketDataService = new MarketDataService(apiProperties,
                    tokenManager);

            log.info(
                    "Schwab services initialized manually (avoiding Spring proxy issues)");

        }
        catch (SchwabApiException e)
        {
            log.error("Failed to initialize Schwab services", e);
            throw new RuntimeException("Failed to initialize Schwab services", e);
        }
    }

    public synchronized static StockQuotesSchwabController getInstance()
    {
        if (instance == null)
        {
            instance = new StockQuotesSchwabController();
        }
        return instance;
    }

    /**
     * Main processing method - get symbols, fetch data, update database
     */
    public void doAllStocksOneDay()
    {
        log.info("Starting daily stock data update");

        try
        {
            schwabDailyPriceData.clear();
            marketDataService.ensureServiceReady("daily stock update");

            getEquitySymbolList();
            if (equitySymbolList.isEmpty())
            {
                log.warn("No symbols found");
                return;
            }

            getEquityDataFromList();
            doHistoricalSQL();
            doUtil_LastDailyStock();

            log.info("Daily update completed: {} symbols, {} data points",
                    equitySymbolList.size(), schwabDailyPriceData.size());

        }
        catch (SchwabApiException e)
        {
            log.error("Daily update failed: {}", e.getMessage(), e);
            System.out.println("Daily update failed: " + e.getMessage());
//            throw new RuntimeException("Daily stock update failed", e);
            System.exit(1);
        }
    }

    /**
     * Load equity symbols from database
     */
    public void getEquitySymbolList()
    {
        equitySymbolList.clear();

        try (Connection con = CMDBController.getConnection();
                PreparedStatement pStmt = con.prepareStatement(
                        EquityInfoModel.TICKER); ResultSet rs = pStmt.
                        executeQuery())
        {
//            int i = 0;
            while (rs.next())
            {
                String ticker = rs.getString("Ticker");
                if (ticker != null && !ticker.trim().isEmpty())
                {
                    equitySymbolList.add(ticker.trim().toUpperCase());

//                    if (++i >= 3) break;
                }
            }

            log.info("Loaded {} symbols from database", equitySymbolList.size());

        }
        catch (SQLException e)
        {
            log.error("Failed to load symbols: {}", e.getMessage());
            throw new RuntimeException("Database error loading symbols", e);
        }
    }

    /**
     * Fetch data from Schwab API for all symbols
     */
    public void getEquityDataFromList()
    {
        if (equitySymbolList.isEmpty())
        {
            log.warn("No symbols to fetch");
            return;
        }

        LocalDate prevDate = CMDBController.getDateForEquityId("aapl");

        try
        {
            String[] symbolsArray;
            symbolsArray = equitySymbolList.toArray(String[]::new);
            log.info("Fetching data for {} symbols", symbolsArray.length);

            List<DailyPriceData> data = marketDataService
                    .getBulkHistoricalData(symbolsArray, prevDate);
            schwabDailyPriceData.clear();
            schwabDailyPriceData.addAll(data);

            long successful = schwabDailyPriceData.stream().filter(
                    DailyPriceData::isSuccess).count();
            log.info("Retrieved {} total data points ({} successful)",
                    schwabDailyPriceData.size(), successful);

        }
        catch (SchwabApiException e)
        {
            log.error("Schwab API error: {}", e.getMessage());
            throw new RuntimeException("API call failed", e);
        }
    }

    private void doHistoricalSQL()
    {
        if (schwabDailyPriceData.isEmpty())
        {
            return;
        }

        String sql = "INSERT IGNORE INTO hlhtxc5_dmOfx.EquityHistory "
                + "(TickerIEX, Ticker, Date, Open, High, Low, Close, Volume) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int successCount = 0;
        int errorCount = 0;
        final int BATCH_SIZE = 500;

        Instant startInstant = Instant.now();
        System.out.println("Start doHistoricalSQL: " + startInstant);

        // Filter valid data first to avoid processing during batch
        List<DailyPriceData> validData = schwabDailyPriceData.stream()
                .filter(DailyPriceData::isSuccess)
                .collect(Collectors.toList());

        errorCount = schwabDailyPriceData.size() - validData.size();
        try (Connection con = CMDBController.getConnection())
        {
            con.setAutoCommit(false);

            try (PreparedStatement pStmt = con.prepareStatement(sql))
            {

                // Process in batches
                for (int i = 0; i < schwabDailyPriceData.size(); i += BATCH_SIZE)
                {
                    int endIndex = Math.min(i + BATCH_SIZE,
                            schwabDailyPriceData.size());
                    List<DailyPriceData> batch = schwabDailyPriceData.subList(i,
                            endIndex);

                    try
                    {
                        for (DailyPriceData data : batch)
                        {
                            // we added some non-historical quote data to the array
                            if (data.getLocalDate() == null)
                            {
                                continue;
                            }
                            pStmt.setString(1, data.getSymbol());
                            pStmt.setString(2, data.getSymbol());
                            pStmt.setDate(3, java.sql.Date.valueOf(data.
                                    getLocalDate()));
                            pStmt.setDouble(4, data.getOpen() != null ? data.
                                    getOpen() : 0.0);
                            pStmt.setDouble(5, data.getHigh() != null ? data.
                                    getHigh() : 0.0);
                            pStmt.setDouble(6, data.getLow() != null ? data.
                                    getLow() : 0.0);
                            pStmt.setDouble(7, data.getClose() != null ? data.
                                    getClose() : 0.0);
                            pStmt.setLong(8, data.getVolume() != null ? data.
                                    getVolume() : 0L);
                            pStmt.addBatch();
                        }

                        // results will be -2 meaning success but unknown
                        // number of rows affected
                        int[] results = pStmt.executeBatch();
                        con.commit();
                        successCount += results.length;

                        log.debug("Processed batch {}-{} ({} records)",
                                i + 1, endIndex, batch.size());

                    }
                    catch (BatchUpdateException e)
                    {
                        // Handle partial batch failures
                        int[] updateCounts = e.getUpdateCounts();
                        for (int count : updateCounts)
                        {
                            if (count >= 0)
                            {
                                successCount++;
                            }
                        }
                        log.warn("Batch update exception: {}", e.getMessage());
                        con.rollback();
                    }
                    catch (SQLException e)
                    {
                        log.error("Batch failed: {}", e.getMessage());
                        con.rollback();
                        throw e;
                    }

                    pStmt.clearBatch();
                }

            }
            finally
            {
                con.setAutoCommit(true);
            }

            log.info("SQL batch insert completed: {} successful, {} errors",
                    successCount, errorCount);
            Instant endInstant = Instant.now();
            System.out.println("End doHistoricalSQL: " + endInstant);
            System.out.println("Duration: " + Duration.between(startInstant,
                    endInstant));

        }
        catch (SQLException e)
        {
            log.error("SQL batch insert failed: {}", e.getMessage());
            throw new RuntimeException("Database insert failed", e);
        }
    }

    /**
     * Insert data into EquityHistory table
     */
    private void doHistoricalSQLOld()
    {
        if (schwabDailyPriceData.isEmpty())
        {
            return;
        }

        String sql = "INSERT IGNORE INTO hlhtxc5_dmOfx.EquityHistory "
                + "(TickerIEX, Ticker, Date, Open, High, Low, Close, Volume) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int successCount = 0;
        int errorCount = 0;
        Instant startInstant = Instant.now();
        System.out.println("Start doHistoricalSQL: " + startInstant);

        try (Connection con = CMDBController.getConnection();
                PreparedStatement pStmt = con.prepareStatement(sql))
        {

            for (DailyPriceData data : schwabDailyPriceData)
            {
                if (!data.isSuccess())
                {
                    errorCount++;
                    continue;
                }

                try
                {
                    pStmt.setString(1, data.getSymbol());
                    pStmt.setString(2, data.getSymbol());
                    pStmt.setDate(3, java.sql.Date.valueOf(data.getLocalDate()));
                    pStmt.setDouble(4,
                            data.getOpen() != null ? data.getOpen() : 0.0);
                    pStmt.setDouble(5,
                            data.getHigh() != null ? data.getHigh() : 0.0);
                    pStmt.setDouble(6,
                            data.getLow() != null ? data.getLow() : 0.0);
                    pStmt.setDouble(7,
                            data.getClose() != null ? data.getClose() : 0.0);
                    pStmt.setLong(8,
                            data.getVolume() != null ? data.getVolume() : 0L);

                    pStmt.executeUpdate();
                    successCount++;

                }
                catch (SQLException e)
                {
                    log.debug("Insert failed for {}: {}", data.getSymbol(), e.
                            getMessage());
                    errorCount++;
                }
            }

            log.info("SQL insert completed: {} successful, {} errors",
                    successCount, errorCount);
            Instant endInstant = Instant.now();
            System.out.println("End doHistoricalSQL: " + endInstant);
            System.out.println("Duration: "
                    + Duration.between(startInstant, endInstant));

        }
        catch (SQLException e)
        {
            log.error("SQL batch insert failed: {}", e.getMessage());
            throw new RuntimeException("Database insert failed", e);
        }
    }

    /**
     * Update utility table with latest prices
     */
    private void doUtil_LastDailyStock()
    {
        CMDBController.executeSQL("TRUNCATE hlhtxc5_dmOfx.Util_LastDailyStock;");

        String sql = "INSERT IGNORE INTO hlhtxc5_dmOfx.Util_LastDailyStock "
                + "(EquityId, `Date`, Open, High, Low, Close, Volume) "
                + "SELECT eh.Ticker, eh.`Date`, eh.`Open`, eh.High, eh.Low, eh.`Close`, eh.Volume "
                + "FROM hlhtxc5_dmOfx.EquityHistory eh "
                + "JOIN (SELECT Ticker, MAX(`Date`) AS MaxDate "
                + "      FROM hlhtxc5_dmOfx.EquityHistory GROUP BY Ticker) latest "
                + "ON eh.Ticker = latest.Ticker AND eh.`Date` = latest.MaxDate "
                + "ORDER BY eh.Ticker, eh.`Date`;";

        CMDBController.executeSQL(sql);
        log.info("Updated Util_LastDailyStock table");
    }

    // Status and utility methods
    public boolean isServiceReady()
    {
        if (marketDataService.isReady())
        {
            // put new tokens into the config file
            // todo:  not done
            this.baseModel.setTokenRefresh(marketDataService.getTokenManager()
                    .getCurrentTokens().getRefreshToken());
            this.baseModel.setAuthUrl(marketDataService.getTokenManager()
                    .getCurrentTokens().getAccessToken());
        }
        return marketDataService.isReady();
    }

//    public String getTokenStatus()
//    {
//        return marketDataService.getTokenStatus();
//    }
    public int getSymbolCount()
    {
        return equitySymbolList.size();
    }

    public int getDataCount()
    {
        return schwabDailyPriceData.size();
    }

    public long getSuccessCount()
    {
        return schwabDailyPriceData.stream().filter(DailyPriceData::isSuccess).
                count();
    }

    public void clearData()
    {
        equitySymbolList.clear();
        schwabDailyPriceData.clear();
    }
}
