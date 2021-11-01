package com.hpi.appcontrollers;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import com.hpi.entities.*;
import java.util.*;

public class DbOfxController
{

    private class inner
    {

        private String demoFields;
        private Boolean bOptional;

        public inner(String demoFields, Boolean bOptional)
        {
        }
    }

    private final String[][] dmOfxClearTables = {
        //table name, fields list
        {"AccountTotals", AccountTotals.DEMO_FIELDS},
        {"Balances", Balances.DEMO_FIELDS},
        {"ClientTransferData", ClientTransferData.DEMO_FIELDS},
        {"ClosedOptionFIFO", ClosedOptionFIFOModel.DEMO_FIELDS},
        {"ClosedOptionTrans", ClosedOptionTrans.DEMO_FIELDS},
        {"ClosedStockFIFO", ClosedStockFIFOModel.DEMO_FIELDS},
        {"ClosedStockTrans", ClosedStockTransModel.DEMO_FIELDS},
        {"ClosingDebt", ClosingDebtModel.DEMO_FIELDS},
        {"ClosingMF", ClosingMFModel.DEMO_FIELDS},
        {"ClosingOptions", ClosingOptionModel.DEMO_FIELDS},
        {"ClosingStock", ClosingStockModel.DEMO_FIELDS},
        {"FIFOClosed", FIFOClosedTransactionModel.DEMO_FIELDS},
        {"FIFOOpenTransactions", FIFOOpenTransactionModel.DEMO_FIELDS},
        {"OpeningDebt", OpeningDebtModel.DEMO_FIELDS},
        {"OpeningMF", OpeningMFModel.DEMO_FIELDS},
        {"OpeningOptions", OpeningOptionModel.DEMO_FIELDS},
        {"OpeningOther", OpeningOtherModel.DEMO_FIELDS},
        {"OpeningStock", OpeningStockModel.DEMO_FIELDS},
        {"OpenOptionFIFO", OpenOptionFIFOModel.DEMO_FIELDS},
        {"OpenStockFIFO", OpenStockFIFOModel.DEMO_FIELDS},
        {"TransactionLog", TransactionLogModel.DEMO_FIELDS}
    };

    /*
     * Singleton
     *
     */
    private static DbOfxController instance;

    protected DbOfxController()
    {
        // protected prevents instantiation outside of package
    }

    public synchronized static DbOfxController getInstance()
    {
        if (DbOfxController.instance == null) {
            DbOfxController.instance = new DbOfxController();
        }
        return DbOfxController.instance;
    }
    //***

    public void doBuildDemoAcct(Integer userId)
    {
        //need to clear the old demo account
        this.doClearUserId(userId);

        this.doBuildDbOfxDemoAcct(userId);
        this.doBuildDmOfxDemoAcct(userId);
    }

    public void doBuildDbOfxDemoAcct(Integer userId)
    {
        //duplicate AcctId 1 to AcctId 4
        //get list of accounts
        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.Bal (select '4', Name, Descr, BalType, Value, DtAsOf, CurSym from hlhtxc5_dbOfx.Bal where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.BankAcctToFrom (Select '4', BankId, BranchId, AcctId2, AcctType, AcctKey from hlhtxc5_dbOfx.BankAcctToFrom where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.BuyDebt (select '4', FiTId, AccrdInt from hlhtxc5_dbOfx.BuyDebt where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.BuyMF (select '4', FiTId, BuyType, RelFiTId from hlhtxc5_dbOfx.BuyMF where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.BuyOpt (select '4', FiTId, OptBuyType, ShPerCtrct from hlhtxc5_dbOfx.BuyOpt where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.BuyOther (select '4', FiTId from hlhtxc5_dbOfx.BuyOther where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.BuyStock (select '4', FiTId, BuyType from hlhtxc5_dbOfx.BuyStock where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.CCAcctToFrom (select '4', AcctId2, AcctKey from hlhtxc5_dbOfx.CCAcctToFrom where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.ClosureOpt (select '4', FiTId, SecId, OptAction, Units, ShPerCtrct, SubAcctSec, RelFiTId, Gain from hlhtxc5_dbOfx.ClosureOpt where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.Income (select '4', FiTId, SecId, IncomeType, Total, SubAcctSec, SubAcctFund, TaxExempt, Withholding, CurSym, OrigCurSym, Inv401kSource from hlhtxc5_dbOfx.Income where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.InvBal (select '4', AvailCash, MarginBalance, ShortBalance, BuyPower from hlhtxc5_dbOfx.InvBal where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.InvBankTran (select '4', FiTId, SubAcctFund from hlhtxc5_dbOfx.InvBankTran where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.InvBuy (select '4', FiTId, SecId, Units, UnitPrice, Markup, Commission, Taxes, Fees, TransLoad, Total, CurSym, OrigCurSym, SubAcctSec, SubAcctFund, LoanId, LoanPrincipal, LoanInterest, Inv401kSource, DtPayroll, PriorYearContrib from hlhtxc5_dbOfx.InvBuy where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.InvExpense (select '4', FiTId, SecId, Total, SubAcctSec, SubAcctFund, CurSym, OrigCurSym, Inv401kSource from hlhtxc5_dbOfx.InvExpense where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.InvPos (select '4', InvPosId, DtAsOf, SecId, HeldInAcct, PosType, Units, UnitPrice, MktVal, DtPriceAsOf, CurSym, Memo, Inv401kSource from hlhtxc5_dbOfx.InvPos where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.InvSell (select '4', FiTId, SecId, Units, UnitPrice, Markdown, Commission, Taxes, Fees, TransLoad, Withholding, TaxExempt, Total, Gain, CurSym, OrigCurSym, SubAcctSec, SubAcctFund, LoanId, StateWithholding, Penalty, Inv401kSource from hlhtxc5_dbOfx.InvSell where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.InvTran (select '4', FiTId, SrvrTId, DtTrade, DtSettle, ReversalFiTId, Memo, Skip from hlhtxc5_dbOfx.InvTran where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.JrnlFund (select '4', FiTId, SubAcctTo, SubAcctFrom, Total from hlhtxc5_dbOfx.JrnlFund where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.JrnlSec (select '4', FiTId, SecId, SubAcctTo, SubAcctFrom, Units from hlhtxc5_dbOfx.JrnlSec where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.MarginInterest (select '4', FiTId, Total, SubAcctFund, Currency, OrigCurrency from hlhtxc5_dbOfx.MarginInterest where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.PosDebt (select '4', InvPosId from hlhtxc5_dbOfx.PosDebt where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.PosMF (select '4', InvPosId, UnitsStreet, UnitsUser, ReinvDiv, ReinvCG from hlhtxc5_dbOfx.PosMF where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.PosOpt (select '4', InvPosId, Secured from hlhtxc5_dbOfx.PosOpt where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.PosOther (select '4', InvPosId from hlhtxc5_dbOfx.PosOther where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.PosStock (select '4', InvPosId, UnitsStreet, UnitsUser, ReinvDiv from hlhtxc5_dbOfx.PosStock where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.Reinvest (select '4', FiTId, SecId, IncomeType, Total, SubAcctSec, Units, UnitPrice, Commission, Taxes, Fees, ReinvLoad, TaxExempt, Currency, OrigCurrency, Inv401kSource, Skip from hlhtxc5_dbOfx.Reinvest where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.RetOfCap (select '4', FiTId, SecId, Total, SubAcctSec, SubAcctFund, Currency, OrigCurrency, Inv401kSource from hlhtxc5_dbOfx.RetOfCap where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.SellDebt (select '4', FiTId, SellReason, AccrdInt from hlhtxc5_dbOfx.SellDebt where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.SellMF (select '4', FiTId, SellType, AvgCostBasis, RelFiTId from hlhtxc5_dbOfx.SellMF where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.SellOpt (select '4', FiTId, OptSellType, ShPerCtrct, RelFiTId, RelType, Secured from hlhtxc5_dbOfx.SellOpt where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.SellOther (select '4', FiTId from hlhtxc5_dbOfx.SellOther where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.SellStock (select '4', FiTId, SellType from hlhtxc5_dbOfx.SellStock where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.Split (select '4', FiTId, SecId, SubAcctSec, OldUnits, NewUnits, Numerator, Denominator, Currency, OrigCurrency, FracCash, SubAcctFund, Inv401kSource from hlhtxc5_dbOfx.Split where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.StmtTrn (select '4', FiTId, TrnType, DtPosted, DtUser, DtAvail, TrnAmt, CorrectFiTId, CorrectAction, SrvrTId, CheckNum, RefNum, SIC, PayeeId, Name, Payee, ExtdName, BankAcctTo, CCAcctTo, Memo, ImageData, CurSym, OrigCurSym, Inv401kSource, OfxPayeeId from hlhtxc5_dbOfx.StmtTrn where AcctId = 1);");

        CMDBController.executeSQL(
            "insert into hlhtxc5_dbOfx.Transfer (select '4', FiTId, SecId, SubAcctSec, Units, TferAction, PosType, InvAcctFrom, AvgCostBasis, UnitPrice, DtPurchase, Inv401kSource from hlhtxc5_dbOfx.Transfer where AcctId = 1);");
    }

    public void doBuildDmOfxDemoAcct(Integer userId)
    {
        String sql, sqlBase;

        //default userId is 1
        if (userId == null) {
            userId = 1;
        }

        //special tables, do userId 816 to userId
        sql
            = "insert into hlhtxc5_dmOfx.ClientEquityAttributes (select '%s', Ticker, TickerIEX, Active, ClientSectorId, TgtPct, AnalystTgt, StkPrice, Comment, TgtLocked, ActPct from hlhtxc5_dmOfx.ClientEquityAttributes where JoomlaId = '816');";
        CMDBController.executeSQL(String.format(sql, userId));

        sql
            = "insert into hlhtxc5_dmOfx.ClientSectorList (select '%s', ClientSectorId, ClientSector, CSecShort, Active, TgtPct, Comment, TgtLocked, ActPct, MktVal, LMktVal, CustomSector from hlhtxc5_dmOfx.ClientSectorList where JoomlaId = '816');";
        CMDBController.executeSQL(String.format(sql, userId));

        sqlBase = "insert into hlhtxc5_dmOfx.%s (select '4', '1', %s from hlhtxc5_dmOfx.%s where DMAcctId = 1);";

        for (String[] table : dmOfxClearTables) {
            //duplicate AcctId 1 to AcctId 4
            CMDBController.executeSQL(String.format(sqlBase, table[0],
                table[1], table[0]));
        }
    }

    public void doClearUserId(Integer userId)
    {
        this.doClearDbOfxUserId(userId);
        this.doClearDmOfxUserId(userId);
    }

    /*
     * Clear DBOFX_DMOFX_SELL userId from dbOfx for downloaded data; leaves dmOfx
     * todo: should not ever do this without clearing dmOfx
     */
    public void doClearDbOfxUserId(Integer userId)
    {
        String sqlBase, sql;
        List<Integer> acctIdList;

        //default userId is 1
        if (userId == null) {
            userId = 1;
        }

        String[] tables = {
            //"Accounts", must leave even though will be an artifact
            "Bal", "BankAcctToFrom",
            //"Brokers", must leave
            //"Brokers2", must leave
            "BuyDebt", "BuyMF", "BuyOpt", "BuyOther", "BuyStock",
            "CCAcctToFrom",
            //"ClientData", leave
            "ClosureOpt",
            //Currency, DebtInfo, FiMFPortion,
            "Income", "InvBal", "InvBankTran", "InvBuy", "InvExpense", "InvPos", "InvSell", "InvTran",
            "JrnlFund", "JrnlSec", "MarginInterest",
            //MFInfo, MFPortion, OptInfo, OtherInfo, Payee
            "PosDebt", "PosMF", "PosOpt", "PosOther", "PosStock",
            "Reinvest", "RetOfCap",
            //"SecId",
            "SellDebt", "SellMF", "SellOpt", "SellOther", "SellStock",
            "Split", "StmtTrn",
            //StockInfo,
            "Transfer"
        };

        //get list of accounts
        sql = "select AcctId from hlhtxc5_dbOfx.Accounts where JoomlaId = '%s';";
        sql = String.format(sql, userId);
        acctIdList = CMDBController.executeSQLSingleIntegerList(sql);

        if (acctIdList.isEmpty()) {
            return;
        }

        sqlBase = "delete from hlhtxc5_dbOfx.%s where AcctId = '%s';";
        //clear all tables for userId
        for (Integer acctId : acctIdList) {
            for (String table : tables) {
                CMDBController.executeSQL(String.format(sqlBase, table, acctId));
            }
        }
    }

    /*
     * Set dbOfx.InvTran.Skip
     */
    public void doIgnoreFiTId(String fiTId, Integer acctId, Integer bSkip)
    {
        String sqlBase;

        if (fiTId == null || acctId == null || bSkip == null) {
            System.out.println("Improper command line parameters");
            return;
        }

        String[] tables = {
            "InvTran"
        };

        //column Skip is tinyInt, 0 false, 1 true
        sqlBase = "update hlhtxc5_dbOfx.%s set Skip = '%s' where AcctId = '%s' and FiTId = '%s';";

        //clear all tables for userId
        for (String table : tables) {
            CMDBController.executeSQL(String.format(sqlBase, table, bSkip, acctId,
                fiTId));
        }
    }

    /**
     * Clear positions data for refresh
     */
    public void doClearPositions()
    {
        String[] truncate = {
            "hlhtxc5_dmOfx.PositionsOpen",
            "hlhtxc5_dmOfx.PositionsOpenTransactions",
            "hlhtxc5_dmOfx.PositionsClosed",
            "hlhtxc5_dmOfx.PositionsClosedTransactions",};

        String[] setComplete = {
            "hlhtxc5_dmOfx.FIFOClosedTransactions",
            "hlhtxc5_dmOfx.FIFOOpenTransactions",};

        for (String table : truncate) {
            CMDBController.executeSQL("truncate " + table);
        }

        for (String table : setComplete) {
            CMDBController.executeSQL("update " + table + " set Complete = 0");
        }
    }

    /*
     * Clear DBOFX_DMOFX_SELL userId from the dmOfx Table; leaves dbOfx source data
     * todo: needs work to be complete
     *todo: why is this in dbOfxController
     */
    public void doClearDmOfxUserId(Integer userId)
    {
        String sqlDmOfx, sqlDbOfx;
        List<Integer> acctIdList;

        //default userId is 1
        if (userId == null) {
            userId = 1;
        }

        //get list of accounts
        sqlDmOfx = "select DMAcctId from hlhtxc5_dmOfx.ClientAccts where JoomlaId = '%s';";
        acctIdList = CMDBController.executeSQLSingleIntegerList(String.format(sqlDmOfx, userId));

        if (acctIdList.isEmpty()) {
            return;
        }

        String tables[] = {
            //todo: commented are optional
            //      "hlhtxc5_dmOfx.AccountTotals",
            //      "hlhtxc5_dmOfx.AppTracking",
            //      "hlhtxc5_dmOfx.Balances",
            //      "hlhtxc5_dmOfx.ClientEquityAttributes",
            //      "hlhtxc5_dmOfx.ClientSectorList",
            "hlhtxc5_dmOfx.ClosedOptionFIFO",
            "hlhtxc5_dmOfx.ClosedOptionTrans",
            "hlhtxc5_dmOfx.ClosedStockFIFO",
            "hlhtxc5_dmOfx.ClosedStockTrans",
            "hlhtxc5_dmOfx.ClosingDebt",
            "hlhtxc5_dmOfx.ClosingMF",
            "hlhtxc5_dmOfx.ClosingOptions",
            "hlhtxc5_dmOfx.ClosingOther",
            "hlhtxc5_dmOfx.ClosingStock",
            //        "hlhtxc5_dmOfx.Drawdown",
            "hlhtxc5_dmOfx.FIFOClosedTransactions",
            "hlhtxc5_dmOfx.FIFOOpenTransactions",
            "hlhtxc5_dmOfx.OpeningDebt",
            "hlhtxc5_dmOfx.OpeningMF",
            "hlhtxc5_dmOfx.OpeningOptions",
            "hlhtxc5_dmOfx.OpeningOther",
            "hlhtxc5_dmOfx.OpeningStock",
            "hlhtxc5_dmOfx.OpenOptionFIFO",
            "hlhtxc5_dmOfx.OpenStockFIFO",
            "hlhtxc5_dmOfx.PositionsClosed",
            "hlhtxc5_dmOfx.PositionsClosedTransactions",
            "hlhtxc5_dmOfx.PositionsOpen",
            "hlhtxc5_dmOfx.PositionsOpenTransactions",
            "hlhtxc5_dmOfx.TransactionLog"
        };

        //clear tables for account
        for (Integer acctId : acctIdList) {
            //all account handling; remove from dmOfx tables anything for the account
            //exceptions are the optional tables
            sqlDmOfx = "delete from %s where JoomlaId = '" + userId + "';";
            for (String table : tables) {
                CMDBController.executeSQL(String.format(sqlDmOfx, table));
            }

            //demo account handling
            //todo: there are additional requirements for the demo accounts
//            if (userId == 1) {
//                //handle special tables from demo account only
//                sqlDmOfx = "delete from hlhtxc5_dmOfx.ClientEquityAttributes where JoomlaId = '%s';";
//                CMDBController.executeSQL(String.format(sqlDmOfx, userId));
//
//                sqlDmOfx = "delete from hlhtxc5_dmOfx.ClientSectorList where JoomlaId = '%s';";
//                CMDBController.executeSQL(String.format(sqlDmOfx, userId));
//
//                sqlDmOfx = "delete from hlhtxc5_dmOfx.TPCPreferences where JoomlaId = '%s';";
//                CMDBController.executeSQL(String.format(sqlDmOfx, userId));
//            }
            //reset dbOfx.InvTran.Complete to 0 for account
            //dbOfx.AcctId = dmOfx.DMAcctId
            sqlDbOfx = "update hlhtxc5_dbOfx.InvTran set Complete = '0' where AcctId = '%s';";
            CMDBController.executeSQL(String.format(sqlDbOfx, acctId));

            //reset dmOfx.clientOpeningStock.Units to OrigUnits
            sqlDmOfx = "update hlhtxc5_dmOfx.ClientOpeningStock set Units = OrigUnits where DMAcctId = '%s';";
            CMDBController.executeSQL(String.format(sqlDmOfx, acctId));

            //reset dmOfx.clientClosingStock.Units to OrigUnits
            sqlDmOfx = "update hlhtxc5_dmOfx.ClientClosingStock set Units = OrigUnits where DMAcctId = '%s';";
            CMDBController.executeSQL(String.format(sqlDmOfx, acctId));
        }
    }

    /*
     * Clears interim tables on dbOfx
     */
    public final void cleanDbOfx()
    {
//        Integer userId;
        String sql;

        sql
            = "delete from hlhtxc5_dbOfx.%s where exists (select * from Accounts as Accounts where %s.AcctId = Accounts.AcctId  and Accounts.JoomlaId = '%s')";

//        userId = CMDBModel.getUserId();
        String tables[] = {
            "PosOpt", "PosMF", "PosDebt", "PosOther",
            "PosStock", "InvPos",
            //            "Bal",
            "InvBal"
        };

        for (String table : tables) {

//            sql = "delete from hlhtxc5_dbOfx." + table + " " + "where exists (select * "
//                      + "from Accounts as Accounts " + "where " + table + ".AcctId = Accounts.AcctId "
//                      + "and Accounts.JoomlaId = " + userId + ")";
            CMDBController.executeSQL(String.format(sql, table, table, CMDBModel.getUserId()));

//            CMDBController.executeSQLSingleIntegerList(sql);
        }
    }
}
