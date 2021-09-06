package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.Iterator;
import org.jsoup.nodes.Element;

public class OfxBankAcctToFrom
        extends OfxAggregateBase
        implements IOfxSQL {

    String bankId;
    String branchId;
    String acctId;
    String acctType;
    String acctKey;

    // private final String errorPrefix;
    public OfxBankAcctToFrom() {
        this.bankId = null;
        this.branchId = null;
        this.acctId = null;
        this.acctType = null;
        this.acctKey = null;

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

        // aElement points to <fi>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "bankid":
                    this.bankId = element.ownText();
                    break;
                case "branchid":
                    this.branchId = element.ownText();
                    break;
                case "acctid":
                    this.acctId = element.ownText();
                    break;
                case "accttype":
                    this.acctType = element.ownText();
                    break;
                case "acctkey":
                    this.acctKey = element.ownText();
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
        String sTable = "hlhtxc5_dbOfx.BankAcctToFrom";
        String[] keys
                = {
                    "AcctId", "BankId",
                    "BranchId", "AcctId2", "AcctType", "AcctKey"
                };
        String[] values
                = {
                    invAcctFrom.invAcctId.toString(), this.bankId,
                    this.branchId, this.acctId, this.acctType, this.acctKey
                };

        return this.doSQL(sTable, keys, values, 2);
    }
}
