package com.hpi.appcontrollers.positions;

import com.hpi.hpiUtils.OCCclass;
import com.hpi.entities.PositionOpenModel;
import com.hpi.entities.PositionOpenTransactionModel;
import java.text.*;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.lang3.math.NumberUtils;

public class PositionBaseStock {

    private final String[] months = new DateFormatSymbols().getShortMonths();
//    private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
//        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final ArrayList<PositionOpenModel> pomAddList = new ArrayList<>();

    /**
     * determine tacticId for each position
     *
     * @param poms
     */
    public void doPositionsTacticId(ArrayList<PositionOpenModel> poms) {
        PositionOpenModel pomTemp;

        pomAddList.clear();

        for (PositionOpenModel pom : poms) {

            switch (pom.getPositionOpenTransactionModels().size()) {
                case 1:
                    this.doOneLeg(pom);
                    break;
                case 2:
                    this.doTwoLegs(pom);
                    //handle custom result
                    if (pom.getTacticId() == PositionOpenModel.TACTICID_CUSTOM) {
                        pomTemp = new PositionOpenModel(pom);

                        pomTemp.getPositionOpenTransactionModels().remove(0);
                        pomTemp.setPositionType(pomTemp.getPositionOpenTransactionModels().get(0).getTransactionType()
                                .equalsIgnoreCase("buytoopen") ? "LONG" : "SHORT");
                        pomTemp.setTacticId(pomTemp.getPositionOpenTransactionModels().get(0).getTransactionType()
                                .equalsIgnoreCase("buytoopen")
                                ? PositionOpenModel.TACTICID_LONG
                                : PositionOpenModel.TACTICID_SHORT);

                        pom.getPositionOpenTransactionModels().remove(1);
                        pom.setPositionType(pom.getPositionOpenTransactionModels().get(0).getTransactionType()
                                .equalsIgnoreCase("buytoopen") ? "LONG" : "SHORT");
                        pom.setTacticId(pom.getPositionOpenTransactionModels().get(0).getTransactionType()
                                .equalsIgnoreCase("buytoopen")
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

                    if (!pomTemp.getTacticId().equals(PositionOpenModel.TACTICID_CUSTOM)) {
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
                    pomTemp.getPositionOpenTransactionModels().remove(0);

                    this.doTwoLegs(pomTemp);

                    if (!pomTemp.getTacticId().equals(PositionOpenModel.TACTICID_CUSTOM)) {
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

                    if (!pomTemp.getTacticId().equals(PositionOpenModel.TACTICID_CUSTOM)) {
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
                    pomTemp.getPositionOpenTransactionModels().get(0).setEquityId(
                            pomTemp.getPositionOpenTransactionModels().get(0)
                                    .getFifoOpenTransactionModels().get(0).getEquityId());

                    pomTemp.setTacticId(pomTemp.getPositionOpenTransactionModels().get(0).getTransactionType()
                            .equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);
                    pomAddList.add(pomTemp);

                    pomTemp = new PositionOpenModel(pom);
                    pomTemp.getPositionOpenTransactionModels().remove(0);
                    pomTemp.getPositionOpenTransactionModels().remove(1);

                    this.doAttributesPotm2Pom(pomTemp);

                    pomTemp.setTacticId(pomTemp.getPositionOpenTransactionModels().get(0).getTransactionType()
                            .equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);
                    pomAddList.add(pomTemp);

                    pom.getPositionOpenTransactionModels().remove(0);
                    pom.getPositionOpenTransactionModels().remove(0);

                    this.doAttributesPotm2Pom(pom);

                    pom.setTacticId(pom.getPositionOpenTransactionModels().get(0).getTransactionType()
                            .equalsIgnoreCase("buytoopen")
                            ? PositionOpenModel.TACTICID_LONG
                            : PositionOpenModel.TACTICID_SHORT);
                    break;
                case 4:
                    this.doFourLegs(pom);
                    if (pom.getTacticId().equals(PositionOpenModel.TACTICID_CUSTOM)) {
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

        for (PositionOpenModel pom : pomAddList) {
            //add any new positions to the array
            poms.add(pom);
        }
    }

    private void doAttributesPotm2Pom(PositionOpenModel pom) {
        Double units;
        Double totalOpen;
        Double totalMktVal;
        Double totalLMktVal;
        Double totalActPct;
        Double gain;
        Double gainPct;

        totalOpen = totalMktVal = totalLMktVal = totalActPct = 0.0;
        for (PositionOpenTransactionModel potm : pom.getPositionOpenTransactionModels()) {
            totalOpen += potm.getTotalOpen();
            totalMktVal += potm.getMktVal();
            totalLMktVal += potm.getLMktVal();
            totalActPct += potm.getActPct();
        }

        gain = totalMktVal + totalOpen;
        gainPct = 100.0 * gain / Math.abs(totalOpen);

        pom.setUnits(pom.getPositionOpenTransactionModels().get(0).getUnits());
        pom.setGain(gain);
        pom.setGainPct(gainPct);
        pom.setTotalOpen(totalOpen);
//        pom.setMktVal(totalMktVal);
//        pom.setLMktVal(totalLMktVal);
//        pom.setActPct(totalActPct);

        pom.setPositionType(totalOpen < 0 ? "LONG" : "SHORT");

        pom.setTicker(pom.getPositionOpenTransactionModels().get(0).getTicker());
        pom.setDateOpen(pom.getPositionOpenTransactionModels().get(0).getDateOpen());

        pom.setPriceOpen(totalOpen / (pom.getUnits()));
        pom.setPrice(totalMktVal / (pom.getUnits()));

        pom.setDays(pom.getPositionOpenTransactionModels().get(0).getDays());
    }

    private void doOneLeg(PositionOpenModel pom) {
        Double totalOpen;
        Double totalMktVal;
        Double totalLMktVal;
        Double totalActPct;

        if (pom.getPositionOpenTransactionModels().get(0).getTransactionType().equalsIgnoreCase("buytoopen")) {
            pom.setTacticId(PositionOpenModel.TACTICID_LONG);
        }

        if (pom.getPositionOpenTransactionModels().get(0).getTransactionType().equalsIgnoreCase("selltoopen")) {
            pom.setTacticId(PositionOpenModel.TACTICID_SHORT);
        }

//        totalOpen = totalMktVal = totalLMktVal = totalActPct = 0.0;
//        for (PositionOpenTransactionModel potm : pom.getPositionOpenTransactionModels()) {
//            totalOpen += potm.getTotalOpen();
//            totalMktVal += potm.getMktVal();
//            totalLMktVal += potm.getLMktVal();
//            totalActPct += potm.getActPct();
//        }
//
//        pom.setUnits(pom.getPositionOpenTransactionModels().get(0).getUnits());
//
//        pom.setIPrice(totalOpen / (pom.getUnits() * 100.0));
//        pom.setPrice(totalMktVal / (pom.getUnits() * 100.0));
//
//        pom.setGain((totalOpen + totalMktVal));
//        pom.setGainPct(100.0 * pom.getGain() / Math.abs(totalOpen));
//        
//        pom.setMktVal(totalMktVal);
//        pom.setLMktVal(totalLMktVal);
//        pom.setActPct(totalActPct);
//
//        pom.setType(totalOpen > 0.0 ? "SHORT" : "LONG");
//
//        pom.setGmtDtTrade(CMHPIUtils.convertSQLDateToLocalDateTime(
//            pom.getPositionOpenTransactionModels().get(0).getGmtDtTradeOpen()));
    }

    /**
     * Given a 2 leg position, find the tacticId and adjust the position
     * attributes
     *
     * @param aggregatedPositionClosedTransactionModels the transactions of the
     * position aggregated
     * @param pom the position
     */
    private void doTwoLegs(PositionOpenModel pom) {
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
        Double totalLMktVal;
        Double totalActPct;

        potm1 = pom.getPositionOpenTransactionModels().get(0);
        potm2 = pom.getPositionOpenTransactionModels().get(1);

        occTrans1 = new OCCclass(potm1.getEquityId());
        occTrans2 = new OCCclass(potm2.getEquityId());

        if (occTrans1.getPutcall().equalsIgnoreCase(occTrans2.getPutcall())) {
            //all are either put or call
            //vertical, calendar, diagonal
            if (occTrans1.getDtExpiry().equals(occTrans2.getDtExpiry())) {
                //all are either put or call
                //all expiries are the same
                if (potm1.getTransactionType().equals(potm2.getTransactionType())) {
                    //all are either put or call
                    //all expiries are the same
                    //all same transactionType (buytoopen ...) 
                    //hit
                    ret = PositionOpenModel.TACTICID_CUSTOM;
                } else {
                    //all are either put or call
                    //all expiries are the same
                    //all different transactionType (buytoopen ...) 
                    //vertical, collar, calendar, diagonal
                    if (occTrans1.getDStrike().equals(occTrans2.getDStrike())) {
                        //all are either put or call
                        //all expiries are the same
                        //all different transactionType (buytoopen ...) 
                        //all strikes the same
                        //not hit
                        ret = PositionOpenModel.TACTICID_CUSTOM;
                    } else {
                        //all are either put or call
                        //all expiries are the same
                        //all different transactionType (buytoopen ...) 
                        //hit
                        Double double1, double2;
                        double1 = Math.abs(pom.getPositionOpenTransactionModels().get(0).getUnits());
                        double2 = Math.abs(pom.getPositionOpenTransactionModels().get(1).getUnits());
                        ret = double1.equals(double2)
                                ? PositionOpenModel.TACTICID_VERTICAL
                                : PositionOpenModel.TACTICID_VERTICAL_CUSTOM;
                    }
                }
            } else {
                //all are either put or call
                //all expiries are not the same
                //calendar, diagonal
                if (occTrans1.getDStrike().equals(occTrans2.getDStrike())) {
                    //all are either put or call
                    //all expiries are not the same
                    //all strikes are the same
                    //hit
                    if (pom.getPositionOpenTransactionModels().get(0).getUnits().equals(pom
                            .getPositionOpenTransactionModels().get(1).getUnits())) {
                        ret = PositionOpenModel.TACTICID_CALENDAR;
                    } else {
                        ret = PositionOpenModel.TACTICID_CALENDAR_CUSTOM;
                    }
                } else {
                    //all are either put or call
                    //all expiries are not the same
                    //all strikes are not the same
                    //hit
                    if (potm1.getTransactionType().equals(potm2.getTransactionType())) {
                        //all are either put or call
                        //all expiries are not the same
                        //all strikes are not the same
                        //all transactiontype (buytoopen...) same
                        ret = PositionOpenModel.TACTICID_CUSTOM;
                    } else {
                        //all are either put or call
                        //all expiries are not the same
                        //all strikes are not the same
                        //all transactiontype (buytoopen...) not the same
                        if (pom.getPositionOpenTransactionModels().get(0).getUnits().equals(pom
                                .getPositionOpenTransactionModels().get(1).getUnits())) {
                            ret = PositionOpenModel.TACTICID_DIAGONAL;
                        } else {
                            ret = PositionOpenModel.TACTICID_DIAGONAL_CUSTOM;
                        }
                    }
                }
            }
        } else {
            //have puts and calls
            //straddle, strangle, collar
            if (occTrans1.getDtExpiry().equals(occTrans2.getDtExpiry())) {
                //not all calls or puts                
                //all expiries are the same
                if (potm1.getTransactionType().equalsIgnoreCase(potm2.getTransactionType())) {
                    //not all calls or puts                
                    //all expiries are the same
                    //all same transactionType (buytoopen ...)
                    //straddle, strangle
                    if (occTrans1.getDStrike().equals(occTrans2.getDStrike())) {
                        //not all calls or puts                
                        //all expiries are the same
                        //all same transactionType (buytoopen ...)
                        //all strikes the same
                        //not hit
                        if (pom.getPositionOpenTransactionModels().get(0).getUnits().equals(pom
                                .getPositionOpenTransactionModels().get(1).getUnits())) {
                            ret = PositionOpenModel.TACTICID_STRADDLE;
                        } else {
                            ret = PositionOpenModel.TACTICID_STRADDLE_CUSTOM;
                        }
                    } else {
                        //not all calls or puts                
                        //all expiries are the same
                        //all same transactionType (buytoopen ...)
                        //all strikes are not the same
                        //hit
                        if (pom.getPositionOpenTransactionModels().get(0).getUnits().equals(pom
                                .getPositionOpenTransactionModels().get(1).getUnits())) {
                            ret = PositionOpenModel.TACTICID_STRANGLE;
                        } else {
                            ret = PositionOpenModel.TACTICID_STRANGLE_CUSTOM;
                        }

                    }
                } else {
                    //not all calls or puts                
                    //all expiries are the same
                    //all not same transactionType (buytoopen ...)
                    //not hit
                    if (pom.getPositionOpenTransactionModels().get(0).getUnits().equals(pom
                            .getPositionOpenTransactionModels().get(1).getUnits())) {
                        ret = PositionOpenModel.TACTICID_COLLAR;
                    } else {
                        ret = PositionOpenModel.TACTICID_COLLAR_CUSTOM;
                    }
                }
            } else {
                //not all calls or puts
                //all expiries are not the same
                //hit
                ret = PositionOpenModel.TACTICID_CUSTOM;
            }
        }

        pom.setTacticId(ret);

        //set the rest of the positionModel attributes
        pom.setUnits(Double.min(pom.getPositionOpenTransactionModels().get(0).getUnits(),
                pom.getPositionOpenTransactionModels().get(1).getUnits()));

        totalOpen = totalClose = totalMktVal = totalLMktVal = totalActPct = 0.0;
        for (PositionOpenTransactionModel pctm : pom.getPositionOpenTransactionModels()) {
            totalOpen += pctm.getTotalOpen();
            totalMktVal += pctm.getMktVal();
            totalLMktVal += pctm.getLMktVal();
            totalActPct += pctm.getActPct();
        }

        pom.setUnits(pom.getPositionOpenTransactionModels().get(0).getUnits());

        pom.setPriceOpen(totalOpen / ((pom.getUnits() * 100.0)));
        pom.setPrice(totalMktVal / ((pom.getUnits() * 100.0)));

        pom.setGain((totalOpen + totalMktVal));
        pom.setGainPct(100.0 * pom.getGain() / Math.abs(totalOpen));
        
        pom.setTotalOpen(totalOpen);

//        pom.setMktVal(totalMktVal);
//        pom.setLMktVal(totalLMktVal);
//        pom.setActPct(totalActPct);

        pom.setPositionType(totalOpen > 0.0 ? "SHORT" : "LONG");

        pom.setDateOpen(pom.getPositionOpenTransactionModels().get(0).getDateOpen());

        if (pom.getPositionOpenTransactionModels().get(0).getDateOpen()
                .after(pom.getPositionOpenTransactionModels().get(1).getDateOpen())) {
            pom.setDateOpen(pom.getPositionOpenTransactionModels().get(0).getDateOpen());
        } else {
            pom.setDateOpen(pom.getPositionOpenTransactionModels().get(1).getDateOpen());
        }
    }

    /**
     * Use the positionClosedModel.positionOpenTransactionModels to establish
     * tacticId
     *
     * @param pom
     */
    private void doFourLegs(PositionOpenModel pom) {
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
        Double dLMktVal;
        ArrayList<PositionOpenTransactionModel> putsList;
        ArrayList<PositionOpenTransactionModel> callsList;
        Iterator<PositionOpenTransactionModel> potmIterator;
        PositionOpenTransactionModel tempPotm;
        Double totalOpen;
        Double totalMktVal;
        Double totalLMktVal;
        Double totalActPct;

        ret = PositionOpenModel.TACTICID_CUSTOM;
        longPut = shortPut = longCall = shortCall = 0;
        bSameExpiry = bSameUnits = bCredit = true;
        dTotal = dMktVal = dLMktVal = 0.0;
        //units are positive for buy, negative for sell.

        putsList = new ArrayList<>();
        callsList = new ArrayList<>();

        //iterate the position closed transaction array
        potmIterator = pom.getPositionOpenTransactionModels().iterator();

        OCCclass occClassPctm0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        while (potmIterator.hasNext()) {
            tempPotm = potmIterator.next();
            OCCclass occClassPctm = new OCCclass(tempPotm.getEquityId());

            if (occClassPctm.getDtExpiry().equals(occClassPctm0.getDtExpiry())) {
                //same expiry
                if (occClassPctm.getPutcall().equalsIgnoreCase("p")) {
                    //same expiry
                    //put
                    putsList.add(tempPotm);

                    if (tempPotm.getTransactionType().equalsIgnoreCase("buytoopen")) {
                        //same expiry
                        //put
                        //long
                        longPut += 1;
                    } else {
                        //same expiry
                        //put
                        //short
                        shortPut += 1;
                    }
                } else {
                    //same expiry
                    //call
                    callsList.add(tempPotm);
                    if (tempPotm.getTransactionType().equalsIgnoreCase("buytoopen")) {
                        //same expiry
                        //call
                        //long
                        longCall += 1;
                    } else {
                        //same expiry
                        //call
                        //short
                        shortCall += 1;
                    }
                }

                Double d1;
                Double d2;

                d1 = Math.abs(tempPotm.getUnits());
                d2 = Math.abs(pom.getPositionOpenTransactionModels().get(0).getUnits());

                bSameUnits = d1.equals(d2);
            } else {
                //different expiry
                //todo: break up into individual positions
                //  or a pair of calls and a pair of puts
                bSameExpiry = false;
            }

            dTotal += tempPotm.getTotalOpen();
            dMktVal += tempPotm.getMktVal();
            dLMktVal += tempPotm.getLMktVal();
        }

        //todo: separate into positions
        if (!bSameExpiry) {
            pom.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
            return;
        }
        if (!bSameUnits) {
            pom.setTacticId(PositionOpenModel.TACTICID_CUSTOM);
            return;
        }

        //completed run through legs; not using
        bCredit = dTotal > 0;

        //now, what tactic is it?
        totalCall = longCall + shortCall;
        totalPut = longPut + shortPut;

        if (totalPut.equals(0)) {
            //all calls
            //todo: all different strikes
            //condor (calls)
            ret = PositionOpenModel.TACTICID_CONDOR;
        }

        if (totalCall.equals(0)) {
            //all puts
            //todo: all different strikes
            //condor (puts)
            ret = PositionOpenModel.TACTICID_CONDOR;
        }

        if (longPut.equals(shortPut) && longPut.equals(longCall) && longPut
                .equals(shortCall)) {
            //equal parts longPut, shortPut, longCall, shortCall
            //todo: all different strikes
            //iron condor
            ret = PositionOpenModel.TACTICID_IRONCONDOR;
        }

        pom.setTacticId(ret);

        pom.setPositionType(bCredit ? "SHORT" : "LONG");

        totalOpen = totalMktVal = totalLMktVal = totalActPct = 0.0;
        for (PositionOpenTransactionModel potm : pom.getPositionOpenTransactionModels()) {
            totalOpen += potm.getTotalOpen();
            totalMktVal += potm.getMktVal();
            totalLMktVal += potm.getLMktVal();
            totalActPct += potm.getActPct();
        }

        pom.setUnits(pom.getPositionOpenTransactionModels().get(0).getUnits());

        pom.setPriceOpen(totalOpen / ((pom.getUnits() * 100.0)));
        pom.setPrice(totalMktVal / ((pom.getUnits() * 100.0)));

        pom.setGain((totalMktVal + totalOpen));
        pom.setGainPct(100.0 * pom.getGain() / Math.abs(totalOpen));

//        pom.setActPct(totalActPct);

        pom.setPositionType(totalOpen > 0.0 ? "SHORT" : "LONG");

        //latest close date should be the last one?
        //not tested
        pom.setDateOpen(pom.getPositionOpenTransactionModels().get(2).getDateOpen());

//        }
    }

    /**
     * create and update the position name in all positions
     *
     * @param poms array of positionOpenModels
     */
    public void doPositionName(ArrayList<PositionOpenModel> poms) {
        for (PositionOpenModel pom : poms) {

            switch (pom.getTacticId()) {
                case PositionOpenModel.TACTICID_CUSTOM:
                    pom.setPositionName("Custom");
                    break;
                case PositionOpenModel.TACTICID_LONG:
                case PositionOpenModel.TACTICID_SHORT:
                case PositionOpenModel.TACTICID_LEAP:
                    pom.setPositionName(this.nameLongShortLeap(pom));
                    break;
                case PositionOpenModel.TACTICID_VERTICAL:
                    pom.setPositionName(this.nameVertical(pom, false));
                    break;
                case PositionOpenModel.TACTICID_STRANGLE:
                    pom.setPositionName(this.nameStrangle(pom, false));
                    break;
                case PositionOpenModel.TACTICID_CALENDAR:
                    pom.setPositionName(this.nameCalendar(pom, false));
                    break;
                case PositionOpenModel.TACTICID_COVERED:
                    pom.setPositionName(this.nameCovered(pom));
                    break;
                case PositionOpenModel.TACTICID_STRADDLE:
                    pom.setPositionName(this.nameStraddle(pom, false));
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
                    pom.setPositionName(this.nameCollar(pom, false));
                    break;
                case PositionOpenModel.TACTICID_DIAGONAL:
                    pom.setPositionName(this.nameDiagonal(pom, false));
                    break;
                case PositionOpenModel.TACTICID_VERTICAL_CUSTOM:
                    pom.setPositionName(this.nameVertical(pom, true));
                    break;
                case PositionOpenModel.TACTICID_STRANGLE_CUSTOM:
                    pom.setPositionName(this.nameStrangle(pom, true));
                    break;
                case PositionOpenModel.TACTICID_CALENDAR_CUSTOM:
                    pom.setPositionName(this.nameCalendar(pom, true));
                    break;
                case PositionOpenModel.TACTICID_STRADDLE_CUSTOM:
                    pom.setPositionName(this.nameStraddle(pom, true));
                    break;
                case PositionOpenModel.TACTICID_COLLAR_CUSTOM:
                    pom.setPositionName(this.nameCollar(pom, true));
                    break;
                case PositionOpenModel.TACTICID_DIAGONAL_CUSTOM:
                    pom.setPositionName(this.nameDiagonal(pom, true));
                    break;
                default:
                    int i = 0;
            }
        }
    }

    String nameLongShortLeap(PositionOpenModel pom) {

        return pom.getTacticId() == PositionOpenModel.TACTICID_LONG 
                ? pom.getEquityId() + " LONG" 
                : pom.getEquityId() + " SHORT";
    }

    String nameLongShortLeap(PositionOpenTransactionModel potm) {

        //return potm.getTransactionType().equalsIgnoreCase("buytoopen") ? "LONG" : "SHORT";
        return potm.getTransactionType().equalsIgnoreCase("buy") ? "LONG" : "SHORT";
    }
    
    String nameVertical(PositionOpenModel pom, Boolean bCustom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());

        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().get(1).getEquityId());

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
        if (bCustom) {
            positionName += " Cstm";
        }

        return positionName;
    }

    String nameStrangle(PositionOpenModel pom, Boolean bCustom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().get(1).getEquityId());

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
        if (bCustom) {
            positionName += " Cstm";
        }

        return positionName;
    }

    String nameCalendar(PositionOpenModel pom, Boolean bCustom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().get(1).getEquityId());

        //aapl 150 ddJanYY/ddFebYY Calendar
        positionName = closedClass0.getTicker();
        positionName += " ";
        positionName += closedClass0.getStrike();
        positionName += " ";
        positionName += closedClass0.getExpDay();
        month = NumberUtils.toInt(closedClass0.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass0.getExpYear();
//        positionName += " ";
        positionName += "/";
        positionName += closedClass1.getExpDay();
        month = NumberUtils.toInt(closedClass1.getExpMonth());
        positionName += months[month - 1];
        positionName += closedClass1.getExpYear();
        positionName += " ";
        positionName += " Calndr";
        if (bCustom) {
            positionName += " Cstm";
        }

        return positionName;
    }

    String nameCovered(PositionOpenModel pom) {
        return "Covered";
    }

    String nameStraddle(PositionOpenModel pom, Boolean bCustom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().get(1).getEquityId());

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
        if (bCustom) {
            positionName += " Cstm";
        }

        return positionName;
    }

    String nameIronCondor(PositionOpenModel pom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().get(1).getEquityId());
        closedClass2 = new OCCclass(pom.getPositionOpenTransactionModels().get(2).getEquityId());
        closedClass3 = new OCCclass(pom.getPositionOpenTransactionModels().get(3).getEquityId());

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

    String nameButterfly(PositionOpenModel pom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().get(1).getEquityId());
        closedClass2 = new OCCclass(pom.getPositionOpenTransactionModels().get(2).getEquityId());
        closedClass3 = new OCCclass(pom.getPositionOpenTransactionModels().get(3).getEquityId());

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

    String nameCondor(PositionOpenModel pom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;
        OCCclass closedClass2;
        OCCclass closedClass3;

        closedClass0 = new OCCclass(pom.getPositionOpenTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pom.getPositionOpenTransactionModels().get(1).getEquityId());
        closedClass2 = new OCCclass(pom.getPositionOpenTransactionModels().get(2).getEquityId());
        closedClass3 = new OCCclass(pom.getPositionOpenTransactionModels().get(3).getEquityId());

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

    String nameCollar(PositionOpenModel pcm, Boolean bCustom) {
        String positionName;
        Integer month;
        OCCclass closedClass0;
        OCCclass closedClass1;

        closedClass0 = new OCCclass(pcm.getPositionOpenTransactionModels().get(0).getEquityId());
        closedClass1 = new OCCclass(pcm.getPositionOpenTransactionModels().get(1).getEquityId());

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
        if (bCustom) {
            positionName += " Cstm";
        }

        return positionName;
    }

    String nameDiagonal(PositionOpenModel pcm, Boolean bCustom) {
        String positionName;
        Integer month;
        OCCclass class0;
        OCCclass class1;

        class0 = new OCCclass(pcm.getPositionOpenTransactionModels().get(0).getEquityId());
        class1 = new OCCclass(pcm.getPositionOpenTransactionModels().get(1).getEquityId());

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
        if (bCustom) {
            positionName += " Cstm";
        }

        return positionName;
    }
}
