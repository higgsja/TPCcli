package CmdLineController;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.entities.*;
import java.sql.*;
import java.util.*;
import org.junit.Test;

public class OptionTest1
    extends CmdLineControllerBase
{

    /**
     * buy to open multiple lots; sell to close multiple lots
     */
//    @Ignore
    @Test
    @java.lang.SuppressWarnings("empty-statement")
    public void optionTest1A()
    {
        String sql;
        ResultSet rs;

        class Local
        {
        };

        System.out.println(Local.class.getEnclosingMethod().getName() + "\n");
        System.out.println("\tBuy to open multiple lots; sell to close multiple lots");

        //add transactions to openingOptions
        this.openingModels.add(this.oom1);
        this.openingModels.add(this.oom2);
        this.openingModels.add(this.oom3);
        this.openingModels.add(this.oom4);
        for (OpeningOptionModel oom : this.openingModels)
        {
            sql = OpeningOptionModel.INSERT_ALL_VALUES;
            sql += oom.getDmAcctId() + ", ";
            sql += oom.getJoomlaId() + ", '";
            sql += oom.getFiTId() + "', '";
            sql += oom.getTicker() + "', '";;
            sql += oom.getEquityId() + "', ";
            sql += null + ", '";    //transactionName
            sql += oom.getDateOpen() + "', '";
            sql += oom.getDateClose() + "', '";
            sql += oom.getDateExpire() + "', ";
            sql += oom.getShPerCtrct() + ", ";
            sql += oom.getUnits() + ", ";
            sql += oom.getPriceOpen() + ", ";
            sql += oom.getPriceClose() + ", ";
            sql += oom.getMarkUpDn() + ", ";
            sql += oom.getCommission() + ", ";
            sql += oom.getTaxes() + ", ";
            sql += oom.getFees() + ", ";
            sql += oom.getTransLoad() + ", ";
            sql += oom.getTotalOpen() + ", ";
            sql += oom.getTotalClose() + ", ";
            sql += null + ",'"; //cursym
            sql += oom.getSubAcctSec() + "', '";
            sql += oom.getSubAcctFund() + "', ";
            sql += null + ", "; //reversalFiTId
            sql += null + ", "; //comment
            sql += oom.getOpeningOpen() + ", ";
            sql += oom.getOpeningHigh() + ", ";
            sql += oom.getOpeningLow() + ", ";
            sql += oom.getOpeningClose() + ", '";
            sql += oom.getEquityType() + "', '";
            sql += oom.getOptionType() + "', '";
            sql += oom.getTransactionType() + "', ";
            sql += oom.getStrikePrice();
            sql += ");";

            CMDBController.executeSQL(sql);
        }

        //add transactions to closingOptions
        this.closingModels.add(this.com1);
        for (ClosingOptionModel com : this.closingModels)
        {
            sql = ClosingOptionModel.INSERT_ALL_VALUES;
            sql += com.getDmAcctId() + ", ";
            sql += com.getJoomlaId() + ", '";
            sql += com.getFiTId() + "', '";
            sql += com.getTicker() + "', '";;
            sql += com.getEquityId() + "', ";
            sql += null + ", '";    //transactionName
            sql += com.getDateOpen() + "', '";
            sql += com.getDateClose() + "', '";
            sql += com.getDateExpire() + "', ";
            sql += com.getShPerCtrct() + ", ";
            sql += com.getUnits() + ", ";
            sql += com.getPriceOpen() + ", ";
            sql += com.getPriceClose() + ", ";
            sql += com.getMarkUpDn() + ", ";
            sql += com.getCommission() + ", ";
            sql += com.getTaxes() + ", ";
            sql += com.getFees() + ", ";
            sql += com.getTransLoad() + ", ";
            sql += com.getTotalOpen() + ", ";
            sql += com.getTotalClose() + ", ";
            sql += null + ", '";
            sql += com.getSubAcctSec() + "', '";
            sql += com.getSubAcctFund() + "', ";
            sql += null + ", "; //reversalFiTId
            sql += null + ", "; //comment
            sql += com.getClosingOpen() + ", ";
            sql += com.getClosingHigh() + ", ";
            sql += com.getClosingLow() + ", ";
            sql += com.getClosingClose() + ", '";
            sql += com.getEquityType() + "', '";
            sql += com.getOptionType() + "', '";
            sql += com.getTransactionType() + "', ";
            sql += com.getStrikePrice();
            sql += ");";

            CMDBController.executeSQL(sql);

            String[] args = new String[5];
            args[0] = "--dataMart";
            args[1] = "--progressBar";
            args[2] = "true";
            args[3] = "--userId";
            args[4] = USER_ID.toString();
            cmdLineController.doCommandLine(args);

//            sql = "select * from PositionsClosed pc where JoomlaId = 5";
//
//            try (Connection con = getConnection();
//                PreparedStatement pStmt = con.prepareStatement(sql))
//            {
//                rs = pStmt.executeQuery();
//                
//                while (rs.next()){
//                    
//                }
//
//            } catch (SQLException ex)
//            {
//
//            }
//
//            String[][] stringTests =
//            {
//                {
//                    "a", cofm.getEquityId(), "PCG   200918C00013000"
//                },
//                {
//                    "b", cofm.getTransactionName(), "PCG 18Sep20 13.0 Call"
//                }
//            };
//
//            Integer[][] integerTests =
//            {
//                {
////                1, optionController.getOptionOpenList().size(), 1
//                }
//            };
//
//            Double[][] doubleTests =
//            {
//                {
//                    1.0, cofm.getUnits(), -10.0
//                },
//                {
//                    2.0, cofm.getPriceOpen(), 2.83
//                },
//                {
//                    3.0, cofm.getTotalOpen(), -2831.1600
//                },
//                {
//                    4.0, cofm.getTotalClose(), 98.81
//                }
//            };
//
//            for (String[] test : stringTests)
//            {
//                (new TestString(test[0], test[1], test[2])).doTest();
//            }
//
////        for (Integer[] test : integerTests)
////        {
////            (new TestInteger(test[0], test[1], test[2])).doTest();
////        }
//            for (Double[] test : doubleTests)
//            {
//                (new TestDouble(test[0], test[1], test[2])).doTest();
//            }
//        }
        }
    }

    private final ArrayList<OpeningOptionModel> openingModels = new ArrayList<>();
    private final ArrayList<ClosingOptionModel> closingModels = new ArrayList<>();

    private final OpeningOptionModel oom1 = OpeningOptionModel.builder()
        .dmAcctId(4)
        .joomlaId(USER_ID)
        .fiTId("211102_6741_3")
        .ticker("AMZN")
        //        .transactionName("")  //no transaction names in openingOptions
        .equityId("AMZN  220121C03330000")
        .dateOpen(java.sql.Date.valueOf("2021-11-02"))
        //        .dateClose(null)
        .dateExpire(java.sql.Date.valueOf("2022-01-21"))
        .shPerCtrct(100)
        .units(2.0)
        .priceOpen(131.79)
        //        .priceClose(null)
        //        .markUpDn(null)
        //        .commission(null)
        //        .taxes(null)
        .fees(0.22)
        //        .transLoad(null)
        .totalOpen(-26358.22)
        //        .totalClose(null)
        //        .curSym(null)
        .subAcctSec("MARGIN")
        .subAcctFund("MARGIN")
        //        .reversalFiTId(null)
        //        .comment("")
        .openingOpen(99.99)
        .openingHigh(99.99)
        .openingLow(99.99)
        .openingClose(99.99)
        .equityType("OPTION")
        .optionType("CALL")
        .transactionType("BUYTOOPEN")
        .strikePrice(3330.0)
        .build();

    private final OpeningOptionModel oom2 = OpeningOptionModel.builder()
        .dmAcctId(4)
        .joomlaId(USER_ID)
        .fiTId("211102_6741_4")
        .ticker("AMZN")
        //        .transactionName("")  //no transaction names in openingOptions
        .equityId("AMZN  220121C03330000")
        .dateOpen(java.sql.Date.valueOf("2021-11-02"))
        //        .dateClose(null)
        .dateExpire(java.sql.Date.valueOf("2022-01-21"))
        .shPerCtrct(100)
        .units(2.0)
        .priceOpen(132.3)
        //        .priceClose(null)
        //        .markUpDn(null)
        //        .commission(null)
        //        .taxes(null)
        .fees(0.22)
        //        .transLoad(null)
        .totalOpen(-26460.22)
        //        .totalClose(null)
        //        .curSym(null)
        .subAcctSec("MARGIN")
        .subAcctFund("MARGIN")
        //        .reversalFiTId(null)
        //        .comment("")
        .openingOpen(99.99)
        .openingHigh(99.99)
        .openingLow(99.99)
        .openingClose(99.99)
        .equityType("OPTION")
        .optionType("CALL")
        .transactionType("BUYTOOPEN")
        .strikePrice(3330.0)
        .build();

    private final OpeningOptionModel oom3 = OpeningOptionModel.builder()
        .dmAcctId(4)
        .joomlaId(USER_ID)
        .fiTId("211102_6741_5")
        .ticker("AMZN")
        //        .transactionName("")  //no transaction names in openingOptions
        .equityId("AMZN  220121C03330000")
        .dateOpen(java.sql.Date.valueOf("2021-11-02"))
        //        .dateClose(null)
        .dateExpire(java.sql.Date.valueOf("2022-01-21"))
        .shPerCtrct(100)
        .units(4.0)
        .priceOpen(132.27)
        //        .priceClose(null)
        //        .markUpDn(null)
        //        .commission(null)
        //        .taxes(null)
        .fees(0.44)
        //        .transLoad(null)
        .totalOpen(-52908.44)
        //        .totalClose(null)
        //        .curSym(null)
        .subAcctSec("MARGIN")
        .subAcctFund("MARGIN")
        //        .reversalFiTId(null)
        //        .comment("")
        .openingOpen(99.99)
        .openingHigh(99.99)
        .openingLow(99.99)
        .openingClose(99.99)
        .equityType("OPTION")
        .optionType("CALL")
        .transactionType("BUYTOOPEN")
        .strikePrice(3330.0)
        .build();

    private final OpeningOptionModel oom4 = OpeningOptionModel.builder()
        .dmAcctId(4)
        .joomlaId(USER_ID)
        .fiTId("211102_6741_6")
        .ticker("AMZN")
        //        .transactionName("")  //no transaction names in openingOptions
        .equityId("AMZN  220121C03330000")
        .dateOpen(java.sql.Date.valueOf("2021-11-02"))
        //        .dateClose(null)
        .dateExpire(java.sql.Date.valueOf("2022-01-21"))
        .shPerCtrct(100)
        .units(2.0)
        .priceOpen(132.28)
        //        .priceClose(null)
        //        .markUpDn(null)
        //        .commission(null)
        //        .taxes(null)
        .fees(0.22)
        //        .transLoad(null)
        .totalOpen(-26456.22)
        //        .totalClose(null)
        //        .curSym(null)
        .subAcctSec("MARGIN")
        .subAcctFund("MARGIN")
        //        .reversalFiTId(null)
        //        .comment("")
        .openingOpen(99.99)
        .openingHigh(99.99)
        .openingLow(99.99)
        .openingClose(99.99)
        .equityType("OPTION")
        .optionType("CALL")
        .transactionType("BUYTOOPEN")
        .strikePrice(3330.0)
        .build();

    private final ClosingOptionModel com1 = ClosingOptionModel.builder()
        .dmAcctId(4)
        .joomlaId(USER_ID)
        .fiTId("211105_6776_0")
        .ticker("AMZN")
        //        .transactionName("")  //no transaction names in openingOptions
        .equityId("AMZN  220121C03330000")
        //        .dateOpen(java.sql.Date.valueOf("2021-11-02"))
        .dateClose(java.sql.Date.valueOf("2021-11-05"))
        .dateExpire(java.sql.Date.valueOf("2022-01-21"))
        .shPerCtrct(100)
        .units(-10.0)
        //        .priceOpen(132.28)
        .priceClose(279.65)
        //        .markUpDn(null)
        //        .commission(null)
        //        .taxes(null)
        .fees(2.54)
        //        .transLoad(null)
        //        .totalOpen(-26456.22)
        .totalClose(279647.4600)
        //        .curSym(null)
        .subAcctSec("MARGIN")
        .subAcctFund("MARGIN")
        //        .reversalFiTId(null)
        //        .comment("")
        //        .closingOpen(null)
        //        .closingHigh(null)
        //        .closingLow(null)
        //        .closingClose(null)
        .equityType("OPTION")
        .optionType("CALL")
        .transactionType("SELLTOCLOSE")
        .strikePrice(3330.0)
        .build();
}
