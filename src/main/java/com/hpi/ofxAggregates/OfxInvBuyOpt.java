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
public class OfxInvBuyOpt
        extends OfxAggregateBase
        implements IOfxSQL {

    OfxInvBuy invBuy;
    String optBuyType;
    Integer shPerCtrct;

    // private final String errorPrefix;
    public OfxInvBuyOpt() {
        this.invBuy = new OfxInvBuy();
        this.optBuyType = null;
        this.shPerCtrct = null;

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

        // aElement points to <buyopt>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "invbuy":
                    this.invBuy.doData(element);
                    break;
                case "optbuytype":
                    this.optBuyType = element.ownText();
                    break;
                case "shperctrct":
                    this.shPerCtrct = Integer.parseInt(element.ownText());
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

        String sTable = "hlhtxc5_dbOfx.BuyOpt";
        String[] keys
                = {
                    "AcctId", "FiTId", "OptBuyType", "ShPerCtrct"
                };
        String[] values
                = {
                    invAcctFrom.invAcctId.toString(), this.invBuy.invTran.fiTId,
                    this.optBuyType, String.valueOf(this.shPerCtrct)
                };

        return this.doSQL(sTable, keys, values, 2);
    }
}
