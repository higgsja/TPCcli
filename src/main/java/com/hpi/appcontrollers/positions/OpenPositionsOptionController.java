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
 * Handle all open option transactions, converting to positions
 */
public class OpenPositionsOptionController
{

    private final Integer userId;
    @Getter
    private final ArrayList<FIFOOpenTransactionModel> fifoOpenTransactionModels;
    @Getter
    private final ArrayList<PositionOpenModel> positionOpenModels;
    @Getter
    private final ArrayList<PositionOpenTransactionModel> positionOpenTransactionModels;
    private final ArrayList<PositionOpenModel> pomAddList;
    private final String[] months;

    /*
     * Singleton
     * process closed Options into positions: one element in processing positions
     *
     */
    private static OpenPositionsOptionController instance;

    static
    {
        OpenPositionsOptionController.instance = null;
    }

    protected OpenPositionsOptionController()
    {
        this.months = new DateFormatSymbols().getShortMonths();
        // protected prevents instantiation outside of package
        this.userId = CMDBModel.getUserId();
        this.fifoOpenTransactionModels = new ArrayList<>();
        this.positionOpenTransactionModels = new ArrayList<>();
        this.positionOpenModels = new ArrayList<>();
        this.pomAddList = new ArrayList<>();
    }

    public synchronized static OpenPositionsOptionController getInstance()
    {
        if (OpenPositionsOptionController.instance == null)
        {
            OpenPositionsOptionController.instance = new OpenPositionsOptionController();
        }
        return OpenPositionsOptionController.instance;
    }

    /**
     * refresh positions from existing data
     */
    public void doOpenPositions()
    {
        //get closed transactions
        this.getTransactions("option");

        this.doFotm2Potm();

        this.doPotm2Pom();

        this.doPositionsTacticId();

        this.doPositionName();

        //push to the database
        this.doSQL();

        //mark all fifoClosedTransactions complete in the database
        this.setFifoOpenTransactionsComplete();
    }

    /**
     * push the positions to the database
     */
    public void doSQL()
    {
        this.positionOpenModels.forEach(pom ->
        {
            Integer positionId;

            //add the positionClosedModel to positionsOpen table
            positionId = this.insertPositionSQL(pom);

            pom.setPositionId(positionId);

            //add the array of transactions to positionsPositionTransactions table
            this.insertPositionOpenTransactionsTableSQL(positionId, pom);
        });
    }

    private void insertPositionOpenTransactionsTableSQL(Integer positionId,
            PositionOpenModel pom)
    {
        String sql;

        for (PositionOpenTransactionModel potm : pom.
                getPositionOpenTransactionModels())
        {
            sql = String.format(
                    PositionOpenTransactionModel.POSITION_TRANSACTION_INSERT,
                    potm.getDmAcctId(),
                    potm.getJoomlaId(),
                    positionId,
                    potm.getFiTId(),
                    //                potm.getEquityId(),   //do not set here as multi-leg positions have no equityId
                    potm.getTransactionName(),
                    potm.getTicker(),
                    potm.getDateOpen(),
                    potm.getUnits(),
                    potm.getPriceOpen(),
                    potm.getDateExpire(),
                    potm.getDays(),
                    potm.getPositionType(),
                    potm.getTotalOpen(),
                    potm.getEquityType(),
                    potm.getGain(),
                    potm.getGainPct(),
                    potm.getTransactionType(),
                    0,
                    potm.getMktVal(),
                    potm.getLMktVal(),
                    potm.getActPct());
            
            //todo: case where there is an open transaction with 0 units
            // causes an Infinity condition. It is ignored here and the 
            // transaction removed from consideration.
            if (!Double.isInfinite(potm.getPriceOpen()))
            {
                CMDBController.executeSQL(sql);
            }
        }
    }

    private void setFifoOpenTransactionsComplete()
    {
        String sql;

        for (FIFOOpenTransactionModel fctm : this.fifoOpenTransactionModels)
        {
            sql = String.format(FIFOOpenTransactionModel.UPDATE_COMPLETE,
                    "FIFOOpenTransactions",
                    FIFOOpenTransactionModel.COMPLETE,
                    fctm.getDmAcctId(),
                    fctm.getJoomlaId(),
                    fctm.getFiTId());

            CMDBController.executeSQL(sql);
        }
    }

    private Integer insertPositionSQL(PositionOpenModel pom)
    {
        String sInsertSQL;

        sInsertSQL = String.format(PositionOpenModel.POSITION_INSERT3,
                pom.getDmAcctId(),
                pom.getJoomlaId(),
                pom.getTicker(),
                pom.getEquityId(),
                pom.getPositionName(),
                pom.getTacticId(),
                pom.getUnits(),
                pom.getPriceOpen(),
                pom.getPrice(),
                pom.getGainPct(),
                pom.getDateOpen(),
                pom.getDays(),
                pom.getGain(),
                pom.getPositionType(),
                pom.getTotalOpen(),
                pom.getTotalClose(),
                pom.getEquityType(),
                pom.getTransactionType(),
                pom.getMktVal());

        return CMDBController.insertAutoRow(sInsertSQL);
    }

    /**
     * create and update the position name in all positions
     */
    public void doPositionName()
    {
        for (PositionOpenModel pom : this.positionOpenModels)
        {

            switch (pom.getTacticId())
            {
                case PositionOpenModel.TACTICID_CUSTOM:
                    pom.setPositionName("Custom");
                    break;
                case PositionOpenModel.TACTICID_LONG:
                case PositionOpenModel.TACTICID_SHORT:
                case PositionOpenModel.TACTICID_LEAP:
                    pom.setPositionName(this.nameLongShortLeap(pom));
                    break;
                case PositionOpenModel.TACTICID_VERTICAL:
                    pom.setPositionName(this.nameVertical(pom,
                            false));
                    break;
                case PositionOpenModel.TACTICID_STRANGLE:
                    pom.setPositionName(this.nameStrangle(pom,
                            false));
                    break;
                case PositionOpenModel.TACTICID_CALENDAR:
                    pom.setPositionName(this.nameCalendar(pom,
                            false));
                    break;
                case PositionOpenModel.TACTICID_COVERED:
                    pom.setPositionName(this.nameCovered(pom));
                    break;
                case PositionOpenModel.TACTICID_STRADDLE:
                    pom.setPositionName(this.nameStraddle(pom,
                            false));
                    break;
                case PositionOpenModel.TACTICID_IRONCONDOR:
                    pom.setPositionName(this.nameIronCondor(pom));
                    break;
                case PositionOpenModel.TACTICID_BUTTERFLY:
                    pom.setPositionName(this.nameButterfly(pom));
                    break;
                case PositionOpenModel.TACTICID_CONDOR:
                    pom.setPositionName(this.nameCondor(pom));
                    break;
                case PositionOpenModel.TACTICID_COLLAR:
                    pom.setPositionName(this.nameCollar(pom,
                            false));
                    break;
                case PositionOpenModel.TACTICID_DIAGONAL:
                    pom.setPositionName(this.nameDiagonal(pom,
                            false));
                    break;
                case PositionOpenModel.TACTICID_VERTICAL_CUSTOM:
                    pom.setPositionName(this.nameVertical(pom,
                            true));
                    break;
                case PositionOpenModel.TACTICID_STRANGLE_CUSTOM:
                    pom.setPositionName(this.nameStrangle(pom,
                            true));
                    break;
                case PositionOpenModel.TACTICID_CALENDAR_CUSTOM:
                    pom.setPositionName(this.nameCalendar(pom,
                            true));
                    break;
                case PositionOpenModel.TACTICID_STRADDLE_CUSTOM:
                    pom.setPositionName(this.nameStraddle(pom,
                            true));
                    break;
                case PositionOpenModel.TACTICID_COLLAR_CUSTOM:
                    pom.setPositionName(this.nameCollar(pom,
                            true));
                    break;
                case PositionOpenModel.TACTICID_DIAGONAL_CUSTOM:
                    pom.setPositionName(this.nameDiagonal(pom,
                            true));
                    break;
                default:
                    int i = 0;
            }
        }
    }

    private String nameLongShortLeap(PositionOpenModel pom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels()
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

    private String nameLongShortLeap(PositionOpenTransactionModel potm)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;

        closedClass0 = new OCCclass(potm.getEquityId());

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

    private String nameVertical(PositionOpenModel pom,
            Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels()
                .get(0)
                .getEquityId());

        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels()
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
        positionName += closedClass0.getPutcall().equalsIgnoreCase("c") ? "Call" : "Put";
        positionName += " Vrtcl";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    private String nameStrangle(PositionOpenModel pom,
            Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(1).getEquityId());

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

    private String nameCalendar(PositionOpenModel pom,
            Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(1).getEquityId());

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

    private String nameCovered(PositionOpenModel pom)
    {
        return "Covered";
    }

    private String nameStraddle(PositionOpenModel pom,
            Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(1).getEquityId());

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

    private String nameIronCondor(PositionOpenModel pom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(1).getEquityId());
        closedClass2 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(2).getEquityId());
        closedClass3 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(3).getEquityId());

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

    private String nameButterfly(PositionOpenModel pom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(1).getEquityId());
        closedClass2 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(2).getEquityId());
        closedClass3 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(3).getEquityId());

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

    private String nameCondor(PositionOpenModel pom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(1).getEquityId());
        closedClass2 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(2).getEquityId());
        closedClass3 = new OCCclass(pom.getPositionOpenTransactionModels().
                get(3).getEquityId());

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

    private String nameCollar(PositionOpenModel pcm,
            Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionOpenTransactionModels().
                get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionOpenTransactionModels().
                get(1).getEquityId());

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

    private String nameDiagonal(PositionOpenModel pcm,
            Boolean bCustom)
    {
        String positionName;
        Integer month;
        OCCclass class0;
        OCCclass class1;

        class0 = new OCCclass(pcm.getPositionOpenTransactionModels().get(0).
                getEquityId());
        class1 = new OCCclass(pcm.getPositionOpenTransactionModels().get(1).
                getEquityId());

        //aapl 129 ddJanYY/130 ddFebYY CALL Diagonal
        positionName = class0.getTicker();
        positionName += " ";
        positionName += class0.getStrike();
        positionName += " ";
        positionName += class0.getExpDay();
        month = NumberUtils.toInt(class0.getExpMonth());
        positionName += months[month - 1];
        positionName += class0.getExpYear();
        positionName += "/";
        positionName += class1.getExpDay();
        month = NumberUtils.toInt(class1.getExpMonth());
        positionName += months[month - 1];
        positionName += class1.getExpYear();
        positionName += " ";
        positionName += class0.getPutcall();
        positionName += " Diagnl";
        if (bCustom)
        {
            positionName += " Cstm";
        }

        return positionName;
    }

    /**
     * aggregate lots into positionOpenTransactionModel
     */
    public void doFotm2Potm()
    {
        PositionOpenTransactionModel potm;

        this.positionOpenTransactionModels.clear();

        for (int i = 0; i < this.fifoOpenTransactionModels.size(); i++)
        {
            if (this.fifoOpenTransactionModels.get(i)
                    .getBComplete())
            {
                //fotm already handled
                continue;
            }

            potm = PositionOpenTransactionModel.builder()
                    .dmAcctId(this.fifoOpenTransactionModels.get(i).
                            getDmAcctId())
                    .joomlaId(this.userId)
                    .positionId(-999)
                    .build();

            //add first fifoOpenTransaction to the positionOpenTransactionModel.fifoOpenTransactionModels
            potm.getFifoOpenTransactionModels()
                    .add(new FIFOOpenTransactionModel(
                            this.fifoOpenTransactionModels.get(i)));

            //add positionOpenTransactionModel to the positionOpenTransactionModel array
            //using the previously built potm
            this.positionOpenTransactionModels.add(potm);

            //mark initial transaction complete
            this.fifoOpenTransactionModels.get(i).setBComplete(true);

            this.addFotm2Potm(i, potm);

            //set attributes in the pctm
            this.doAttributesPotm(potm);
        }
    }

    private void doAttributesPotm(PositionOpenTransactionModel potm)
    {
        Double totalUnits;
        Double totalOpen;
        Double totalMktVal;
        Double gain;
        Double gainPct;
        java.sql.Date dateOpen;

        dateOpen = new java.sql.Date(0);

        totalUnits = totalOpen = totalMktVal = 0.0;

        for (FIFOOpenTransactionModel fotm : potm.getFifoOpenTransactionModels())
        {
            totalUnits += fotm.getUnits();
            totalOpen += fotm.getTotalOpen();
            totalMktVal += fotm.getMktVal();

            //want this to reflect the last date of an opening transaction
            dateOpen = dateOpen.compareTo(fotm.getDateOpen()) > 0 ? dateOpen : fotm.
                    getDateOpen();
        }

        gain = totalOpen + totalMktVal;
        gainPct = 100.0 * gain / abs(totalOpen);

        potm.setUnits(totalUnits);
        potm.setTotalOpen(totalOpen);

        potm.setMktVal(totalMktVal);

        potm.setGain(gain);
        potm.setGainPct(gainPct);

        potm.setTransactionType(potm.getFifoOpenTransactionModels().get(0).
                getTransactionType());
        potm.setPositionType(potm.getFifoOpenTransactionModels().get(0).
                getPositionType());
        potm.setEquityType(potm.getFifoOpenTransactionModels().get(0).
                getEquityType());

        potm.setFiTId(
                potm.getFifoOpenTransactionModels().get(0).getFiTId() + "_x");
        potm.setTicker(potm.getFifoOpenTransactionModels().get(0).getTicker());
        potm.setEquityId(potm.getFifoOpenTransactionModels().get(0).
                getEquityId());

        potm.setDateOpen(dateOpen);

        potm.setPriceOpen(abs(totalOpen) / (totalUnits * 100.0));

        potm.setDateExpire(potm.getFifoOpenTransactionModels().get(0).
                getDateExpire());

        potm.setDays(potm.getFifoOpenTransactionModels().get(0).getDays());

        potm.setBComplete(false);

        potm.setTransactionName(this.nameLongShortLeap(potm));
    }

    /**
     * establish initial positions
     */
    public void doPotm2Pom()
    {
        PositionOpenModel pom;

        this.positionOpenModels.clear();

        for (int i = 0; i < this.positionOpenTransactionModels.size(); i++)
        {
            if (this.positionOpenTransactionModels.get(i)
                    .getBComplete())
            {
                //fctm already handled
                continue;
            }

            pom = PositionOpenModel.builder()
                    .positionId(-999)
                    .dmAcctId(this.positionOpenTransactionModels.get(i).
                            getDmAcctId())
                    .joomlaId(this.userId)
                    .build();

            //add first positionOpenTransaction to the positionOpenModel.positionOpenTransactionModels
            pom.getPositionOpenTransactionModels()
                    .add(new PositionOpenTransactionModel(
                            this.positionOpenTransactionModels.get(i)));

            //add positionOpenModel to the positionOpenModels array
            //using the previously built pcm
            this.positionOpenModels.add(pom);

            //mark initial transaction complete
            this.positionOpenTransactionModels.get(i)
                    .setBComplete(true);

            this.addPotm2Pom(i,
                    pom);

            //set attributes in the pcm
            this.doAttributesPotm2Pom(pom);
        }
    }

    private void addPotm2Pom(Integer i,
            PositionOpenModel pom)
    {
        Integer pomStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        pomStart = i;

        //loop the rest of the positionTransactionModels array for transactions
        //  to add to pcm positionCloseTransactionModels array
        for (int j = i + 1; j < this.positionOpenTransactionModels.size(); j++)
        {
            if (this.positionOpenTransactionModels.get(j)
                    .getBComplete())
            {
                //never hit
                continue;
            }

            if (!this.positionOpenTransactionModels.get(pomStart)
                    .getTicker()
                    .equals(this.positionOpenTransactionModels.get(j)
                            .getTicker()))
            {
                //not same ticker
                break;
            }

            if (!this.positionOpenTransactionModels.get(pomStart)
                    .getDmAcctId()
                    .equals(this.positionOpenTransactionModels.get(j)
                            .getDmAcctId()))
            {
                //not same DMAcctId
                break;
            }

            if (!this.positionOpenTransactionModels.get(pomStart)
                    .getDateOpen()
                    .equals(this.positionOpenTransactionModels.get(j)
                            .getDateOpen()))
            {
                //not same open date
                break;
            }

            //j transaction should be part of the pctm
            pom.getPositionOpenTransactionModels()
                    .add(new PositionOpenTransactionModel(
                            this.positionOpenTransactionModels.get(j)));

            // mark it complete
            this.positionOpenTransactionModels.get(j)
                    .setBComplete(true);
        }
    }

    private void doAttributesPotm2Pom(PositionOpenModel pom)
    {
        Double totalOpen;
        Double totalMktVal;
        Double totalUnits;
        Double gain;
        Double gainPct;
        java.sql.Date dateOpen;
        Double totalTotalClose;

        totalOpen = totalMktVal = totalUnits = totalTotalClose = 0.0;
        dateOpen = new java.sql.Date(0);

        for (PositionOpenTransactionModel potm : pom.
                getPositionOpenTransactionModels())
        {
            totalUnits += potm.getUnits();
            totalOpen += potm.getTotalOpen();
            totalMktVal += potm.getMktVal();

            dateOpen = potm.getDateOpen().after(dateOpen) ? potm.getDateOpen() : dateOpen;
        }

        gain = totalMktVal + totalOpen;
        gainPct = 100.0 * gain / abs(totalOpen);

        pom.setUnits(totalUnits);
        pom.setGain(gain);
        pom.setGainPct(gainPct);
        pom.setTotalOpen(totalOpen);
        pom.setMktVal(totalMktVal);

        pom.setPositionType(pom.getPositionOpenTransactionModels().get(0).
                getPositionType());
        pom.setEquityType(pom.getPositionOpenTransactionModels().get(0).
                getEquityType());
        //incorrect
//        pom.setTransactionType(pom.getPositionOpenTransactionModels().get(0).getTransactionType());
        pom.setTransactionType(pom.getTotalOpen() > 0 ? "SHORT" : "LONG");

        pom.setTicker(pom.getPositionOpenTransactionModels().get(0).getTicker());
        //incorrect as there is no equityId for multi-leg; make all null to avoid confusion
//        pom.setEquityId(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        pom.setEquityId(null);

        pom.setDateOpen(dateOpen);

        pom.setPriceOpen(abs(totalOpen) / abs(pom.getUnits() * 100.0));
        pom.setPrice(abs(totalMktVal) / abs(pom.getUnits() * 100.0));

        pom.setDays(pom.getPositionOpenTransactionModels().get(0).getDays());
    }

    /**
     * aggregating equal fotm to a potm
     *
     * @param i
     * @param potm
     */
    private void addFotm2Potm(Integer i,
            PositionOpenTransactionModel potm)
    {
        Integer potmStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        potmStart = i;

        //loop the rest of the fifoOpenTransactionModels array for transactions
        //  to add to potm fifoOpenTransactionModels array
        for (int j = i + 1; j < this.fifoOpenTransactionModels.size(); j++)
        {
            if (this.fifoOpenTransactionModels.get(j)
                    .getBComplete())
            {
                //should never hit
                continue;
            }
            if (!this.fifoOpenTransactionModels.get(potmStart)
                    .getEquityId()
                    .equals(this.fifoOpenTransactionModels.get(j)
                            .getEquityId()))
            {
                //not the same equityId
                break;
            }
            if (!this.fifoOpenTransactionModels.get(potmStart)
                    .getDateOpen()
                    .equals(this.fifoOpenTransactionModels.get(j)
                            .getDateOpen()))
            {
                //not same open date
                break;
            }

            //j transaction should be part of the pctm
            potm.getFifoOpenTransactionModels()
                    .add(new FIFOOpenTransactionModel(
                            this.fifoOpenTransactionModels.get(j)));

            // mark it complete
            this.fifoOpenTransactionModels.get(j)
                    .setBComplete(true);
        }
    }

    /**
     * Use the positionClosedModel.positionOpenTransactionModels to establish
     * tacticId
     *
     * @param pom
     */
    private void doFourLegs(PositionOpenModel pom)
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
//        Double dMktVal;
//        Double dLMktVal;
        ArrayList<PositionOpenTransactionModel> putsList;
        ArrayList<PositionOpenTransactionModel> callsList;
        Iterator<PositionOpenTransactionModel> potmIterator;
        PositionOpenTransactionModel tempPotm;
        Double totalOpen;
        Double totalMktVal;
//        Double totalLMktVal;
//        Double totalActPct;

        ret = PositionOpenModel.TACTICID_CUSTOM;
        longPut = shortPut = longCall = shortCall = 0;
        bSameExpiry = bSameUnits = bCredit = true;
        dTotal = 0.0;
        //units are positive for buy, negative for sell.

        putsList = new ArrayList<>();
        callsList = new ArrayList<>();

        //iterate the position closed transaction array
        potmIterator = pom.getPositionOpenTransactionModels()
                .iterator();

        OCCclass occClassPctm0 = new OCCclass(pom.
                getPositionOpenTransactionModels()
                .get(0)
                .getEquityId());
        while (potmIterator.hasNext())
        {
            tempPotm = potmIterator.next();
            OCCclass occClassPctm = new OCCclass(tempPotm.getEquityId());

            if (occClassPctm.getDtExpiry()
                    .equals(occClassPctm0.getDtExpiry()))
            {
                //same expiry
                if (occClassPctm.getPutcall()
                        .equalsIgnoreCase("p"))
                {
                    //same expiry
                    //put
                    putsList.add(tempPotm);

                    if (tempPotm.getTransactionType()
                            .equalsIgnoreCase("buytoopen"))
                    {
                        //same expiry
                        //put
                        //long
                        longPut += 1;
                    }
                    else
                    {
                        //same expiry
                        //put
                        //short
                        shortPut += 1;
                    }
                }
                else
                {
                    //same expiry
                    //call
                    callsList.add(tempPotm);
                    if (tempPotm.getTransactionType()
                            .equalsIgnoreCase("buytoopen"))
                    {
                        //same expiry
                        //call
                        //long
                        longCall += 1;
                    }
                    else
                    {
                        //same expiry
                        //call
                        //short
                        shortCall += 1;
                    }
                }

                Double d1;
                Double d2;

                d1 = Math.abs(tempPotm.getUnits());
                d2 = Math.abs(pom.getPositionOpenTransactionModels()
                        .get(0)
                        .getUnits());

                bSameUnits = d1.equals(d2);
            }
            else
            {
                //different expiry
                //todo: break up into individual positions
                //  or a pair of calls and a pair of puts
                bSameExpiry = false;
            }

            dTotal += tempPotm.getTotalOpen();
//            dMktVal += tempPotm.getMktVal();
//            dLMktVal += tempPotm.getLMktVal();
        }

        //todo: separate into positions
        if (!bSameExpiry)
        {
            pom.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
            return;
        }
        if (!bSameUnits)
        {
            pom.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
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

        if (longPut.equals(shortPut) && longPut.equals(longCall) && longPut
                .equals(shortCall))
        {
            //equal parts longPut, shortPut, longCall, shortCall
            //todo: all different strikes
            //iron condor
            ret = PositionOpenModel.TACTICID_IRONCONDOR;
        }

        pom.setTacticId(ret);

        pom.setPositionType(bCredit ? "SHORT" : "LONG");

        totalOpen = totalMktVal = 0.0;
        for (PositionOpenTransactionModel potm : pom.
                getPositionOpenTransactionModels())
        {
            totalOpen += potm.getTotalOpen();
            totalMktVal += potm.getMktVal();
//            totalLMktVal += potm.getLMktVal();
//            totalActPct += potm.getActPct();
        }

        pom.setUnits(pom.getPositionOpenTransactionModels()
                .get(0)
                .getUnits());

        pom.setPriceOpen(totalOpen / ((pom.getUnits() * 100.0)));
        pom.setPrice(totalMktVal / ((pom.getUnits() * 100.0)));

        pom.setGain((totalMktVal + totalOpen));
        pom.setGainPct(100.0 * pom.getGain() / Math.abs(totalOpen));

        pom.setPositionType(totalOpen > 0.0 ? "SHORT" : "LONG");

        //latest close date should be the last one?
        //not tested
        pom.setDateOpen(pom.getPositionOpenTransactionModels().get(2).
                getDateOpen());
    }

    /**
     * Given a 2 leg position, find the tacticId and adjust the position
     * attributes
     *
     * @param aggregatedPositionClosedTransactionModels the transactions of the
     * position aggregated
     * @param pom the position
     */
    private void doTwoLegs(PositionOpenModel pom)
    {
        /**
         * vertical straddle strangle collar calendar diagonal
         */
        Integer ret;
        PositionOpenTransactionModel potm1;
        PositionOpenTransactionModel potm2;
        OCCclass occTrans1;
        OCCclass occTrans2;
        Double units;
        Double totalOpen;
        Double totalClose;
        Double totalMktVal;
//        Double totalLMktVal;
//        Double totalActPct;

        potm1 = pom.getPositionOpenTransactionModels().get(0);
        potm2 = pom.getPositionOpenTransactionModels().get(1);

        occTrans1 = new OCCclass(potm1.getEquityId());
        occTrans2 = new OCCclass(potm2.getEquityId());

        if (occTrans1.getPutcall()
                .equalsIgnoreCase(occTrans2.getPutcall()))
        {
            //all are either put or call
            //vertical, calendar, diagonal
            if (occTrans1.getDtExpiry().equals(occTrans2.getDtExpiry()))
            {
                //all are either put or call
                //all expiries are the same
                if (potm1.getTransactionType()
                        .equals(potm2.getTransactionType()))
                {
                    //all are either put or call
                    //all expiries are the same
                    //all same transType (buytoopen ...)
                    //hit
                    ret = PositionOpenModel.TACTICID_CUSTOM;
                }
                else
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
                    }
                    else
                    {
                        //all are either put or call
                        //all expiries are the same
                        //all different transType (buytoopen ...)
                        //hit
                        Double double1, double2;
                        double1 = Math.abs(pom.
                                getPositionOpenTransactionModels().get(0).
                                getUnits());
                        double2 = Math.abs(pom.
                                getPositionOpenTransactionModels().get(1).
                                getUnits());
                        ret = double1.equals(double2)
                                ? PositionOpenModel.TACTICID_VERTICAL
                                : PositionOpenModel.TACTICID_VERTICAL_CUSTOM;
                    }
                }
            }
            else
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
                    if (pom.getPositionOpenTransactionModels().get(0).getUnits()
                            .equals(pom.getPositionOpenTransactionModels().
                                    get(1).getUnits()))
                    {
                        ret = PositionOpenModel.TACTICID_CALENDAR;
                    }
                    else
                    {
                        ret = PositionOpenModel.TACTICID_CALENDAR_CUSTOM;
                    }
                }
                else
                {
                    if (pom.getPositionOpenTransactionModels()
                            .get(0)
                            .getTransactionType()
                            .equalsIgnoreCase(pom.
                                    getPositionOpenTransactionModels().get(1).
                                    getTransactionType()))
                    {
                        //all are either put or call
                        //all expiries are not the same
                        //all strikes are not the same
                        //all transType (buytoopen ...) are the same
                        ret = PositionOpenModel.TACTICID_CUSTOM;
                    }
                    else
                    {
                        if (pom.getPositionOpenTransactionModels().get(0).
                                getUnits()
                                .equals(pom.getPositionOpenTransactionModels().
                                        get(1).getUnits()))
                        {
                            //all are either put or call
                            //all expiries are not the same
                            //all strikes are not the same
                            //all transType (buytoopen ...) are not the same
                            //all units are the same
                            ret = PositionOpenModel.TACTICID_DIAGONAL;
                        }
                        else
                        {
                            //all are either put or call
                            //all expiries are not the same
                            //all strikes are not the same
                            //all transType (buytoopen ...) are not the same
                            //all units are not the same
                            ret = PositionOpenModel.TACTICID_DIAGONAL_CUSTOM;
                        }
                    }
                }
            }
        }
        else
        {
            //have puts and calls
            //straddle, strangle, collar
            if (occTrans1.getDtExpiry().equals(occTrans2.getDtExpiry()))
            {
                //not all calls or puts
                //all expiries are the same
                if (potm1.getTransactionType().equalsIgnoreCase(potm2.
                        getTransactionType()))
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
                        if (pom.getPositionOpenTransactionModels().get(0).
                                getUnits()
                                .equals(pom.getPositionOpenTransactionModels().
                                        get(1).getUnits()))
                        {
                            ret = PositionOpenModel.TACTICID_STRADDLE;
                        }
                        else
                        {
                            ret = PositionOpenModel.TACTICID_STRADDLE_CUSTOM;
                        }
                    }
                    else
                    {
                        //not all calls or puts
                        //all expiries are the same
                        //all same transType (buytoopen ...)
                        //all strikes are not the same
                        //hit
                        if (pom.getPositionOpenTransactionModels().get(0).
                                getUnits()
                                .equals(pom.getPositionOpenTransactionModels().
                                        get(1).getUnits()))
                        {
                            ret = PositionOpenModel.TACTICID_STRANGLE;
                        }
                        else
                        {
                            ret = PositionOpenModel.TACTICID_STRANGLE_CUSTOM;
                        }

                    }
                }
                else
                {
                    //not all calls or puts
                    //all expiries are the same
                    //all not same transType (buytoopen ...)
                    //not hit
                    if (pom.getPositionOpenTransactionModels().get(0).getUnits()
                            .equals(pom.getPositionOpenTransactionModels().
                                    get(1).getUnits()))
                    {
                        ret = PositionOpenModel.TACTICID_COLLAR;
                    }
                    else
                    {
                        ret = PositionOpenModel.TACTICID_COLLAR_CUSTOM;
                    }
                }
            }
            else
            {
                //not all calls or puts
                //all expiries are not the same
                //hit
                ret = PositionOpenModel.TACTICID_CUSTOM;
            }
        }

        pom.setTacticId(ret);

        //set the rest of the positionModel attributes
        pom.setUnits(Double.min(pom.getPositionOpenTransactionModels().get(0).
                getUnits(),
                pom.getPositionOpenTransactionModels().get(1).getUnits()));

        totalOpen = totalClose = totalMktVal = 0.0;
        for (PositionOpenTransactionModel potm : pom.
                getPositionOpenTransactionModels())
        {
            totalOpen += potm.getTotalOpen();
            totalMktVal += potm.getMktVal();
//            totalLMktVal += potm.getLMktVal();
//            totalActPct += potm.getActPct();
        }

        pom.setUnits(pom.getPositionOpenTransactionModels()
                .get(0)
                .getUnits());

        pom.setPriceOpen(totalOpen / ((pom.getUnits() * 100.0)));
        pom.setPrice(totalMktVal / ((pom.getUnits() * 100.0)));

        pom.setGain((totalOpen + totalMktVal));
        pom.setGainPct(100.0 * pom.getGain() / Math.abs(totalOpen));

        pom.setTotalOpen(totalOpen);
        pom.setTotalClose(totalClose);

//        pom.setMktVal(totalMktVal);
//        pom.setLMktVal(totalLMktVal);
//        pom.setActPct(totalActPct);
        pom.setPositionType(totalOpen > 0.0 ? "SHORT" : "LONG");

        pom.setDateOpen(pom.getPositionOpenTransactionModels().get(0).
                getDateOpen());

        if (pom.getPositionOpenTransactionModels().get(0).getDateOpen()
                .after(pom.getPositionOpenTransactionModels().get(1).
                        getDateOpen()))
        {
            pom.setDateOpen(pom.getPositionOpenTransactionModels().get(0).
                    getDateOpen());
        }
        else
        {
            pom.setDateOpen(pom.getPositionOpenTransactionModels().get(1).
                    getDateOpen());
        }
    }

    /**
     * handle positions with one leg
     *
     * @param pom
     */
    private void doOneLeg(PositionOpenModel pom)
    {
        Double totalOpen;
        Double totalMktVal;
        Double totalLMktVal;
        Double totalActPct;

        if (pom.getPositionOpenTransactionModels().get(0).getTransactionType().
                equalsIgnoreCase("buytoopen"))
        {
            pom.setTacticId(PositionOpenModel.TACTICID_LONG);
        }

        if (pom.getPositionOpenTransactionModels().get(0).getTransactionType().
                equalsIgnoreCase("selltoopen"))
        {
            pom.setTacticId(PositionOpenModel.TACTICID_SHORT);
        }
    }

    /**
     * determine tacticId for each position
     */
    public void doPositionsTacticId()
    {
        PositionOpenModel pomTemp;

        pomAddList.clear();

        for (PositionOpenModel pom : this.positionOpenModels)
        {

            switch (pom.getPositionOpenTransactionModels()
                    .size())
            {
                case 1:
                    this.doOneLeg(pom);
                    break;
                case 2:
                    this.doTwoLegs(pom);
                    //handle custom result
                    if (pom.getTacticId() == PositionOpenModel.TACTICID_CUSTOM)
                    {
                        pomTemp = new PositionOpenModel(pom);

                        pomTemp.getPositionOpenTransactionModels().remove(0);
                        pomTemp.setPositionType(pomTemp.
                                getPositionOpenTransactionModels().get(0)
                                .getTransactionType().equalsIgnoreCase(
                                        "buytoopen") ? "LONG" : "SHORT");
                        pomTemp.setTacticId(pomTemp.
                                getPositionOpenTransactionModels().get(0)
                                .getTransactionType().equalsIgnoreCase(
                                        "buytoopen")
                                        ? PositionOpenModel.TACTICID_LONG
                                        : PositionOpenModel.TACTICID_SHORT);

                        pom.getPositionOpenTransactionModels().remove(1);
                        pom.setPositionType(pom.
                                getPositionOpenTransactionModels().get(0)
                                .getTransactionType().equalsIgnoreCase(
                                        "buytoopen") ? "LONG" : "SHORT");
                        pom.setTacticId(pom.getPositionOpenTransactionModels().
                                get(0)
                                .getTransactionType().equalsIgnoreCase(
                                        "buytoopen")
                                        ? PositionOpenModel.TACTICID_LONG
                                        : PositionOpenModel.TACTICID_SHORT);

                        this.doAttributesPotm2Pom(pomTemp);
                        this.doAttributesPotm2Pom(pom);

                        pomAddList.add(pomTemp);
                    }

                    break;
                case 3:
                    //so far, no named 3 legged positions
                    //could be a spurious set of transactions on the same day
                    //otherwise, just separate into unique positions
                    //hard route: look for 2 leg positions
                    //  0,1; 1,2; 0,2
                    pomTemp = new PositionOpenModel(pom);
                    //test 0, 1
                    pomTemp.getPositionOpenTransactionModels().remove(2);

                    this.doTwoLegs(pomTemp);

                    if (!pomTemp.getTacticId().equals(
                            PositionOpenModel.TACTICID_CUSTOM))
                    {
                        //0, 1 is a position
                        //add to addList
                        //not tested
                        this.doAttributesPotm2Pom(pomTemp);

                        pomAddList.add(pomTemp);
                        this.doTwoLegs(pomTemp);

                        //edit pcm to remove 0, 1 and leave as a one leg position; fix attributes
                        pom.getPositionOpenTransactionModels().remove(0);
                        pom.getPositionOpenTransactionModels().remove(0);

                        this.doAttributesPotm2Pom(pom);

                        this.doOneLeg(pom);
                        break;
                    }

                    pomTemp = new PositionOpenModel(pom);
                    //test 1, 2
                    pomTemp.getPositionOpenTransactionModels()
                            .remove(0);

                    this.doTwoLegs(pomTemp);

                    if (!pomTemp.getTacticId().equals(
                            PositionOpenModel.TACTICID_CUSTOM))
                    {
                        //1, 2 is a position
                        //add to addList
                        pomAddList.add(pomTemp);

                        this.doAttributesPotm2Pom(pomTemp);

                        this.doTwoLegs(pomTemp);

                        //edit pcm to remove 1, 2 and leave as a one leg position; fix attributes
                        pom.getPositionOpenTransactionModels().remove(1);
                        pom.getPositionOpenTransactionModels().remove(1);

                        this.doAttributesPotm2Pom(pom);

                        this.doOneLeg(pom);
                        break;
                    }

                    pomTemp = new PositionOpenModel(pom);
                    //test 0,2
                    pomTemp.getPositionOpenTransactionModels().remove(1);
                    this.doTwoLegs(pomTemp);

                    if (!pomTemp.getTacticId().equals(
                            PositionOpenModel.TACTICID_CUSTOM))
                    {
                        //0, 2 is a position
                        //add to addList
                        //not tested
                        this.doAttributesPotm2Pom(pomTemp);

                        pomAddList.add(pomTemp);
                        this.doTwoLegs(pomTemp);

                        //edit pcm to remove 2,3 and leave as a one leg position; fix attributes
                        pom.getPositionOpenTransactionModels().remove(0);
                        pom.getPositionOpenTransactionModels().remove(1);

                        this.doAttributesPotm2Pom(pom);

                        this.doOneLeg(pom);
                        break;
                    }

                    //if none of the hard route pan out, go the easy route
                    //easy route
                    pomTemp = new PositionOpenModel(pom);
                    pomTemp.getPositionOpenTransactionModels().remove(1);
                    pomTemp.getPositionOpenTransactionModels().remove(1);
                    //only one pctm in the pcm
                    //set the pctm equityId to the fctm equityId
                    pomTemp.getPositionOpenTransactionModels().get(0).
                            setEquityId(
                                    pomTemp.getPositionOpenTransactionModels().
                                            get(0).
                                            getFifoOpenTransactionModels()
                                            .get(0).getEquityId());

                    pomTemp.setTacticId(pomTemp.
                            getPositionOpenTransactionModels().get(0)
                            .getTransactionType().equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);
                    pomAddList.add(pomTemp);

                    pomTemp = new PositionOpenModel(pom);
                    pomTemp.getPositionOpenTransactionModels().remove(0);
                    pomTemp.getPositionOpenTransactionModels().remove(1);

                    this.doAttributesPotm2Pom(pomTemp);

                    pomTemp.setTacticId(pomTemp.
                            getPositionOpenTransactionModels().get(0)
                            .getTransactionType().equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);
                    pomAddList.add(pomTemp);

                    pom.getPositionOpenTransactionModels().remove(0);
                    pom.getPositionOpenTransactionModels().remove(0);

                    this.doAttributesPotm2Pom(pom);

                    pom.setTacticId(pom.getPositionOpenTransactionModels().
                            get(0)
                            .getTransactionType().equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);
                    break;
                case 4:
                    this.doFourLegs(pom);
                    if (pom.getTacticId().equals(
                            PositionOpenModel.TACTICID_CUSTOM))
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
                    pom.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
                    break;
                default:
            }
        }

        for (PositionOpenModel pom : pomAddList)
        {
            //add any new positions to the array
            this.positionOpenModels.add(pom);
        }
    }

    private void getTransactions(String equityType)
    {
        this.fifoOpenTransactionModels.clear();

        //get transactions for the joomlaId
        String sql = String.format(
                FIFOOpenTransactionModel.SELECT_INCOMPLETE_BY_JOOMLAID_EQUITYTYPE,
                "FIFOOpenTransactions",
                this.userId,
                equityType);
        try (Connection con = CMDBController.getConnection();
                PreparedStatement pStmt = con.prepareStatement(sql);
                ResultSet rs = pStmt.executeQuery();)
        {

            while (rs.next())
            {
                this.fifoOpenTransactionModels.add(new FIFOOpenTransactionModel(
                        rs.getInt("DMAcctId"),
                        rs.getInt("JoomlaId"),
                        rs.getString("FiTId"),
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
                        rs.getInt("Complete") == 1,
                        rs.getString("OptionType"),
                        rs.getDouble("StrikePrice"),
                        rs.getInt("ShPerCtrct"),
                        rs.getInt("Days"),
                        rs.getInt("ClientSectorId"),
                        rs.getDouble("MktVal"),
                        rs.getDouble("LMktVal"),
                        rs.getDouble("ActPct")));
            }
        }
        catch (SQLException ex)
        {
            CMHPIUtils.showDefaultMsg(
                    CMLanguageController.getDBErrorProp("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }
}
