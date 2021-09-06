/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.Iterator;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxInvSell
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvTran invTran;
    OfxSecId secId;
    Double units;
    Double unitPrice;
    Double markdown;
    Double commission;
    Double taxes;
    Double fees;
    Double load;
    Double withholding;
    String taxExempt;
    Double total;
    Double gain;
    OfxCurrency currency;
    OfxCurrency origCurrency;
    String subAcctSec;
    String subAcctFund;
    String loanId;
    Double stateWithholding;
    Double penalty;
    String inv401kSource;

    // private final String errorPrefix;

    public OfxInvSell()
    {
        this.invTran = new OfxInvTran();
        this.secId = new OfxSecId();
        this.units = null;
        this.unitPrice = null;
        this.markdown = null;
        this.commission = null;
        this.taxes = null;
        this.fees = null;
        this.load = null;
        this.withholding = null;
        this.taxExempt = null;
        this.total = null;
        this.gain = null;
        this.currency = new OfxCurrency();
        this.origCurrency = new OfxCurrency();
        this.subAcctSec = null;
        this.subAcctFund = null;
        this.loanId = null;
        this.stateWithholding = null;
        this.penalty = null;
        this.inv401kSource = null;
        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invsell>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "invtran":
                    this.invTran.doData(element);
                    break;
                case "secid":
                    this.secId.doData(element);
                    break;
                case "units":
                    this.units = Double.parseDouble(element.ownText());
                    break;
                case "unitprice":
                    this.unitPrice = Double.parseDouble(element.ownText());
                    break;
                case "markdown":
                    this.markdown = Double.parseDouble(element.ownText());
                    break;
                case "commission":
                    this.commission = Double.parseDouble(element.ownText());
                    break;
                case "taxes":
                    this.taxes = Double.parseDouble(element.ownText());
                    break;
                case "fees":
                    this.fees = Double.parseDouble(element.ownText());
                    break;
                case "load":
                    this.load = Double.parseDouble(element.ownText());
                    break;
                case "withholding":
                    this.withholding = Double.parseDouble(element.ownText());
                    break;
                case "taxexempt":
                    this.taxExempt = element.ownText();
                    break;
                case "total":
                    this.total = Double.parseDouble(element.ownText());
                    break;
                case "gain":
                    this.gain = Double.parseDouble(element.ownText());
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                case "origcurrency":
                    this.origCurrency.doData(element);
                    break;
                case "subacctsec":
                    this.subAcctSec = element.ownText();
                    break;
                case "subacctfund":
                    this.subAcctFund = element.ownText();
                    break;
                case "loanid":
                    this.loanId = element.ownText();
                    break;
                case "statewithholding":
                    this.stateWithholding = Double.parseDouble(element.ownText());
                    break;
                case "penalty":
                    this.penalty = Double.parseDouble(element.ownText());
                    break;
                case "inv401ksource":
                    this.inv401kSource = element.ownText();
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

                    //Logger.getLogger(this.getClass()).info(s);
            }
        }
        return true;
    }

    @Override
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        this.invTran.doSQL(invAcctFrom);
//        this.secId.doSQL(invAcctFrom);
        this.currency.doSQL(invAcctFrom);
        this.origCurrency.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.InvSell";

        String[] keys =
        {
            "AcctId", "FiTId", "SecId", "Units", "UnitPrice", "Markdown",
            "Commission", "Taxes", "Fees", "TransLoad", "Withholding",
            "TaxExempt", "Total", "Gain", "CurSym", "OrigCurSym", "SubAcctSec",
            "SubAcctFund", "LoanId", "StateWithholding", "Penalty",
            "Inv401kSource"

        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
            this.secId.uniqueId, this.units.toString(),
            String.valueOf(this.unitPrice),String.valueOf(this.markdown),
            String.valueOf(this.commission),String.valueOf(this.taxes),
            String.valueOf(this.fees),String.valueOf(this.load),
            String.valueOf(this.withholding), this.taxExempt,
            String.valueOf(this.total),
            String.valueOf(this.gain),
            this.currency.curSym,
            this.origCurrency.curSym, this.subAcctSec, this.subAcctFund,
            this.loanId, String.valueOf(this.stateWithholding),
            String.valueOf(this.penalty), this.inv401kSource
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}
