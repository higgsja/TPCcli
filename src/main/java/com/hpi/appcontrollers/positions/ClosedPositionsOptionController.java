package com.hpi.appcontrollers.positions;

import com.hpi.hpiUtils.OCCclass;
import com.hpi.TPCCMcontrollers.CMDBController;
import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.entities.*;
import com.hpi.hpiUtils.CMHPIUtils;
import static java.lang.Math.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import javax.swing.JOptionPane;
import lombok.Getter;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Handle all closed option transactions, converting to positions
 */
public class ClosedPositionsOptionController
{

    private final Integer userId;
    @Getter private final ArrayList<FIFOClosedTransactionModel> fifoClosedTransactionModels;
    @Getter private final ArrayList<PositionClosedModel> positionClosedModels;
    @Getter private final ArrayList<PositionClosedTransactionModel> positionClosedTransactionModels;
    private final ArrayList<PositionClosedModel> pcmAddList;
    private final String[] months;

    /*
     * Singleton
     * process closed Options into positions: one element in processing positions
     *
     */
    private static ClosedPositionsOptionController instance;

    static
    {
        ClosedPositionsOptionController.instance = null;
    }

    protected ClosedPositionsOptionController()
    {
        this.months = new DateFormatSymbols().getShortMonths();
        // protected prevents instantiation outside of package
        this.userId = CMDBModel.getUserId();
        this.fifoClosedTransactionModels = new ArrayList<>();
        this.positionClosedTransactionModels = new ArrayList<>();
        this.positionClosedModels = new ArrayList<>();
        this.pcmAddList = new ArrayList<>();
    }

    public synchronized static ClosedPositionsOptionController getInstance()
    {
        if (ClosedPositionsOptionController.instance == null)
        {
            ClosedPositionsOptionController.instance = new ClosedPositionsOptionController();
        }
        return ClosedPositionsOptionController.instance;
    }

    /**
     * refresh positions from existing data
     */
    public void doClosedPositions()
    {
        //get closed transactions
        this.getTransactions("option");

        this.doFctm2Pctm();

        this.doPctm2Pcm();

        this.doPositionsTacticId();

        this.doPositionName();

        //push to the database
        this.doSQL();

        //mark all fifoClosedTransactions complete in the database
        this.setFifoClosedTransactionsComplete();
    }

    /**
     * push the positions to the database
     */
    public void doSQL()
    {
        for (PositionClosedModel pcm : this.positionClosedModels)
        {
            Integer positionId;

            //add the positionClosedModel to positionsClosed table
            positionId = this.insertPositionSQL(pcm);

            if (positionId == -1)
            {
                //todo: got an error spy
                CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    "\n\n=======================that pcg issue===================\n\n", JOptionPane.ERROR_MESSAGE);
                return;
            }

            pcm.setPositionId(positionId);

            //add the array of transactions to positionsClosedTransactions table
            this.insertPositionClosedTransactionsTableSQL(positionId, pcm);
        }
    }

    private void insertPositionClosedTransactionsTableSQL(Integer positionId,
        PositionClosedModel pcm)
    {
        String sql;

        for (PositionClosedTransactionModel pctm : pcm.getPositionClosedTransactionModels())
        {
            sql = String.format(PositionClosedTransactionModel.POSITION_TRANSACTION_INSERT,
                pctm.getDmAcctId(),
                pctm.getJoomlaId(),
                positionId,
                pctm.getFiTId(),
                //                pctm.getEquityId(),   //do not set here as multi-leg positions have no equityId
                pctm.getTransactionName(),
                pctm.getTicker(),
                pctm.getDateOpen(),
                pctm.getDateClose(),
                pctm.getUnits(),
                pctm.getPriceOpen(),
                pctm.getPriceClose(),
                pctm.getDays(),
                pctm.getPositionType(),
                pctm.getTotalOpen(),
                pctm.getTotalClose(),
                pctm.getEquityType(),
                pctm.getGain(),
                pctm.getGainPct(),
                pctm.getTransactionType(),
                0);

            CMDBController.executeSQL(sql);
        }
    }

    public void setFifoClosedTransactionsComplete()
    {
        String sql;

        for (FIFOClosedTransactionModel fctm : this.fifoClosedTransactionModels)
        {
            sql = String.format(FIFOClosedTransactionModel.UPDATE_COMPLETE,
                FIFOClosedTransactionModel.COMPLETE,
                fctm.getDmAcctId(),
                fctm.getJoomlaId(),
                fctm.getFiTId());

            CMDBController.executeSQL(sql);
        }
    }

    private Integer insertPositionSQL(PositionClosedModel pcm)
    {
        String sInsertSQL;

        sInsertSQL = String.format(PositionClosedModel.POSITION_INSERT,
            pcm.getDmAcctId(),
            pcm.getJoomlaId(),
            pcm.getTicker(),
            pcm.getEquityId(),
            pcm.getPositionName(),
            pcm.getTacticId(),
            pcm.getUnits(),
            pcm.getPriceOpen(),
            pcm.getPrice(),
            pcm.getGainPct(),
            pcm.getDateOpen(),
            pcm.getDateClose(),
            pcm.getDays(),
            pcm.getGain(),
            pcm.getPositionType(),
            pcm.getTransactionType(),
            pcm.getTotalOpen(),
            pcm.getTotalClose(),
            pcm.getEquityType());

        return CMDBController.insertAutoRow(sInsertSQL);
    }

    /**
     * create and update the position name in all positions
     */
    public void doPositionName()
    {
        for (PositionClosedModel pcm : this.positionClosedModels)
        {

            switch (pcm.getTacticId())
            {
                case PositionOpenModel.TACTICID_CUSTOM:
                    pcm.setPositionName("Custom");
                    break;
                case PositionOpenModel.TACTICID_LONG:
                case PositionOpenModel.TACTICID_SHORT:
                case PositionOpenModel.TACTICID_LEAP:
                    pcm.setPositionName(this.nameLongShortLeap(pcm));
                    break;
                case PositionOpenModel.TACTICID_VERTICAL:
                    pcm.setPositionName(this.nameVertical(pcm,
                        false));
                    break;
                case PositionOpenModel.TACTICID_STRANGLE:
                    pcm.setPositionName(this.nameStrangle(pcm,
                        false));
                    break;
                case PositionOpenModel.TACTICID_CALENDAR:
                    pcm.setPositionName(this.nameCalendar(pcm,
                        false));
                    break;
                case PositionOpenModel.TACTICID_COVERED:
                    pcm.setPositionName(this.nameCovered(pcm));
                    break;
                case PositionOpenModel.TACTICID_STRADDLE:
                    pcm.setPositionName(this.nameStraddle(pcm,
                        false));
                    break;
                case PositionOpenModel.TACTICID_IRONCONDOR:
                    pcm.setPositionName(this.nameIronCondor(pcm));
                    break;
                case PositionOpenModel.TACTICID_BUTTERFLY:
                    pcm.setPositionName(this.nameButterfly(pcm));
                    break;
                case PositionOpenModel.TACTICID_CONDOR:
                    pcm.setPositionName(this.nameCondor(pcm));
                    break;
                case PositionOpenModel.TACTICID_COLLAR:
                    pcm.setPositionName(this.nameCollar(pcm,
                        false));
                    break;
                case PositionOpenModel.TACTICID_DIAGONAL:
                    pcm.setPositionName(this.nameDiagonal(pcm,
                        false));
                    break;
                case PositionOpenModel.TACTICID_VERTICAL_CUSTOM:
                    pcm.setPositionName(this.nameVertical(pcm,
                        true));
                    break;
                case PositionOpenModel.TACTICID_STRANGLE_CUSTOM:
                    pcm.setPositionName(this.nameStrangle(pcm,
                        true));
                    break;
                case PositionOpenModel.TACTICID_CALENDAR_CUSTOM:
                    pcm.setPositionName(this.nameCalendar(pcm,
                        true));
                    break;
                case PositionOpenModel.TACTICID_STRADDLE_CUSTOM:
                    pcm.setPositionName(this.nameStraddle(pcm,
                        true));
                    break;
                case PositionOpenModel.TACTICID_COLLAR_CUSTOM:
                    pcm.setPositionName(this.nameCollar(pcm,
                        true));
                    break;
                case PositionOpenModel.TACTICID_DIAGONAL_CUSTOM:
                    pcm.setPositionName(this.nameDiagonal(pcm,
                        true));
                    break;
                default:
                    int i = 0;
            }
        }
    }

    private String nameLongShortLeap(PositionClosedModel pcm)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels()
            .get(0)
            .getEquityId());

        //aapl ddJanYY 129 Call
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += " ";
        positionName += closedClass0.getPutcall()
            .equalsIgnoreCase("c") ? "Call" : "Put";

        return positionName;
    }

    private String nameLongShortLeap(PositionClosedTransactionModel pctm)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;

        closedClass0 = new OCCclass(pctm.getEquityId());

        //aapl ddJanYY 129 Call
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += " ";
        positionName += closedClass0.getPutcall().equalsIgnoreCase("c") ? "Call" : "Put";

        return positionName;
    }

    private String nameVertical(PositionClosedModel pcm,
        Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels()
            .get(0)
            .getEquityId());

        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels()
            .get(1)
            .getEquityId());

        //aapl ddJanYY 129/130 Call Vertical
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += "/";
        positionName += closedClass1.getStrike();
        positionName += " ";
        positionName += closedClass0.getPutcall()
            .equalsIgnoreCase("c") ? "Call" : "Put";
        positionName += " Vrtcl";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    private String nameStrangle(PositionClosedModel pcm, Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());

        //aapl ddJanYY 150/120 Strangle
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += closedClass0.getPutcall();
        positionName += "/";
        positionName += closedClass1.getStrike();
        positionName += closedClass1.getPutcall();
        positionName += " Strngl";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    private String nameCalendar(PositionClosedModel pcm,
        Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());

        //aapl 150 ddJanYY/ddFebYY Calendar
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += "/";
        positionName += closedClass1.getExpDay();
        month = NumberUtils.toInt(closedClass1.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass1.getExpYear();
        positionName += " ";
        positionName += " Calndr";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    private String nameCovered(PositionClosedModel pcm)
    {
        return "Covered";
    }

    private String nameStraddle(PositionClosedModel pcm,
        Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());

        //aapl ddJanYY 150 Straddle
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += " Stradl";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    private String nameIronCondor(PositionClosedModel pcm)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());
        closedClass2 = new OCCclass(pcm.getPositionClosedTransactionModels().get(2).getEquityId());
        closedClass3 = new OCCclass(pcm.getPositionClosedTransactionModels().get(3).getEquityId());

        //aapl ddJanYY 185/196/161/172 Iron Condor
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += "/";
        positionName += closedClass1.getStrike();
        positionName += "/";
        positionName += closedClass2.getStrike();
        positionName += "/";
        positionName += closedClass3.getStrike();
        positionName += " I Cndr";

        return positionName;
    }

    private String nameButterfly(PositionClosedModel pcm)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());
        closedClass2 = new OCCclass(pcm.getPositionClosedTransactionModels().get(2).getEquityId());
        closedClass3 = new OCCclass(pcm.getPositionClosedTransactionModels().get(3).getEquityId());

        //aapl ddJanYY 185/196/210 CALL Butterfly
        //todo: leave like an iron condor for now
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += "/";
        positionName += closedClass1.getStrike();
        positionName += "/";
        positionName += closedClass2.getStrike();
        positionName += "/";
        positionName += closedClass3.getStrike();
        positionName += " Btrfly*";

        return positionName;
    }

    private String nameCondor(PositionClosedModel pcm)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());
        closedClass2 = new OCCclass(pcm.getPositionClosedTransactionModels().get(2).getEquityId());
        closedClass3 = new OCCclass(pcm.getPositionClosedTransactionModels().get(3).getEquityId());

        //aapl ddJanYY 185/192/196/210 Call Condor
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += "/";
        positionName += closedClass1.getStrike();
        positionName += "/";
        positionName += closedClass2.getStrike();
        positionName += "/";
        positionName += closedClass3.getStrike();
        positionName += " ";
        positionName += closedClass0.getPutcall();
        positionName += " Condor";

        return positionName;
    }

    private String nameCollar(PositionClosedModel pcm,
        Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());

        //aapl ddJanYY 129/130 Collar
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += "/";
        positionName += closedClass1.getStrike();
        positionName += " ";
        positionName += closedClass0.getPutcall();
        positionName += " Collar";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    private String nameDiagonal(PositionClosedModel pcm,
        Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionClosedTransactionModels().get(1).getEquityId());

        //aapl 129 ddJanYY/130 ddFebYY CALL Diagonal
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
        positionName += "/";
        positionName += closedClass1.getExpDay();
        month = NumberUtils.toInt(closedClass1.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass1.getExpYear();
        positionName += " ";
        positionName += closedClass0.getPutcall();
        positionName += " Diagnl";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    /**
     * aggregate lots into positionClosedTransactionModel
     */
    public void doFctm2Pctm()
    {
        PositionClosedTransactionModel pctm;

        this.positionClosedTransactionModels.clear();

        for (int i = 0; i < this.fifoClosedTransactionModels.size(); i++)
        {
            if (this.fifoClosedTransactionModels.get(i).getBComplete())
            {
                //fctm already handled
                continue;
            }

            pctm = PositionClosedTransactionModel.builder()
                .dmAcctId(this.fifoClosedTransactionModels.get(i).getDmAcctId())
                .joomlaId(this.userId)
                .positionId(-999)
                .build();

            //add first fifoClosedTransaction to the positionClosedTransactionModel.fifoClosedTransactionModels
            pctm.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(
                this.fifoClosedTransactionModels.get(i)));

            //add positionClosedTransactionModel to the positionClosedTransactionModel array
            //using the previously built pctm
            this.positionClosedTransactionModels.add(pctm);

            //mark initial transaction complete
            this.fifoClosedTransactionModels.get(i).setBComplete(true);

            this.addFctm2Pctm(i, pctm);

            //set attributes in the pctm
            this.doAttributesPctm(pctm);
        }
    }

    private void doAttributesPctm(PositionClosedTransactionModel pctm)
    {
        Double totalUnits;
        Double totalOpen;
        Double totalClose;
//        Double transactionModelsPriceClose;
//        Double transactionModelsPriceOpen;
//        Double transactionModelsUnits;
//        Double transactionModelsTotalOpen;
//        Double transactionModelsTotalClose;
//        Double totalUnitsOpen;
//        Double totalUnitsClose;
        Double gain;
        Double gainPct;
        java.sql.Date dateOpen;
        java.sql.Date dateClose;

        dateOpen = new java.sql.Date(0);
        dateClose = new java.sql.Date(0);

        totalUnits = totalOpen = totalClose
            //            = transactionModelsPriceOpen 
            //            = transactionModelsPriceClose = transactionModelsUnits 
            //            = transactionModelsTotalOpen = transactionModelsTotalClose 
            //            = totalUnitsOpen = totalUnitsClose 
            = 0.0;

        for (FIFOClosedTransactionModel fctm : pctm.getFifoClosedTransactionModels())
        {
//            transactionModelsPriceOpen += fctm.getPriceOpen() * fctm.getUnits();
//            transactionModelsPriceClose += fctm.getPriceClose() * fctm.getUnits();
//            transactionModelsTotalOpen += fctm.getTotalOpen();
//            transactionModelsTotalClose += fctm.getTotalClose();
//            transactionModelsUnits += abs(fctm.getUnits());
            totalUnits += fctm.getUnits();
            totalOpen += fctm.getTotalOpen();
            totalClose += fctm.getTotalClose();

            //want this to reflect the last time a position was opened
            dateOpen = dateOpen.compareTo(fctm.getDateOpen()) > 0 ? dateOpen : fctm.getDateOpen();

            //want this to reflect the last time a position was closed
            dateClose = dateClose.compareTo(fctm.getDateClose()) > 0 ? dateClose : fctm.getDateClose();
        }

        gain = totalOpen + totalClose;
        gainPct = 100.0 * gain / abs(totalOpen);

        pctm.setUnits(totalUnits);
        pctm.setTotalOpen(totalOpen);
        pctm.setTotalClose(totalClose);

        pctm.setGain(gain);
        pctm.setGainPct(gainPct);

        pctm.setTransactionType(pctm.getFifoClosedTransactionModels().get(0).getTransactionType());
        pctm.setPositionType(pctm.getFifoClosedTransactionModels().get(0).getPositionType());
        pctm.setEquityType(pctm.getFifoClosedTransactionModels().get(0).getEquityType());

        pctm.setFiTId(pctm.getFifoClosedTransactionModels().get(0).getFiTId() + "_y");
        pctm.setTicker(pctm.getFifoClosedTransactionModels().get(0).getTicker());
        pctm.setEquityId(pctm.getFifoClosedTransactionModels().get(0).getEquityId());

        pctm.setDateOpen(dateOpen);
        pctm.setDateClose(dateClose);

        pctm.setPriceOpen(totalOpen / (totalUnits * 100.0));
        pctm.setPriceClose(totalClose / (totalUnits * 100.0));

        pctm.setDateExpire(pctm.getFifoClosedTransactionModels().get(0).getDateExpire());

        pctm.setDays(pctm.getFifoClosedTransactionModels().get(0).getDays());

        pctm.setBComplete(false);

        pctm.setTransactionName(this.nameLongShortLeap(pctm));
    }

    /**
     * establish initial positions
     */
    public void doPctm2Pcm()
    {
        PositionClosedModel pcm;

        this.positionClosedModels.clear();

        for (int i = 0; i < this.positionClosedTransactionModels.size(); i++)
        {
            if (this.positionClosedTransactionModels.get(i).getBComplete())
            {
                //fctm already handled
                continue;
            }

            pcm = PositionClosedModel.builder()
                .positionId(-999)
                .dmAcctId(this.positionClosedTransactionModels.get(i).getDmAcctId())
                .joomlaId(this.userId)
                .build();

            //add first positionClosedTransaction to the positionClosedModel.positionClosedTransactionModels
            pcm.getPositionClosedTransactionModels().add(new PositionClosedTransactionModel(
                this.positionClosedTransactionModels.get(i)));

            //add positionClosedModel to the positionClosedModels array
            //using the previously built pcm
            this.positionClosedModels.add(pcm);

            //mark initial transaction complete
            this.positionClosedTransactionModels.get(i).setBComplete(true);

            this.addPctm2Pcm(i, pcm);

            //set attributes in the pcm
            this.doAttributesPctm2Pcm(pcm);
        }
    }

    private void addPctm2Pcm(Integer i, PositionClosedModel pcm)
    {
        Integer pcmStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        pcmStart = i;

        //loop the rest of the positionTransactionModels array for transactions
        //  to add to pcm positionCloseTransactionModels array
        for (int j = i + 1; j < this.positionClosedTransactionModels.size(); j++)
        {
            if (this.positionClosedTransactionModels.get(j).getBComplete())
            {
                //never hit
                continue;
            }

            if (!this.positionClosedTransactionModels.get(pcmStart).getTicker()
                .equals(this.positionClosedTransactionModels.get(j).getTicker()))
            {
                //not same ticker
                break;
            }

            if (!this.positionClosedTransactionModels.get(pcmStart).getDmAcctId()
                .equals(this.positionClosedTransactionModels.get(j).getDmAcctId()))
            {
                //not same DMAcctId
                break;
            }

            if (this.positionClosedTransactionModels.get(pcmStart).getDateOpen()
                .equals(this.positionClosedTransactionModels.get(pcmStart).getDateClose()))
            {
                //special case
                //open and close dates are the same; we aggregated these lots; now needs to be unique pcm
                break;
            }

            if (!this.positionClosedTransactionModels.get(pcmStart).getDateOpen()
                .equals(this.positionClosedTransactionModels.get(j).getDateOpen()))
            {
                //not same open date
                break;
            }
//              ok for transactionType to be different in pcm
//            if (!this.fifoClosedTransactionModels.get(pctmStart).getTransType().equalsIgnoreCase(
//                    this.fifoClosedTransactionModels.get(j).getTransType())) {
//                //not the same transaction type
//                break;
//            }

            //todo: for now, restrict to multi-legs that have the same number of units
            //there are legit position types that do not meet this but we will not handle them right now
            Double tUnits1 = abs(this.positionClosedTransactionModels.get(pcmStart).getUnits());
            Double tUnits2 = abs(this.positionClosedTransactionModels.get(j).getUnits());
            if (!tUnits1.equals(tUnits2))
            {
                //not same units
                break;
            }

            //j transaction should be part of the pctm
            pcm.getPositionClosedTransactionModels().add(new PositionClosedTransactionModel(
                this.positionClosedTransactionModels.get(j)));

            // mark it complete
            this.positionClosedTransactionModels.get(j).setBComplete(true);
        }
    }

    private void doAttributesPctm2Pcm(PositionClosedModel pcm)
    {
        Double totalUnits;
        Double totalOpen;
        Double totalClose;
        Double gain;
        Double gainPct;
        java.sql.Date dateClose;

        dateClose = new java.sql.Date(0);
        totalUnits = totalOpen = totalClose = 0.0;

        for (PositionClosedTransactionModel pctm : pcm.getPositionClosedTransactionModels())
        {
            totalUnits += pctm.getUnits();
            totalOpen += pctm.getTotalOpen();
            totalClose += pctm.getTotalClose();

            dateClose = dateClose.after(pctm.getDateClose()) ? dateClose : pctm.getDateClose();
        }

        gain = totalOpen + totalClose;
        gainPct = 100.0 * gain / Math.abs(totalOpen);

        //todo: we restricted multi-leg to have the same units each so just get the first one
        pcm.setUnits(Math.abs(pcm.getPositionClosedTransactionModels().get(0).getUnits()));

        pcm.setGain(gain);
        pcm.setGainPct(gainPct);
        pcm.setTotalOpen(totalOpen);
        pcm.setTotalClose(totalClose);

        pcm.setPositionType(pcm.getPositionClosedTransactionModels().get(0).getPositionType());
        pcm.setEquityType(pcm.getPositionClosedTransactionModels().get(0).getEquityType());
        //this is incorrect as transactions can be combination of selltoopen, buytoopen, etc.
//        pcm.setTransactionType(pcm.getPositionClosedTransactionModels().get(0).getTransactionType());
//          look at sign of totalOpen instead
        pcm.setTransactionType(pcm.getTotalOpen() > 0 ? "SHORT" : "LONG");

        pcm.setTicker(pcm.getPositionClosedTransactionModels().get(0).getTicker());
        //incorrect as there is no equityId for multi-leg; make all null to avoid confusion
//        pcm.setEquityId(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        pcm.setEquityId(null);

        pcm.setDateOpen(pcm.getPositionClosedTransactionModels().get(0).getDateOpen());
        pcm.setDateClose(dateClose);

        //cases where this results in division by 0
        //need to go deeper to fifoClosedTransactionModels to get average open/close price
        pcm.setPriceOpen(totalOpen / (pcm.getUnits() * 100.0));
        pcm.setPrice(totalClose / (pcm.getUnits() * 100.0));

        pcm.setDays(pcm.getPositionClosedTransactionModels().get(0).getDays());
    }

    /**
     * aggregating equal fctm to a pctm
     *
     * @param i
     * @param pctm
     */
    private void addFctm2Pctm(Integer i,
        PositionClosedTransactionModel pctm)
    {
        Integer pctmStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        pctmStart = i;

        //loop the rest of the fifoTransactionModels array for transactions
        //  to add to pctm fifoCloseTransactionModels array
        for (int j = i + 1; j < this.fifoClosedTransactionModels.size(); j++)
        {
            if (this.fifoClosedTransactionModels.get(j).getBComplete())
            {
                //should never hit
                continue;
            }
            if (!this.fifoClosedTransactionModels.get(pctmStart).getDmAcctId()
                .equals(this.fifoClosedTransactionModels.get(j).getDmAcctId()))
            {
                //not the same account
                break;
            }
            if (!this.fifoClosedTransactionModels.get(pctmStart).getEquityId()
                .equals(this.fifoClosedTransactionModels.get(j).getEquityId()))
            {
                //not the same equityId
                break;
            }

            if (this.fifoClosedTransactionModels.get(pctmStart).getDateOpen()
                .equals(this.fifoClosedTransactionModels.get(pctmStart).getDateClose())
                && !this.fifoClosedTransactionModels.get(j).getDateOpen()
                    .equals(this.fifoClosedTransactionModels.get(j).getDateClose()))
            {
                //special case where open and close date the same. need those to be distinct pctm
                //  but also aggregated when more than one lot
                //  otherwise, ignore closing date
                break;
            }

            if (!this.fifoClosedTransactionModels.get(pctmStart).getDateOpen()
                .equals(this.fifoClosedTransactionModels.get(j).getDateOpen()))
            {
                //not same open date
                break;
            }

            //j transaction should be part of the pctm
            pctm.getFifoClosedTransactionModels().add(new FIFOClosedTransactionModel(
                this.fifoClosedTransactionModels.get(j)));

            // mark it complete
            this.fifoClosedTransactionModels.get(j).setBComplete(true);
        }
    }

    /**
     * Use the positionClosedModel.positionClosedTransactionModels to establish
     * tacticId
     *
     * @param pcm
     */
    private void doFourLegs(PositionClosedModel pcm)
    {
        //condor (calls), condor (puts),
        //iron butterfly (calls), iron butterfly (puts),
        //iron condor (calls & puts), double diagonal

        Integer ret;
        Integer longPut;
        Integer shortPut;
        Integer longCall;
        Integer shortCall;
        Integer totalCall;
        Integer totalPut;
        Boolean bSameExpiry;
        Boolean bSameUnits;
        Boolean bCredit;
        Double dTotal;
        Double dMktVal;
        ArrayList<PositionClosedTransactionModel> putsList;
        ArrayList<PositionClosedTransactionModel> callsList;
        Iterator<PositionClosedTransactionModel> pctmIterator;
        PositionClosedTransactionModel tempPctm;
        Double totalClose;
        Double totalOpen;

        ret = PositionOpenModel.TACTICID_CUSTOM;
        longPut = shortPut = longCall = shortCall = 0;
        bSameExpiry = bSameUnits = bCredit = true;
        dTotal = dMktVal = 0.0;
        //units are positive for buy, negative for sell.

        putsList = new ArrayList<>();
        callsList = new ArrayList<>();

        //iterate the position closed transaction array
        pctmIterator = pcm.getPositionClosedTransactionModels().iterator();

        OCCclass occClassPctm0 = new OCCclass(pcm.getPositionClosedTransactionModels().get(0).getEquityId());
        while (pctmIterator.hasNext())
        {
            tempPctm = pctmIterator.next();
            OCCclass occClassPctm = new OCCclass(tempPctm.getEquityId());

            if (occClassPctm.getDtExpiry().equals(occClassPctm0.getDtExpiry()))
            {
                //same expiry
                if (occClassPctm.getPutcall().equalsIgnoreCase("p"))
                {
                    //same expiry
                    //put
                    putsList.add(tempPctm);

                    if (tempPctm.getTransactionType().equalsIgnoreCase("buytoopen"))
                    {
                        //same expiry
                        //put
                        //long
                        longPut += 1;
                    } else
                    {
                        //same expiry
                        //put
                        //short
                        shortPut += 1;
                    }
                } else
                {
                    //same expiry
                    //call
                    callsList.add(tempPctm);
                    if (tempPctm.getTransactionType().equalsIgnoreCase("buytoopen"))
                    {
                        //same expiry
                        //call
                        //long
                        longCall += 1;
                    } else
                    {
                        //same expiry
                        //call
                        //short
                        shortCall += 1;
                    }
                }

                Double d1;
                Double d2;

                d1 = Math.abs(tempPctm.getUnits());
                d2 = Math.abs(pcm.getPositionClosedTransactionModels().get(0).getUnits());

                bSameUnits = d1.equals(d2);
            } else
            {
                //different expiry
                //todo: break up into individual positions
                //  or a pair of calls and a pair of puts
                bSameExpiry = false;
            }

            dTotal += tempPctm.getTotalOpen();
            dMktVal += tempPctm.getTotalClose();
        }

        //todo: separate into positions
        if (!bSameExpiry)
        {
            pcm.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
            return;
        }
        if (!bSameUnits)
        {
            pcm.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
            return;
        }

        //completed run through legs; not using
        bCredit = dTotal > 0;

        //now, what tactic is it?
        totalCall = longCall + shortCall;
        totalPut = longPut + shortPut;

        if (totalPut.equals(0))
        {
            //all calls
            //todo: all different strikes
            //condor (calls)
            ret = PositionOpenModel.TACTICID_CONDOR;
        }

        if (totalCall.equals(0))
        {
            //all puts
            //todo: all different strikes
            //condor (puts)
            ret = PositionOpenModel.TACTICID_CONDOR;
        }

        if (longPut.equals(shortPut) && longPut.equals(longCall) && longPut.equals(shortCall))
        {
            //equal parts longPut, shortPut, longCall, shortCall
            //todo: all different strikes
            //iron condor
            ret = PositionOpenModel.TACTICID_IRONCONDOR;
        }

        pcm.setTacticId(ret);

        pcm.setPositionType(bCredit ? "SHORT" : "LONG");

        //set the rest of the positionModel attributes
        totalOpen = totalClose = 0.0;
        for (PositionClosedTransactionModel pctm : pcm.getPositionClosedTransactionModels())
        {
            totalOpen += pctm.getTotalOpen();
            totalClose += pctm.getTotalClose();
        }

        pcm.setUnits(pcm.getPositionClosedTransactionModels().get(0).getUnits());

        pcm.setPriceOpen(totalOpen / ((pcm.getUnits() * 100.0)));
        pcm.setPrice(totalClose / ((pcm.getUnits() * 100.0)));

        pcm.setGain((totalOpen + totalClose));
        pcm.setGainPct(100.0 * pcm.getGain() / Math.abs(totalOpen));

        pcm.setPositionType(totalOpen > 0.0 ? "SHORT" : "LONG");

        //latest close date should be the last one?
        pcm.setDateClose(pcm.getPositionClosedTransactionModels().get(2).getDateClose());
    }

    /**
     * Given a 2 leg position, find the tacticId and adjust the position
     * attributes
     *
     * @param aggregatedPositionClosedTransactionModels the transactions of the
     * position aggregated
     * @param pcm the position
     */
    private void doTwoLegs(PositionClosedModel pcm)
    {
        /**
         * vertical straddle strangle collar calendar diagonal
         */
        Integer ret;
        PositionClosedTransactionModel pctm1;
        PositionClosedTransactionModel pctm2;
        OCCclass occTrans1;
        OCCclass occTrans2;
        Double units;
        Double totalOpen;
        Double totalClose;

        pctm1 = pcm.getPositionClosedTransactionModels().get(0);
        pctm2 = pcm.getPositionClosedTransactionModels().get(1);

        occTrans1 = new OCCclass(pctm1.getEquityId());
        occTrans2 = new OCCclass(pctm2.getEquityId());

        if (occTrans1.getPutcall().equalsIgnoreCase(occTrans2.getPutcall()))
        {
            //all are either put or call
            //vertical, calendar, diagonal
            if (occTrans1.getDtExpiry().equals(occTrans2.getDtExpiry()))
            {
                //all are either put or call
                //all expiries are the same
                if (pctm1.getTransactionType().equals(pctm2.getTransactionType()))
                {
                    //all are either put or call
                    //all expiries are the same
                    //all same transType (buytoopen ...)
                    //hit
                    ret = PositionOpenModel.TACTICID_CUSTOM;
                } else
                {
                    //all are either put or call
                    //all expiries are the same
                    //all different transType (buytoopen ...)
                    //vertical, collar, calendar, diagonal
                    if (occTrans1.getDStrike().equals(occTrans2.getDStrike()))
                    {
                        //all are either put or call
                        //all expiries are the same
                        //all different transType (buytoopen ...)
                        //all strikes the same
                        //not hit
                        ret = PositionOpenModel.TACTICID_CUSTOM;
                    } else
                    {
                        //all are either put or call
                        //all expiries are the same
                        //all different transType (buytoopen ...)
                        //hit
                        Double double1, double2;
                        double1 = Math.abs(pcm.getPositionClosedTransactionModels().get(0).getUnits());
                        double2 = Math.abs(pcm.getPositionClosedTransactionModels().get(1).getUnits());
                        ret = double1.equals(double2)
                            ? PositionOpenModel.TACTICID_VERTICAL
                            : PositionOpenModel.TACTICID_VERTICAL_CUSTOM;
                    }
                }
            } else
            {
                //all are either put or call
                //all expiries are not the same
                //calendar, diagonal
                if (occTrans1.getDStrike().equals(occTrans2.getDStrike()))
                {
                    //all are either put or call
                    //all expiries are not the same
                    //all strikes are the same
                    //hit
                    if (pcm.getPositionClosedTransactionModels().get(0).getUnits()
                        .equals(pcm.getPositionClosedTransactionModels().get(1).getUnits()))
                    {
                        ret = PositionOpenModel.TACTICID_CALENDAR;
                    } else
                    {
                        ret = PositionOpenModel.TACTICID_CALENDAR_CUSTOM;
                    }
                } else
                {
                    //all are either put or call
                    //all expiries are not the same
                    //all strikes are not the same
                    //hit
                    if (pctm1.getTransactionType().equals(pctm2.getTransactionType()))
                    {
                        //all are either put or call
                        //all expiries are not the same
                        //all strikes are not the same

                        //all transType (buytoopen ...) same
                        ret = PositionOpenModel.TACTICID_CUSTOM;
                    } else
                    {
                        //all are either put or call
                        //all expiries are not the same
                        //all strikes are not the same
                        //all transType(buytoopen ...) not the same
                        if (pcm.getPositionClosedTransactionModels().get(0).getUnits()
                            .equals(pcm.getPositionClosedTransactionModels().get(1).getUnits()))
                        {
                            ret = PositionOpenModel.TACTICID_DIAGONAL;
                        } else
                        {
                            ret = PositionOpenModel.TACTICID_DIAGONAL_CUSTOM;
                        }
                    }
                }
            }
        } else
        {
            //have puts and calls
            //straddle, strangle, collar
            if (occTrans1.getDtExpiry().equals(occTrans2.getDtExpiry()))
            {
                //not all calls or puts
                //all expiries are the same
                if (pctm1.getTransactionType().equalsIgnoreCase(pctm2.getTransactionType()))
                {
                    //not all calls or puts
                    //all expiries are the same
                    //all same transType (buytoopen ...)
                    //straddle, strangle
                    if (occTrans1.getDStrike().equals(occTrans2.getDStrike()))
                    {
                        //not all calls or puts
                        //all expiries are the same
                        //all same transType (buytoopen ...)
                        //all strikes the same
                        //not hit
                        if (pcm.getPositionClosedTransactionModels().get(0).getUnits()
                            .equals(pcm.getPositionClosedTransactionModels().get(1).getUnits()))
                        {
                            ret = PositionOpenModel.TACTICID_STRADDLE;
                        } else
                        {
                            ret = PositionOpenModel.TACTICID_STRADDLE_CUSTOM;
                        }
                    } else
                    {
                        //not all calls or puts
                        //all expiries are the same
                        //all same transType (buytoopen ...)
                        //all strikes are not the same
                        //hit
                        if (pcm.getPositionClosedTransactionModels().get(0).getUnits()
                            .equals(pcm.getPositionClosedTransactionModels().get(1).getUnits()))
                        {
                            ret = PositionOpenModel.TACTICID_STRANGLE;
                        } else
                        {
                            ret = PositionOpenModel.TACTICID_STRANGLE_CUSTOM;
                        }

                    }
                } else
                {
                    //not all calls or puts
                    //all expiries are the same
                    //all not same transType (buytoopen ...)
                    //not hit
                    if (pcm.getPositionClosedTransactionModels().get(0).getUnits()
                        .equals(pcm.getPositionClosedTransactionModels().get(1).getUnits()))
                    {
                        ret = PositionOpenModel.TACTICID_COLLAR;
                    } else
                    {
                        ret = PositionOpenModel.TACTICID_COLLAR_CUSTOM;
                    }
                }
            } else
            {
                //not all calls or puts
                //all expiries are not the same
                //hit
                ret = PositionOpenModel.TACTICID_CUSTOM;
            }
        }

        pcm.setTacticId(ret);

        //set the rest of the positionModel attributes
        pcm.setUnits(Double.min(pcm.getPositionClosedTransactionModels().get(0).getUnits(),
            pcm.getPositionClosedTransactionModels().get(1).getUnits()));

        totalOpen = totalClose = 0.0;
        for (PositionClosedTransactionModel pctm : pcm.getPositionClosedTransactionModels())
        {
            totalOpen += pctm.getTotalOpen();
            totalClose += pctm.getTotalClose();
        }

        pcm.setUnits(pcm.getPositionClosedTransactionModels().get(0).getUnits());

        pcm.setPriceOpen(totalOpen / ((pcm.getUnits() * 100.0)));
        pcm.setPrice(totalClose / ((pcm.getUnits() * 100.0)));

        pcm.setGain((totalOpen + totalClose));
        pcm.setGainPct(100.0 * pcm.getGain() / Math.abs(totalOpen));

        pcm.setPositionType(totalOpen > 0.0 ? "SHORT" : "LONG");

        pcm.setDateOpen(pcm.getPositionClosedTransactionModels().get(0).getDateOpen());

        if (pcm.getPositionClosedTransactionModels().get(0).getDateClose()
            .after(pcm.getPositionClosedTransactionModels().get(1).getDateClose()))
        {
            pcm.setDateClose(pcm.getPositionClosedTransactionModels().get(0).getDateClose());
        } else
        {
            pcm.setDateClose(pcm.getPositionClosedTransactionModels().get(1).getDateClose());
        }
    }

    /**
     * handle positions with one leg
     *
     * @param pcm
     */
    private void doOneLeg(PositionClosedModel pcm)
    {
        Double totalOpen;
        Double totalClose;

        if (pcm.getPositionClosedTransactionModels().get(0).getTransactionType().equalsIgnoreCase("buytoopen"))
        {
            pcm.setTacticId(PositionOpenModel.TACTICID_LONG);
        }

        if (pcm.getPositionClosedTransactionModels().get(0).getTransactionType().equalsIgnoreCase("selltoopen"))
        {
            pcm.setTacticId(PositionOpenModel.TACTICID_SHORT);
        }

        totalOpen = totalClose = 0.0;
        for (PositionClosedTransactionModel pctm : pcm.getPositionClosedTransactionModels())
        {
            totalOpen += pctm.getTotalOpen();
            totalClose += pctm.getTotalClose();
        }

        pcm.setUnits(pcm.getPositionClosedTransactionModels().get(0).getUnits());

        pcm.setPriceOpen(totalOpen / (pcm.getUnits() * 100.0));
        pcm.setPrice(totalClose / (pcm.getUnits() * 100.0));

        pcm.setGain((totalOpen + totalClose));
        pcm.setGainPct(100.0 * pcm.getGain() / Math.abs(totalOpen));

        pcm.setPositionType(totalOpen > 0.0 ? "SHORT" : "LONG");

        pcm.setDateOpen(pcm.getPositionClosedTransactionModels().get(0).getDateOpen());

        pcm.setDateClose(pcm.getPositionClosedTransactionModels().get(0).getDateClose());
    }

    /**
     * determine tacticId for each position
     */
    public void doPositionsTacticId()
    {
        PositionClosedModel pcmTemp;

        pcmAddList.clear();

        for (PositionClosedModel pcm : this.positionClosedModels)
        {

            switch (pcm.getPositionClosedTransactionModels()
                .size())
            {
                case 1:
                    this.doOneLeg(pcm);
                    break;
                case 2:
                    this.doTwoLegs(pcm);
                    //handle custom result
                    if (pcm.getTacticId() == PositionOpenModel.TACTICID_CUSTOM)
                    {
                        pcmTemp = new PositionClosedModel(pcm);

                        pcmTemp.getPositionClosedTransactionModels().remove(0);
                        pcmTemp.setPositionType(pcmTemp.getPositionClosedTransactionModels()
                            .get(0).getTransactionType().equalsIgnoreCase("buytoopen") ? "LONG" : "SHORT");
                        pcmTemp.setTacticId(pcmTemp.getPositionClosedTransactionModels()
                            .get(0).getTransactionType().equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);

                        pcm.getPositionClosedTransactionModels().remove(1);
                        pcm.setPositionType(pcm.getPositionClosedTransactionModels().get(0).getTransactionType()
                            .equalsIgnoreCase("buytoopen") ? "LONG" : "SHORT");
                        pcm.setTacticId(pcm.getPositionClosedTransactionModels().get(0).getTransactionType()
                            .equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);

                        this.doAttributesPctm2Pcm(pcmTemp);
                        this.doAttributesPctm2Pcm(pcm);

                        pcmAddList.add(pcmTemp);
                    }

                    break;
                case 3:
                    //so far, no named 3 legged positions
                    //could be a spurious set of transactions on the same day
                    //otherwise, just separate into unique positions
                    //hard route: look for 2 leg positions
                    //  0,1; 1,2; 0,2
                    pcmTemp = new PositionClosedModel(pcm);
                    //test 0, 1
                    pcmTemp.getPositionClosedTransactionModels().remove(2);

                    this.doTwoLegs(pcmTemp);

                    if (!pcmTemp.getTacticId().equals(PositionOpenModel.TACTICID_CUSTOM))
                    {
                        //0, 1 is a position
                        //add to addList
                        //not tested
                        this.doAttributesPctm2Pcm(pcmTemp);

                        pcmAddList.add(pcmTemp);
                        this.doTwoLegs(pcmTemp);

                        //edit pcm to remove 0, 1 and leave as a one leg position; fix attributes
                        pcm.getPositionClosedTransactionModels().remove(0);
                        pcm.getPositionClosedTransactionModels().remove(0);

                        this.doAttributesPctm2Pcm(pcm);

                        this.doOneLeg(pcm);
                        break;
                    }

                    pcmTemp = new PositionClosedModel(pcm);
                    //test 1, 2
                    pcmTemp.getPositionClosedTransactionModels()
                        .remove(0);

                    this.doTwoLegs(pcmTemp);

                    if (!pcmTemp.getTacticId()
                        .equals(PositionOpenModel.TACTICID_CUSTOM))
                    {
                        //1, 2 is a position
                        //add to addList
                        pcmAddList.add(pcmTemp);

                        this.doAttributesPctm2Pcm(pcmTemp);

                        this.doTwoLegs(pcmTemp);

                        //edit pcm to remove 1, 2 and leave as a one leg position; fix attributes
                        pcm.getPositionClosedTransactionModels().remove(1);
                        pcm.getPositionClosedTransactionModels().remove(1);

                        this.doAttributesPctm2Pcm(pcm);

                        this.doOneLeg(pcm);
                        break;
                    }

                    pcmTemp = new PositionClosedModel(pcm);
                    //test 0,2
                    pcmTemp.getPositionClosedTransactionModels().remove(1);
                    this.doTwoLegs(pcmTemp);

                    if (!pcmTemp.getTacticId().equals(PositionOpenModel.TACTICID_CUSTOM))
                    {
                        //0, 2 is a position
                        //add to addList
                        //not tested
                        this.doAttributesPctm2Pcm(pcmTemp);

                        pcmAddList.add(pcmTemp);
                        this.doTwoLegs(pcmTemp);

                        //edit pcm to remove 2,3 and leave as a one leg position; fix attributes
                        pcm.getPositionClosedTransactionModels().remove(0);
                        pcm.getPositionClosedTransactionModels().remove(1);

                        this.doAttributesPctm2Pcm(pcm);

                        this.doOneLeg(pcm);
                        break;
                    }

                    //if none of the hard route pan out, go the easy route
                    //easy route
                    pcmTemp = new PositionClosedModel(pcm);
                    pcmTemp.getPositionClosedTransactionModels().remove(1);
                    pcmTemp.getPositionClosedTransactionModels().remove(1);
                    //only one pctm in the pcm
                    //set the pctm equityId to the fctm equityId
                    pcmTemp.getPositionClosedTransactionModels()
                        .get(0).setEquityId(pcmTemp.getPositionClosedTransactionModels()
                        .get(0).getFifoClosedTransactionModels().get(0).getEquityId());

                    pcmTemp.setTacticId(pcmTemp.getPositionClosedTransactionModels()
                        .get(0).getTransactionType().equalsIgnoreCase("buytoopen")
                        ? PositionOpenModel.TACTICID_LONG
                        : PositionOpenModel.TACTICID_SHORT);
                    pcmAddList.add(pcmTemp);

                    pcmTemp = new PositionClosedModel(pcm);
                    pcmTemp.getPositionClosedTransactionModels().remove(0);
                    pcmTemp.getPositionClosedTransactionModels().remove(1);

                    this.doAttributesPctm2Pcm(pcmTemp);

                    pcmTemp.setTacticId(pcmTemp.getPositionClosedTransactionModels()
                        .get(0).getTransactionType().equalsIgnoreCase("buytoopen")
                        ? PositionOpenModel.TACTICID_LONG
                        : PositionOpenModel.TACTICID_SHORT);
                    pcmAddList.add(pcmTemp);

                    pcm.getPositionClosedTransactionModels().remove(0);
                    pcm.getPositionClosedTransactionModels().remove(0);

                    this.doAttributesPctm2Pcm(pcm);

                    pcm.setTacticId(pcm.getPositionClosedTransactionModels()
                        .get(0).getTransactionType().equalsIgnoreCase("buytoopen")
                        ? PositionOpenModel.TACTICID_LONG
                        : PositionOpenModel.TACTICID_SHORT);
                    break;
                case 4:
                    this.doFourLegs(pcm);
                    if (pcm.getTacticId().equals(PositionOpenModel.TACTICID_CUSTOM))
                    {
                        int i = 0;
                    }
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    pcm.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
                    break;
                default:
            }
        }

        for (PositionClosedModel pcm : pcmAddList)
        {
            //add any new positions to the array
            this.positionClosedModels.add(pcm);
        }
    }

    private void getTransactions(String equityType)
    {
        this.fifoClosedTransactionModels.clear();

        //get transactions for the joomlaId
        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(String.format(
                FIFOClosedTransactionModel.SELECT_INCOMPLETE_BY_JOOMLAID_EQUITYTYPE, this.userId, equityType));
            ResultSet rs = pStmt.executeQuery();)
        {

            while (rs.next())
            {
                this.fifoClosedTransactionModels.add(new FIFOClosedTransactionModel(
                    rs.getInt("DMAcctId"),
                    rs.getInt("JoomlaId"),
                    rs.getString("FiTId"),
                    rs.getInt("TransactionGrp"),
                    rs.getString("Ticker"),
                    rs.getString("EquityId"),
                    rs.getString("TransactionName"),
                    rs.getDate("DateOpen"),
                    rs.getDate("DateClose"),
                    rs.getDate("DateExpire"),
                    rs.getDouble("Units"),
                    rs.getDouble("PriceOpen"),
                    rs.getDouble("PriceClose"),
                    rs.getDouble("TotalOpen"),
                    rs.getDouble("TotalClose"),
                    rs.getDouble("Gain"),
                    rs.getDouble("GainPct"),
                    rs.getString("EquityType"),
                    rs.getString("PositionType"),
                    rs.getString("TransactionType"),
                    rs.getInt("Complete"),
                    rs.getInt("Days"),
                    rs.getInt("Complete") == 1));
            }
        } catch (SQLException ex)
        {
            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }
}
