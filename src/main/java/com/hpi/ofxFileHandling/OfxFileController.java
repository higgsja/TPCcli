package com.hpi.ofxFileHandling;

import com.hpi.appcontrollers.CmdLineController;
import com.hpi.TPCCMcontrollers.BrokersController;
import com.hpi.ofxAggregates.OfxAggregateBase;
import com.hpi.ofxAggregates.OfxOfx;
import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.TPCCMprefs.*;
import com.hpi.hpiUtils.CMHPIUtils;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 */
public class OfxFileController
    extends OfxAggregateBase
{

    private OfxOfx ofx;
    private Document doc;
    private final String errorPrefix;
    private String fErrorPrefix;
    CMProgressBarCLI bar;
    private ArrayList<String> fileList;
    //*** Singleton
    private static OfxFileController instance;

    protected OfxFileController()
    {
        // protected prevents instantiation outside of package
        this.doc = null;

        this.errorPrefix
            = this.getClass().getName();
        this.fErrorPrefix = null;
    }

    public synchronized static OfxFileController getInstance()
    {
        if (OfxFileController.instance == null)
        {
            OfxFileController.instance = new OfxFileController();
        }
        return OfxFileController.instance;
    }

    

    /**
     * Retrieves list of files in the directory databases.config
     * specifies as &lt;QuickenData&gt; and processes them
     *
     * @return
     */
    public Boolean processOfxFiles2SQLSetup()
    {
        String s;
        File folder;
        File[] files;

        // initialize BrokersController
        BrokersController.getInstance();

        // get list of files to process
        this.fileList = new ArrayList<>();
        folder = new File(CMDirectoriesModel.getInstance().
            getProps().getProperty("OfxFiles"));

        files = folder.listFiles();

        if (files == null || files.length == 0)
        {
            return true;
        }

        for (File file : files)
        {
            if (file.isFile())
            {
                this.fileList.add(file.getAbsolutePath());
            }
        }

        if (this.fileList.isEmpty())
        {
            return true;
        }

        for (String file : this.fileList)
        {
            s = String.format(CMLanguageController.getAppProp("FCFormat1"),
                file,
                CMHPIUtils.getLongDate());

            System.out.println("Processing file: " + s + "\n");

            if (!processOfxFile(file))
            {
//                s = String.format(CMLanguageController.getAppProp("FCFormat2"),
//                      file, CMHPIUtils.getInstance().getLongDate());
                //return false;
                continue;
            }

            // ignore the signon message other than status
            if (this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode() != 0)
            {
                switch (this.ofx.getSignOnMsgsRSv1().getSonRS().
                    getStatus().getCode())
                {
                    case 2000:
                    case 3000:
                    case 3001:
                    case 13504:
                    case 15000:
                    case 15500:
                    case 15501:
                    case 15502:
                    case 15506:
                    case 15507:
                    case 15510:
                    case 15511:
                    case 15512:
                    case 15513:
//                        s = CMLanguageController.getErrorProps().
//                              getProperty("OfxSignonStatus"
//                                    + this.ofx.getSignOnMsgsRSv1().
//                                          getSonRS().getStatus().getCode());
                        break;
                    default:
//                        s = String.format(CMLanguageController.
//                              getErrorProps().getProperty("Formatted3"),
//                              this.ofx.getSignOnMsgsRSv1().getSonRS().
//                                    getStatus().getCode());

                }
                return false;
            }

            if (processOfx2SQL())
            {
//                s = String.format(CMLanguageController.getAppProp("FCFormat3"),
//                      file, CMHPIUtils.getInstance().getLongDate());

                try
                {
                    Files.deleteIfExists(Paths.get(file));
                } catch (IOException ex)
                {
                    // not important
                }
            } else
            {
                return false;
            }
        }
        return true;
    }

    public Boolean processOfxFile2SQLSetup(String sInputFile)
    {
        // String s;

//         s = String.format(CMLanguageController.getAppProp("FCFormat1"),
//               sInputFile,
//               CMHPIUtils.getLongDate());
//         // initialize BrokersController
//         BrokersController.getInstance();
//         // ensure in right database
// //        this.connectDB("OfxBroker");
//         if (!processOfxFile(sInputFile))
//         {
//             s = String.format(CMLanguageController.getAppProp("FCFormat2"),
//                   sInputFile, CMHPIUtils.getLongDate());
//             return false;
//         }
        // ignore the signon message other than status
        if (this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode() != 0)
        {
            //todo: provide an error message
//            s = "OfxSignonStatus"
//                    + this.ofx.getSignOnMsgsRSv1().getSonRS().
//                            getStatus().getCode().toString();
            // switch (this.ofx.getSignOnMsgsRSv1().getSonRS().
            //       getStatus().getCode())
            // {
            //     case 2000:
            //     case 3000:
            //     case 3001:
            //     case 13504:
            //     case 15000:
            //     case 15500:
            //     case 15501:
            //     case 15502:
            //     case 15506:
            //     case 15507:
            //     case 15510:
            //     case 15511:
            //     case 15512:
            //     case 15513:
            //         s = CMLanguageController.getErrorProps().
            //               getProperty("OfxSignonStatus"
            //                     + this.ofx.getSignOnMsgsRSv1().
            //                           getSonRS().getStatus().getCode());
            //         break;
            //     default:
            //         s = String.format(CMLanguageController.
            //               getErrorProps().getProperty("Formatted3"),
            //               this.ofx.getSignOnMsgsRSv1().getSonRS().
            //                     getStatus().getCode());

            // }
            return false;
        }

        if (processOfx2SQL())
        {
            // s = String.format(CMLanguageController.getAppProp("FCFormat3"),
            //       sInputFile, CMHPIUtils.getLongDate());

            return true;
        }

        return false;
    }

    public void processOfxDoc2SQLSetup(Document doc, CMProgressBarCLI bar)
    {
        String s;

        // initialize BrokersController
        BrokersController.getInstance();

        this.doc = doc;
        this.bar = bar;
        this.ofx = new OfxOfx();

        this.processOfxDoc();

        // ignore the signon message other than status
        if (this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode() != 0)
        {
            //todo: provide an error message
//            s = "OfxSignonStatus"
//                    + this.ofx.getSignOnMsgsRSv1().getSonRS().
//                            getStatus().getCode().toString();
            switch (this.ofx.getSignOnMsgsRSv1().getSonRS().
                getStatus().getCode())
            {
                case 2000:
                case 3000:
                case 3001:
                case 13504:
                case 15000:
                case 15500:
                case 15501:
                case 15502:
                case 15506:
                case 15507:
                case 15510:
                case 15511:
                case 15512:
                case 15513:
                    s = CMLanguageController.getErrorProps().
                        getProperty("OfxSignonStatus"
                            + this.ofx.getSignOnMsgsRSv1().
                                getSonRS().getStatus().getCode());
                    break;
                default:
                    s = String.format(CMLanguageController.
                        getErrorProps().getProperty("Formatted3"),
                        this.ofx.getSignOnMsgsRSv1().getSonRS().getStatus().getCode());
            }
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"), s);

            CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProp("Title")
                + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName() + "\n\t",
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);
            return;
//            throw new UnsupportedOperationException(s);
        }

        this.barLabel("        Updating database ...");

        processOfx2SQL();
    }

    private Boolean processOfx2SQL()
    {
        // from this ofx object, use <invacctfrom> to establish
        // the broker and account
        // returns the AcctId
        // ensure in the right database
//        CMDBController.getInstance().close();
//        this.connectDB("OfxBroker");
        this.ofx.getInvStmtMsgsRSv1().getInvStmtTrnRS().
            getInvStmtRS().getInvAcctFrom().
            doSQL(this.ofx.getSignOnMsgsRSv1().getSonRS().getOfxFI());

        this.ofx.getInvStmtMsgsRSv1().getInvStmtTrnRS().
            getInvStmtRS().doSQL(this.ofx.getInvStmtMsgsRSv1().
                getInvStmtTrnRS().getInvStmtRS().
                getInvAcctFrom());

        this.ofx.getSecListMsgsRSv1().getSecList().doSQL(this.ofx.
            getInvStmtMsgsRSv1().
            getInvStmtTrnRS().getInvStmtRS().
            getInvAcctFrom());

        // run any required stored procedures after all Ofx work is complete
        ofxBrokerAdjustments();

        return true;
    }

    public Boolean processOfxFile(String sInputFile)
    {
        if (!processFile2XMLDoc(sInputFile))
        {
            return false;
        }

        return processOfxDoc();
    }

    public Boolean processOfxDoc()
    {
        Element e1;
        String s;

        this.ofx = new OfxOfx();

        if ((e1 = doc.select("ofx").first()) == null)
        {
            s = String.format(CMLanguageController.
                getErrorProp("OfxResponseEmpty"));

            CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProp("Title")
                + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].
                    getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }

        return this.ofx.doData(e1);
    }

    /*
     * Given an input file, run through conversion to XML
     *
     * @param sInputFile
     * @return
     */
    public Boolean processFile2XMLDoc(String sInputFile)
    {
        // conversion of file to XML, returned in 'doc'
        return (doc = FixSGML2XML.getInstance().
            doSGMLFile2XML(sInputFile)) != null;
    }

    /*
     * Given an input file, run through conversion to XML
     * and place results in an output file
     *
     * @param sInputFile
     * @param sOutputFile
     * @return
     */
    public Boolean processFile2XMLFile(String sInputFile, String sOutputFile)
    {
        fErrorPrefix = Thread.currentThread().getStackTrace()[2].getMethodName();

        Path path;
        String s;

        // test validity of output file
        path = Paths.get(sOutputFile);
        try ( OutputStream output = Files.newOutputStream(path,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING))
        {
            output.close();

            // conversion of file to XML, returned in 'doc'
            if (!this.processFile2XMLDoc(sInputFile))
            {
                Files.delete(path);
                return false;
            }
        } catch (IOException ex)
        {
            // unable to create the output file
            s = String.format(CMLanguageController.
                getErrorProps().getProperty("Formatted10"),
                sOutputFile);
            CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProps().
                getProperty("Title"),
                errorPrefix,
                fErrorPrefix,
                s,
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try ( BufferedWriter output = Files.newBufferedWriter(path,
            Charset.forName("US-ASCII"),
            StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING))
        {
            s = doc.toString();
            output.write(s, 0, s.length());
            output.close();
        } catch (IOException ex)
        {
            // unable to create the output file
            s = String.format(CMLanguageController.
                getErrorProps().getProperty("Formatted10"),
                sOutputFile);
            CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProps().
                getProperty("Title"),
                errorPrefix,
                fErrorPrefix,
                s,
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    void barUpdate(Integer numerator, Integer denominator)
    {
        // CmdLineController cmdLineController;

        // cmdLineController = CmdLineController.getInstance();
        if (CmdLineController.getsCLIProgressBar().equalsIgnoreCase("false"))
        {
            return;
        }

        this.bar.barUpdate(numerator, denominator);
    }

    void barLabel(String sLabel)
    {
        // CmdLineController cmdLineController;

        // cmdLineController = CmdLineController.getInstance();
        if (CmdLineController.getsCLIProgressBar().equalsIgnoreCase("true"))
        {
            this.bar.barLabel(sLabel);
        }
    }

    private void ofxBrokerAdjustments()
    {
        //none at this time
//        int i = 0;
        //we have created a unique EquityId field in SecInfo and must populate it
        //stock: set to SecInfo.Ticker
        //options: set to OCC id
        //mutual funds: set to SecInfo.SecName
        //had a stored procedure to handle this
        //moved to ininital creation of the transaction
//        CMDBController.callStored("hlhtxc5_dbOfx.setEquityId");

//        String sql, db;

//        db = "hlhtxc5_dbOfx.";
//
//        // Need to guarantee common EquityId across all providers
//        // so create our own
//        // Set EquityId for mutual funds
//        sql = "update " + db + "SecInfo "
//              + "inner join hlhtxc5_dbOfx.MFInfo on "
//              + "hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.MFInfo.BrokerId "
//              + "and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.MFInfo.SecId "
//              + "set EquityId = hlhtxc5_dbOfx.SecInfo.SecName "
//              + ";";
//        CMDBController.executeSQLSingleIntegerList(sql);
//
        // Set EquityId for Options
//        sql = "update " + db + "SecInfo "
//              + "inner join hlhtxc5_dbOfx.OptInfo on (hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.OptInfo.BrokerId "
//            + "and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.OptInfo.SecId) "
//              + "set EquityId = hlhtxc5_dbOfx.optionToOCC(hlhtxc5_dbOfx.SecInfo.Ticker, "
//              + "left(hlhtxc5_dbOfx.OptInfo.DtExpire, 8), "
//              + "hlhtxc5_dbOfx.OptInfo.OptType, hlhtxc5_dbOfx.OptInfo.StrikePrice) "
//            + "where left(hlhtxc5_dbOfx.SecInfo.Ticker, 1) <> '#\';";
//        CMDBController.executeSQL(sql);

//        // Set EquityId for Stock
//        sql = "update " + db + "SecInfo "
//              + "inner join hlhtxc5_dbOfx.StockInfo on hlhtxc5_dbOfx.SecInfo.BrokerId = hlhtxc5_dbOfx.StockInfo.BrokerId "
//              + "and hlhtxc5_dbOfx.SecInfo.SecId = hlhtxc5_dbOfx.StockInfo.SecId "
//              + "set EquityId = hlhtxc5_dbOfx.SecInfo.Ticker "
//              + ";";
//        CMDBController.executeSQL(sql);
    }

//    private void connectDB(String sActiveDBType)
//    {
//        TPCCMDatabaseModel.DB db;
//
//        db = TPCCMDatabaseModel.getInstance().getActiveDB(sActiveDBType);
//
//        if (!CMDBController.getDbName().equalsIgnoreCase(db.getsDbName()))
//        {
//            // current connection is the wrong one
//            CMDBController.getInstance(
//                  db.getsDbName(),
//                  db.getsFullURL(),
//                  db.getsUId(),
//                  db.getsPW(),
//                  db.getsDriver());
//        }
//    }
}
