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
public class OfxInvSplit
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvTran invTran;
    OfxSecId secId;
    String subAcctSec;
    Double oldUnits;
    Double newUnits;
    Double numerator;
    Double denominator;
    OfxCurrency currency;
    OfxCurrency origCurrency;
    Double fracCash;
    String subAcctFund;
    String inv401kSource;

    // private final String errorPrefix;

    public OfxInvSplit()
    {
        this.invTran = new OfxInvTran();
        this.secId = new OfxSecId();
        this.subAcctSec = null;
        this.oldUnits = null;
        this.newUnits = null;
        this.numerator = null;
        this.denominator = null;
        this.currency = new OfxCurrency();
        this.origCurrency = new OfxCurrency();
        this.fracCash = null;
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

        // aElement points to <split>
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
                case "subacctsec":
                    this.subAcctSec = element.ownText();
                    break;
                case "oldunits":
                    this.oldUnits = Double.parseDouble(element.ownText());
                    break;
                case "newunits":
                    this.newUnits = Double.parseDouble(element.ownText());
                    break;
                case "numerator":
                    this.numerator = Double.parseDouble(element.ownText());
                    break;
                case "denominator":
                    this.denominator = Double.parseDouble(element.ownText());
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                case "origcurrency":
                    this.origCurrency.doData(element);
                    break;
                case "fraccash":
                    this.fracCash = Double.parseDouble(element.ownText());
                    break;
                case "subacctfund":
                    this.subAcctFund = element.ownText();
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

        String sTable = "hlhtxc5_dbOfx.Split";

        String[] keys =
        {
            "AcctId", "FiTId",
            "SecId", "SubAcctSec",
            "OldUnits", "NewUnits",
            "Numerator", "Denominator",
            "Currency", "OrigCurrency",
            "FracCash", "SubAcctFund", "Inv401kSource"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
            this.secId.uniqueId, this.subAcctSec,
            String.valueOf(this.oldUnits), String.valueOf(this.newUnits),
            String.valueOf(this.numerator), String.valueOf(this.denominator),
            this.currency.curSym, this.origCurrency.curSym,
            String.valueOf(this.fracCash), this.subAcctFund, this.inv401kSource
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}
