/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.ArrayList;
import java.util.Iterator;
import org.jsoup.nodes.Element;


/**
 *
 */
public class OfxInvStmtRS
        extends OfxAggregateBase
{

    String invStmtRSDtAsOf;
    String invStmtRSCurDef;
    OfxInvAcctFrom invAcctFrom;
    ArrayList<OfxInvBankTran> invBankTranList;
    String invTranListDtStart;
    String invTranListDtEnd;
    ArrayList<OfxInvBuyDebt> invBuyDebtList;
    ArrayList<OfxInvSellDebt> invSellDebtList;
    ArrayList<OfxInvBuyMF> invBuyMFList;
    ArrayList<OfxInvSellMF> invSellMFList;
    ArrayList<OfxInvBuyOpt> invBuyOptList;
    ArrayList<OfxInvSellOpt> invSellOptList;
    ArrayList<OfxInvBuyOther> invBuyOtherList;
    ArrayList<OfxInvSellOther> invSellOtherList;
    ArrayList<OfxInvBuyStock> invBuyStockList;
    ArrayList<OfxInvSellStock> invSellStockList;
    ArrayList<OfxInvClosureOpt> invClosureOptList;
    ArrayList<OfxInvIncome> invIncomeList;
    ArrayList<OfxInvExpense> invExpenseList;
    ArrayList<OfxInvJrnlFund> invJrnlFundList;
    ArrayList<OfxInvJrnlSec> invJrnlSecList;
    ArrayList<OfxInvMarginInterest> invMarginInterestList;
    ArrayList<OfxInvReinvest> invReinvestList;
    ArrayList<OfxInvRetOfCap> invRetOfCapList;
    ArrayList<OfxInvSplit> invSplitList;
    ArrayList<OfxInvTransfer> invTransferList;
    ArrayList<OfxPosDebt> invPosDebtList;
    ArrayList<OfxPosMF> invPosMFList;
    ArrayList<OfxPosOpt> invPosOptList;
    ArrayList<OfxPosOther> invPosOtherList;
    ArrayList<OfxPosStock> invPosStockList;
    ArrayList<OfxOOBuyDebt> ooBuyDebtList;
    ArrayList<OfxOOBuyMF> ooBuyMFList;
    ArrayList<OfxOOBuyOpt> ooBuyOptList;
    ArrayList<OfxOOBuyOther> ooBuyOtherList;
    ArrayList<OfxOOBuyStock> ooBuyStockList;
    ArrayList<OfxOOSellDebt> ooSellDebtList;
    ArrayList<OfxOOSellMF> ooSellMFList;
    ArrayList<OfxOOSellOpt> ooSellOptList;
    ArrayList<OfxOOSellOther> ooSellOtherList;
    ArrayList<OfxOOSellStock> ooSellStockList;
    ArrayList<OfxOOSwitchMF> ooSwitchMFList;
    OfxInvBal invBal;
    String mktgInfo;
    OfxInv401k inv401k;
    OfxInv401kBal inv401kBal;
    private final String errorPrefix;

    public OfxInvStmtRS()
    {
        this.invStmtRSDtAsOf = null;
        this.invStmtRSCurDef = null;
        this.invAcctFrom = new OfxInvAcctFrom();
        this.invBankTranList = new ArrayList<>();
        this.invTranListDtStart = null;
        this.invTranListDtEnd = null;
        this.invBuyDebtList = new ArrayList<>();
        this.invSellDebtList = new ArrayList<>();
        this.invBuyMFList = new ArrayList<>();
        this.invSellMFList = new ArrayList<>();
        this.invBuyOptList = new ArrayList<>();
        this.invSellOptList = new ArrayList<>();
        this.invBuyOtherList = new ArrayList<>();
        this.invSellOtherList = new ArrayList<>();
        this.invBuyStockList = new ArrayList<>();
        this.invSellStockList = new ArrayList<>();
        this.invClosureOptList = new ArrayList<>();
        this.invIncomeList = new ArrayList<>();
        this.invExpenseList = new ArrayList<>();
        this.invJrnlFundList = new ArrayList<>();
        this.invJrnlSecList = new ArrayList<>();
        this.invMarginInterestList = new ArrayList<>();
        this.invReinvestList = new ArrayList<>();
        this.invRetOfCapList = new ArrayList<>();
        this.invSplitList = new ArrayList<>();
        this.invTransferList = new ArrayList<>();

        this.invPosDebtList = new ArrayList<>();
        this.invPosMFList = new ArrayList<>();
        this.invPosOptList = new ArrayList<>();
        this.invPosOtherList = new ArrayList<>();
        this.invPosStockList = new ArrayList<>();
        this.ooBuyDebtList = new ArrayList<>();
        this.ooBuyMFList = new ArrayList<>();
        this.ooBuyOptList = new ArrayList<>();
        this.ooBuyOtherList = new ArrayList<>();
        this.ooBuyStockList = new ArrayList<>();
        this.ooSellDebtList = new ArrayList<>();
        this.ooSellMFList = new ArrayList<>();
        this.ooSellOptList = new ArrayList<>();
        this.ooSellOtherList = new ArrayList<>();
        this.ooSellStockList = new ArrayList<>();
        this.ooSwitchMFList = new ArrayList<>();
        this.invBal = new OfxInvBal();
        this.mktgInfo = null;
        this.inv401k = new OfxInv401k();
        this.inv401kBal = new OfxInv401kBal();

        this.errorPrefix = this.getClass().getName();
    }

    /**
     *
     * @param aElement
     * @param signOnMsgsRSv1
     * @return
     */
    public Boolean doData(Element aElement, OfxSignOnMsgsRSv1 signOnMsgsRSv1)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invstmtrs>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "dtasof":
                    this.invStmtRSDtAsOf = element.ownText();
                    this.invAcctFrom.setStmtDtAsOf(invStmtRSDtAsOf);
                    break;
                case "curdef":
                    this.invStmtRSCurDef = element.ownText();
                    break;
                case "invacctfrom":
                    this.invAcctFrom.doData(element, signOnMsgsRSv1);
                    break;
                case "invtranlist":
                    if (!doInvTranList(element, signOnMsgsRSv1))
                    {
                        return false;
                    }
                    break;
                case "invposlist":
                    if (!doInvPosList(element))
                    {
                        return false;
                    }
                    break;
                case "invbal":
                    this.invBal.doData(element);
                    break;
                case "invoolist":
                    if (!doInvOOList(element))
                    {
                        return false;
                    }
                    break;
                case "mktginfo":
                    this.mktgInfo = element.ownText();
                    break;
                case "inv401k":
                    this.inv401k.doData(element);
                    break;
                case "inv401kbal":
                    this.inv401kBal.doData(element);
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());
            }
        }
        return true;
    }

    private Boolean doInvTranList(Element aElement, OfxSignOnMsgsRSv1 signOnMsgsRSv1)
    {
        //tested
        OfxInvBankTran invBankTran;
        OfxInvBuyDebt buyDebt;
        OfxInvSellDebt sellDebt;
        OfxInvBuyMF buyMF;
        OfxInvSellMF sellMF;
        OfxInvBuyOpt buyOpt;
        OfxInvSellOpt sellOpt;
        OfxInvBuyOther buyOther;
        OfxInvSellOther sellOther;
        OfxInvBuyStock buyStock;
        OfxInvSellStock sellStock;
        OfxInvClosureOpt closureOpt;
        OfxInvIncome income;
        OfxInvExpense expense;
        OfxInvJrnlFund jrnlFund;
        OfxInvJrnlSec jrnlSec;
        OfxInvMarginInterest marginInterest;
        OfxInvReinvest reinvest;
        OfxInvRetOfCap retOfCap;
        OfxInvSplit split;
        OfxInvTransfer transfer;
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invtranlist>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "dtstart":
                    this.invTranListDtStart = element.ownText();
                    break;
                case "dtend":
                    this.invTranListDtEnd = element.ownText();
                    break;
                case "buydebt":
                    buyDebt = new OfxInvBuyDebt();
                    if (buyDebt.doData(element))
                    {
                        this.invBuyDebtList.add(buyDebt);
                    }
                    break;
                case "buymf":
                    buyMF = new OfxInvBuyMF();
                    if (buyMF.doData(element))
                    {
                        this.invBuyMFList.add(buyMF);
                    }
                    break;
                case "buyopt":
                    buyOpt = new OfxInvBuyOpt();
                    if (buyOpt.doData(element))
                    {
                        this.invBuyOptList.add(buyOpt);
                    }
                    break;
//                case "buyother":
//                    buyOther = new OfxInvBuyOther();
//                    if (buyOther.doData(element))
//                    {
//                        this.invBuyOtherList.add(buyOther);
//                    }
//                    break;
                case "buystock":
                    buyStock = new OfxInvBuyStock();
                    if (buyStock.doData(element))
                    {
                        this.invBuyStockList.add(buyStock);
                    }
                    break;
                case "closureopt":
                    closureOpt = new OfxInvClosureOpt();
                    if (closureOpt.doData(element))
                    {
                        this.invClosureOptList.add(closureOpt);
                    }
                    break;
                case "income":
                    income = new OfxInvIncome();
                    if (income.doData(element))
                    {
                        this.invIncomeList.add(income);
                    }
                    break;
                case "invexpense":
                    expense = new OfxInvExpense();
                    if (expense.doData(element))
                    {
                        this.invExpenseList.add(expense);
                    }
                    break;
                case "jrnlfund":
                    jrnlFund = new OfxInvJrnlFund();
                    if (jrnlFund.doData(element))
                    {
                        this.invJrnlFundList.add(jrnlFund);
                    }
                    break;
                case "jrnlsec":
                    jrnlSec = new OfxInvJrnlSec();
                    if (jrnlSec.doData(element))
                    {
                        this.invJrnlSecList.add(jrnlSec);
                    }
                    break;
                case "margininterest":
                    marginInterest = new OfxInvMarginInterest();
                    if (marginInterest.doData(element))
                    {
                        this.invMarginInterestList.add(marginInterest);
                    }
                    break;
                case "reinvest":
                    reinvest = new OfxInvReinvest();
                    if (reinvest.doData(element))
                    {
                        this.invReinvestList.add(reinvest);
                    }
                    break;
                case "retofcap":
                    retOfCap = new OfxInvRetOfCap();
                    if (retOfCap.doData(element))
                    {
                        this.invRetOfCapList.add(retOfCap);
                    }
                    break;
                case "selldebt":
                    sellDebt = new OfxInvSellDebt();
                    if (sellDebt.doData(element))
                    {
                        this.invSellDebtList.add(sellDebt);
                    }
                    break;
                case "sellmf":
                    sellMF = new OfxInvSellMF();
                    if (sellMF.doData(element))
                    {
                        this.invSellMFList.add(sellMF);
                    }
                    break;
                case "sellopt":
                    sellOpt = new OfxInvSellOpt();
                    if (sellOpt.doData(element))
                    {
                        this.invSellOptList.add(sellOpt);
                    }
                    break;
//                case "sellother":
//                    sellOther = new OfxInvSellOther();
//                    if (sellOther.doData(element))
//                    {
//                        this.invSellOtherList.add(sellOther);
//                    }
//                    break;
                case "sellstock":
                    sellStock = new OfxInvSellStock();
                    if (sellStock.doData(element))
                    {
                        this.invSellStockList.add(sellStock);
                    }
                    break;
                case "invbanktran":
                    invBankTran = new OfxInvBankTran();
                    if (invBankTran.doData(element))
                    {
                        this.invBankTranList.add(invBankTran);
                    }
                    break;
//                case "split":
//                    split = new OfxInvSplit();
//                    if (split.doData(element))
//                    {
//                        this.invSplitList.add(split);
//                    }
//                    break;
                case "transfer":
                    transfer = new OfxInvTransfer();
                    if (transfer.doData(element, signOnMsgsRSv1))
                    {
                        this.invTransferList.add(transfer);
                    }
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());
            }
        }
        return true;
    }

    private Boolean doInvPosList(Element aElement)
    {
        //tested
        OfxPosDebt posDebt;
        OfxPosMF posMF;
        OfxPosOpt posOpt;
        OfxPosOther posOther;
        OfxPosStock posStock;
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invposlist>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "posdebt":
                    posDebt = new OfxPosDebt();
                    if (posDebt.doData(element))
                    {
                        this.invPosDebtList.add(posDebt);
                    }
                    break;
                case "posmf":
                    posMF = new OfxPosMF();
                    if (posMF.doData(element))
                    {
                        this.invPosMFList.add(posMF);
                    }
                    break;
                case "posopt":
                    posOpt = new OfxPosOpt();
                    if (posOpt.doData(element))
                    {
                        this.invPosOptList.add(posOpt);
                    }
                    break;
                case "posother":
                    posOther = new OfxPosOther();
                    if (posOther.doData(element))
                    {
                        this.invPosOtherList.add(posOther);
                    }
                    break;
                case "posstock":
                    posStock = new OfxPosStock();
                    if (posStock.doData(element))
                    {
                        this.invPosStockList.add(posStock);
                    }
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());
            }
        }
        return true;
    }

    private Boolean doInvOOList(Element aElement)
    {
        OfxOOBuyDebt ooBuyDebt;
        OfxOOBuyMF ooBuyMF;
        OfxOOBuyOpt ooBuyOpt;
        OfxOOBuyOther ooBuyOther;
        OfxOOBuyStock ooBuyStock;
        OfxOOSellDebt ooSellDebt;
        OfxOOSellMF ooSellMF;
        OfxOOSellOpt ooSellOpt;
        OfxOOSellOther ooSellOther;
        OfxOOSellStock ooSellStock;
        OfxOOSwitchMF switchMF;

        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invoolist>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "oobuydebt":
                    ooBuyDebt = new OfxOOBuyDebt();
                    if (ooBuyDebt.doData(element))
                    {
                        this.ooBuyDebtList.add(ooBuyDebt);
                    }
                    break;
                case "oobuymf":
                    ooBuyMF = new OfxOOBuyMF();
                    if (ooBuyMF.doData(element))
                    {
                        this.ooBuyMFList.add(ooBuyMF);
                    }
                    break;
                case "oobuyopt":
                    ooBuyOpt = new OfxOOBuyOpt();
                    if (ooBuyOpt.doData(element))
                    {
                        this.ooBuyOptList.add(ooBuyOpt);
                    }
                    break;
                case "oobuyother":
                    ooBuyOther = new OfxOOBuyOther();
                    if (ooBuyOther.doData(element))
                    {
                        this.ooBuyOtherList.add(ooBuyOther);
                    }
                    break;
                case "oobuystock":
                    ooBuyStock = new OfxOOBuyStock();
                    if (ooBuyStock.doData(element))
                    {
                        this.ooBuyStockList.add(ooBuyStock);
                    }
                    break;
                case "ooselldebt":
                    ooSellDebt = new OfxOOSellDebt();
                    if (ooSellDebt.doData(element))
                    {
                        this.ooSellDebtList.add(ooSellDebt);
                    }
                    break;
                case "oosellmf":
                    ooSellMF = new OfxOOSellMF();
                    if (ooSellMF.doData(element))
                    {
                        this.ooSellMFList.add(ooSellMF);
                    }
                    break;
                case "oosellopt":
                    ooSellOpt = new OfxOOSellOpt();
                    if (ooSellOpt.doData(element))
                    {
                        this.ooSellOptList.add(ooSellOpt);
                    }
                    break;
                case "oosellother":
                    ooSellOther = new OfxOOSellOther();
                    if (ooSellOther.doData(element))
                    {
                        this.ooSellOtherList.add(ooSellOther);
                    }
                    break;
                case "oosellstock":
                    ooSellStock = new OfxOOSellStock();
                    if (ooSellStock.doData(element))
                    {
                        this.ooSellStockList.add(ooSellStock);
                    }
                    break;
                case "switchmf":
                    switchMF = new OfxOOSwitchMF();
                    if (switchMF.doData(element))
                    {
                        this.ooSwitchMFList.add(switchMF);
                    }
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());
            }
        }
        return true;
    }

    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        Iterator iterator;

        //already completed before this.
        //this.invAcctFrom.doSQL();
        iterator = this.invBankTranList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invBuyDebtList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invSellDebtList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invBuyMFList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invSellMFList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invBuyOptList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invSellOptList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invBuyOtherList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invSellOtherList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invBuyStockList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invSellStockList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invClosureOptList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invIncomeList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invExpenseList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invJrnlFundList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invJrnlSecList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }
//todo: imageData handling
//        iterator = this.imageData.iterator();
//        while (iterator.hasNext())
//        {
//            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
//        }
        iterator = this.invMarginInterestList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invReinvestList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invRetOfCapList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invSplitList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invTransferList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invPosDebtList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invPosMFList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invPosOptList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invPosOtherList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

        iterator = this.invPosStockList.iterator();
        while (iterator.hasNext())
        {
            ((IOfxSQL) iterator.next()).doSQL(invAcctFrom);
        }

//todo: open orders; not required for our needs
//this.ooBuyDebtList.doSQL(BrokerId);
//this.ooBuyMFList.doSQL(BrokerId);
//this.ooBuyOptList.doSQL(BrokerId);
//this.ooBuyOtherList.doSQL(BrokerId);
//this.ooBuyStockList.doSQL(BrokerId);
//this.ooSellDebtList.doSQL(BrokerId);
//this.ooSellMFList.doSQL(BrokerId);
//this.ooSellOptList.doSQL(BrokerId);
//this.ooSellOtherList.doSQL(BrokerId);
//this.ooSellStockList.doSQL(BrokerId);
//this.ooSwitchMFList.doSQL(BrokerId);
    this.invBal.doSQL(invAcctFrom, this.invTranListDtEnd, this.invStmtRSDtAsOf);
// todo: not implemented; not required for our needs
//this.mktgInfo.doSQL(BrokerId);
//this.inv401k.doSQL(BrokerId);
//this.inv401kBal.doSQL(BrokerId);
        return true;
    }

    public OfxInvAcctFrom getInvAcctFrom()
    {
        return invAcctFrom;
    }
}
