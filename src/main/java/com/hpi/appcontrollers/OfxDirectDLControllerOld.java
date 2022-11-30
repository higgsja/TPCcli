package com.hpi.appcontrollers;

import com.hpi.ofxFileHandling.FixSGML2XML;
import com.hpi.ofxFileHandling.OfxFileController;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.TPCCMsql.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.*;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class OfxDirectDLControllerOld
//      extends DBCore
{

    private Document doc;
    private static final CMProgressBarCLI PROGRESS_BAR_CLI;
    /*
     * Singleton
     *
     */
    private static OfxDirectDLControllerOld instance;

    static {
        OfxDirectDLControllerOld.instance = null;
        PROGRESS_BAR_CLI = new CMProgressBarCLI(CmdLineController.
            getsCLIProgressBar());
    }

    protected OfxDirectDLControllerOld()
    {
        // protected prevents instantiation outside of package
    }

    public synchronized static OfxDirectDLControllerOld getInstance()
    {
        if (OfxDirectDLControllerOld.instance == null) {
            OfxDirectDLControllerOld.instance = new OfxDirectDLControllerOld();
        }
        return OfxDirectDLControllerOld.instance;
    }
    //***

    public void doDirectOfx()
    {
        PROGRESS_BAR_CLI.barLabel("Processing Financial Institutions:");
        // loop through financial institutions
        for (CMOfxDLFIModel fi : CMOfxDirectModel.getFIMODELS()) {
            if (fi.getActive().equalsIgnoreCase("Yes")) {
                PROGRESS_BAR_CLI.barLabel(
                    "  Processing Financial Institution: " + fi.getFiName());
            } else {
                PROGRESS_BAR_CLI.barLabel("  Skipping Financial Institution: " + fi.getFiName());
                continue;
            }

            PROGRESS_BAR_CLI.barLabel("    Processing Accounts:");

            // loop through accounts
            for (CMOfxDLAccountModel acct : fi.getAccountModels()) {
                if (acct.getAcctActive().equalsIgnoreCase("Yes")) {
                    PROGRESS_BAR_CLI.barLabel("      Processing Account: " + acct.getAcctName());
                    // get response from the server into doc
                    this.getInvStmtResponse(fi, acct);
                } else {
                    PROGRESS_BAR_CLI.barLabel("      Skipping Account: " + acct.getAcctName());
                }
            }
        }
    }

    public void getInvStmtResponse(CMOfxDLFIModel fi, CMOfxDLAccountModel acct)
    {
        LocalDate startDate;
        DateTimeFormatter formatter;

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (CmdLineController.sDate.isEmpty()) {
            startDate = getMaxAcctDate(acct);
        } else {
            startDate = LocalDate.parse(CmdLineController.sDate, formatter);

        }

        System.out.println("        Start Date: " + startDate.toString());

        // check database for latest date to construct request
        this.getInvStmtData(fi, acct, startDate);
    }

    private LocalDate getMaxAcctDate(CMOfxDLAccountModel acct)
    {
        String sql;
        ResultSet rs;
        LocalDate startDate;
        Integer userId;

        userId = CMDBModel.getUserId();

        sql = "select max(DtTrade) as MaxDate from hlhtxc5_dbOfx.InvTran, Accounts where Accounts.InvAcctIdFi = '"
            + acct.getAcctNumber() + "' " + "and JoomlaId = '" + userId + "' limit 1;";

        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(sql)) {
            pStmt.clearWarnings();
            rs = pStmt.executeQuery();

            sql = "";
            while (rs.next()) {
                // yyyymmdd
                if (rs.getString("MaxDate") != null) {
                    sql = rs.getString("MaxDate").substring(0, 8);
                }
            }
            pStmt.close();
            con.close();
        } catch (SQLException ex) {
            sql = String.format(
                CMLanguageController.getErrorProp("Formatted14"),
                ex.getMessage());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                sql,
                JOptionPane.ERROR_MESSAGE);

            throw new CMDAOException(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

        if (!sql.isEmpty()) {
            startDate = LocalDate.of(
                Integer.parseInt(sql.substring(0, 4)),
                Integer.parseInt(sql.substring(4, 6)),
                Integer.parseInt(sql.substring(6, 8)));
        } else {
            startDate = LocalDate.of(1970, Month.JANUARY, 1);
        }

        return startDate;
    }

    private void getInvStmtData(CMOfxDLFIModel fi,
        CMOfxDLAccountModel acct,
        LocalDate startDate)
    {
        StringBuilder sb;
        String s;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        // build the request
        sb = new StringBuilder(5000);
        sb.append(OfxDirectDLControllerOld.OFX_REQUEST_HEADER);

        sb.append("<OFX>");
        sb.append(System.lineSeparator());

        // these need some string work to replace variables
        s = String.format(OfxDirectDLControllerOld.OFX_SIGNON,
            CMHPIUtils.getLongDate(), // date of request
            acct.getAcctUId(), // user id
            acct.getAcctPW(), // user password
            fi.getFiOrg(), // financial institution organization
            fi.getFiId() // financial institution ID
        );
        sb.append(s);

        s = String.format(OfxDirectDLControllerOld.OFX_INVSTMT_REQUEST,
            CMHPIUtils.getLongDate(), //   transaction number
            fi.getBrokerId(), //                    broker id
            acct.getAcctNumber(), //                account number
            simpleDateFormat.format(Date.valueOf(startDate.toString())),
            "Y", //                                 include transactions
            "N", //                                 include open orders
            "Y", //                                 include positions
            "Y" //                                  include balances
        );
        sb.append(s);

        sb.append("</OFX>");
        sb.append(System.lineSeparator());

        // request complete
        // execute it
        this.getFiResponse(fi, acct, sb);
    }

    private void getFiResponse(CMOfxDLFIModel fi,
        CMOfxDLAccountModel acct, StringBuilder sbQuery)
    {
        String ofxQuery;
        URL ofxUrl;
        HttpsURLConnection connection;
        connection = null;
        String line, s;
        int intStatus;
        StringBuilder sb;
        FixSGML2XML fixSGML;
        OfxFileController ofxFileController;
        //Element e1;

        line = null;
        intStatus = -10;
        sb = new StringBuilder();

        // some financial institutions don't like additional white space
        ofxQuery = sbQuery.toString().replace("\t", "");

        try {
            ofxUrl = new URL(fi.getFiUrl());
            connection = (HttpsURLConnection) ofxUrl.openConnection();
            connection.setRequestMethod("POST");
            //connection.setRequestProperty("Content-Type", "application/x-oFX");
            connection.setRequestProperty("Content-Type", "application/xml");
//            connection.addRequestProperty("Content-length", 
//                Integer.toString(ofxQuery.length()));
            connection.setDoOutput(true);   // post
            connection.connect();

            PROGRESS_BAR_CLI.barLabel(
                "        Retrieving data ...");

            // send the query to the connection
           // byte[] out = ofxQuery.getBytes(StandardCharsets.UTF_8);
            
           // OutputStream stream = connection.getOutputStream();
            
           // stream.write(ofxQuery.getBytes(StandardCharsets.UTF_8));
            
            
            try (Writer writerOfx = new OutputStreamWriter(
                connection.getOutputStream(), "US-ASCII")) {
                writerOfx.write(ofxQuery);
                writerOfx.flush();

                intStatus = connection.getResponseCode();
                line = connection.getResponseMessage();
                
                //for some reason just will not work though everything seems fine
                //see reqbin.com/post-online
                if (connection.getResponseCode() >= 400) {
                    if (connection.getErrorStream() != null) {
                        try (BufferedReader readerOfx = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(),
                                "US-ASCII"))) {
                            while ((line = readerOfx.readLine()) != null) {
                                sb.append(line);
                            }
                        } catch (IOException ex) {
                            throw ex;
                        }

                        doc = Jsoup.parse(sb.toString());
                        // find the body and put it in the error message for display
                        Element element;
                        element = doc.select("body").first();
                        if (element == null) {
                            /*
                             * line = "HTTPS error code: " +
                             * Integer.toString(intStatus)
                             * + "\n"
                             * + "HTTPS message: "
                             * + "HTTPS error stream: "
                             * + doc.body().ownText();
                             * throw new IOException(line);
                             */
                            s = String.format(CMLanguageController.
                                getErrorProp("GeneralError"),
                                "No text returned.");

                            CMHPIUtils.showDefaultMsg(
                                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                                Thread.currentThread().getStackTrace()[1].
                                    getClassName(),
                                Thread.currentThread().getStackTrace()[1].
                                    getMethodName(),
                                s,
                                JOptionPane.ERROR_MESSAGE);
                            //do not throw, continue to execute
//                            throw new UnsupportedOperationException(s);
                        }

                        //in >=400 response code just return
                        if (connection.getResponseCode() >= 400) {
                            return;
                        }

                        // so, reset doc to the <body> element
                        String aString = doc.select("body").first().toString();
                        doc = Jsoup.parse(aString);

                        s = String.format(CMLanguageController.
                            getErrorProp("GeneralError"), doc.toString());

                        CMHPIUtils.showDefaultMsg(
                            CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);

                        //do not stop
//                        throw new UnsupportedOperationException(s);
                    } else {
                        s = String.format(CMLanguageController.
                            getErrorProp("GeneralError"), line);

                        CMHPIUtils.showDefaultMsg(
                            CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                getMethodName(),
                            s + "; Code: " + intStatus,
                            JOptionPane.ERROR_MESSAGE);

                        //in >=400 response code just return
                        if (connection.getResponseCode() >= 400) {
                            return;
                        }

                        //do not stop
//                        throw new UnsupportedOperationException(s);
                    }
                }
            } catch (IOException ex) {
                s = String.format(CMLanguageController.
                    getErrorProp("GeneralError"),
                    ex.toString());

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    s,
                    JOptionPane.ERROR_MESSAGE);

                throw new UnsupportedOperationException(ex.toString());
            }
        } catch (IOException e) {
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }

        if (intStatus != HttpsURLConnection.HTTP_OK) {
            // todo: z low deal with redirects 3xx
            line = "HTTPS error code: " + Integer.toString(intStatus) + "\n" + "HTTPS Message: " + line + "\n";

            s = String.format(CMLanguageController.
                getErrorProp("HttpError"),
                Integer.toString(intStatus),
                line + System.lineSeparator());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].
                    getClassName(),
                Thread.currentThread().getStackTrace()[1].
                    getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            //do not stop
//            throw new UnsupportedOperationException(s);
        }

//        PROGRESS_BAR_CLI.barLabel(
//            "        Processing response from financial institution ...");
        try (BufferedReader readerOfx = new BufferedReader(
            new InputStreamReader(connection.getInputStream(),
                "US-ASCII"))) {
            while ((line = readerOfx.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

//            throw new UnsupportedOperationException(s);
        } finally {
            connection.disconnect();
        }

        // Write raw return to file
//        PROGRESS_BAR_CLI.barLabel("        Writing raw return file ...");
        writeRaw(sb.toString(), acct.getAcctName());

        fixSGML = FixSGML2XML.getInstance();
        doc = fixSGML.doSGMLDoc2XML(Jsoup.parse(sb.toString()));
        if (doc == null || (doc.select("ofx").first()) == null) {
            s = String.format(CMLanguageController.
                getErrorProp("OfxResponseEmpty"));

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

//            throw new UnsupportedOperationException(s);
        }

        // Write adjusted return to file
//        PROGRESS_BAR_CLI.barLabel("        Writing XML return file ...");
        writeAdjusted(acct.getAcctName());

        ofxFileController = OfxFileController.getInstance();

        if (this.doc != null) {
            ofxFileController.processOfxDoc2SQLSetup(this.doc, PROGRESS_BAR_CLI);
        }
    }

    private void writeRaw(String aString, String sAcct)
    {
        File fileWrite;
        String s;

        fileWrite = new File(CMDirectoriesModel.getInstance().
            getModelProp("Reports") + java.io.File.separator + sAcct + ".OFX");
        try (BufferedWriter writerRaw = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(fileWrite),
                StandardCharsets.UTF_8))) {
            writerRaw.write(aString);
            writerRaw.flush();
        } catch (IOException e) {
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }
    }

    private void writeAdjusted(String sAcct)
    {
        File fileWrite;
        String s;

        if (doc == null) {
            return;
        }
        fileWrite = new File(CMDirectoriesModel.getInstance().
            getModelProp("Reports") + java.io.File.separator + sAcct + ".txt");

        try (BufferedWriter writerRaw = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(fileWrite),
                StandardCharsets.UTF_8))) {
            writerRaw.write(doc.body().toString());
            writerRaw.flush();
        } catch (IOException e) {
            s = String.format(CMLanguageController.
                getErrorProp("GeneralError"),
                e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                s,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(s);
        }
    }
    private final static String OFX_REQUEST_HEADER = "OFXHEADER:100\n" + "DATA:OFXSGML\n" + "VERSION:102\n"
        + "SECURITY:NONE\n" + "ENCODING:USASCII\n" + "CHARSET:1252\n" + "COMPRESSION:NONE\n" + "OLDFILEUID:NONE\n"
        + "NEWFILEUID:NONE\n\n";
    private final static String OFX_SIGNON = "<SIGNONMSGSRQV1>\n" + "<SONRQ>\n" + "<DTCLIENT>%s\n" + "<USERID>%s\n"
        + "<USERPASS>%s\n" + "<LANGUAGE>ENG\n" + "<FI>\n" + "<ORG>%s\n" + "<FID>%s\n" + "</FI>\n" + "<APPID>Money\n"
        + "<APPVER>1600\n" + "</SONRQ>\n" + "</SIGNONMSGSRQV1>\n\n";
    private final static String OFX_INVSTMT_REQUEST = "<INVSTMTMSGSRQV1>\n" + "<INVSTMTTRNRQ>\n" + "<TRNUID>%s-01\n"
        + "<INVSTMTRQ>\n" + "<INVACCTFROM>\n" + "<BROKERID>%s\n" + "<ACCTID>%s\n" + "</INVACCTFROM>\n" + "<INCTRAN>\n"
        + "<DTSTART>%s\n" + "<INCLUDE>%s\n" + "</INCTRAN>\n" + "<INCOO>%s\n" + "<INCPOS>\n" + "<INCLUDE>%s\n"
        + "</INCPOS>\n" + "<INCBAL>%s\n" + "</INVSTMTRQ>\n" + "</INVSTMTTRNRQ>\n" + "</INVSTMTMSGSRQV1>\n";
}
