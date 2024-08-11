package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.Iterator;
import org.jsoup.nodes.Element;

public class OfxInvTran
        extends OfxAggregateBase
{

    String fiTId;
    String srvrTId;
    String dtTrade;
    String dtSettle;
    String reversalFiTId;
    String memo;

    // private final String errorPrefix;

    public OfxInvTran()
    {
        this.fiTId = null;
        this.srvrTId = null;
        this.dtTrade = null;
        this.dtSettle = null;
        this.reversalFiTId = null;
        this.memo = null;

        // this.errorPrefix = this.getClass().getName();

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

        // aElement points to <invtran>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "fitid":
                    this.fiTId = element.ownText();
                    break;
                case "srvrtid":
                    this.srvrTId = element.ownText();
                    break;
                case "dttrade":
                    this.dtTrade = element.ownText();
                    break;
                case "dtsettle":
                    this.dtSettle = element.ownText();
                    break;
                case "reversalfitid":
                    this.reversalFiTId = element.ownText();
                    break;
                case "memo":
                    this.memo = element.ownText();
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

    public Boolean doSQL(OfxInvAcctFrom invAcctFrom)
    {
        String sTable = "hlhtxc5_dbOfx.InvTran";
        String[] keys =
        {
            "AcctId", "FiTId", "SrvrTId", "DtTrade", "DtSettle",
            "ReversalFiTId", "Memo"
        };
        String[] values =
        {
            invAcctFrom.invAcctId.toString(), this.fiTId, this.srvrTId,
            this.dtTrade, this.dtSettle, this.reversalFiTId, this.memo
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}
