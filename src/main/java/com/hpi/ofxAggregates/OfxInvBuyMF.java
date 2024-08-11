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
public class OfxInvBuyMF
        extends OfxAggregateBase
        implements IOfxSQL {

    OfxInvBuy invBuy;
    String buyType;
    String relFiTId;

    // private final String errorPrefix;
    public OfxInvBuyMF() {
        this.invBuy = new OfxInvBuy();
        this.buyType = null;
        this.relFiTId = null;

        // this.errorPrefix = this.getClass().getName();
    }

    public Boolean doData(Element aElement) {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <buymf>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "invbuy":
                    this.invBuy.doData(element);
                    break;
                case "buytype":
                    this.buyType = element.ownText();
                    break;
                case "relfitid":
                    this.relFiTId = element.ownText();
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
        this.invBuy.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.BuyMF";
        String[] keys
                = {
                    "AcctId", "FiTId", "BuyType", "RelfiTId"
                };
        String[] values
                = {
                    invAcctFrom.invAcctId.toString(), this.invBuy.invTran.fiTId,
                    this.buyType, this.relFiTId
                };

        return this.doSQL(sTable, keys, values, 2);
    }
}
