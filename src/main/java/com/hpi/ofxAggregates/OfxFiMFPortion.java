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
public class OfxFiMFPortion
        extends OfxAggregateBase {

    String fiAssetClass;
    Double percent;

    private final String errorPrefix;
    private String fErrorPrefix;

    public OfxFiMFPortion() {
        this.fiAssetClass = null;
        this.percent = null;

        this.errorPrefix = this.getClass().getName();

        this.fErrorPrefix = null;
    }

    /**
     *
     * @param aElement
     * @return
     */
    public Boolean doData(Element aElement) {
        fErrorPrefix = Thread.currentThread().getStackTrace()[1].getMethodName();

        String s;
        Element element;
        Iterator<Element> iterator;

        // aElement points to <fiportion>
        iterator = aElement.children().iterator();

        while (iterator.hasNext()) {
            element = iterator.next();

            switch (element.tagName().toLowerCase()) {
                case "fiassetclass":
                    this.fiAssetClass = element.ownText();
                    break;
                case "percent":
                    this.percent = Double.parseDouble(element.ownText());
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

    public Boolean doSQL(Integer BrokerId, String secId) {
        //tested
        fErrorPrefix = Thread.currentThread().getStackTrace()[1].getMethodName();

        CMHPIUtils.showDefaultMsg(CMLanguageController.getAppProps().getProperty("Title"),
                errorPrefix,
                fErrorPrefix,
                "Not tested.",
                JOptionPane.ERROR_MESSAGE);

        String sTable = "hlhtxc5_dbOfx.FiMFPortion";
        String[] keys
                = {
                    "BrokerId", "SecId", "FiAssetClass", "Percent"
                };
        String[] values
                = {
                    BrokerId.toString(), secId, this.fiAssetClass, this.percent.toString()
                };

        return (this.doSQL(sTable, keys, values, 2));
    }
}
