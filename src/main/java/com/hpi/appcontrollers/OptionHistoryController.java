package com.hpi.appcontrollers;

import com.hpi.hpiUtils.CMHPIUtils;
import com.hpi.TPCCMcontrollers.*;
import com.hpi.TPCCMprefs.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * No longer used; had a service from discountoptiondata.com
 * 
 * @author Joe@Higgs-Tx.com
 */
public class OptionHistoryController {

    private ArrayList<String> fileList;
    /*
     * Singleton
     *
     */
    private static OptionHistoryController instance;

    protected OptionHistoryController() {
        // protected prevents instantiation outside of package
    }

    public synchronized static OptionHistoryController getInstance() {
        if (OptionHistoryController.instance == null) {
            OptionHistoryController.instance = new OptionHistoryController();
        }
        return OptionHistoryController.instance;
    }
    //***

    public void doOptionHistory() {
        this.doHistorical();
        this.doUtil_LastDailyOption();
    }

    /*
     * Process files from defined location
     */
    private void doHistorical() {
        String sql;
        File folder;
        DateTimeFormatter dtf;

        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        // get list of files to process
        this.fileList = new ArrayList<>();

        folder = new File(CMDirectoriesModel.getInstance().
            getProps().getProperty("OptionHistory"));

        File[] files = folder.listFiles();

        if (files.length == 0) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                this.fileList.add(file.getAbsolutePath());
            }
        }

        if (this.fileList.isEmpty()) {
            return;
        }

        // loop through files
        for (String sPath : this.fileList) {
            System.out.println(dtf.format(LocalDateTime.now()));

            CMHPIUtils.showDefaultMsg(
                CMLanguageController.getAppProps().
                    getProperty("Title"),
                Thread.currentThread().getStackTrace()[1].getClassName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                sPath,
                JOptionPane.INFORMATION_MESSAGE);

            // process the file
            sql =
                "load data local infile '%s' replace into table hlhtxc5_dmOfx.optionHistory fields terminated by ',' lines terminated by '\\n' ignore 1 lines (OptionKey, Symbol, ExpirationDate, AskPrice, AskSize, BidPrice, BidSize, LastPrice, PutCall, StrikePrice, Volume, OpenInterest, UnderlyingPrice, DataDate);";
            sql = String.format(sql, sPath);
            CMDBController.updateSQLNoCommit(sql);

            try {
                Files.deleteIfExists(Paths.get(sPath));
            }
            catch (IOException ex) {
                // not important
            }

            System.out.println(dtf.format(LocalDateTime.now()));
        }

//        doUtil_LastDailyOption();
    }

    /*
     * Refresh table of active products with latest prices
     */
    private void doUtil_LastDailyOption() {
        String sql;

        sql = "truncate hlhtxc5_dmOfx.Util_LastDailyOption;";
        CMDBController.executeSQL(sql);

        //set Price to average of bid and ask
        sql =
            "insert ignore into hlhtxc5_dmOfx.Util_LastDailyOption select oof.EquityId, max(oh.DataDate) as DataDate, oh.BidPrice, oh.AskPrice, oh.LastPrice, round((oh.AskPrice + oh.BidPrice) / 2, 2) as Price, oh.PutCall, oh.StrikePrice from hlhtxc5_dmOfx.OpenOptionFIFO as oof left join hlhtxc5_dmOfx.OptionHistory as oh on oh.EquityId = oof.EquityId where oh.DataDate >= subdate(now(), interval 4 day) group by oof.EquityId";
//            "insert ignore into hlhtxc5_dmOfx.Util_LastDailyOption select oof.EquityId, max(oh.DataDate) as DataDate, oh.BidPrice, oh.AskPrice, oh.LastPrice, round((oh.AskPrice + oh.BidPrice) / 2, 2) as Price, oh.PutCall, oh.StrikePrice from hlhtxc5_dmOfx.OpenOptionFIFO as oof left join hlhtxc5_dmOfx.optionHistory as oh on oh.EquityId = oof.EquityId where oh.DataDate >= subdate(now(), interval 4 day) group by oof.EquityId;";

        CMDBController.executeSQL(sql);
    }
}
