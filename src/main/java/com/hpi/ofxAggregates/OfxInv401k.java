/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.CMLanguageController;
import com.hpi.hpiUtils.CMHPIUtils;

import org.jsoup.nodes.Element;

import javax.swing.JOptionPane;
// import org.jsoup.nodes.Element;




/**
 *
 * @author Joe@Higgs-Tx.com
 */
public class OfxInv401k
{


    private final String errorPrefix;
    private String fErrorPrefix;

    public OfxInv401k()
    {

        this.errorPrefix = this.getClass().getName();

        this.fErrorPrefix = null;
    }

    public Boolean doData(Element aElement)
    {
        fErrorPrefix = Thread.currentThread().getStackTrace()[1].getMethodName();

        // aElement points to <inv401k>
        // todo: this is huge. skip for now.
        CMHPIUtils.showDefaultMsg(CMLanguageController.
              getAppProps().getProperty("Title"),
                errorPrefix,
                fErrorPrefix,
                "Not implemented.",
                JOptionPane.ERROR_MESSAGE);

        return true;
    }
}
