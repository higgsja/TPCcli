/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMprefs.CMDBModel;
import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.Iterator;
import org.jsoup.nodes.Element;

/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxInvAcctFrom
    extends OfxAggregateBase
{

    String stmtDtAsOf;
    Integer brokerId;
    Integer invAcctId;
    String brokerIdFi;
    String invAcctIdFi;
    String FId;

    // private final String errorPrefix;

    public OfxInvAcctFrom()
    {
        this.stmtDtAsOf = null;

        this.brokerId = null;
        this.invAcctId = null;

        this.brokerIdFi = null;
        this.invAcctIdFi = null;

        this.FId = null;

        // this.errorPrefix = this.getClass().getName();

    }

    /**
     *
     * @param aElement
     * @param signOnMsgsRSv1
     *
     * @return
     */
    public Boolean doData(Element aElement, OfxSignOnMsgsRSv1 signOnMsgsRSv1)
    {
        //tested
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invacctfrom>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            //tested
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "brokerid":
                    this.brokerIdFi = element.ownText();
                    if (signOnMsgsRSv1.sonRS.ofxFI.fid == null)
                    {
                        this.FId = this.brokerIdFi;
                    }
                    else
                    {
                        this.FId = signOnMsgsRSv1.sonRS.ofxFI.fid;
                    }
                    break;
                case "acctid":
                    this.invAcctIdFi = element.ownText();
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                        getErrorProps().getProperty("Formatted3"),
                        element.tagName());
            }
        }
        return true;
    }

    /**
     *
     * @param ofxFI
     *
     * @return
     */
    public Boolean doSQL(OfxFI ofxFI)
    {
        String checkSQL;

        // handle the broker
        String sTable = "hlhtxc5_dbOfx.Brokers";
        String[] keys =
        {
            "Org",
            "FId", "BrokerIdFi"
        };
        String[] values =
        {
            ofxFI.getOrg() == null ? this.brokerIdFi : ofxFI.getOrg(),
            ofxFI.getFid() == null ? this.brokerIdFi : ofxFI.getFid(),
            this.brokerIdFi
        };

        checkSQL = String.format(CMLanguageController.getOfxSqlProp(
            "OfxSQLBrokersTableQueryBrokerId"), this.brokerIdFi);

        this.brokerId = this.doSQLAuto(sTable, keys, values, checkSQL);
        //this is coming back null
        if (this.brokerId == null)
        {
            return false;
        }

        // handle the account
        sTable = "hlhtxc5_dbOfx.Accounts";
        keys = new String[]
        {
            "BrokerId",
            "JoomlaId", "InvAcctIdFi"
        };
        values = new String[]
        {
            this.brokerId.toString(),
            CMDBModel.getUserId().toString(), this.invAcctIdFi
        };

        checkSQL = String.format(CMLanguageController.getOfxSqlProp(
            "OfxSQLAccountsTableQueryAcctId"),
            this.brokerId, CMDBModel.getUserId().toString(),
            this.invAcctIdFi);

        this.invAcctId = this.doSQLAuto(sTable, keys, values, checkSQL);

        return this.invAcctId != null;
    }

    public Integer getBrokerId()
    {
        return brokerId;
    }

    public Integer getAcctId()
    {
        return invAcctId;
    }

    public String getStmtDtAsOf()
    {
        return stmtDtAsOf;
    }

    public void setStmtDtAsOf(String stmtDtAsOf)
    {
        this.stmtDtAsOf = stmtDtAsOf;
    }

    public Integer getInvAcctId()
    {
        return invAcctId;
    }

    public String getBrokerIdFi()
    {
        return brokerIdFi;
    }

    public String getInvAcctIdFi()
    {
        return invAcctIdFi;
    }

    public String getFId()
    {
        return FId;
    }


}
