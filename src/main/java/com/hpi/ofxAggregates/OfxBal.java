package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.Iterator;
import org.jsoup.nodes.Element;

/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxBal
        extends OfxAggregateBase
        implements IOfxSQL {

    String name;
    String desc;
    String balType;
    Double value;
    String dtAsOf;
    OfxCurrency currency;

    // private final String errorPrefix;
    public OfxBal() {
        this.name = null;
        this.desc = null;
        this.balType = null;
        this.value = null;
        this.dtAsOf = null;
        this.currency = new OfxCurrency();

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

        // aElement points to <bal>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "name":
                    this.name = element.ownText();
                    break;
                case "desc":
                    this.desc = element.ownText();
                    break;
                case "baltype":
                    this.balType = element.ownText();
                    break;
                case "value":
                    this.value = Double.parseDouble(element.ownText());
                    break;
                case "dtasof":
                    this.dtAsOf = element.ownText();
                    break;
                case "currency":
                    this.currency.doData(element);
                    break;
                default:
                    // actually do not care that there are extra elements
                    //  but let's log them
                    s = String.format(CMLanguageController.
                            getErrorProps().getProperty("Formatted3"),
                            element.tagName());

//                    //Logger.getLogger(this.getClass()).info(s);
//                    //Logger.getLogger(this.getClass()).info(s);
            }
        }
        return true;
    }

    public Boolean doSQL(OfxInvAcctFrom invAcctFrom,
            String invTranListDtEnd) {
        this.currency.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.Bal";
        String[] keys
                = {
                    "AcctId", "Name", "Descr", "BalType",
                    "Value", "DtAsOf", "CurSym"
                };
        String[] values
                = {
                    invAcctFrom.invAcctId.toString(), this.name, this.desc, this.balType,
                    String.valueOf(this.value),
                    invTranListDtEnd.substring(0, 8),
                    //this.dtAsOf,
                    this.currency.curSym
                };

        return this.doSQL(sTable, keys, values, 2);
    }

    @Override
    public Boolean doSQL(OfxInvAcctFrom invAcctFrom) {
        // never called
        return false;
    }
}
