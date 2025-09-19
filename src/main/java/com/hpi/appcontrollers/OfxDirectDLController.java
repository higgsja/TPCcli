package com.hpi.appcontrollers;

import com.hpi.ofxFileHandling.FixSGML2XML;
import com.hpi.ofxFileHandling.OfxFileController;
import com.hpi.hpiUtils.CMProgressBarCLI;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.TPCCMsql.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.*;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class OfxDirectDLController {

    private static final Logger logger = LoggerFactory.getLogger(CmdLineController.class);

    private Document doc;
    private static final CMProgressBarCLI PROGRESS_BAR_CLI;
    
    /*
     * Singleton
     */
    private static OfxDirectDLController instance;

    static {
        OfxDirectDLController.instance = null;
        PROGRESS_BAR_CLI = new CMProgressBarCLI(CmdLineController.getsCLIProgressBar());
    }

    protected OfxDirectDLController() {
        // protected prevents instantiation outside of package
        logger.debug("OfxDirectDLController instance created");
    }

    public static synchronized OfxDirectDLController getInstance() {
        if (OfxDirectDLController.instance == null) {
            OfxDirectDLController.instance = new OfxDirectDLController();
            logger.debug("OfxDirectDLController singleton instance initialized");
        }
        return OfxDirectDLController.instance;
    }

    public void doDirectOfx() {
        logger.info("Starting direct OFX processing for all financial institutions");
        PROGRESS_BAR_CLI.barLabel("Processing Financial Institutions:");
        
        int totalInstitutions = CMOfxDirectModel.getFIMODELS().size();
        int processedInstitutions = 0;
        int skippedInstitutions = 0;
        
        // loop through financial institutions
        for (CMOfxDLFIModel fi : CMOfxDirectModel.getFIMODELS()) {
            if (fi.getActive().equalsIgnoreCase("Yes")) {
                logger.info("Processing Financial Institution: {} (ID: {})", fi.getFiName(), fi.getFiId());
                PROGRESS_BAR_CLI.barLabel("  Processing Financial Institution: " + fi.getFiName());
                processedInstitutions++;
                
                this.processFinancialInstitution(fi);
            } else {
                logger.debug("Skipping inactive Financial Institution: {}", fi.getFiName());
                PROGRESS_BAR_CLI.barLabel("  Skipping Financial Institution: " + fi.getFiName());
                skippedInstitutions++;
                continue;
            }
        }
        
        logger.info("Direct OFX processing completed. Processed: {}, Skipped: {}, Total: {}", 
                   processedInstitutions, skippedInstitutions, totalInstitutions);
    }
    
    private void processFinancialInstitution(CMOfxDLFIModel fi) {
        logger.debug("Processing accounts for FI: {}", fi.getFiName());
        PROGRESS_BAR_CLI.barLabel("    Processing Accounts:");
        
        int totalAccounts = fi.getAccountModels().size();
        int processedAccounts = 0;
        int skippedAccounts = 0;

        // loop through accounts
        for (CMOfxDLAccountModel acct : fi.getAccountModels()) {
            if (acct.getAcctActive().equalsIgnoreCase("Yes")) {
                logger.info("Processing Account: {} (Number: {}) for FI: {}", 
                           acct.getAcctName(), acct.getAcctNumber(), fi.getFiName());
                PROGRESS_BAR_CLI.barLabel("      Processing Account: " + acct.getAcctName());
                
                try {
                    // get response from the server into doc
                    this.getInvStmtResponse(fi, acct);
                    processedAccounts++;
                    logger.debug("Successfully processed account: {}", acct.getAcctName());
                } catch (Exception e) {
                    logger.error("Failed to process account: {} for FI: {}", acct.getAcctName(), fi.getFiName(), e);
                    // Continue processing other accounts even if one fails
                }
            } else {
                logger.debug("Skipping inactive account: {} for FI: {}", acct.getAcctName(), fi.getFiName());
                PROGRESS_BAR_CLI.barLabel("      Skipping Account: " + acct.getAcctName());
                skippedAccounts++;
            }
        }
        
        logger.info("Completed processing FI: {}. Accounts processed: {}, skipped: {}, total: {}", 
                   fi.getFiName(), processedAccounts, skippedAccounts, totalAccounts);
    }

    public void getInvStmtResponse(CMOfxDLFIModel fi, CMOfxDLAccountModel acct) {
        LocalDate startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (CmdLineController.sDate.isEmpty()) {
            logger.debug("No command line date specified, retrieving max date from database for account: {}", acct.getAcctName());
            startDate = getMaxAcctDate(acct);
        } else {
            logger.debug("Using command line specified date: {}", CmdLineController.sDate);
            startDate = LocalDate.parse(CmdLineController.sDate, formatter);
        }

        logger.info("Processing account: {} with start date: {}", acct.getAcctName(), startDate.toString());
        System.out.println("        Start Date: " + startDate.toString());

        // check database for latest date to construct request
        this.getInvStmtData(fi, acct, startDate);
    }

    private LocalDate getMaxAcctDate(CMOfxDLAccountModel acct) {
        String sql;
        ResultSet rs;
        LocalDate startDate;
        Integer userId = CMDBModel.getUserId();

        logger.debug("Retrieving max account date for account: {} and user: {}", acct.getAcctNumber(), userId);

        // FIXED: Use parameterized query to prevent SQL injection
        sql = "SELECT MAX(DtTrade) as MaxDate FROM hlhtxc5_dbOfx.InvTran, Accounts " +
              "WHERE Accounts.InvAcctIdFi = ? AND JoomlaId = ? LIMIT 1";

        try (Connection con = CMDBController.getConnection();
             PreparedStatement pStmt = con.prepareStatement(sql)) {
            
            pStmt.setString(1, acct.getAcctNumber());
            pStmt.setInt(2, userId);
            pStmt.clearWarnings();
            rs = pStmt.executeQuery();

            String maxDateStr = "";
            while (rs.next()) {
                // yyyymmdd
                if (rs.getString("MaxDate") != null) {
                    maxDateStr = rs.getString("MaxDate").substring(0, 8);
                    logger.debug("Found max date: {} for account: {}", maxDateStr, acct.getAcctNumber());
                }
            }
            
            if (!maxDateStr.isEmpty()) {
                startDate = LocalDate.of(
                    Integer.parseInt(maxDateStr.substring(0, 4)),
                    Integer.parseInt(maxDateStr.substring(4, 6)),
                    Integer.parseInt(maxDateStr.substring(6, 8)));
                logger.debug("Parsed start date: {} for account: {}", startDate, acct.getAcctNumber());
            } else {
                startDate = LocalDate.of(1970, Month.JANUARY, 1);
                logger.info("No previous data found for account: {}, using default start date: {}", 
                           acct.getAcctNumber(), startDate);
            }
            
        } catch (SQLException ex) {
            logger.error("Database error while retrieving max account date for account: {}", 
                        acct.getAcctNumber(), ex);
            
            String errorMsg = String.format(
                CMLanguageController.getErrorProp("Formatted14"),
                ex.getMessage());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                errorMsg,
                JOptionPane.ERROR_MESSAGE);

            throw new CMDAOException(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

        return startDate;
    }

    private StringBuilder buildRequest(CMOfxDLFIModel fi, CMOfxDLAccountModel acct, LocalDate startDate) {
        logger.debug("Building OFX request for FI: {}, Account: {}, Start Date: {}", 
                    fi.getFiName(), acct.getAcctName(), startDate);

        StringBuilder sbRequest = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        // build the request
        sbRequest.append(OFX_REQUEST_HEADER);
        sbRequest.append("<OFX>");
        sbRequest.append(System.lineSeparator());

        // SIGNON section
        String signonSection = String.format(OFX_SIGNON,
            CMHPIUtils.getLongDate(), // date of request
            acct.getAcctUId(), // user id
            acct.getAcctPW(), // user password
            fi.getFiOrg(), // financial institution organization
            fi.getFiId() // financial institution ID
        );
        sbRequest.append(signonSection);

        // INVSTMT REQUEST section
        String invStmtSection = String.format(OFX_INVSTMT_REQUEST,
            CMHPIUtils.getLongDate(), //   transaction number
            fi.getBrokerId(), //           broker id
            acct.getAcctNumber(), //       account number
            simpleDateFormat.format(Date.valueOf(startDate.toString())),    //dtStart
            "Y", //                        include transactions
            "N", //                        include open orders
            "Y", //                        include positions
            "Y" //                         include balances
        );
        sbRequest.append(invStmtSection);

        sbRequest.append("</OFX>");
        sbRequest.append(System.lineSeparator());

        logger.debug("OFX request built successfully. Request length: {} characters", sbRequest.length());
        return sbRequest;
    }

    private void getInvStmtData(CMOfxDLFIModel fi, CMOfxDLAccountModel acct, LocalDate startDate) {
        logger.info("Getting investment statement data for FI: {}, Account: {}", fi.getFiName(), acct.getAcctName());
        
        StringBuilder sbRequest = buildRequest(fi, acct, startDate);
        StringBuilder sbResponse = getResponse(fi, sbRequest);
        
        if (sbResponse.length() > 0) {
            logger.debug("Received response of {} characters from FI: {}", sbResponse.length(), fi.getFiName());
            this.handleFiResponse(acct, sbResponse);
        } else {
            logger.warn("Received empty response from FI: {} for account: {}", fi.getFiName(), acct.getAcctName());
        }
    }

    private StringBuilder getResponse(CMOfxDLFIModel fi, StringBuilder sbRequest) {
        logger.debug("Sending HTTP request to FI: {} at URL: {}", fi.getFiName(), fi.getFiUrl());
        
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
        StringBuilder sbResponse = new StringBuilder();

        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(fi.getFiUrl()))
                .version(HttpClient.Version.HTTP_2)
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .POST(BodyPublishers.ofString(sbRequest.toString()))
                .build();
                
            logger.debug("HTTP request created, sending to: {}", fi.getFiUrl());
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            // either gets data or error response
            // does not get here if url is wrong; exception instead
            sbResponse.append(response.body());
            
            logger.info("HTTP response received from FI: {}. Status Code: {}, Response Length: {} characters", 
                       fi.getFiName(), response.statusCode(), sbResponse.length());
            
            //check response for error code
            if (response.statusCode() != 200) {
                String errorLine = "HTTPS error code: " + response.statusCode() +
                    "\n" + "HTTPS Message: " + sbResponse.toString() + "\n";

                logger.error("HTTP error from FI: {}. Status Code: {}, Error: {}", 
                           fi.getFiName(), response.statusCode(), errorLine);

                String errorMsg = String.format(CMLanguageController.getErrorProp("HttpError"),
                    Integer.toString(response.statusCode()),
                    errorLine + System.lineSeparator());

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    errorMsg,
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Exception while sending HTTP request to FI: {} at URL: {}", 
                        fi.getFiName(), fi.getFiUrl(), e);
            sbResponse.append(e.getMessage());
        }

        return sbResponse;
    }

    private void handleFiResponse(CMOfxDLAccountModel acct, StringBuilder sbResponse) {
        logger.info("Handling response from financial institution for account: {}", acct.getAcctName());
        
        FixSGML2XML fixSGML;
        String errorMsg;
        OfxFileController ofxFileController;

        // Write raw return to file
        logger.debug("Writing raw OFX response to file for account: {}", acct.getAcctName());
        writeRaw(sbResponse.toString(), acct.getAcctName());

        try {
            fixSGML = FixSGML2XML.getInstance();
            doc = fixSGML.doSGMLDoc2XML(Jsoup.parse(sbResponse.toString()));
            
            if (doc == null || (doc.select("ofx").first()) == null) {
                errorMsg = String.format(CMLanguageController.getErrorProp("OfxResponseEmpty"));
                logger.error("OFX response is empty or invalid for account: {}", acct.getAcctName());

                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    errorMsg,
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            logger.debug("Successfully parsed OFX response for account: {}", acct.getAcctName());

            // Write adjusted return to file
            logger.debug("Writing processed XML response to file for account: {}", acct.getAcctName());
            writeAdjusted(acct.getAcctName());

            ofxFileController = OfxFileController.getInstance();

            if (this.doc != null) {
                logger.info("Processing OFX document to SQL for account: {}", acct.getAcctName());
                ofxFileController.processOfxDoc2SQLSetup(this.doc, PROGRESS_BAR_CLI);
                logger.debug("Successfully processed OFX document to SQL for account: {}", acct.getAcctName());
            }
        } catch (Exception e) {
            logger.error("Error processing OFX response for account: {}", acct.getAcctName(), e);
            throw new RuntimeException("Failed to process OFX response for account: " + acct.getAcctName(), e);
        }
    }

    private void writeRaw(String aString, String sAcct) {
        logger.debug("Writing raw OFX file for account: {}", sAcct);
        
        File fileWrite = new File(CMDirectoriesModel.getInstance().getModelProp("Reports") + 
                                 File.separator + sAcct + ".OFX");
        
        try (BufferedWriter writerRaw = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(fileWrite), StandardCharsets.UTF_8))) {
            
            writerRaw.write(aString);
            writerRaw.flush();
            logger.debug("Successfully wrote raw OFX file: {}", fileWrite.getAbsolutePath());
            
        } catch (IOException e) {
            logger.error("Failed to write raw OFX file for account: {} to path: {}", 
                        sAcct, fileWrite.getAbsolutePath(), e);
            
            String errorMsg = String.format(CMLanguageController.getErrorProp("GeneralError"), e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                errorMsg,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(errorMsg);
        }
    }

    private void writeAdjusted(String sAcct) {
        logger.debug("Writing adjusted XML file for account: {}", sAcct);
        
        if (doc == null) {
            logger.warn("Document is null, skipping adjusted file write for account: {}", sAcct);
            return;
        }
        
        File fileWrite = new File(CMDirectoriesModel.getInstance().getModelProp("Reports") + 
                                 File.separator + sAcct + ".txt");

        try (BufferedWriter writerRaw = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(fileWrite), StandardCharsets.UTF_8))) {
            
            writerRaw.write(doc.body().toString());
            writerRaw.flush();
            logger.debug("Successfully wrote adjusted XML file: {}", fileWrite.getAbsolutePath());
            
        } catch (IOException e) {
            logger.error("Failed to write adjusted XML file for account: {} to path: {}", 
                        sAcct, fileWrite.getAbsolutePath(), e);
            
            String errorMsg = String.format(CMLanguageController.getErrorProp("GeneralError"), e.toString());

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProp("Title") + CMLanguageController.getErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                errorMsg,
                JOptionPane.ERROR_MESSAGE);

            throw new UnsupportedOperationException(errorMsg);
        }
    }

    // OFX Request Templates
    private static final String OFX_REQUEST_HEADER = 
        "OFXHEADER:100\n" + 
        "DATA:OFXSGML\n" + 
        "VERSION:102\n" +
        "SECURITY:NONE\n" + 
        "ENCODING:USASCII\n" + 
        "CHARSET:1252\n" + 
        "COMPRESSION:NONE\n" + 
        "OLDFILEUID:NONE\n" +
        "NEWFILEUID:NONE\n\n";
        
    private static final String OFX_SIGNON = 
        "<SIGNONMSGSRQV1>\n" + 
        "<SONRQ>\n" + 
        "<DTCLIENT>%s\n" + 
        "<USERID>%s\n" +
        "<USERPASS>%s\n" + 
        "<LANGUAGE>ENG\n" + 
        "<FI>\n" + 
        "<ORG>%s\n" + 
        "<FID>%s\n" + 
        "</FI>\n" + 
        "<APPID>Money\n" +
        "<APPVER>1600\n" + 
        "</SONRQ>\n" + 
        "</SIGNONMSGSRQV1>\n\n";
        
    private static final String OFX_INVSTMT_REQUEST = 
        "<INVSTMTMSGSRQV1>\n" + 
        "<INVSTMTTRNRQ>\n" + 
        "<TRNUID>%s-01\n" +
        "<INVSTMTRQ>\n" + 
        "<INVACCTFROM>\n" + 
        "<BROKERID>%s\n" + 
        "<ACCTID>%s\n" + 
        "</INVACCTFROM>\n" + 
        "<INCTRAN>\n" +
        "<DTSTART>%s\n" +
        "<INCLUDE>%s\n" + 
        "</INCTRAN>\n" + 
        "<INCOO>%s\n" + 
        "<INCPOS>\n" + 
        "<INCLUDE>%s\n" +
        "</INCPOS>\n" + 
        "<INCBAL>%s\n" + 
        "</INVSTMTRQ>\n" + 
        "</INVSTMTTRNRQ>\n" + 
        "</INVSTMTMSGSRQV1>\n";
}