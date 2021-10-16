package com.hpi.appcontrollers;

import com.hpi.TPCCMcontrollers.OptionController;
import com.hpi.TPCCMcontrollers.StockController;
import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.TPCCMsql.*;
import com.hpi.entities.*;
import java.sql.*;
import javax.swing.JOptionPane;
import lombok.*;

public class DataMartController //      extends DBCore
{

    @Setter private Integer userId;
    @Getter private Integer sectorId;

    /*
     * Singleton
     *
     */
    private static DataMartController instance;

    protected DataMartController()
    {
        // protected prevents instantiation outside of package
        this.userId = CMDBModel.getUserId();
    }

    public synchronized static DataMartController getInstance()
    {
        if (DataMartController.instance == null) {
            DataMartController.instance = new DataMartController();
        }
        return DataMartController.instance;
    }
    //***

    public final void cleanDataMart()
    {
        String sql;

        String tables[] = { //todo: these need to be temp tables
            //            "AccountTotals",
            //            "Balances",
            //            "ClosedOptionFIFO",
            //            "ClosedOptionTrans",
            //                    "ClosedStockFIFO",
            //            "ClosedStockTrans",
            //            "ClosingDebt",
            //            "ClosingMF",
            //            "ClosingOptions",
            //            "ClosingOther",
            //            "ClosingStockModel",
            //            "FIFOClosed",
            "FIFOOpenTransactions",
            //            "OpeningDebt",
            //            "OpeningMF",
            //            "OpeningOptions",
            //            "OpeningOther",
            //            "OpeningStockModel",
            "OpenOptionFIFO",
            "OpenStockFIFO",
            "PositionsOpen",
            "PositionsOpenTransactions"
        };

        for (String s : tables) {
            sql = "delete from hlhtxc5_dmOfx.%s where JoomlaId = '%s';";

            CMDBController.executeSQL(String.format(sql,
                s,
                this.userId));
        }
    }

    /**
     * Take data from dbOfx to create the DataMart. DataMart must be cleared
     * before this.
     */
    public void processOfxDBtoDataMart()
    {
        //ok
        this.doUIdFoundation();

        this.doCashCEACSLSync();

        this.doBrokerIdSync();

        this.doAccountSync();

        this.doClientAccounts();

//        this.doClientTransfer();
//
//        this.doDmOfxOpeningDebt();
//
//        this.doOpeningMF();
//
//        this.doClosingMF();
        this.doDbx2DmxStock();

        this.doClientStock();

        this.doDbx2DmxOptions();

        //todo
//        this.doClientOpeningOptions();
//        this.doClientClosingOptions();
        this.doOptionsUpdatePrices();
    }

    public void processDataMart()
    {
//        //raw ofx to dataMart
//        this.processOfxDBtoDataMart();
//
//        this.processFIFOStockLotsAccounts();
//
//        this.processFIFOOptionLotsAccounts();

        this.doGainPctUpdates();

        this.doOpenFIFO();

        /**
         * At this point have populated fifoOpenTransactions stock, options but not cash
         */
        //ensure sectorList has CASH component to update
        this.doCashToSectorList();

        //ensure cea has CASH component to update
        this.insertCash2CEA();

        this.insertBalances();

        this.insertCash2FIFOTransactions();
        /**
         * CASH inserted to fifoTransactions
         */
        this.addSectorId2FIFOTransactions();
        /**
         * fifoOpenTransactions clientSectorId updated from cea
         */

        this.doMktVals();

        //this.doMktValTotals();
        this.doActiveValidation();

        this.doHPILMktVal();

        this.doClientEquityAttributesActPctUpdate();

//        this.doClientSectorListMktLmktUpdate();
        this.updateAnalystTgts();

        this.doClosedFIFO();

        this.doYearEndPrices();

        this.doAccountTotals();

    }

    public synchronized void processFIFOStockLotsAccounts()
    {
        StockController stockController;

        stockController = StockController.getInstance();
        System.out.println("  Commencing DataMart updates");
        System.out.println("    Commencing Stock Lots ...");
        stockController.processFIFOStockLotsAccounts();
        System.out.println("    Stock Lots complete");
    }

    public synchronized void processFIFOOptionLotsAccounts()
    {
        OptionController optionController;

        optionController = OptionController.getInstance();
        System.out.println("    Commencing Option Lots ...");
        optionController.processFIFOOptionLotsAccounts();
        System.out.println("    Option Lots complete");
    }

    public final void doDbx2DmxStock()
    {
        //sell short
        CMDBController.executeSQL(String.format(OpeningStockModel.DBOFX_DMOFX_SELL,
            this.userId));
        //buy
        CMDBController.executeSQL(String.format(OpeningStockModel.DBOFX_DMOFX_BUY,
            this.userId));

        //sell to close
        CMDBController.executeSQL(String.format(ClosingStockModel.DBOFX_DMOFX_SELL,
            this.userId));
        //buy to cover
        CMDBController.executeSQL(String.format(ClosingStockModel.DBOFX_DMOFX_BUY,
            this.userId));

        //set InvTran.Complete for openingStock table
        CMDBController.executeSQL(String.format(OpeningStockModel.DBOFX_UPDATE,
            this.userId));

        //set InvTran.Complete for closingStock table
        CMDBController.executeSQL(String.format(ClosingStockModel.DBOFX_UPDATE,
            this.userId));
    }

    public void doClientStock()
    {
        //clientOpen
        CMDBController.executeSQL(String.format(ClientOpeningStockModel.DMOFX,
            OpeningStockModel.ALL_FIELDS,
            ClientOpeningStockModel.ALL_FIELDS,
            this.userId));

        //clientClose
        CMDBController.executeSQL(String.format(ClientClosingStockModel.DMOFX,
            ClosingStockModel.ALL_FIELDS,
            ClientClosingStockModel.ALL_FIELDS,
            this.userId));

        //update Units to 0.0; we retain OrigUnits in case we have to regenerate things
        CMDBController.executeSQL(String.format(ClientOpeningStockModel.DMOFX_UNITS_UPDATE,
            this.userId));

        //update Units to 0.0; we retain OrigUnits in case we have to regenerate things
        CMDBController.executeSQL(String.format(ClientClosingStockModel.DMOFX_UNITS_UPDATE,
            this.userId));
    }

    public void doDbx2DmxOptions()
    {
        //buy to open
        CMDBController.executeSQL(String.format(OpeningOptionModel.DBOFX_DMOFX_BUY,
            OpeningOptionModel.ALL_FIELDS,
            this.userId));

        //sell to open
        CMDBController.executeSQL(String.format(OpeningOptionModel.DBOFX_DMOFX_SELL,
            OpeningOptionModel.ALL_FIELDS,
            this.userId));

        //buy to close
        CMDBController.executeSQL(String.format(ClosingOptionModel.DBOFX_DMOFX_BUY,
            ClosingOptionModel.ALL_FIELDS,
            this.userId));

        //sell to close
        CMDBController.executeSQL(String
            .format(ClosingOptionModel.DBOFX_CLOSUREOPT_DMOFX,
                ClosingOptionModel.ALL_FIELDS,
                this.userId));

        //closureOpt transactions
        CMDBController.executeSQL(String.format(ClosingOptionModel.DBOFX_DMOFX_SELL,
            ClosingOptionModel.ALL_FIELDS,
            this.userId));

        //set InvTran.Complete for openingOptions table
        CMDBController.executeSQL(String.format(OpeningOptionModel.DBOFX_UPDATE,
            this.userId));

        //set InvTran.Complete for closingOptions table
        CMDBController.executeSQL(String.format(ClosingOptionModel.DBOFX_UPDATE,
            this.userId));
    }

    private void doUIdFoundation()
    {
        //ok
        doStandardSectors();

        doUserTickers();

        doTickerSectorId();
    }

    /*
     * ensure all standard sectors are in CSL
     */
    private void doStandardSectors()
    {
//        String sql;
//        ResultSet rs;

        // avoid auto_increment growing with duplicates
        // get user ClientSectors; loop and test against equityInfo
//        sql = "select distinct Sector from hlhtxc5_dmOfx.EquityInfo;";
        String[] keys = {
            "JoomlaId",
            "ClientSectorId",
            "ClientSector",
            "CSecShort"
        };

        try (Connection con3 = CMDBController.getConnection();
            PreparedStatement pStmt3 = con3.prepareStatement(EquityInfoModel.SELECT_DISTINCT_SECTORS);
            ResultSet rs = pStmt3.executeQuery();) {
            while (rs.next()) {
                String[] cslValues = {
                    Integer.toString(userId),
                    null,
                    rs.getString("Sector"),
                    rs.getString("Sector")
                    .substring(0,
                    rs.getString("Sector")
                    .length() > 9 ? 10 : rs.getString("Sector")
                    .length())
                };

                CMDBController.doSQLAuto("hlhtxc5_dmOfx.ClientSectorList",
                    keys,
                    cslValues,
                    String.format(
                        ClientSectorModel.SELECT_SECTOR,
                        this.userId,
                        rs.getString("Sector")));
//                        "select ClientSectorId, ClientSector from hlhtxc5_dmOfx.ClientSectorList where JoomlaId = '%s' and ClientSector = '%s';",
//                        this.userId, rs.getString("Sector")));
            }
        } catch (SQLException ex) {
            // failed on duplicate, ok
            if (ex.getErrorCode() != 1062) {
                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread()
                        .getStackTrace()[1].getClassName(),
                    Thread.currentThread()
                        .getStackTrace()[1].getMethodName(),
                    ex.getMessage(),
                    JOptionPane.INFORMATION_MESSAGE);

                throw new CMDAOException(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread()
                        .getStackTrace()[1].getClassName(),
                    Thread.currentThread()
                        .getStackTrace()[1].getMethodName(),
                    EquityInfoModel.SELECT_DISTINCT_SECTORS + ";\n"
                    + ex.getMessage(),
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /*
     * ensure all user tickers are in CEA
     */
    private void doUserTickers()
    {
        String sql, sql1;
        ResultSet rs;

        sql
            = "select distinct Ticker from (select SecId from hlhtxc5_dbOfx.InvBuy as InvBuy, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.Accounts as Accounts where InvBuy.AcctId = Accounts.AcctId and InvTran.AcctId = Accounts.AcctId and InvTran.FiTId = InvBuy.FiTId and InvTran.Skip = 0 and JoomlaId = '%s') as A, hlhtxc5_dbOfx.SecInfo where SecInfo.SecId = A.SecId order by Ticker";
        sql = String.format(sql,
            userId);

        sql1
            = "insert ignore into hlhtxc5_dmOfx.ClientEquityAttributes (JoomlaId, Ticker, TickerIEX) values ";

        try (Connection con1 = CMDBController.getConnection();
            //            Connection con2 = CMDBController.getConnection();
            PreparedStatement pStmt = con1.prepareStatement(sql);
            Statement stmt = con1.createStatement();) {
            if (pStmt.execute()) {
                rs = pStmt.getResultSet();

                if (rs.last()) {
                    rs.beforeFirst();

                    while (rs.next()) {
                        sql1 += "('" + this.userId + "', '"
                            + rs.getString("Ticker") + "', '"
                            + rs.getString("Ticker") + "'), ";
                    }

                    sql1 = sql1.substring(0,
                        sql1.length() - 2) + ";";

                    stmt.execute(sql1);
                }
            }
        } catch (SQLException ex) {
            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread()
                    .getStackTrace()[1].getClassName(),
                Thread.currentThread()
                    .getStackTrace()[1].getMethodName(),
                ex.getMessage(),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * ensure all CEA tickers have legitimate ClientSectorId
     */
    private void doTickerSectorId()
    {
        CMDBController.executeSQL(String.format(ClientEquityAttributesModel.UPDATE_SECTORID,
            this.userId));
    }

    /*
     * correct CASH sectorId in CEA
     */
    private void doCashCEACSLSync()
    {
        CMDBController.executeSQL(String.format(ClientEquityAttributesModel.UPDATE_CEA_CASH_SECTORID,
            this.userId));
    }

    /*
     * sync brokers from dbOfx to dmOfx
     */
    private void doBrokerIdSync()
    {
//        String sql;
//        sql
//            = "insert ignore into hlhtxc5_dmOfx.Brokers (BrokerId, Org, FId, BrokerIdFi) SELECT BrokerId, Org, FId, BrokerIdFi FROM hlhtxc5_dbOfx.Brokers;";
        CMDBController.executeSQL(BrokersModel.SYNC_BROKER_DB_TO_DM);
    }

    /*
     * sync accounts from dbOfx to dmOfx
     */
    private void doAccountSync()
    {
        String[] accountsKeys = {
            "JoomlaId",
            "BrokerId",
            "AcctId",
            "Org",
            "FId",
            "BrokerIdFi",
            "InvAcctIdFi"
        };

        try (Connection con3 = CMDBController.getConnection();
            PreparedStatement pStmt3 = con3.prepareStatement(String.format(AccountsModel.SYNC_ACCTS_DB_TO_DM,
                this.userId));
            ResultSet rs = pStmt3.executeQuery();) {
            while (rs.next()) {
                // loop through dbOfx accounts
                String[] accountsValues = {
                    Integer.toString(rs.getInt("JoomlaId")),
                    Integer.toString(rs.getInt("BrokerId")),
                    Integer.toString(rs.getInt("AcctId")),
                    rs.getString("Org"),
                    rs.getString("Fid"),
                    rs.getString("BrokerIdFi"),
                    rs.getString("InvAcctIdFi")
                };

                CMDBController.doSQLAuto("hlhtxc5_dmOfx.Accounts",
                    accountsKeys,
                    accountsValues,
                    "select AcctId from hlhtxc5_dmOfx.Accounts where AcctId = '"
                    + rs.getInt(3) + "';");
            }
        } catch (SQLException ex) {
            // failed on duplicate, ok
            if (ex.getErrorCode() != 1062) {
                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread()
                        .getStackTrace()[1].getClassName(),
                    Thread.currentThread()
                        .getStackTrace()[1].getMethodName(),
                    ex.getMessage(),
                    JOptionPane.INFORMATION_MESSAGE);

                throw new CMDAOException(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread()
                        .getStackTrace()[1].getClassName(),
                    Thread.currentThread()
                        .getStackTrace()[1].getMethodName(),
                    String.format(AccountsModel.SYNC_ACCTS_DB_TO_DM,
                        this.userId) + ";\n"
                    + ex.getMessage(),
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /*
     * add client accounts
     */
    private void doClientAccounts()
    {
        String sql;

        sql
            = "insert ignore into hlhtxc5_dmOfx.ClientAccts (JoomlaId, DMAcctId, ClientAcctName) select JoomlaId, Accounts.DMAcctId, concat_ws(\"_\", Accounts.BrokerIdFi, Accounts.InvAcctIdFi) as ClientAcctName from hlhtxc5_dmOfx.Accounts where hlhtxc5_dmOfx.Accounts.JoomlaId = '%s';";
        CMDBController.executeSQL(String.format(sql,
            this.userId));
    }

    /*
     * update underlying prices in openingOptions
     */
    private void doOptionsUpdatePrices()
    {
        //update opening
        CMDBController.executeSQL(String.format(OpeningOptionModel.DMOFX_UPDATE_PRICES,
            this.userId));
        //update closing
        CMDBController.executeSQL(String.format(ClosingOptionModel.DMOFX_UPDATE_PRICES,
            this.userId));
    }

    /**
     * ensure CEA has a CASH element
     */
    public void insertCash2CEA()
    {
        CMDBController.executeSQL(String.format(ClientEquityAttributesModel.INSERT_CASH_2_CEA,
            this.userId,
            this.sectorId));
    }

    /**
     * put dbOfx balances into dmOfx
     * todo: eTrade sucks on the TIC account for this; IRA is fine
     * for eTrade, the TIC value is negative and wrong
     */
    public void insertBalances()
    {
        CMDBController.executeSQL(String.format(Balances.INSERT_BAL_DB_2_DM,
            this.userId));
    }

    /**
     * put CASH into fifoTransaction
     */
    public void insertCash2FIFOTransactions()
    {
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.INSERT_CASH_BALANCE,
            this.sectorId,
            this.userId));
    }

    /**
     * use cea.ClientSectorId, exclude CASH as already handled
     */
    public void addSectorId2FIFOTransactions()
    {
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_CLIENTSECTORID,
            this.userId));
    }

    public void updateAnalystTgts()
    {
//        String sql;
//
//        sql
//            = "update hlhtxc5_dmOfx.ClientEquityAttributes as C inner join (select Ticker, MAX(`Date`) AS `Date` FROM hlhtxc5_dmOfx.EquityInfo GROUP BY Ticker ORDER BY Ticker ASC) as A on C.Ticker = A.Ticker inner join hlhtxc5_dmOfx.EquityInfo on EquityInfo.`Date` = A.`Date` and EquityInfo.Ticker = A.Ticker set C.AnalystTgt = EquityInfo.TgtPrice where A.`Date` = EquityInfo.`Date` and C.Ticker = A.Ticker and JoomlaId = '%s';";
//        sql = String.format(sql, this.userId);
        CMDBController.executeSQL(String.format(ClientEquityAttributesModel.UPDATE_ANALYST_TGTS,
            this.userId));
    }

    /**
     * put openOptionFIFO and openStockFIFO transactions into fifoOpenTransactions
     * todo: same for MF, etc.
     * todo: why not cash here?
     */
    public void doOpenFIFO()
    {
        //option
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.OPEN_OPTION_FIFO_2_FIFO_OPEN_TRANSACTIONS,
            this.userId));

        //stock
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.OPEN_STOCK_FIFO_2_FIFO_OPEN_TRANSACTIONS,
            this.userId));

        //cash
        //todo:
    }

    /**
     * CASH is an equity and a sector to provide the right views. So, ensure sectorList has CASH
     */
    public void doCashToSectorList()
    {
//        String checkSQL;

        // ensure a CASH entry in ClientSectorList
        String[] keys = {
            "ClientSectorId",
            "JoomlaId",
            "ClientSector",
            "CSecShort",
            "Active",
            "TgtPct",
            "Comment",
            "TgtLocked",
            "ActPct",
            "MktVal",
            "LMktVal",
            "CustomSector"
        };
        String[] values = {
            null,
            userId.toString(),
            "CASH",
            "CASH",
            "Yes",
            "0",
            "",
            "No",
            "0",
            "0",
            "0",
            "0"
        };
//        checkSQL
//            = "select ClientSectorId from hlhtxc5_dmOfx.ClientSectorList where ClientSector = 'CASH' and JoomlaId = '%s';";
//        checkSQL = String.format(checkSQL, userId);
        this.sectorId = CMDBController.doSQLAuto("ClientSectorList",
            keys,
            values,
            String.format(ClientSectorModel.CASH_SECTORID,
                this.userId));
    }

    /*
     * put Account Total into AccountTotals table
     */
    public void doAccountTotals()
    {
//        String sql;
//
//        sql
//            = "insert ignore into hlhtxc5_dmOfx.AccountTotals(DMAcctId, JoomlaId, StmtDt, Cost, MktValue) select fp.DMAcctId, fp.JoomlaId, A.DtAsOf, 0, sum(fp.MktVal) as MktValue from hlhtxc5_dmOfx.FIFOOpenTransactions as fp, (select DMAcctId, JoomLaId, max(DtAsOf) as DtAsOf from hlhtxc5_dmOfx.Balances where JoomlaId = '%s' group by DMAcctId, JoomLaId) as A where fp.DMAcctId = A.DMAcctId and fp.JoomLaId = A.JoomlaId and fp.JoomlaId = '%s' group by fp.DMAcctId, fp.JoomlaId;";
        CMDBController.executeSQL(String.format(AccountTotals.INSERT_ACCT_TOTALS,
            this.userId,
            this.userId));
    }

    /**
     * updates fifoOpenTransactions for stocks, options MktVal
     */
    public void doMktVals()
    {
        // add mkt value for stocks/options
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_STOCK_MKTVAL,
            userId));

        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_OPTION_MKTVAL,
            userId));
        System.out.println("\t\tUpdate stock/option MktVal in FIFOOpenTransactions");
    }

    /*
     * put Account Total into AccountTotals table
     */
    public void doMktValTotals()
    {
        //cannot tell where this is used for anything
        String sql1, sql2, sql3;

        Double dCashTotal;
        Double dStockTotal;
        Double dOptionTotal;
        Double dPortfolioTotal;
//        Double dPortfolioLTotal;

        dCashTotal = dStockTotal = dOptionTotal = 0.0;
//            cashSectorId = 0;
//        dLMktRatio = 0.0;
        // dCashTotal = 0.0;
        // dStockTotal = 0.0;
        // dOptionTotal = 0.0;
        // dPortfolioTotal = 0.0;
//        dPortfolioLTotal = 0.0;
        // FIFOTransactions totals
//        sql
//            = "select sum(MktVal) from hlhtxc5_dmOfx.FIFOOpenTransactions where FIFOOpenTransactions.EquityId = 'CASH' and FIFOOpenTransactions.JoomlaId = '%s';";
        sql1 = String.format(FIFOOpenTransactionModel.MKTVAL_TOTAL_CASH,
            userId);
//        sql2
//            = "select sum(MktVal) from hlhtxc5_dmOfx.FIFOOpenTransactions where EquityType = 'STOCK' and FIFOOpenTransactions.JoomlaId = '%s';";
        sql2 = String.format(FIFOOpenTransactionModel.MKTVAL_TOTAL_STOCK,
            userId);
//        sql3
//            = "select sum(MktVal) from hlhtxc5_dmOfx.FIFOOpenTransactions where EquityType = 'OPTION' and FIFOOpenTransactions.JoomlaId = '%s';";
        sql3 = String.format(FIFOOpenTransactionModel.MKTVAL_TOTAL_OPTION,
            userId);

        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt1 = con.prepareStatement(sql1);
            PreparedStatement pStmt2 = con.prepareStatement(sql2);
            PreparedStatement pStmt3 = con.prepareStatement(sql3);
            ResultSet rs1 = pStmt1.executeQuery();
            ResultSet rs2 = pStmt2.executeQuery();
            ResultSet rs3 = pStmt3.executeQuery();) {
            while (rs1.next()) {
                dCashTotal = rs1.getDouble(1);
            }

            while (rs2.next()) {
                dStockTotal = rs2.getDouble(1);
            }

            while (rs3.next()) {
                dOptionTotal = rs3.getDouble(1);
            }

            dPortfolioTotal = dCashTotal + dStockTotal + dOptionTotal;
        } catch (SQLException ex) {
            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread()
                    .getStackTrace()[1].getClassName(),
                Thread.currentThread()
                    .getStackTrace()[1].
                    getMethodName(),
                ex.getMessage(),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void doActiveValidation()
    {
//        String sql;
//        // ensure equities with either target or actual percent are active
//        sql
//            = "update hlhtxc5_dmOfx.ClientEquityAttributes set Active = if(isnull(ActPct), if(isnull(TgtPct), Active, if(TgtPct > 0, 'Yes', Active)), if(ActPct > 0, 'Yes', Active)) where JoomlaId = '%s';";
//        sql = String.format(sql, userId);
        CMDBController.executeSQL(String.format(ClientEquityAttributesModel.FORCE_UPDATE_ACTIVE,
            this.userId));
//        System.out.println("\t\tUpdate Active in CEA");

        // ensure sectors with either target or actual percent are active
//        sql
//            = "update hlhtxc5_dmOfx.ClientSectorList set Active = if(isnull(ActPct), if(isnull(TgtPct), Active, if(TgtPct > 0, 'Yes', Active)), if(ActPct > 0, 'Yes', Active)) where JoomlaId = '%s';";
//        sql = String.format(sql, userId);
        CMDBController.executeSQL(String.format(ClientSectorModel.FORCE_UPDATE_ACTIVE,
            this.userId));
//        System.out.println("\t\tUpdate Active in CSL");
    }

    public void doClientSectorListMktLmktUpdate()
    {
//        String sql;
//
//        // update mkt and lmkt values in the sector list
//        sql
//            = "update hlhtxc5_dmOfx.ClientSectorList as CSL left join (select FIFOOpenTransactions.ClientSectorId, sum(FIFOOpenTransactions.MktVal) as sumMktVal, sum(FIFOOpenTransactions.LMktVal) as sumLMktVal from hlhtxc5_dmOfx.ClientSectorList as CSL, hlhtxc5_dmOfx.FIFOOpenTransactions where CSL.JoomlaId = '%s' and CSL.JoomlaId = FIFOOpenTransactions.JoomlaId and CSL.ClientSectorId = FIFOOpenTransactions.ClientSectorId group by FIFOOpenTransactions.ClientSectorId) as A on A.ClientSectorId = CSL.ClientSectorId set CSL.MktVal = if(isnull(A.sumMktVal), 0, A.sumMktVal), CSL.LMktVal = if(isnull(A.sumLMktVal), 0, A.sumLMktVal);";
//        sql = String.format(sql, userId);
        CMDBController.executeSQL(String.format(ClientSectorModel.UPDATE_MKTVAL_LMKTVAL,
            this.userId));
//        System.out.println("\t\tUpdate MktVal and LMktVal CSL");
    }

    public void doClientEquityAttributesActPctUpdate()
    {
//        String sql;

        // update ActPct into CEA table
        CMDBController.executeSQL(String.format(ClientEquityAttributesModel.UPDATE_ACT_PCT,
            userId,
            userId));
//        System.out.println("\t\tupdate ActPct in CEA");
    }

    public void doGainPctUpdates()
    {
        CMDBController.executeSQL(String.format(OpenStockFIFOModel.UPDATE_GAIN_PCT,
            userId));
    }

    public void doHPILMktVal()
    {
        ResultSet rs;
        Double dLMktRatio;
        Double dPortfolioLTotal;

        dLMktRatio = dPortfolioLTotal = 0.0;

        /*
         * HPI leveraged option value calculation
         * todo: include some factors for risk, e.g., Delta 80 v. Delta 70
         * and time to expiry, e.g., 6 months or more better than less
         */
        // LMktVal is the value of shares controlled by the option contract
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_OPTION_LMKTVAL,
            userId));

        // Get the leverage ratio on options
        // todo: using abs to handle short positions. When have positions,
        //  consider using the position exposed value instead
        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(
                String.format(FIFOOpenTransactionModel.LEVERAGE_RATIO_OPTIONS,
                    userId))) {
            pStmt.clearWarnings();
            rs = pStmt.executeQuery();

            while (rs.next()) {
                dLMktRatio = rs.getDouble(1);
            }

            // if there are no options, dLMktRatio will be 0; force to 1.0
            if (dLMktRatio == 0.0) {
                dLMktRatio = 1.0;
            }

            rs.close();
            pStmt.close();
            con.close();
        } catch (SQLException ex) {
            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread()
                    .getStackTrace()[1].getClassName(),
                Thread.currentThread()
                    .getStackTrace()[1].
                    getMethodName(),
                ex.getMessage(),
                JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("\t\tGet dLMktRatio: " + dLMktRatio);

        // HPI leveraged stock value calculation
        // not leveraged
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_STOCK_LMKTVAL,
            userId));

        // add leveraged mkt value for cash
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_CASH_LMKTVAL,
            dLMktRatio.toString(),
            userId));
        // Get the portfolio leveraged total
        //  while short positions bring in cash, still represent risk
        //  treat all positions as a positive number for this purpose
        //  we made sure they were positive above ...
        try (Connection con = CMDBController.getConnection();
            //                  Connection con = DriverManager.getConnection(
            //                  CMDBController.getPool());
            PreparedStatement pStmt = con.prepareStatement(
                String.format(FIFOOpenTransactionModel.LEVERAGED_TOTAL,
                    userId))) {
            pStmt.clearWarnings();
            rs = pStmt.executeQuery();

            while (rs.next()) {
                dPortfolioLTotal = rs.getDouble(1);
            }

            rs.close();
            pStmt.close();
            con.close();
        } catch (SQLException ex) {
            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread()
                    .getStackTrace()[1].getClassName(),
                Thread.currentThread()
                    .getStackTrace()[1].
                    getMethodName(),
                ex.getMessage(),
                JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("\t\tGet dPortfolioLTotal: " + dPortfolioLTotal);

        // update actual percent of portfolio based on leveraged mkt val
        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_OPTION_ACTUAL_PCT,
            dPortfolioLTotal.toString(),
            dPortfolioLTotal.toString(),
            userId));

        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_STOCK_ACTUAL_PCT,
            dPortfolioLTotal.toString(),
            dPortfolioLTotal.toString(),
            userId));

        CMDBController.executeSQL(String.format(FIFOOpenTransactionModel.UPDATE_CASH_ACTUAL_PCT,
            dPortfolioLTotal.toString(),
            dPortfolioLTotal.toString(),
            userId));

        //fifoOpenTransactions is complete; update clientSectorList mktVal and LMktVal
        this.doClientSectorListMktLmktUpdate();

        // update actual portfolio percent in the sector list
        CMDBController.executeSQL(String.format(
            String.format(FIFOOpenTransactionModel.UPDATE_ACTUAL_PORTFOLIO_PCT_IN_SECTOR_LIST,
                dPortfolioLTotal.toString(),
                dPortfolioLTotal.toString(),
                userId)));
    }

    public void doClosedFIFO()
    {
        //put close stock into table: ok
        CMDBController.executeSQL(String.format(
            FIFOClosedTransactionModel.INSERT_CLOSED_STOCK_FIFO_2_FIFO_CLOSED_TRANSACTIONS,
            userId));

        // put closed options into table
        CMDBController.executeSQL(String.format(
            FIFOClosedTransactionModel.INSERT_CLOSED_OPTION_FIFO_2_FIFO_CLOSED_TRANSACTIONS,
            this.userId));
    }

    /*
     * Require year end prices for positions held over the end of the year
     */
    public void doYearEndPrices()
    {
        //todo
        //            //calculate gain when held over year end
//            //add year end prices stocks
//            CMDBController.callStored(
//                "hlhtxc5_dmOfx.updateFIFOClosed_psStockOpenPrices('" +
//                dataMartUserId + "')");
        //add year end prices options
//            CMDBController.callStored(
//                "hlhtxc5_dmOfx.updateFIFOClosed_psOptionOpenPrices('" +
//                userId + "')");
    }

//    /*
//     * add client transfers
//     */
//    private void doClientTransfer() {
//        String sql;
//
//        //todo: so far no data here. transfers occur in InvTran
//        sql =
//            "insert ignore into hlhtxc5_dmOfx.ClientTransferData select Accounts.DMAcctId, Transfer.FiTId, '%s', Transfer.SecId, Transfer.SubAcctSec, Transfer.TFerAction, Transfer.Units, IF(Transfer.AvgCostBasis IS NULL, 0, Transfer.AvgCostBasis) as CostBasis, InvTran.DtSettle  as GMTDtSettle, IF(Transfer.DtPurchase IS NULL, '1970-01-01 00:00:00', Transfer.DtPurchase) as GMTDtPurchase FROM hlhtxc5_dbOfx.Transfer as Transfer, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dmOfx.Accounts as Accounts where Transfer.Skip = 0 and InvTran.Skip = 0 and Transfer.FiTId = InvTran.FiTId and Transfer.AcctId = Accounts.AcctId and Accounts.JoomlaId = '%s';";
//        CMDBController.executeSQL(String.format(sql, this.userId, this.userId));
//    }
//    /*
//     * add mutual fund opening transactions
//     */
//    private void doOpeningMF() {
//        String sql;
//
//        sql =
//            "insert ignore into OpeningMF (DMAcctId, FiTId, JoomlaId, SecId, EquityId, Ticker, GMTDtTrade, GMTDtSettle, Units, UnitPrice, MarkUpDn, Commission, Taxes, Fees, TransLoad, Total, CurSym, SubAcctSec, SubAcctFund, ReversalFiTId, BuyType, RelFiTID) select Accounts.DMAcctId, BuyMF.FiTId, Accounts.JoomlaId, InvBuy.SecId, SecInfo.EquityId, SecInfo.Ticker, InvTran.DtTrade, InvTran.DtSettle, InvBuy.Units, InvBuy.UnitPrice, InvBuy.Markup, InvBuy.Commission, InvBuy.Taxes, InvBuy.Fees, InvBuy.TransLoad, InvBuy.Total, InvBuy.CurSym, InvBuy.SubAcctSec, InvBuy.SubAcctFund, InvTran.ReversalFiTId, BuyMF.BuyType, BuyMF.RelFiTId from hlhtxc5_dbOfx.InvBuy as InvBuy, hlhtxc5_dbOfx.BuyMF as BuyMF, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.SecId as SecId, hlhtxc5_dbOfx.SecInfo as SecInfo, hlhtxc5_dmOfx.Accounts as Accounts where InvTran.Skip = 0 and  InvBuy.AcctId = BuyMF.AcctId and InvBuy.FiTId = BuyMF.FiTId and InvTran.AcctId = InvBuy.AcctId and InvTran.FiTId = InvBuy.FiTId and SecId.SecId = InvBuy.SecId and SecInfo.BrokerId = SecId.BrokerId and SecInfo.SecId = SecId.SecId and SecId.BrokerId = Accounts.BrokerId and BuyMF.AcctId = Accounts.AcctId and Accounts.JoomlaId = '%s';";
//        CMDBController.executeSQL(String.format(sql, this.userId));
//    }
//    /*
//     * add mutual fund closing transactions
//     */
//    private void doClosingMF() {
//        String sql;
//        sql =
//            "insert ignore into ClosingMF (DMAcctId, JoomlaId, FiTId, SecId, EquityId, Ticker, GMTDtTrade, GMTDtSettle, Units, UnitPrice, MarkUpDn, Commission, Taxes, Fees, TransLoad, Total, CurSym, SubAcctSec, SubAcctFund, ReversalFiTId, SellType, RelFiTID, AvgCostBasis) select Accounts.DMAcctId, Accounts.JoomlaId, SellMF.FiTId, InvSell.SecId, SecInfo.EquityId, SecInfo.Ticker, InvTran.DtTrade as GMTDtTrade, InvTran.DtSettle as GMTDtSettle, InvSell.Units, InvSell.UnitPrice, InvSell.MarkDown, InvSell.Commission, InvSell.Taxes, InvSell.Fees, InvSell.TransLoad, InvSell.Total, InvSell.CurSym, InvSell.SubAcctSec, InvSell.SubAcctFund, InvTran.ReversalFiTId, SellMF.SellType, SellMF.RelFiTId, SellMF.AvgCostBasis from hlhtxc5_dbOfx.InvSell as InvSell, hlhtxc5_dbOfx.SellMF as SellMF, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.SecId as SecId, hlhtxc5_dbOfx.SecInfo as SecInfo, hlhtxc5_dmOfx.Accounts as Accounts where InvTran.Skip = 0 and InvSell.AcctId = SellMF.AcctId and InvSell.FiTId = SellMF.FiTId and InvTran.AcctId = InvSell.AcctId and InvTran.FiTId = InvSell.FiTId and SecId.SecId = InvSell.SecId and SecInfo.BrokerId = SecId.BrokerId and SecInfo.SecId = SecId.SecId and SecId.BrokerId = Accounts.BrokerId and SellMF.AcctId = Accounts.AcctId and Accounts.JoomlaId = '%s';";
//        CMDBController.executeSQL(String.format(sql, this.userId));
//    }
//    /*
//     * add Other opening transactions
//     */
//    private void doOpeningOtherSellOther() {
//        String sql;
//        sql =
//            "insert ignore into hlhtxc5_dmOfx.OpeningOther (DMAcctId, FiTId, JoomlaId, SecId, EquityId, Ticker, GMTDtTrade, GMTDtSettle, Units, UnitPrice, MarkUpDn, Commission, Taxes, Fees, TransLoad, Total, CurSym, SubAcctSec, SubAcctFund, ReversalFiTId) select Accounts.DMAcctId, BuyOther.FiTId, Accounts.JoomlaId, InvBuy.SecId, SecInfo.EquityId, SecInfo.Ticker, InvTran.DtTrade, InvTran.DtSettle, InvBuy.Units, InvBuy.UnitPrice, InvBuy.Markup, InvBuy.Commission, InvBuy.Taxes, InvBuy.Fees, InvBuy.TransLoad, InvBuy.Total, InvBuy.CurSym, InvBuy.SubAcctSec, InvBuy.SubAcctFund, InvTran.ReversalFiTId from hlhtxc5_dbOfx.InvBuy as InvBuy, hlhtxc5_dbOfx.BuyOther as BuyOther, hlhtxc5_dbOfx.InvTran as InvTran, hlhtxc5_dbOfx.SecId as SecId, hlhtxc5_dbOfx.SecInfo as SecInfo, hlhtxc5_dmOfx.Accounts as Accounts where InvTran.Skip = 0 and InvBuy.AcctId = BuyOther.AcctId and InvBuy.FiTId = BuyOther.FiTId and InvTran.AcctId = InvBuy.AcctId and InvTran.FiTId = InvBuy.FiTId and SecId.SecId = InvBuy.SecId and SecInfo.BrokerId = SecId.BrokerId and SecInfo.SecId = SecId.SecId and SecId.BrokerId = Accounts.BrokerId and BuyOther.AcctId = Accounts.AcctId and Accounts.JoomlaId = '%s';";
//        CMDBController.executeSQL(String.format(sql, this.userId));
//    }
//    /*
//     * add transfer opening options transactions
//     */
//    private void doTransferOpeningOptions() {
//        CMDBController.callStored(
//            "hlhtxc5_dmOfx.TransfersOpeningOptions_insert('" + userId + "')");
//    }
//
//    private void doTransferClosingOptions() {
//        CMDBController.callStored(
//            "hlhtxc5_dmOfx.TransfersClosingOptions_insert('" + userId + "')");
//    }
//
//    private void doTransferOpeningOther() {
//        CMDBController.callStored(
//            "hlhtxc5_dmOfx.TransfersOpeningOther_insert('" + userId + "')");
//    }
//    /*
//     * update underlying prices in openingOptions
//     */
//    private void doOpeningOptionsUpdatePrices() {
//        CMDBController.executeSQL(String.format(OpeningOptionModel.DMOFX_UPDATE_PRICES, this.userId));
//    }
//
//    /*
//     * update underlying prices in closingOptions
//     */
//    private void doClosingOptionsUpdatePrices() {
//CMDBController.executeSQL(String.format(ClosingOptionModel.DMOFX_UPDATE_PRICES, this.userId));
////        String sql;
////
////        sql
////                = "update hlhtxc5_dmOfx.ClosingOptions CO join hlhtxc5_dmOfx.ClientEquityAttributes CEA on CO.Ticker = CEA.Ticker join hlhtxc5_dmOfx.EquityHistory DLHistory on CEA.TickerIEX = DLHistory.TickerIEX set CO.ClosingOpen = DLHistory.`Open`, CO.ClosingHigh = DLHistory.High, CO.ClosingLow = DLHistory.Low, CO.ClosingClose = DLHistory.Close where DLHistory.`Date` = cast(CO.GMTDtTrade as date) and CO.ClosingClose = 0.0 and CEA.JoomlaId = '%s';";
////        CMDBController.executeSQL(String.format(sql, this.userId));
//    }
}
