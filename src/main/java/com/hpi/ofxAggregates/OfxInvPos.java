package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;
import java.util.Iterator;
import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxInvPos
        extends OfxAggregateBase
        implements IOfxSQL
{

    Integer invPosId;
    OfxSecId secId;
    String heldInAcct;
    String posType;
    Double units;
    Double unitPrice;
    Double mktVal;
    String dtPriceAsOf;
    OfxCurrency currency;
    String memo;
    String inv401kSource;

    // private final String errorPrefix;

    public OfxInvPos()
    {
        this.invPosId = null;
        this.secId = new OfxSecId();
        this.heldInAcct = null;
        this.posType = null;
        this.units = null;
        this.unitPrice = null;
        this.mktVal = null;
        this.dtPriceAsOf = null;
        this.currency = new OfxCurrency();
        this.memo = null;
        this.inv401kSource = null;

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

        // aElement points to <invpos>
        iterator = aElement.children().iterator();

        while (iterator.hasNext())
        {
            element = iterator.next();

            switch (element.tagName().toLowerCase())
            {
                case "secid":
                    this.secId.doData(element);
                    break;
                case "heldinacct":
                    this.heldInAcct = element.ownText();
                    break;
                case "postype":
                    this.posType = element.ownText();
                    break;
                case "units":
                    this.units = Double.parseDouble(element.ownText());
                    break;
                case "unitprice":
                    this.unitPrice = Double.parseDouble(element.ownText());
                    break;
                case "mktval":
                    this.mktVal = Double.parseDouble(element.ownText());
                    break;
                case "dtpriceasof":
                    this.dtPriceAsOf = element.ownText();
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                case "memo":
                    this.memo = element.ownText();
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
        throw new UnsupportedOperationException("Never use this.");
    }

    public Boolean doSQL(OfxInvAcctFrom invAcctFrom, Boolean bIsOption)
    {

        String sDtAsOf;
        this.currency.doSQL(invAcctFrom);

        String checkSQL;

        if (bIsOption)
        {
            switch (invAcctFrom.FId)
            {
                case OfxSecInfo.FID_TRADESTATION:
                    // issue here is that they truncate it such that
                    // AAPL181019C150201810 is the secId; thus the date
                    // is missing off the end.
                    //
                    // additionally, they get it right for InvPos
                    this.secId.uniqueId = this.secId.uniqueId.substring(0,
                            this.secId.uniqueId.length() - 2);
                    break;
                default:
            }
        }

        String sTable = "hlhtxc5_dbOfx.InvPos";

        String[] keys =
        {
            "AcctId", "InvPosId", "DtAsOf", "SecId",
            "HeldInAcct", "PosType", "Units", "UnitPrice",
            "MktVal", "DtPriceAsOf"/* , "CurSym", "Memo", "Inv401kSource" */
        };

        sDtAsOf = CMHPIUtils.getShortDateISOOfx(invAcctFrom.stmtDtAsOf);

        // only use the day, no time value
        sDtAsOf = sDtAsOf.substring(0, sDtAsOf.indexOf(' '));

        String[] values =
        {
            invAcctFrom.invAcctId.toString(),
            null,
            sDtAsOf,
            this.secId.uniqueId, this.heldInAcct, this.posType,
            String.valueOf(this.units), String.valueOf(this.unitPrice),
            String.valueOf(this.mktVal), this.dtPriceAsOf/* ,
         * this.currency.curSym,
         * this.memo, this.inv401kSource */
        };
        checkSQL = String.format(CMLanguageController.getOfxSqlProp(
                "OfxSQLInvPosTableQueryInvPosId"),
                invAcctFrom.invAcctId.toString(), sDtAsOf,
                this.secId.uniqueId, this.secId.uniqueId, this.heldInAcct,
                this.posType, this.units.toString(), this.unitPrice.toString(),
                this.mktVal.toString(), this.dtPriceAsOf/* ,
         * this.currency.curSym,
         * this.memo.replace("'", "''"),
         * this.inv401kSource */);

        this.invPosId = this.doSQLAuto(sTable, keys, values, checkSQL);
        return this.invPosId != null;
    }
}
