/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hpi.ofxAggregates;

import com.hpi.TPCCMcontrollers.*;
import com.hpi.hpiUtils.CMHPIUtils;
import java.sql.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Joe@Higgs-Tx.com
 */
public abstract class OfxAggregateBase
{

    // private final String errorPrefix;
    // private String fErrorPrefix;

    public OfxAggregateBase()
    {
        // this.errorPrefix = OfxAggregateBase.this.getClass().getName();
        // this.fErrorPrefix = null;
    }

    /**
     *
     * @param sTable: table name
     * @param keys: column names
     * @param values: column values
     * @param primary: number of columns in the primary key
     * @return
     */
    public Boolean doSQL(String sTable, String[] keys,
          String[] values, Integer primary)
    {
//        CMDBController dbConnection;
        String sUpdateSQL, sInsertSQL;

        // build insert query
        sInsertSQL = createInsertQuery(sTable, keys, values);

        // build update query
        sUpdateSQL = createUpdateQuery(sTable, keys, values, primary);

        CMDBController.upsertRow(sUpdateSQL, sInsertSQL);

        return true;
    }

    private String createInsertQuery(String sTable, String[] keys,
          String[] values)
    {
        StringBuffer s1, s2;
        // Integer i;

        s1 = new StringBuffer();
        s2 = new StringBuffer();

        s1.setLength(0);
        s2.setLength(0);

        for (String s3 : keys)
        {
            s1.append(s3);
            s1.append(",");
        }

        // drop the extra comma
        s1.setLength(s1.length() - 1);

        for (String s3 : values)
        {
            if (s3 == null || s3.equalsIgnoreCase("null"))
            {
                //s2.append(null);
                s2.append((String) null);
                s2.append(",");
                //s2 += null + ",";
            }
            else
            {
                s2.append("\"");
                s2.append(s3);
                s2.append("\",");
                //s2 += "\"" + s3 + "\",";
            }
        }

        s2.setLength(s2.length() - 1);
        //s2 = s2.substring(0, s2.length() - 1);

        return String.format(CMLanguageController.getOfxSqlProp("OfxSQLInsert"),
              sTable,
              s1,
              s2);
    }

    private String createUpdateQuery(String sTable, String[] keys,
          String[] values, Integer primary)
    {
        StringBuffer s1, s2;
        Integer i;

        // start after the primary keys; cases where there are
        // only primary keys, so there can be no update
        s1 = new StringBuffer();
        s2 = new StringBuffer();

        s1.setLength(0);
        s2.setLength(0);

        if (primary == keys.length)
        {
            return "";
        }

        for (i = primary; i <= keys.length - 1; i++)
        {
            if (values[i] == null || values[i].equalsIgnoreCase("null"))
            {
                //s1 += keys[i] + "=" + values[i] + ",";
                s1.append(keys[i]);
                s1.append("=");
                s1.append(values[i]);
                s1.append(",");
            }
            else
            {
                //s1 += keys[i] + "=\"" + values[i] + "\",";
                s1.append(keys[i]);
                s1.append("=\"");
                s1.append(values[i]);
                s1.append("\",");
            }
        }

        // remove extra comma
        s1.setLength(s1.length() - 1);

        // need another for on the 'where' clause
        for (i = 0; i < primary; i++)
        {
            //s2 += keys[i] + "=\"" + values[i] + "\" and ";
            s2.append(keys[i]);
            s2.append("=\"");
            s2.append(values[i]);
            s2.append("\" and ");
        }

        // remove extra bit
        s2.setLength(s2.length() - 5);

        return String.format(CMLanguageController.getOfxSqlProp("OfxSQLUpdate"),
              sTable,
              s1,
              s2);
    }

    public Integer doSQLAuto(String sTable, String[] keys,
          String[] values, String checkSQL)
    {
        // fErrorPrefix = Thread.currentThread().getStackTrace()[1].getMethodName();

        String s;
        String sInsertSQL;
        Integer iAutoId;
        ResultSet rs;

        iAutoId = null;

        // check if already have the row
        // with auto, count goes up on failure so would rather not
        // allow the failure to insert
        //
        // if we do have the row, do an update
        checkSQL = checkSQL.replace("'null'", "null");

        try (Connection con = CMDBController.getConnection();
             PreparedStatement pStatement = con.prepareStatement(checkSQL))
        {

            pStatement.clearWarnings();
            rs = pStatement.executeQuery();
//not getting the id here
            if (rs.first())
            {
                iAutoId = rs.getInt(1);
                pStatement.close();
                return iAutoId;
            }
            pStatement.close();
            con.close();
        }
        catch (SQLException e)
        {
            s = String.format(CMLanguageController.getErrorProps().
                  getProperty("Formatted14"),
                  e.toString());

            CMHPIUtils.showDefaultMsg(
                  CMLanguageController.getErrorProps().
                        getProperty("Title"),
                  Thread.currentThread().getStackTrace()[1].getClassName(),
                  Thread.currentThread().getStackTrace()[1].getMethodName(),
                  s,
                  JOptionPane.ERROR_MESSAGE);
            return iAutoId;
        }
        sInsertSQL = createInsertQuery(sTable, keys, values);

        return CMDBController.insertAutoRow(sInsertSQL);
    }
}
