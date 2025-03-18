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
import org.apache.log4j.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

@Getter @Setter
public class CmdLineController
    extends OfxAggregateBase
{
    private static final Logger logger = Logger.getLogger(CmdLineController.class.getName());

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

//    @Option(name = "--equityHistoryIEX", usage = "Retrieve history from last date")
//    @SuppressWarnings("FieldMayBeFinal")
//    static Boolean bequityHistoryIEX = false;

//    @Option(name = "--equityHistoryIEXMin", usage
//        = "Retrieve history from last "
//        + "date on select tickers")
//    @SuppressWarnings("FieldMayBeFinal")
//    static Boolean bequityHistoryIEXMin = false;

    @Option(name = "--file", usage = "Fully specified file name")
    static String sFilename;

    @Option(name = "--mpt", usage = "Modern Portfolio Theory Test")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bMpt = false;

    @Option(name = "--ofxInstitutions", usage
        = "update ofxInstitutions table from OFXHome")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bOfxInstitutions = false;

    @Option(name = "--processOfxFiles", usage
        = "process files in the directory, use --file")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bOfxFiles = false;

    @Option(name = "--etradeStockQuote", usage
        = "Download stock price data from etrade")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bEtradeStockQuote = false;
    
    @Option(name = "--etradeOptionQuote", usage
        = "Download stock price data from etrade")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bEtradeOptionQuote = false;

    @Option(name = "--clearDmOfxUserId", usage
        = "--clearDmOfxUserId [--userId [userId]]: Clears account dmOfx data for restart; dbOfx data not touched")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bClearDmOfxAcct = false;

    public static void setBClearDmOfxAcct(Boolean clear)
    {
        bClearDmOfxAcct = clear;
    }

    @Option(name = "--clearPositions", usage
        = "Clears all position data for all users for refresh; dbOfx data not touched")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bClearPositions = false;

    @Option(name = "--clearDbOfxUserId [--userId [userId]]", usage = "Clears account dbOfx data for restart; "
        + "dmOfx data not touched: best to use --clearDmOfxUserId then --clearDbOfxUserId")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bClearDbOfxAcct = false;

    @Option(name = "--userId", usage = "Joomla numerical user Id")
    static Integer userId;

    @Option(name = "--acctId", usage = "Numerical Account Id")
    static Integer acctId;

    @Option(name = "--buildDemo [--userId [userId]]", usage
        = "Builds demo user with given [userId]")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bBuildDemoAcct = false;

    @Option(name = "--ignoreFiTId", usage
        = "Marks the given FiTId as 'ignore' in all dbOfx tables")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bIgnoreFiTId = false;

    @Option(name = "--resetSkip", usage
        = "Marks the given FiTId Skip field to false in all dbOfx tables")
    @SuppressWarnings("FieldMayBeFinal")
    static Boolean bResetSkip = false;

    @Option(name = "--fiTId", usage = "FiTId to be marked 'ignore'")
    static String fiTId;

    //*** Singleton
    private static CmdLineController instance;

    protected CmdLineController()
    {
        // protected prevents instantiation outside of package
        CmdLineController.sCLIProgressBar = "true";
//        this.sectorId = null;
    }

    public synchronized static CmdLineController getInstance()
    {
        if (CmdLineController.instance == null)
        {
            CmdLineController.instance = new CmdLineController();
        }
        return CmdLineController.instance;
    }
    //***

//    @Autowired private ClosedPositionsOptionController2 closedPositionsOptionController;
    @SuppressWarnings("DM_EXIT")
    public void doCommandLine(String[] args)
    {
//        CMDBModel.getInstance().getJoomlaId();
        logger.info("Command line arguments");

        if (args.length == 0)
        {
            CmdLineController.bhelp = true;
        }
        if (!this.doParseCmdLine(args))
        {
            return;
        }

        if (CmdLineController.bhelp)
        {
            System.out.print(String.format(CMLanguageController.
                getAppProp("Help"),
                CMGlobalsModel.getCURRENT_BUILD_VERSION()
                + "."
                + CMGlobalsModel.getCURRENT_BUILD()
                + System.getProperty("line.separator")));

            System.exit(0);
        }
        
        CMDBController.initDBConnection();
        
        CMDBModel.getInstance().getJoomlaId();

        if (CmdLineController.bClearDmOfxAcct)
        {
            //use command line userId
            DbOfxController.getInstance().doClearDmOfxUserId(CmdLineController.userId);
//            return;
        }

        if (CmdLineController.bClearPositions)
        {
            DbOfxController.getInstance().doClearPositions();
//            return;
        }

        if (CmdLineController.bClearDbOfxAcct)
        {
            //use command line userId
            DbOfxController.getInstance().doClearDbOfxUserId(CmdLineController.userId);
            return;
        }

        if (CmdLineController.bIgnoreFiTId)
        {
            DbOfxController.getInstance().doIgnoreFiTId(CmdLineController.fiTId, CmdLineController.acctId, 1);
            return;
        }

        if (CmdLineController.bResetSkip)
        {
            DbOfxController.getInstance().
                doIgnoreFiTId(CmdLineController.fiTId,
                    CmdLineController.acctId, 0);
            return;
        }

        if (CmdLineController.bBuildDemoAcct)
        {
            //use command line userId
            DbOfxController.getInstance().doBuildDemoAcct(CmdLineController.userId);
            return;
        }

        if (CmdLineController.bEquityInfo)
        {
            FinVizController4.doEquityInfo();

            return;
        }

        if (CmdLineController.bEtradeStockQuote) 
        {
            StockQuotesEtradeController x = new StockQuotesEtradeController();
            x.doAllStocksOneDay();
//            StockQuotesEtradeController.doAllStocksOneDay();
        }
        
        if (CmdLineController.bEtradeOptionQuote) 
        {
            OptionQuotesEtradeController x = new OptionQuotesEtradeController();
            x.doAllOptionsOneDay();
//            OptionQuotesEtradeController.doAllOptionsOneDay();
        }

        if (CmdLineController.bOfxInstitutions)
        {
            OFXHomeController.getInstance().doOfxData();
        }

        if (CmdLineController.bOfxFiles)
        {
            // files
            OfxFileController.getInstance().processOfxFiles2SQLSetup();
        }

        if (CmdLineController.bAll)
        {
            String sql;

            try
            {
                this.updateAppTracking("TPCCL|dataMart");
            } catch (SQLException ex)
            {
                ////Logger.getLogger(this.getClass().getName(), null);
            }

//            CmdLineController.userId = CMDBModel.getUserId();
            if (userId != null)
            {
                CMDBModel.setUserId(userId);
//                DbOfxController.getInstance().doClearDmOfxUserId(CmdLineController.userId);
            }

            if (CMDBModel.getUserId() == null)
            {
                // there are arguments but they do not match what we can handle
                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[2].getMethodName(),
                    CMLanguageController.getDBErrorProp("LoginFailed"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // Clear transient tables
            DbOfxController.getInstance().cleanDbOfx();
            DataMartController.getInstance().cleanDataMart();

            // process ofx data from financial institutes
            // direct
            OfxDirectDLController.getInstance().doDirectOfx();
            // files
            OfxFileController.getInstance().processOfxFiles2SQLSetup();

            //process dbOfx data to dataMart
            DataMartController.getInstance().processOfxDBtoDataMart();

            //process dmOfx stock to DataMart
            DataMartController.getInstance().processFIFOStockLotsAccounts();

            //process dmOfx options to dataMart
            DataMartController.getInstance().processFIFOOptionLotsAccounts();

            //main dataMart processing: ok to here
            DataMartController.getInstance().processDataMart();

            System.out.println("      start positions\n");

            OpenPositionsStockController.getInstance().doPositions();
            ClosedPositionsStockController.getInstance().doPositions();

            OpenPositionsOptionController.getInstance().doOpenPositions();
            ClosedPositionsOptionController.getInstance().doClosedPositions();

            System.out.println("      finish positions\n");

            System.out.println("        --- FINISHED ---\n");
        }
        
        if (CmdLineController.bAllNoUpdate)
        {
            String sql;

            try
            {
                this.updateAppTracking("TPCCL|dataMartNoBrokerUpdate");
            } catch (SQLException ex)
            {
                ////Logger.getLogger(this.getClass().getName(), null);
            }

//            CmdLineController.userId = CMDBModel.getUserId();
            if (userId != null)
            {
                CMDBModel.setUserId(userId);
//                DbOfxController.getInstance().doClearDmOfxUserId(CmdLineController.userId);
            }

            if (CMDBModel.getUserId() == null)
            {
                // there are arguments but they do not match what we can handle
                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[2].getMethodName(),
                    CMLanguageController.getDBErrorProp("LoginFailed"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // Clear transient tables
            DbOfxController.getInstance().cleanDbOfx();
            DataMartController.getInstance().cleanDataMart();

            //process dbOfx data to dataMart
            DataMartController.getInstance().processOfxDBtoDataMart();

            //process dmOfx stock to DataMart
            DataMartController.getInstance().processFIFOStockLotsAccounts();

            //process dmOfx options to dataMart
            DataMartController.getInstance().processFIFOOptionLotsAccounts();

            //main dataMart processing: ok to here
            DataMartController.getInstance().processDataMart();

            System.out.println("      start positions\n");

            OpenPositionsStockController.getInstance().doPositions();
            ClosedPositionsStockController.getInstance().doPositions();

            OpenPositionsOptionController.getInstance().doOpenPositions();
            ClosedPositionsOptionController.getInstance().doClosedPositions();

            System.out.println("      finish positions\n");

            System.out.println("        --- FINISHED ---\n");
        }

        if (CmdLineController.bPositions)
        {

            System.out.println("        --- STARTING ---\n");
            try
            {
                this.updateAppTracking("TPCCL|positions");
            } catch (SQLException ex)
            {
                ////Logger.getLogger(this.getClass().getName(), null);
            }

//            userId = CMDBModel.getUserId();
            if (CMDBModel.getUserId() == null)
            {
                // there are arguments but they do not match what we can handle
                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[2].getMethodName(),
                    CMLanguageController.getDBErrorProp("LoginFailed"),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

//            OpenPositionsStockController.getInstance().doPositions();
//            ClosedPositionsStockController.getInstance().doPositions();

            OpenPositionsOptionController.getInstance().doOpenPositions();
            ClosedPositionsOptionController.getInstance().doClosedPositions();

            System.out.println("        --- FINISHED ---\n");
        }
    }

    public Boolean doParseCmdLine(String[] aArgs)
    {
        Properties errorProps;
        CmdLineParser parser;
        parser = new CmdLineParser(this);

        args = aArgs.clone();

        errorProps = CMLanguageController.getErrorProps();

        try
        {
            parser.parseArgument(args);
        } catch (CmdLineException e)
        {
            String s;

            s = String.format(errorProps.getProperty("Formatted1"),
                Arrays.toString(args));

            // there are arguments but they do not match what we can handle
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

    public void updateAppTracking(String sAction)
        throws SQLException
    {
        String sql;

        if (CMDBModel.getUserId() == null
            || CMDBModel.getUserId() == 816)
        {
            return;
        }

        sql
            = "insert into hlhtxc5_dmOfx.AppTracking (JoomlaId, Action) values ('"
            + CMDBModel.getUserId() + "', '" + sAction + "');";

        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(sql))
        {
            pStmt.executeUpdate();
            pStmt.close();
            con.close();
        } catch (SQLException ex)
        {
            throw new CMDAOException(CMLanguageController.
                getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                ex.getMessage(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static synchronized String getsCLIProgressBar()
    {
        return CmdLineController.sCLIProgressBar;
    }
}
