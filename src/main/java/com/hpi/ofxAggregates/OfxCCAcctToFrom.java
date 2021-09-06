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
public class OfxCCAcctToFrom
        extends OfxAggregateBase
        implements IOfxSQL {

    String acctId;
    String acctKey;

    // private final  String errorPrefix;
    public OfxCCAcctToFrom() {
        this.acctId = null;
        this.acctKey = null;

        // this.errorPrefix = this.getClass().getName();
    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement) {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <fi>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "acctid":
                    this.acctId = element.ownText();
                    break;
                case "acctkey":
                    this.acctKey = element.ownText();
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
        String sTable = "hlhtxc5_dbOfx.CCAcctToFrom";
        String[] keys
                = {
                    "AcctId", "AcctId2", "AcctKey"
                };
        String[] values
                = {
                    invAcctFrom.invAcctId.toString(), this.acctId, this.acctKey
                };

        return this.doSQL(sTable, keys, values, 2);
    }
}
