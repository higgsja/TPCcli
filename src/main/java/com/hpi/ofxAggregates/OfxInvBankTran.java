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
public class OfxInvBankTran
        extends OfxAggregateBase
        implements IOfxSQL {

    OfxStmtTrn stmtTrn;
    String subAcctFund;
    String fid;

    // private final String errorPrefix;
    public OfxInvBankTran() {
        this.stmtTrn = new OfxStmtTrn();
        this.subAcctFund = null;
        this.fid = null;

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

        // aElement points to <invbanktran>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "stmttrn":
                    this.stmtTrn.doData(element);
                    break;
                case "subacctfund":
                    this.subAcctFund = element.ownText();
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
        //tested
        this.stmtTrn.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.InvBankTran";
        String[] keys
                = {
                    "AcctId", "FiTId", "SubAcctFund"
                };
        String[] values
                = {
                    invAcctFrom.invAcctId.toString(), this.stmtTrn.fiTId,
                    this.subAcctFund
                };

        return this.doSQL(sTable, keys, values, 2);
    }
}
