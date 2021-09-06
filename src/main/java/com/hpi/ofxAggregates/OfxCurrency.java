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
public class OfxCurrency
        extends OfxAggregateBase
        implements IOfxSQL {

    Double curRate;
    String curSym;

    // private final String errorPrefix;
    public OfxCurrency() {
        this.curRate = null;
        this.curSym = null;

        // this.errorPrefix = this.getClass().getName();
    }

    public Boolean doData(Element aElement) {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <currency>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "currate":
                    this.curRate = Double.parseDouble(element.ownText());
                    break;
                case "cursym":
                    this.curSym = element.ownText();
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
        if (this.curSym == null || this.curSym.isEmpty()) {
            return true;
        }

        String sTable = "hlhtxc5_dbOfx.Currency";
        String[] keys
                = {
                    "BrokerId", "CurSym", "CurRate"
                };
        String[] values
                = {
                    invAcctFrom.brokerId.toString(), this.curSym,
                    String.valueOf(this.curRate)
                };

        return this.doSQL(sTable, keys, values, 2);
    }
}
