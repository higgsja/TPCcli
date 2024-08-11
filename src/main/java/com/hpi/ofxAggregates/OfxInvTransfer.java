/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxInvTransfer
        extends OfxAggregateBase
        implements IOfxSQL
{

    OfxInvTran invTran;
    OfxSecId secId;
    String subAcctSec;
    Double units;
    String tferAction;
    String posType;
    OfxInvAcctFrom invAcctFrom;
    Double avgCostBasis;
    Double unitPrice;
    String dtPurchase;
    String inv401kSource;
    

    public OfxInvTransfer()
    {
        this.invTran = new OfxInvTran();
        this.secId = new OfxSecId();
        this.subAcctSec = null;
        this.units = null;
        this.tferAction = null;
        this.posType = null;
        this.invAcctFrom = new OfxInvAcctFrom();
        this.avgCostBasis = null;
        this.unitPrice = null;
        this.dtPurchase = null;
        this.inv401kSource = null;
    }

    /**
     *
     * @param aElement
     * @param signOnMsgsRSv1
     * @return
     */
    public Boolean doData(Element aElement, OfxSignOnMsgsRSv1 signOnMsgsRSv1)
    {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <transfer>
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
                case "units":
                    this.units = Double.parseDouble(element.ownText());
                    break;
                case "tferaction":
                    this.tferAction = element.ownText();
                    break;
                case "postype":
                    this.posType = element.ownText();
                    break;
                case "invacctfrom":
                    this.invAcctFrom.doData(element, signOnMsgsRSv1);
                    break;
                case "avgcostbasis":
                    this.avgCostBasis = Double.parseDouble(element.ownText());
                    break;
                case "unitprice":
                    this.unitPrice = Double.parseDouble(element.ownText());
                    break;
                case "dtpurchase":
                    this.dtPurchase = element.ownText();
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
        if (this.invAcctFrom.brokerIdFi != null
                || this.invAcctFrom.invAcctIdFi != null)
        {
            CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().getProperty("Title"),
                    Thread.currentThread().getStackTrace()[1].getClassName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                    "Not tested. InvAcctFrom has data.",
                    JOptionPane.ERROR_MESSAGE);
        }
//        this.invAcctFrom.doSQL(ofxFI);
//        this.secId.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.Transfer";
        
        String[] keys =
        {
            "AcctId", "FiTId",
            "SecId", "SubAcctSec", "Units",
            "TferAction", "PosType",
            "InvAcctFrom",
            "AvgCostBasis", "UnitPrice",
            "DtPurchase", "Inv401kSource"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
            this.secId.uniqueId, this.subAcctSec, String.valueOf(this.units),
            this.tferAction, this.posType,
            String.valueOf(this.invAcctFrom.invAcctId),
            String.valueOf(this.avgCostBasis), String.valueOf(this.unitPrice),
            this.dtPurchase, this.inv401kSource
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}
