package com.hpi.appcontrollers;

import com.hpi.ofxAggregates.OfxAggregateBase;
import com.hpi.ofxFileHandling.OfxFileController;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.TPCCMsql.*;
import com.hpi.appcontrollers.positions.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import javax.swing.JOptionPane;
import lombok.*;

// LOG4J 2.x IMPORTS
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

@Getter
@Setter
public class CmdLineController extends OfxAggregateBase {

    // LOG4J 2.x LOGGER - Modern approach
    private static final Logger logger = LogManager.getLogger(CmdLineController.class);

    String[] args;

    @Option(name = "--help", usage = "help")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bhelp = false;

    @Option(name = "--date", usage = "yyyy-MM-DD date")
    static String sDate = "";

    @Option(name = "--progressBar", usage = "Progress bar")
    private static String sCLIProgressBar;

    @Option(name = "--dataMart", usage = "Rebuilds the data mart")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bAll = false;

    @Option(name = "--dataMartNoUpdate", usage = "Rebuilds the data mart without updating from brokers")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bAllNoUpdate = false;

    @Option(name = "--positions", usage = "Translate Transactions to Positions")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bPositions = false;

    @Option(name = "--directory", usage = "Fully specified directory")
    static String sDirectory;

    @Option(name = "--equityInfo", usage = "Update equityInfo table")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bEquityInfo = false;

    @Option(name = "--file", usage = "Fully specified file name")
    static String sFilename;

    @Option(name = "--mpt", usage = "Modern Portfolio Theory Test")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bMpt = false;

    @Option(name = "--ofxInstitutions", usage = "update ofxInstitutions table from OFXHome")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bOfxInstitutions = false;

    @Option(name = "--processOfxFiles", usage = "process files in the directory, use --file")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bOfxFiles = false;

    @Option(name = "--etradeStockQuote", usage = "Download stock price data from etrade")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bEtradeStockQuote = false;

    @Option(name = "--etradeOptionQuote", usage = "Download stock price data from etrade")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bEtradeOptionQuote = false;

    @Option(name = "--clearDmOfxUserId", usage = "--clearDmOfxUserId [--userId [userId]]: Clears account dmOfx data for restart; dbOfx data not touched")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bClearDmOfxAcct = false;

    public static void setBClearDmOfxAcct(Boolean clear) {
        bClearDmOfxAcct = clear;
    }

    @Option(name = "--clearPositions", usage = "Clears all position data for all users for refresh; dbOfx data not touched")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bClearPositions = false;

    @Option(name = "--clearDbOfxUserId [--userId [userId]]", usage = "Clears account dbOfx data for restart; dmOfx data not touched: best to use --clearDmOfxUserId then --clearDbOfxUserId")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bClearDbOfxAcct = false;

    @Option(name = "--userId", usage = "Joomla numerical user Id")
    static Integer userId;

    @Option(name = "--acctId", usage = "Numerical Account Id")
    static Integer acctId;

    @Option(name = "--buildDemo [--userId [userId]]", usage = "Builds demo user with given [userId]")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bBuildDemoAcct = false;

    @Option(name = "--ignoreFiTId", usage = "Marks the given FiTId as 'ignore' in all dbOfx tables")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bIgnoreFiTId = false;

    @Option(name = "--resetSkip", usage = "Marks the given FiTId Skip field to false in all dbOfx tables")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bResetSkip = false;

    @Option(name = "--fiTId", usage = "FiTId to be marked 'ignore'")
    static String fiTId;

    //*** Singleton
    private static CmdLineController instance;

    protected CmdLineController() {
        // protected prevents instantiation outside of package
        CmdLineController.sCLIProgressBar = "true";
    }

    public static synchronized CmdLineController getInstance() {
        if (CmdLineController.instance == null) {
            CmdLineController.instance = new CmdLineController();
        }
        return CmdLineController.instance;
    }

    @SuppressWarnings("DM_EXIT")
    public void doCommandLine(String[] args) {
        logger.info("Command line arguments processing started");

        if (args.length == 0) {
            CmdLineController.bhelp = true;
        }
        if (!this.doParseCmdLine(args)) {
            return;
        }

        if (CmdLineController.bhelp) {
            System.out.print(String.format(CMLanguageController.getAppProp("Help"),
                    CMGlobalsModel.getCURRENT_BUILD_VERSION()
                    + "."
                    + CMGlobalsModel.getCURRENT_BUILD()
                    + System.getProperty("line.separator")));
            System.exit(0);
        }

        CMDBController.initDBConnection();
        CMDBModel.getInstance().getJoomlaId();

        if (CmdLineController.bClearDmOfxAcct) {
            logger.info("Clearing DmOfx account data for userId: {}", CmdLineController.userId);
            DbOfxController.getInstance().doClearDmOfxUserId(CmdLineController.userId);
        }

        if (CmdLineController.bClearPositions) {
            logger.info("Clearing all positions data");
            DbOfxController.getInstance().doClearPositions();
        }

        if (CmdLineController.bClearDbOfxAcct) {
            logger.info("Clearing DbOfx account data for userId: {}", CmdLineController.userId);
            DbOfxController.getInstance().doClearDbOfxUserId(CmdLineController.userId);
            return;
        }

        if (CmdLineController.bIgnoreFiTId) {
            logger.info("Marking FiTId {} as ignore for account {}", CmdLineController.fiTId, CmdLineController.acctId);
            DbOfxController.getInstance().doIgnoreFiTId(CmdLineController.fiTId, CmdLineController.acctId, 1);
            return;
        }

        if (CmdLineController.bResetSkip) {
            logger.info("Resetting skip flag for FiTId {} in account {}", CmdLineController.fiTId, CmdLineController.acctId);
            DbOfxController.getInstance().doIgnoreFiTId(CmdLineController.fiTId, CmdLineController.acctId, 0);
            return;
        }

        if (CmdLineController.bBuildDemoAcct) {
            logger.info("Building demo account for userId: {}", CmdLineController.userId);
            DbOfxController.getInstance().doBuildDemoAcct(CmdLineController.userId);
            return;
        }

        if (CmdLineController.bEquityInfo) {
            logger.info("Starting equity info update");
            FinVizController4.doEquityInfo();
            return;
        }

        if (CmdLineController.bEtradeStockQuote) {
            logger.info("Starting E*Trade stock quote download");
            StockQuotesEtradeController x = new StockQuotesEtradeController();
            x.doAllStocksOneDay();
        }

        if (CmdLineController.bEtradeOptionQuote) {
            logger.info("Starting E*Trade option quote download");
            OptionQuotesEtradeController x = new OptionQuotesEtradeController();
            x.doAllOptionsOneDay();
        }

        if (CmdLineController.bOfxInstitutions) {
            logger.info("Starting OFX institutions update");
            OFXHomeController.getInstance().doOfxData();
        }

        if (CmdLineController.bOfxFiles) {
            logger.info("Processing OFX files");
            OfxFileController.getInstance().processOfxFiles2SQLSetup();
        }

        if (CmdLineController.bAll) {
            logger.info("Starting full data mart rebuild process");
            try {
                this.updateAppTracking("TPCCL|dataMart");
            } catch (SQLException ex) {
                logger.error("Failed to update app tracking for dataMart operation", ex);
            }

            if (userId != null) {
                CMDBModel.setUserId(userId);
                logger.debug("Set user ID to: {}", userId);
            }

            if (CMDBModel.getUserId() == null) {
                logger.error("User authentication failed - no valid user ID");
                CMHPIUtils.showDefaultMsg(
                        CMLanguageController.getDBErrorProp("Title"),
                        Thread.currentThread().getStackTrace()[1].getClassName(),
                        Thread.currentThread().getStackTrace()[2].getMethodName(),
                        CMLanguageController.getDBErrorProp("LoginFailed"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            logger.info("Clearing transient tables");
            DbOfxController.getInstance().cleanDbOfx();
            DataMartController.getInstance().cleanDataMart();

            logger.info("Processing OFX data from financial institutes");
            OfxDirectDLController.getInstance().doDirectOfx();
            OfxFileController.getInstance().processOfxFiles2SQLSetup();

            logger.info("Processing dbOfx data to dataMart");
            DataMartController.getInstance().processOfxDBtoDataMart();
            DataMartController.getInstance().processFIFOStockLotsAccounts();
            DataMartController.getInstance().processFIFOOptionLotsAccounts();
            DataMartController.getInstance().processDataMart();

            logger.info("Starting positions processing");
            System.out.println("      start positions\n");
            OpenPositionsStockController.getInstance().doPositions();
            ClosedPositionsStockController.getInstance().doPositions();
            OpenPositionsOptionController.getInstance().doOpenPositions();
            ClosedPositionsOptionController.getInstance().doClosedPositions();
            System.out.println("      finish positions\n");
            System.out.println("        --- FINISHED ---\n");
            logger.info("Full data mart rebuild completed successfully");
        }

        if (CmdLineController.bAllNoUpdate) {
            logger.info("Starting data mart rebuild without broker updates");
            try {
                this.updateAppTracking("TPCCL|dataMartNoBrokerUpdate");
            } catch (SQLException ex) {
                logger.error("Failed to update app tracking for dataMartNoBrokerUpdate operation", ex);
            }

            if (userId != null) {
                CMDBModel.setUserId(userId);
                logger.debug("Set user ID to: {}", userId);
            }

            if (CMDBModel.getUserId() == null) {
                logger.error("User authentication failed - no valid user ID");
                CMHPIUtils.showDefaultMsg(
                        CMLanguageController.getDBErrorProp("Title"),
                        Thread.currentThread().getStackTrace()[1].getClassName(),
                        Thread.currentThread().getStackTrace()[2].getMethodName(),
                        CMLanguageController.getDBErrorProp("LoginFailed"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            logger.info("Clearing transient tables");
            DbOfxController.getInstance().cleanDbOfx();
            DataMartController.getInstance().cleanDataMart();

            logger.info("Processing existing data to dataMart");
            DataMartController.getInstance().processOfxDBtoDataMart();
            DataMartController.getInstance().processFIFOStockLotsAccounts();
            DataMartController.getInstance().processFIFOOptionLotsAccounts();
            DataMartController.getInstance().processDataMart();

            logger.info("Starting positions processing");
            System.out.println("      start positions\n");
            OpenPositionsStockController.getInstance().doPositions();
            ClosedPositionsStockController.getInstance().doPositions();
            OpenPositionsOptionController.getInstance().doOpenPositions();
            ClosedPositionsOptionController.getInstance().doClosedPositions();
            System.out.println("      finish positions\n");
            System.out.println("        --- FINISHED ---\n");
            logger.info("Data mart rebuild (no update) completed successfully");
        }

        if (CmdLineController.bPositions) {
            logger.info("Starting positions-only processing");
            System.out.println("        --- STARTING ---\n");
            try {
                this.updateAppTracking("TPCCL|positions");
            } catch (SQLException ex) {
                logger.error("Failed to update app tracking for positions operation", ex);
            }

            if (CMDBModel.getUserId() == null) {
                logger.error("User authentication failed - no valid user ID for positions processing");
                CMHPIUtils.showDefaultMsg(
                        CMLanguageController.getDBErrorProp("Title"),
                        Thread.currentThread().getStackTrace()[1].getClassName(),
                        Thread.currentThread().getStackTrace()[2].getMethodName(),
                        CMLanguageController.getDBErrorProp("LoginFailed"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            OpenPositionsOptionController.getInstance().doOpenPositions();
            ClosedPositionsOptionController.getInstance().doClosedPositions();
            System.out.println("        --- FINISHED ---\n");
            logger.info("Positions processing completed successfully");
        }
    }

    public Boolean doParseCmdLine(String[] aArgs) {
        Properties errorProps;
        CmdLineParser parser = new CmdLineParser(this);
        args = aArgs.clone();
        errorProps = CMLanguageController.getErrorProps();

        try {
            parser.parseArgument(args);
            logger.debug("Command line arguments parsed successfully: {}", Arrays.toString(args));
        } catch (CmdLineException e) {
            String s = String.format(errorProps.getProperty("Formatted1"), Arrays.toString(args));
            logger.error("Failed to parse command line arguments: {}", Arrays.toString(args), e);
            
            CMHPIUtils.showDefaultMsg(
                    errorProps.getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[2].getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public void updateAppTracking(String sAction) throws SQLException {
        if (CMDBModel.getUserId() == null || CMDBModel.getUserId() == 816) {
            logger.debug("Skipping app tracking for userId: {}", CMDBModel.getUserId());
            return;
        }

        String sql = "INSERT INTO hlhtxc5_dmOfx.AppTracking (JoomlaId, Action) VALUES (?, ?)";
        logger.debug("Updating app tracking - User: {}, Action: {}", CMDBModel.getUserId(), sAction);

        try (Connection con = CMDBController.getConnection();
             PreparedStatement pStmt = con.prepareStatement(sql)) {
            
            pStmt.setInt(1, CMDBModel.getUserId());
            pStmt.setString(2, sAction);
            pStmt.executeUpdate();
            
            logger.debug("App tracking updated successfully");
            
        } catch (SQLException ex) {
            logger.error("Database error in updateAppTracking - User: {}, Action: {}", 
                        CMDBModel.getUserId(), sAction, ex);
            throw new CMDAOException(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    ex.getMessage(), 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static synchronized String getsCLIProgressBar() {
        return CmdLineController.sCLIProgressBar;
    }
}