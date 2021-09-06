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
public class OfxStmtTrn
        extends OfxAggregateBase
        implements IOfxSQL
{

    String trnType;
    String dtPosted;
    String dtUser;
    String dtAvail;
    Double trnAmt;
    String fiTId;
    String correctFiTId;
    String correctAction;
    String srvrTId;
    String checkNum;
    String refNum;
    String sic;
    String payeeId;
    String name;
    OfxPayee payee;
    String extdName;
    OfxBankAcctToFrom bankAcctTo;
    OfxCCAcctToFrom ccAcctTo;
    String memo;
    OfxImageData imageData;
    OfxCurrency currency;
    OfxCurrency origCurrency;
    String inv401kSource;
    
    private final String errorPrefix;

    public OfxStmtTrn()
    {
        this.trnType = null;
        this.dtPosted = null;
        this.dtUser = null;
        this.dtAvail = null;
        this.trnAmt = null;
        this.fiTId = null;
        this.correctFiTId = null;
        this.correctAction = null;
        this.srvrTId = null;
        this.checkNum = null;
        this.refNum = null;
        this.sic = null;
        this.payeeId = null;
        this.name = null;
        this.payee = new OfxPayee();
        this.extdName = null;
        this.bankAcctTo = new OfxBankAcctToFrom();
        this.ccAcctTo = new OfxCCAcctToFrom();
        this.memo = null;
        this.imageData = new OfxImageData();
        this.currency = new OfxCurrency();
        this.origCurrency = new OfxCurrency();
        this.inv401kSource = null;

        this.errorPrefix = this.getClass().getName();
        
    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <stmttrn>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "trntype":
                    this.trnType = element.ownText();
                    break;
                case "dtposted":
                    this.dtPosted = element.ownText();
                    break;
                case "dtuser":
                    this.dtUser = element.ownText();
                    break;
                case "dtavail":
                    this.dtAvail = element.ownText();
                    break;
                case "trnamt":
                    this.trnAmt = Double.parseDouble(element.ownText());
                    break;
                case "fitid":
                    this.fiTId = element.ownText();
                    break;
                case "correctfitid":
                    this.correctFiTId = element.ownText();
                    break;
                case "correctaction":
                    this.correctAction = element.ownText();
                    break;
                case "srvrtid":
                    this.srvrTId = element.ownText();
                    break;
                case "checknum":
                    this.checkNum = element.ownText();
                    break;
                case "refnum":
                    this.refNum = element.ownText();
                    break;
                case "sic":
                    this.sic = element.ownText();
                    break;
                case "payeeid":
                    this.payeeId = element.ownText();
                    break;
                case "name":
                    this.name = element.ownText();
                    break;
                case "payee":
                    this.payee.doData(element);
                    break;
                case "extdname":
                    this.extdName = element.ownText();
                    break;
                case "bankacctto":
                    this.bankAcctTo.doData(element);
                    break;
                case "ccacctto":
                    this.ccAcctTo.doData(element);
                    break;
                case "memo":
                    this.memo = element.ownText();
                    break;
                case "imagedata":
                    this.imageData.doData(element);
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
        //tested
        if (this.payee.ofxPayeeId != null)
        {
            this.payee.doSQL(invAcctFrom);
        }
        //todo: bankacctto; ccacctto; imagedata
        if (this.bankAcctTo.acctId != null)
        {
            CMHPIUtils.showDefaultMsg(CMLanguageController.
                        getAppProps().getProperty("Title"),
                    errorPrefix,
                    "doSQL",
                    "Not finished. BankAcctTo has data.",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (this.ccAcctTo.acctId != null)
        {
            CMHPIUtils.showDefaultMsg(CMLanguageController.
                        getAppProps().getProperty("Title"),
                    errorPrefix,
                    "doSQL",
                    "Not finished. CCAcctTo has data.",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (this.imageData.imageType != null)
        {
            CMHPIUtils.showDefaultMsg(CMLanguageController.
                        getAppProps().getProperty("Title"),
                    errorPrefix,
                    "doSQL",
                    "Not finished. ImageData has data.",
                    JOptionPane.ERROR_MESSAGE);
        }
        this.currency.doSQL(invAcctFrom);
        this.origCurrency.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.StmtTrn";
        String[] keys =
        {
            "AcctId", "FiTId", "TrnType", "DtPosted", "DtUser", "DtAvail",
            "TrnAmt", "CorrectFiTId", "CorrectAction", "SrvrTId", "CheckNum",
            "RefNum", "SIC", "PayeeId", "Name", "ExtdName", "BankAcctTo",
            "CCAcctTo", "Memo", "ImageData", "CurSym", "OrigCurSym",
            "Inv401kSource", "OfxPayeeId"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.fiTId, this.trnType,
            this.dtPosted, this.dtUser, this.dtAvail, String.valueOf(this.trnAmt),
            this.correctFiTId, this.correctAction, this.srvrTId, this.checkNum,
            this.refNum, this.sic, this.payeeId, this.name, this.extdName,
            //todo: this.bankAcctTo, this.ccAccTo
            "9999", "9999",
            this.memo,
            //todo: this.imageData,
            "9999",
            this.currency.curSym, this.origCurrency.curSym, this.inv401kSource,
            String.valueOf(this.payee.ofxPayeeId)
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}
