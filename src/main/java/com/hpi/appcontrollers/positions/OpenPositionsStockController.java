package com.hpi.appcontrollers.positions;

import com.hpi.TPCCMcontrollers.CMDBController;
import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.entities.FIFOOpenTransactionModel;
import com.hpi.entities.PositionOpenModel;
import com.hpi.entities.PositionOpenTransactionModel;
import com.hpi.hpiUtils.CMHPIUtils;
import static java.lang.Math.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenPositionsStockController
    extends PositionBaseStock
{

    private final Integer userId;
    /*
     * Singleton
     *
     */
    private static OpenPositionsStockController instance;

    static
    {
        OpenPositionsStockController.instance = null;
    }

    protected OpenPositionsStockController()
    {
        // protected prevents instantiation outside of package
        this.userId = CMDBModel.getUserId();
        this.fifoTransactionModels = new ArrayList<>();
        this.positionTransactionModels = new ArrayList<>();
        this.positionModels = new ArrayList<>();
    }

    public synchronized static OpenPositionsStockController getInstance()
    {
        if (OpenPositionsStockController.instance == null)
        {
            OpenPositionsStockController.instance = new OpenPositionsStockController();
        }
        return OpenPositionsStockController.instance;
    }
    //***

    private final ArrayList<FIFOOpenTransactionModel> fifoTransactionModels;
    private final ArrayList<PositionOpenTransactionModel> positionTransactionModels;
    private final ArrayList<PositionOpenModel> positionModels;

    public void doPositions()
    {

        this.getTransactions();

        this.doFtm2Ptm();

        this.doPtm2Pm();

        //push to database
        this.doSQL();

        //mark transactions complete
        this.setFifoTransactionsComplete();
    }

    /**
     * aggregate lots into positionOpenTransactionModel
     */
    public void doFtm2Ptm()
    {
        PositionOpenTransactionModel ptm;

        this.positionTransactionModels.clear();

        for (int i = 0; i < this.fifoTransactionModels.size(); i++)
        {
            if (this.fifoTransactionModels.get(i)
                .getBComplete())
            {
                //fotm already handled
                continue;
            }

            //initialize positionTransactionModel
            ptm = PositionOpenTransactionModel.builder()
                .dmAcctId(this.fifoTransactionModels.get(i).getDmAcctId())
                .joomlaId(this.userId)
                .positionId(-999)
                .build();

            //add first fifoTransaction to the positionTransactionModel.fifoTransactionModels
            ptm.getFifoOpenTransactionModels()
                .add(new FIFOOpenTransactionModel(
                    this.fifoTransactionModels.get(i)));

            //add positionTransactionModel to the positionTransactionModel array
            //using the previously built ptm
            this.positionTransactionModels.add(ptm);

            //mark initial transaction complete
            this.fifoTransactionModels.get(i)
                .setBComplete(true);

            //add more fifoTransactionModels
            this.addFtm2Ptm(i, ptm);

            //set attributes in the pctm
            this.doAttributesPotm(ptm);
        }
    }

    private void getTransactions()
    {
        this.fifoTransactionModels.clear();

        //get open stock transactions for the joomlaId
        try (Connection con = CMDBController.getConnection();
            PreparedStatement pStmt = con.prepareStatement(String
                .format(FIFOOpenTransactionModel.SELECT_INCOMPLETE_BY_JOOMLAID_EQUITYTYPE,
                    "FIFOOpenTransactions",
                    this.userId,
                    "stock"));
            ResultSet rs = pStmt.executeQuery();)
        {

            while (rs.next())
            {
                this.fifoTransactionModels.add(new FIFOOpenTransactionModel(
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
        } catch (SQLException ex)
        {
            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getDBErrorProp("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setFifoTransactionsComplete()
    {
        String sql;

        for (FIFOOpenTransactionModel fctm : this.fifoTransactionModels)
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

    /**
     * establish initial positions
     */
    public void doPtm2Pm()
    {
        PositionOpenModel pom;

        this.positionModels.clear();

        for (int i = 0; i < this.positionTransactionModels.size(); i++)
        {
            if (this.positionTransactionModels.get(i).getBComplete())
            {
                //fctm already handled
                continue;
            }

            //initialize positionOpenModel
            pom = PositionOpenModel.builder()
                .positionId(-999)
                .dmAcctId(this.positionTransactionModels.get(i).getDmAcctId())
                .joomlaId(this.userId)
                .build();

            //add first positionOpenTransaction to the positionOpenModel.positionOpenTransactionModels
            pom.getPositionOpenTransactionModels()
                .add(new PositionOpenTransactionModel(this.positionTransactionModels.get(i)));

            //add positionOpenModel to the positionOpenModels array
            //using the previously built pcm
            this.positionModels.add(pom);

            //mark initial transaction complete
            this.positionTransactionModels.get(i).setBComplete(true);

            //more positionTransactionModels to add
            this.addPtm2Pm(i, pom);

            //set attributes in the pcm
            this.doAttributesPtm2Pm(pom);
        }
    }

    private void addPtm2Pm(Integer i,
        PositionOpenModel pm)
    {
        Integer pmStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        pmStart = i;

        //loop the rest of the positionTransactionModels array for transactions
        //  to add to pcm positionCloseTransactionModels array
        for (int j = i + 1; j < this.positionTransactionModels.size(); j++)
        {
            if (this.positionTransactionModels.get(j).getBComplete())
            {
                //never hit
                continue;
            }

            if (!this.positionTransactionModels.get(pmStart).getTicker()
                .equals(this.positionTransactionModels.get(j).getTicker()))
            {
                //not same ticker
                break;
            }

            //todo: at some point want to enable look at single accounts
            //  for now focussed on aggregated info
//            if (!this.positionTransactionModels.get(pmStart)
//                .getDmAcctId()
//                .equals(this.positionTransactionModels.get(j)
//                    .getDmAcctId())) {
//                //not same DMAcctId
//                break;
//            }
            //j transaction should be part of the pctm
            pm.getPositionOpenTransactionModels()
                .add(new PositionOpenTransactionModel(this.positionTransactionModels.get(j)));

            // mark it complete
            this.positionTransactionModels.get(j).setBComplete(true);
        }
    }

    private void doAttributesPtm2Pm(PositionOpenModel pm)
    {
        Double units;
        Double totalOpen;
        Double totalMktVal;
        Double gain;
        Double gainPct;
        java.sql.Date dateOpen;

        totalOpen = totalMktVal = units = 0.0;
        dateOpen = new java.sql.Date(0);

        for (PositionOpenTransactionModel ptm : pm.getPositionOpenTransactionModels())
        {
            units += ptm.getUnits();
            totalOpen += ptm.getTotalOpen();
            totalMktVal += ptm.getMktVal();
            dateOpen = ptm.getDateOpen().after(dateOpen) ? ptm.getDateOpen() : dateOpen;
        }

        gain = totalMktVal + totalOpen;
        gainPct = 100.0 * gain / Math.abs(totalOpen);

        pm.setUnits(units);
        pm.setGain(gain);
        pm.setGainPct(gainPct);
        pm.setTotalOpen(totalOpen);

        pm.setPositionType(pm.getPositionOpenTransactionModels().get(0).getPositionType());

        pm.setEquityId(pm.getPositionOpenTransactionModels().get(0).getEquityId());

        pm.setTicker(pm.getPositionOpenTransactionModels().get(0).getTicker());

        //pm.setDateOpen(pm.getPositionOpenTransactionModels().get(0).getDateOpen());
        pm.setDateOpen(dateOpen);

        pm.setPriceOpen(Math.abs(totalOpen) / abs(pm.getUnits()));
        pm.setPrice(totalMktVal / abs(pm.getUnits()));

        pm.setDays(pm.getPositionOpenTransactionModels().get(0).getDays());

        pm.setPositionName(pm.getPositionOpenTransactionModels().get(0).getTransactionName());

        pm.setTransactionType(pm.getPositionOpenTransactionModels().get(0).getTransactionType());
        pm.setPositionType(pm.getPositionOpenTransactionModels().get(0).getPositionType());
        pm.setEquityType(pm.getPositionOpenTransactionModels().get(0).getEquityType());

        pm.setTacticId(pm.getPositionType().equalsIgnoreCase("LONG")
            ? PositionOpenModel.TACTICID_LONG : PositionOpenModel.TACTICID_SHORT);
    }

    /**
     * push the positions to the database
     */
    public void doSQL()
    {
        String sql;

        for (PositionOpenModel pom : this.positionModels)
        {
            Integer positionId;

            //add the positionOpenModel to positionsOpen table
            positionId = this.insertPositionSQL(pom);

            pom.setPositionId(positionId);

            //add the array of transactions to positionsClosedTransactions table
            this.insertPositionTransactionsSQL(positionId, pom);
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
            0, //days
            pom.getGain(),
            pom.getPositionType(),
            pom.getTotalOpen(),
            pom.getTotalClose(),
            pom.getEquityType(),
            pom.getTransactionType(),
            null); //mktVal

//            pom.getMktVal(),
//            pom.getLMktVal(),
//            pom.getActPct());
        return CMDBController.insertAutoRow(sInsertSQL);
    }

    private void insertPositionTransactionsSQL(Integer positionId,
        PositionOpenModel pom)
    {
        String sql;

        for (PositionOpenTransactionModel potm : pom.getPositionOpenTransactionModels())
        {
            sql = String.format(PositionOpenTransactionModel.POSITION_TRANSACTION_INSERT,
                potm.getDmAcctId(),
                potm.getJoomlaId(),
                positionId,
                potm.getFiTId(),
                //                potm.getEquityId(), //do not set here as multi-leg positions have no equityId
                potm.getTransactionName(),
                potm.getTicker(),
                potm.getDateOpen(),
                potm.getUnits(),
                potm.getPriceOpen(),
                new java.sql.Date(0), //dateExpire
                0, //days
                potm.getPositionType(),
                potm.getTotalOpen(),
                potm.getEquityType(),
                potm.getGain(),
                potm.getGainPct(),
                potm.getTransactionType(),
                0, //complete
                potm.getMktVal(),
                potm.getLMktVal(),
                potm.getActPct());

            CMDBController.executeSQL(sql);
        }
    }

    /**
     * aggregating equal fotm to a potm
     *
     * @param i
     * @param ptm
     */
    private void addFtm2Ptm(Integer i,
        PositionOpenTransactionModel ptm)
    {
        Integer potmStart;

        //expect multiple lots on each leg
        //move starting point to new position index
        potmStart = i;

        //loop the rest of the fifoOpenTransactionModels array for transactions
        //  to add to potm fifoOpenTransactionModels array
        for (int j = i + 1; j < this.fifoTransactionModels.size(); j++)
        {
            if (this.fifoTransactionModels.get(j)
                //already handled
                .getBComplete())
            {
                //should never hit
                continue;
            }

            if (!this.fifoTransactionModels.get(potmStart)
                .getEquityId()
                .equals(this.fifoTransactionModels.get(j)
                    .getEquityId()))
            {
                //not the same equityId
                break;
            }
//              not important for stocks; want to aggregate them irrespective of purchase date
//            if (!this.fifoTransactionModels.get(potmStart).getDateOpen()
//                    .equals(this.fifoTransactionModels.get(j).getDateOpen())) {
//                //not same open date
//                break;
//            }
            if (!this.fifoTransactionModels.get(potmStart)
                .getDmAcctId()
                .equals(this.fifoTransactionModels.get(j).getDmAcctId()))
            {
                //not same DMAcctId
                break;
            }

            //j transaction should be part of the pctm
            ptm.getFifoOpenTransactionModels()
                .add(new FIFOOpenTransactionModel(this.fifoTransactionModels.get(j)));

            // mark it complete
            this.fifoTransactionModels.get(j).setBComplete(true);
        }
    }

    private void doAttributesPotm(PositionOpenTransactionModel potm)
    {
        Double units;
        Double totalOpen;
        Double totalMktVal;
        Double totalLMktVal;
        Double gain;
        Double gainPct;
        Double totalActPct;
        java.sql.Date dateOpen;

        dateOpen = new java.sql.Date(0);

        units = totalOpen = totalMktVal = totalLMktVal = totalActPct = 0.0;

        for (FIFOOpenTransactionModel ftm : potm.getFifoOpenTransactionModels())
        {
            units += ftm.getUnits();
            totalOpen += ftm.getTotalOpen();
            totalMktVal += ftm.getMktVal();
            totalLMktVal += ftm.getLMktVal();
            //todo: actPct is 0 here; ok as do not use actPct in positions
            //want this to reflect the last date of an opening transaction
            dateOpen = dateOpen.compareTo(ftm.getDateOpen()) > 0
                ? dateOpen
                : ftm.getDateOpen();
        }

        gain = totalOpen + totalMktVal;
        gainPct = 100.0 * gain / abs(totalOpen);

        potm.setUnits(units);
        potm.setTotalOpen(totalOpen);
        potm.setMktVal(totalMktVal);
        potm.setLMktVal(totalLMktVal);
        potm.setActPct(totalActPct);

        potm.setGain(gain);
        potm.setGainPct(gainPct);

        potm.setTransactionType(potm.getFifoOpenTransactionModels().get(0).getTransactionType());

        potm.setFiTId(potm.getFifoOpenTransactionModels().get(0).getFiTId() + "_x");
        potm.setTicker(potm.getFifoOpenTransactionModels().get(0).getTicker());
        potm.setDateOpen(dateOpen);

        potm.setPriceOpen(totalOpen / abs(units));

        potm.setDays(potm.getFifoOpenTransactionModels().get(0).getDays());

        potm.setPositionType(potm.getFifoOpenTransactionModels().get(0).getPositionType());

        potm.setEquityType(potm.getFifoOpenTransactionModels().get(0).getEquityType());

        potm.setBComplete(false);

        potm.setEquityId(potm.getFifoOpenTransactionModels().get(0).getEquityId());

        potm.setTransactionName(potm.getTicker() + " " + this.nameLongShortLeap(potm));
    }
}
