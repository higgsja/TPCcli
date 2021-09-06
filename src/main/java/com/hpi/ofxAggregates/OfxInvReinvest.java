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
public class OfxInvReinvest
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvTran invTran;
    OfxSecId secId;
    String incomeType;
    Double total;
    String subAcctSec;
    Double units;
    Double unitPrice;
    Double commission;
    Double taxes;
    Double fees;
    Double load;
    String taxExempt;
    OfxCurrency currency;
    OfxCurrency origCurrency;
    String inv401kSource;

    // private final String errorPrefix;

    public OfxInvReinvest()
    {
        this.invTran = new OfxInvTran();
        this.secId = new OfxSecId();
        this.incomeType = null;
        this.total = null;
        this.subAcctSec = null;
        this.unitPrice = null;
        this.commission = null;
        this.taxes = null;
        this.fees = null;
        this.load = null;
        this.taxExempt = null;
        this.currency = new OfxCurrency();
        this.origCurrency = new OfxCurrency();
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

        // aElement points to <reinvest>
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
                case "incometype":
                    this.incomeType = element.ownText();
                    break;
                case "total":
                    this.total = Double.parseDouble(element.ownText());
                    break;
                case "subacctsec":
                    this.subAcctSec = element.ownText();
                    break;
                case "units":
                    this.units = Double.parseDouble(element.ownText());
                    break;
                case "unitprice":
                    this.unitPrice = Double.parseDouble(element.ownText());
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
                case "taxexempt":
                    this.taxExempt = element.ownText();
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                case "origcurrency":
                    this.origCurrency.doData(element);
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

        String sTable = "hlhtxc5_dbOfx.Reinvest";

        String[] keys =
        {
            "AcctId", "FiTId",
            "SecId", "IncomeType", "Total",
            "SubAcctSec", "Units",
            "UnitPrice", "Commission",
            "Taxes", "Fees",
            "ReinvLoad", "TaxExempt",
            "Currency", "OrigCurrency",
            "Inv401kSource"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
            this.secId.uniqueId, this.incomeType, String.valueOf(this.total),
            this.subAcctSec, String.valueOf(this.units),
            String.valueOf(this.unitPrice), String.valueOf(this.commission),
            String.valueOf(this.taxes), String.valueOf(this.fees),
            String.valueOf(this.load), this.taxExempt,
            this.currency.curSym, this.origCurrency.curSym,
            this.inv401kSource
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}
