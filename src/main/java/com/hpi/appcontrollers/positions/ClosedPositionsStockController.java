package com.hpi.appcontrollers.positions;

import com.hpi.TPCCMcontrollers.CMDBController;
import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.entities.*;
import com.hpi.hpiUtils.CMHPIUtils;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClosedPositionsStockController
        extends PositionBaseStock
{

    private final Integer userId;
    /*
     * Singleton
     *
     */
    private static ClosedPositionsStockController instance;

    static
    {
        ClosedPositionsStockController.instance = null;
    }

    protected ClosedPositionsStockController()
    {
        // protected prevents instantiation outside of package
        this.userId = CMDBModel.getUserId();
        this.fifoClosedTransactionModels = new ArrayList<>();
        this.positionTransactionModels = new ArrayList<>();
        this.positionModels = new ArrayList<>();
    }

    public synchronized static ClosedPositionsStockController getInstance()
    {
        if (ClosedPositionsStockController.instance == null)
        {
            ClosedPositionsStockController.instance = new ClosedPositionsStockController();
        }
        return ClosedPositionsStockController.instance;
    }
    //***

    private final ArrayList<FIFOClosedTransactionModel> fifoClosedTransactionModels;
    private final ArrayList<PositionClosedTransactionModel> positionTransactionModels;
    private final ArrayList<PositionClosedModel> positionModels;

    public void doPositions()
    {
        this.getTransactions();

        this.doFtm2Ptm();

        this.doPtm2Pm();

        //push to database
        this.doSQL();

        //make transactions complete
        this.setFifoTransactionsComplete();
    }

    /**
     * fifoClosedTransactions -> positionsClosedTransactions
     * aggregate lots into positionClosedTransactionModel
     */
    public void doFtm2Ptm()
    {
        PositionClosedTransactionModel ptm;

        this.positionTransactionModels.clear();

        for (int i = 0; i < this.fifoClosedTransactionModels.size(); i++)
        {
            if (this.fifoClosedTransactionModels.get(i).getBComplete())
            {
                //fotm already handled
                continue;
            }

            ptm = PositionClosedTransactionModel.builder()
                    .dmAcctId(this.fifoClosedTransactionModels.get(i).
                            getDmAcctId())
                    .joomlaId(this.userId)
                    .positionId(-999)
                    .build();

            //add first fifoTransaction to the positionTransactionModel.fifoClosedTransactionModels
            ptm.getFifoClosedTransactionModels()
                    .add(new FIFOClosedTransactionModel(
                            this.fifoClosedTransactionModels.get(i)));

            //add positionTransactionModel to the positionTransactionModel array
            //using the previously built potm
            this.positionTransactionModels.add(ptm);

            //mark initial transaction complete
            this.fifoClosedTransactionModels.get(i).setBComplete(true);

            this.addFtm2Ptm(i, ptm);

            //set attributes in the pctm
            this.doAttributesPtm(ptm);
        }
    }

    private void getTransactions()
    {
        ResultSet rs;

        this.fifoClosedTransactionModels.clear();

        //get open stock transactions for the joomlaId
        try (Connection con = CMDBController.getConnection();
                PreparedStatement pStmt = con.prepareStatement(String
                        .format(FIFOClosedTransactionModel.SELECT_INCOMPLETE_BY_JOOMLAID_EQUITYTYPE,
                                this.userId,
                                "stock"));)
        {
            pStmt.clearWarnings();
            rs = pStmt.executeQuery();

            while (rs.next())
            {
                this.fifoClosedTransactionModels.add(FIFOClosedTransactionModel.
                        builder()
                        .dmAcctId(rs.getInt("DMAcctId"))
                        .joomlaId(rs.getInt("JoomlaId"))
                        .fiTId(rs.getString("FiTId"))
                        .ticker(rs.getString("Ticker"))
                        .equityId(rs.getString("EquityId"))
                        .transactionName(rs.getString("TransactionName"))
                        .dateOpen(rs.getDate("DateOpen"))
                        .dateClose(rs.getDate("DateClose"))
                        .dateExpire(rs.getDate("DateExpire"))
                        .units(rs.getDouble("Units"))
                        .priceOpen(rs.getDouble("PriceOpen"))
                        .priceClose(rs.getDouble("PriceClose"))
                        .totalOpen(rs.getDouble("TotalOpen"))
                        .totalClose(rs.getDouble("TotalClose"))
                        .gain(rs.getDouble("Gain"))
                        .gainPct(rs.getDouble("GainPct"))
                        .equityType(rs.getString("EquityType"))
                        .positionType(rs.getString("PositionType"))
                        .transactionType(rs.getString("TransactionType"))
                        .complete(rs.getInt("Complete"))
                        .bComplete(rs.getInt("Complete") == 1)
                        .days((rs.getInt("Days")))
                        .build());
            }
        }
        catch (SQLException ex)
        {
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

    public void setFifoTransactionsComplete()
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

    /**
     * establish initial positions
     */
    public void doPtm2Pm()
    {
        PositionClosedModel pom;

        this.positionModels.clear();

        for (int i = 0; i < this.positionTransactionModels.size(); i++)
        {
            if (this.positionTransactionModels.get(i)
                    .getBComplete())
            {
                //fctm already handled
                continue;
            }

            pom = PositionClosedModel.builder()
                    .positionId(-999)
                    .dmAcctId(this.positionTransactionModels.get(i).
                            getDmAcctId())
                    .joomlaId(this.userId)
                    .build();

            //add first positionOpenTransaction to the positionOpenModel.positionOpenTransactionModels
            pom.getPositionClosedTransactionModels()
                    .add(new PositionClosedTransactionModel(
                            this.positionTransactionModels.get(i)));

            //add positionOpenModel to the positionOpenModels array
            //using the previously built pom
            this.positionModels.add(pom);

            //mark initial transaction complete
            this.positionTransactionModels.get(i).setBComplete(true);

            this.addPtm2Pm(i, pom);

            //set attributes in the pcm
            this.doAttributesPtm2Pm(pom);
        }
    }

    private void addPtm2Pm(Integer i,
            PositionClosedModel pm)
    {
        Integer pmStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        pmStart = i;

        //loop the rest of the positionTransactionModels array for transactions
        //  to add to pcm positionCloseTransactionModels array
        for (int j = i + 1; j < this.positionTransactionModels.size(); j++)
        {
            if (this.positionTransactionModels.get(j)
                    .getBComplete())
            {
                //never hit
                continue;
            }

            if (!this.positionTransactionModels.get(pmStart)
                    .getTicker()
                    .equals(this.positionTransactionModels.get(j)
                            .getTicker()))
            {
                //not same ticker
                break;
            }

            if (!this.positionTransactionModels.get(pmStart)
                    .getDmAcctId()
                    .equals(this.positionTransactionModels.get(j)
                            .getDmAcctId()))
            {
                //not same DMAcctId
                break;
            }

            if (this.positionTransactionModels.get(pmStart)
                    .getDateOpen()
                    .equals(this.positionTransactionModels.get(pmStart)
                            .getDateClose()))
            {
                //special case
                //open and close dates are the same; we aggregated these lots; now needs to be unique pcm
                break;
            }

            if (!this.positionTransactionModels.get(pmStart)
                    .getDateOpen()
                    .equals(this.positionTransactionModels.get(j)
                            .getDateOpen()))
            {
                //not same open date
                break;
            }

            //j transaction should be part of the pctm
            pm.getPositionClosedTransactionModels()
                    .add(new PositionClosedTransactionModel(
                            this.positionTransactionModels.get(j)));

            // mark it complete
            this.positionTransactionModels.get(j)
                    .setBComplete(true);
        }
    }

    private void doAttributesPtm2Pm(PositionClosedModel pm)
    {
        Double totalUnits;
        Double totalOpen;
        Double totalClose;
        Double gain;
        Double gainPct;
        java.sql.Date dateClose;

        totalUnits = totalOpen = totalClose = 0.0;
        dateClose = new java.sql.Date(0);

        for (PositionClosedTransactionModel ptm : pm.
                getPositionClosedTransactionModels())
        {
            totalOpen += ptm.getTotalOpen();
            totalClose += ptm.getTotalClose();
            totalUnits += ptm.getUnits();

            //ptm is java.sql.Date; latch the latest date
            if (ptm.getDateClose() == null)
            {
                dateClose = null;
            }
            else
            {
                dateClose = dateClose.before(ptm.getDateClose()) ? ptm.
                        getDateClose() : dateClose;
            }
        }

        gain = totalClose + totalOpen;
        gainPct = 100.0 * gain / Math.abs(totalOpen);

        pm.setUnits(totalUnits);
        pm.setGain(gain);
        pm.setGainPct(gainPct);
        pm.setTotalOpen(totalOpen);
        pm.setTotalClose(totalClose);

        pm.setPositionType(pm.getPositionClosedTransactionModels().get(0).
                getPositionType());
        pm.setEquityType(pm.getPositionClosedTransactionModels().get(0).
                getEquityType());
        pm.setTransactionType(pm.getPositionClosedTransactionModels().get(0).
                getTransactionType());

        pm.setTicker(pm.getPositionClosedTransactionModels().get(0).getTicker());
        pm.setEquityId(pm.getTicker());
        pm.setPositionName(pm.getTicker() + " " + pm.getPositionType());

        pm.setDateOpen(pm.getPositionClosedTransactionModels().get(0).
                getDateOpen());

        pm.setDateClose(dateClose);

        pm.setPriceOpen(totalOpen / pm.getUnits());
        pm.setPrice(totalClose / Math.abs(pm.getUnits()));

        pm.setDays(0);

        pm.setTacticId(pm.getPositionType().equalsIgnoreCase("LONG")
                ? PositionOpenModel.TACTICID_LONG : PositionOpenModel.TACTICID_SHORT);
    }

    /**
     * push the positions to the database
     */
    public void doSQL()
    {
        String sql;

        this.positionModels.forEach(pm ->
        {
            Integer positionId;

            // if null it was a bad data issue
            if (pm.getDateClose() != null)
            {
                //add the positionClosedModel to positionsClosed table
                positionId = this.insertPositionSQL(pm);

                //positionId is -1
                pm.setPositionId(positionId);

                //add the array of transactions to positionsClosedTransactions table
                this.insertPositionTransactionsSQL(positionId, pm);
            }
        });
    }

    private Integer insertPositionSQL(PositionClosedModel pm)
    {
        String sInsertSQL;

        sInsertSQL = String.format(PositionClosedModel.POSITION_INSERT,
                pm.getDmAcctId(),
                pm.getJoomlaId(),
                pm.getTicker(),
                pm.getEquityId(),
                pm.getPositionName(),
                pm.getTacticId(),
                pm.getUnits(),
                pm.getPriceOpen(),
                pm.getPrice(),
                pm.getGainPct(),
                pm.getDateOpen(),
                pm.getDateClose(),
                pm.getDays(),
                pm.getGain(),
                pm.getPositionType(),
                pm.getTransactionType(),
                pm.getTotalOpen(),
                pm.getTotalClose(),
                pm.getEquityType());

        return CMDBController.insertAutoRow(sInsertSQL);
    }

    private void insertPositionTransactionsSQL(Integer positionId,
            PositionClosedModel pm)
    {
        String sql;

        for (PositionClosedTransactionModel potm : pm.
                getPositionClosedTransactionModels())
        {
            sql = String.format(
                    PositionClosedTransactionModel.POSITION_TRANSACTION_INSERT,
                    potm.getDmAcctId(),
                    potm.getJoomlaId(),
                    positionId,
                    potm.getFiTId(),
                    //                potm.getEquityId(), cannot do for options so do not do for stocks either
                    potm.getTransactionName(),
                    potm.getTicker(),
                    potm.getDateOpen(),
                    potm.getDateClose(),
                    potm.getUnits(),
                    potm.getPriceOpen(),
                    potm.getPriceClose(),
                    potm.getDays(),
                    potm.getPositionType(),
                    potm.getTotalOpen(),
                    potm.getTotalClose(),
                    potm.getEquityType(),
                    potm.getGain(),
                    potm.getGainPct(),
                    potm.getTransactionType(),
                    0);//complete

            CMDBController.executeSQL(sql);
        }
    }

    /**
     * aggregating equal fotm to a potm
     *
     * @param i
     * @param ptm
     */
    private void addFtm2Ptm(Integer i, PositionClosedTransactionModel ptm)
    {
        Integer potmStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        potmStart = i;

        //loop the rest of the fifoOpenTransactionModels array for transactions
        //  to add to potm fifoOpenTransactionModels array
        for (int j = i + 1; j < this.fifoClosedTransactionModels.size(); j++)
        {
            if (this.fifoClosedTransactionModels.get(j).getBComplete())
            {
                //should never hit
                continue;
            }
            if (!this.fifoClosedTransactionModels.get(potmStart).getDmAcctId()
                    .equals(this.fifoClosedTransactionModels.get(j).
                            getDmAcctId()))
            {
                //not the same account
                break;
            }
            if (!this.fifoClosedTransactionModels.get(potmStart).getEquityId()
                    .equals(this.fifoClosedTransactionModels.get(j).
                            getEquityId()))
            {
                //not the same equityId
                break;
            }

            if (this.fifoClosedTransactionModels.get(potmStart).getDateOpen()
                    .equals(this.fifoClosedTransactionModels.get(potmStart).
                            getDateClose())
                    && !this.fifoClosedTransactionModels.get(j).getDateOpen()
                            .equals(this.fifoClosedTransactionModels.get(j).
                                    getDateClose()))
            {
                //special case where open and close date the same. need those to be distinct ptm
                //  but also aggregated when more than one lot
                break;
            }

            if (this.fifoClosedTransactionModels.get(potmStart)
                    .getDateClose() == null)
            {
                // data mismatches can cause this to be null
                break;
            }

            if (!this.fifoClosedTransactionModels.get(potmStart)
                    .getDateClose()
                    .equals(this.fifoClosedTransactionModels.get(j)
                            .getDateClose()))
            {
                //not same close date
                break;
            }

            if (!this.fifoClosedTransactionModels.get(potmStart)
                    .getPositionType()
                    .equals(this.fifoClosedTransactionModels.get(j)
                            .getPositionType()))
            {
                //not same positionType
                break;
            }

            //j transaction should be part of the pctm
            ptm.getFifoClosedTransactionModels()
                    .add(new FIFOClosedTransactionModel(
                            this.fifoClosedTransactionModels.get(j)));

            // mark it complete
            this.fifoClosedTransactionModels.get(j).setBComplete(true);
        }
    }

    private void doAttributesPtm(PositionClosedTransactionModel ptm)
    {
        Double totalUnits;
        Double totalOpen;
        Double totalClose;
        Double gain;
        Double gainPct;
        java.sql.Date dateOpen;
        java.sql.Date dateClose;

        dateOpen = new java.sql.Date(0);
        dateClose = new java.sql.Date(0);

        totalUnits = totalOpen = totalClose = 0.0;

        for (FIFOClosedTransactionModel ftm : ptm.
                getFifoClosedTransactionModels())
        {
            totalUnits += ftm.getUnits();
            totalOpen += ftm.getTotalOpen();
            totalClose += ftm.getTotalClose();

            //want this to reflect the last time a position was opened
            if (ftm.getDateOpen() == null)
            {
                dateOpen = null;
            }
            else
            {
                dateOpen = dateOpen.compareTo(ftm.getDateOpen()) < 0 ? ftm.
                        getDateOpen() : dateOpen;
            }

            //want this to reflect the last time a position was closed
            if (ftm.getDateClose() == null)
            {
                // bad data can cause this
                dateClose = null;
            }
            else
            {
                dateClose = dateClose.compareTo(ftm.getDateClose()) < 0 ? ftm.
                        getDateClose() : dateClose;
            }
        }

        gain = totalOpen + totalClose;
        gainPct = 100.0 * gain / Math.abs(totalOpen);

        ptm.setUnits(totalUnits);
        ptm.setTotalOpen(totalOpen);
        ptm.setTotalClose(totalClose);

        ptm.setGain(gain);
        ptm.setGainPct(gainPct);

        ptm.setTransactionType(ptm.getFifoClosedTransactionModels().get(0).
                getTransactionType());
        ptm.setPositionType(ptm.getFifoClosedTransactionModels().get(0).
                getPositionType());
        ptm.setEquityType(ptm.getFifoClosedTransactionModels().get(0).
                getEquityType());

        ptm.setFiTId(
                ptm.getFifoClosedTransactionModels().get(0).getFiTId() + "_w");

        ptm.setTicker(ptm.getFifoClosedTransactionModels().get(0).getTicker());
        ptm.setEquityId(ptm.getTicker());
//        ptm.setTransactionName(ptm.getTicker());

        ptm.setDateOpen(dateOpen);
        ptm.setDateClose(dateClose);

        //todo: need to back out commission,fees, etc
        ptm.setPriceOpen(totalOpen / totalUnits);
        ptm.setPriceClose(totalClose / totalUnits);

        ptm.setDays(0);
//        ptm.setPosType(ptm.getFifoClosedTransactionModels().get(0).getPositionType());

        ptm.setBComplete(false);

        ptm.setTransactionName(
                ptm.getTicker() + " " + (totalOpen < 0 ? "LONG" : "SHORT"));
    }
}
