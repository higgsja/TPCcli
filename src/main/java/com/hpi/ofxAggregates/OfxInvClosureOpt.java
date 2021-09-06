package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import java.util.Iterator;
import lombok.*;
import org.jsoup.nodes.Element;

@Getter
@Setter
public class OfxInvClosureOpt
    extends OfxAggregateBase
    implements IOfxSQL {
    private OfxInvTran invTran;
    private OfxSecId secId;
    private String optAction;
    private Double units;
    private Integer shPerCtrct;
    private String subAcctSec;
    private String relFiTId;
    private Double gain;

    private final String errorPrefix;

    public OfxInvClosureOpt() {
        this.invTran = new OfxInvTran();
        this.secId = new OfxSecId();
        this.optAction = null;
        this.units = null;
        this.shPerCtrct = null;
        this.subAcctSec = null;
        this.relFiTId = null;
        this.gain = null;

        this.errorPrefix = this.getClass().getName();
    }

    /**
     *
     * @param aElement
     *
     * @return
     */
    public Boolean doData(Element aElement) {
        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <closureopt>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "invtran":
                    this.invTran.doData(element);
                    break;
                case "secid":
                    this.secId.doData(element);
                    break;
                case "optaction":
                    this.optAction = element.ownText();
                    break;
                case "units":
                    this.units = Double.parseDouble(element.ownText());
                    break;
                case "shperctrct":
                    this.shPerCtrct = Integer.parseInt(element.ownText());
                    break;
                case "subacctsec":
                    this.subAcctSec = element.ownText();
                    break;
                case "relfitid":
                    this.relFiTId = element.ownText();
                    break;
                case "gain":
                    this.gain = Double.parseDouble(element.ownText());
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
        this.invTran.doSQL(invAcctFrom);
//        this.secId.doSQL(invAcctFrom);

        String sTable = "hlhtxc5_dbOfx.ClosureOpt";

        String[] keys = {
            "AcctId", "FiTId",
            "SecId", "OptAction",
            "Units", "ShPerCtrct",
            "SubAcctSec", "RelFiTId", "Gain"
        };
        String[] values = {
            invAcctFrom.invAcctId.toString(), this.invTran.fiTId,
            String.valueOf(this.secId.uniqueId), this.optAction,
            String.valueOf(this.units), String.valueOf(this.shPerCtrct),
            this.subAcctSec, this.relFiTId, String.valueOf(this.gain)
        };

        return this.doSQL(sTable, keys, values, 2);
    }
}
