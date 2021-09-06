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
public class OfxDebtInfo
        extends OfxAggregateBase
        implements IOfxSQL {

    OfxSecInfo secInfo;
    Double parValue;
    String debtType;
    String debtClass;
    Double couponRt;
    String dtCoupon;
    String couponFreq;
    Double callPrice;
    Double yieldToCall;
    String dtCall;
    String callType;
    Double yieldToMat;
    String dtMat;
    String assetClass;
    String fiAssetClass;

    // private final String errorPrefix;
    public OfxDebtInfo() {
        this.secInfo = new OfxSecInfo();
        this.parValue = null;
        this.debtType = null;
        this.couponRt = null;
        this.dtCoupon = null;
        this.couponFreq = null;
        this.callPrice = null;
        this.yieldToCall = null;
        this.dtCall = null;
        this.callType = null;
        this.yieldToMat = null;
        this.dtMat = null;
        this.assetClass = null;
        this.fiAssetClass = null;

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

        // aElement points to <debtinfo>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "secinfo":
                    this.secInfo.doData(element);
                    break;
                case "parvalue":
                    this.parValue = Double.parseDouble(element.ownText());
                    break;
                case "debttype":
                    this.debtType = element.ownText();
                    break;
                case "debtclass":
                    this.debtClass = element.ownText();
                    break;
                case "couponrt":
                    this.couponRt = Double.parseDouble(element.ownText());
                    break;
                case "dtcoupon":
                    this.dtCoupon = element.ownText();
                    break;
                case "couponfreq":
                    this.couponFreq = element.ownText();
                    break;
                case "callprice":
                    this.callPrice = Double.parseDouble(element.ownText());
                    break;
                case "yieldtocall":
                    this.yieldToCall = Double.parseDouble(element.ownText());
                    break;
                case "dtcall":
                    this.dtCall = element.ownText();
                    break;
                case "calltype":
                    this.callType = element.ownText();
                    break;
                case "yieldtomat":
                    this.yieldToMat = Double.parseDouble(element.ownText());
                    break;
                case "dtmat":
                    this.dtMat = element.ownText();
                    break;
                case "assetclass":
                    this.assetClass = element.ownText();
                    break;
                case "fiassetclass":
                    this.fiAssetClass = element.ownText();
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
        this.secInfo.doSQL(invAcctFrom, false);

        String sTable = "hlhtxc5_dbOfx.DebtInfo";

        String[] keys
                = {
                    "BrokerId", "SecId", "ParValue", "DebtType", "DebtClass", "CouponRt",
                    "DtCoupon", "CouponFreq", "CallPrice", "YieldToCall", "DtCall",
                    "CallType", "YieldToMat", "DtMat", "AssetClass", "FiAssetClass"
                };
        String[] values
                = {
                    invAcctFrom.brokerId.toString(), this.secInfo.secId.uniqueId,
                    this.parValue.toString(), this.debtType, this.debtClass,
                    String.valueOf(this.couponRt), this.dtCoupon, this.couponFreq,
                    String.valueOf(this.callPrice), String.valueOf(this.yieldToCall),
                    this.dtCall, this.callType, String.valueOf(this.yieldToMat),
                    this.dtMat, this.assetClass, this.fiAssetClass
                };

        return (this.doSQL(sTable, keys, values, 2));
    }
}
