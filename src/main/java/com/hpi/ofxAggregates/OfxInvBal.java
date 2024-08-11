package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
// import org.apache.commons.lang3.*;
import org.jsoup.nodes.Element;


/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxInvBal
        extends OfxAggregateBase
{

    Double availCash;
    Double marginBalance;
    Double shortBalance;
    Double buyPower;
    ArrayList<OfxBal> balList;

    public OfxInvBal()
    {
        this.availCash = 0.0;
        this.marginBalance = 0.0;
        this.shortBalance = 0.0;
        this.buyPower = 0.0;
        this.balList = new ArrayList<>();
    }

    /**
     *
     * @param aElement
     */
    public void doData(Element aElement)
    {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <invbal>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "availcash":
                    this.availCash = Double.parseDouble(element.ownText());
                    break;
                case "marginbalance":
                    this.marginBalance = Double.parseDouble(element.ownText());
                    break;
                case "shortbalance":
                    this.shortBalance = Double.parseDouble(element.ownText());
                    break;
                case "buypower":
                    this.buyPower = element.ownText().isEmpty() ? null
                            : Double.parseDouble(element.ownText());
                    break;
                case "ballist":
                    this.doBalList(element);
                    break;
                default:
                    // actually do not care that there are extra elements
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

                    CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProp("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                    getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                    getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doBalList(Element aElement)
    {
        OfxBal bal;
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <ballist>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "bal":
                    bal = new OfxBal();
                    if (bal.doData(element))
                    {
                        this.balList.add(bal);
                    }
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

                    CMHPIUtils.showDefaultMsg(CMLanguageController.getErrorProp("Title"),
                            Thread.currentThread().getStackTrace()[1].
                                    getClassName(),
                            Thread.currentThread().getStackTrace()[1].
                                    getMethodName(),
                            s,
                            JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public Boolean doSQL(OfxInvAcctFrom invAcctFrom,
          String invTranListDtEnd, String stmtDate)
    {
        /**
         * Currently do not care about the balList so ignore it
         *
         * This only comes with the statement, so is new every time
         * and there is only one
         *
         * Use upsert to ensure that on any given day, no matter how
         * many times run, the latest balance gets landed
         */

        String sTable = "hlhtxc5_dbOfx.InvBal";
        String[] keys =
        {
            "AcctId", "DtAsOf", "AvailCash",
            "MarginBalance", "ShortBalance",
            "BuyPower"
        };
        String[] values =
        {
            String.valueOf(invAcctFrom.invAcctId),
            stmtDate.substring(0, 8),
            String.valueOf(this.availCash),
            String.valueOf(this.marginBalance),
            String.valueOf(this.shortBalance),
            String.valueOf(this.buyPower)
        };

        this.doSQL(sTable, keys, values, 2);

        // deal with balList
        for (OfxBal bal : balList)
        {
            bal.doSQL(invAcctFrom, invTranListDtEnd);
        }

        // also want all balances in table Bal
        sTable = "hlhtxc5_dbOfx.Bal";
        keys = new String[7];
        keys[0] = "AcctId";
        keys[1] = "Name";
        keys[2] = "Descr";
        keys[3] = "BalType";
        keys[4] = "Value";
        keys[5] = "DtAsOf";
        keys[6] = "CurSym";
        values = new String[7];
        values[0] = invAcctFrom.invAcctId.toString();
        values[1] = "Available Cash";
        values[2] = "Available Cash";
        values[3] = "DOLLAR";
        values[4] = String.valueOf(this.availCash);
//        values[5] = null;
        values[5] = invTranListDtEnd.substring(0,8);
        values[6] = null;

        this.doSQL(sTable, keys, values, 2);

        if (this.marginBalance != null)
        {
            values[0] = invAcctFrom.invAcctId.toString();
            values[1] = "Margin Balance";
            values[2] = "Margin Balance";
            values[3] = "DOLLAR";
            values[4] = String.valueOf(this.marginBalance);

            this.doSQL(sTable, keys, values, 2);
        }

        if (this.shortBalance != null)
        {
            values[0] = invAcctFrom.invAcctId.toString();
            values[1] = "Short Balance";
            values[2] = "Short Balance";
            values[3] = "DOLLAR";
            values[4] = String.valueOf(this.shortBalance);

            this.doSQL(sTable, keys, values, 2);
        }

        if (this.buyPower != null)
        {
            values[0] = invAcctFrom.invAcctId.toString();
            values[1] = "Buying Power";
            values[2] = "Buying Power";
            values[3] = "DOLLAR";
            values[4] = String.valueOf(this.buyPower);

            this.doSQL(sTable, keys, values, 2);
        }

        return true;
    }
}
