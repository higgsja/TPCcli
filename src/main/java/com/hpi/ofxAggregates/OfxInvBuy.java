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
public class OfxInvBuy
        extends OfxAggregateBase
        implements IOfxSQL {

    OfxInvTran invTran;
    OfxSecId secId;
    Double units;
    Double unitPrice;
    Double markup;
    Double commission;
    Double taxes;
    Double fees;
    Double load;
    Double total;
    OfxCurrency currency;
    OfxCurrency origCurrency;
    String subAcctSec;
    String subAcctFund;
    String loanId;
    Double loanPrincipal;
    Double loanInterest;
    String inv401kSource;
    String dtPayroll;
    String priorYearContrib;

    // private final String errorPrefix;
    public OfxInvBuy() {
        this.invTran = new OfxInvTran();
        this.secId = new OfxSecId();
        this.units = null;
        this.unitPrice = null;
        this.markup = null;
        this.commission = null;
        this.taxes = null;
        this.fees = null;
        this.load = null;
        this.total = null;
        this.currency = new OfxCurrency();
        this.origCurrency = new OfxCurrency();
        this.subAcctSec = null;
        this.subAcctFund = null;
        this.loanId = null;
        this.loanPrincipal = null;
        this.loanInterest = null;
        this.inv401kSource = null;
        this.dtPayroll = null;
        this.priorYearContrib = null;
        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement) {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invbuy>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
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
                case "markup":
                    this.markup = Double.parseDouble(element.ownText());
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
                case "total":
                    this.total = Double.parseDouble(element.ownText());
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
                case "loanprincipal":
                    this.loanPrincipal = Double.parseDouble(element.ownText());
                    break;
                case "loaninterest":
                    this.loanInterest = Double.parseDouble(element.ownText());
                    break;
                case "inv401ksource":
                    this.inv401kSource = element.ownText();
                    break;
                case "dtpayroll":
                    this.dtPayroll = element.ownText();
                    break;
                case "prioryearcontrib":
                    this.priorYearContrib = element.ownText();
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

//                    //Logger.getLogger(this.getClass()).info(s);
            }
        }
        return true;
    }

    @Override
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom) {
        this.invTran.doSQL(invAcctFrom);
//        this.secId.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.InvBuy";

        String[] keys
                = {
                    "AcctId", "FiTId", "SecId", "Units", "UnitPrice", "Markup",
                    "Commission", "Taxes", "Fees", "TransLoad", "Total", "CurSym",
                    "OrigCurSym", "SubAcctSec", "SubAcctFund", "LoanId", "LoanPrincipal",
                    "LoanInterest", "Inv401kSource", "DtPayroll", "PriorYearContrib"

                };
        String[] values
                = {
                    invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
                    this.secId.uniqueId, String.valueOf(this.units),
                    String.valueOf(this.unitPrice), String.valueOf(this.markup),
                    String.valueOf(this.commission), String.valueOf(this.taxes),
                    String.valueOf(this.fees), String.valueOf(this.load),
                    String.valueOf(this.total), this.currency.curSym,
                    this.origCurrency.curSym, this.subAcctSec,
                    this.subAcctFund, this.loanId,
                    String.valueOf(this.loanPrincipal), String.valueOf(this.loanInterest),
                    this.inv401kSource, this.dtPayroll, this.priorYearContrib

                };

        return this.doSQL(sTable, keys, values, 2);
    }
}
