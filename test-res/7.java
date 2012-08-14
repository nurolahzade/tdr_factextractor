/**
 * $Id$
 *
 * easyCSV: The JAVA(TM) library for reading, writing and converting CSV files
 *
 * Copyright (C) 2006  Oliver J. Siegmar <oliver@siegmar.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.easycsv;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easycsv.CsvDataStore;
import org.easycsv.CsvHeaderDetector;
import org.easycsv.CsvRow;
import org.easycsv.EncloseStrategy;
import org.easycsv.ExportSettings;
import org.easycsv.ImportSettings;
import org.easycsv.filter.HashLineFilter;
import org.easycsv.filter.PatternLineFilter;
import org.easycsv.handler.CsvFileHandler;
import org.easycsv.handler.CsvStringHandler;
import org.easycsv.handler.ExcelHandler;


/**
 *
 * @author Oliver J. Siegmar
 * @version $LastChangedRevision$
 *
 */
public class StringHandlerTest extends TestCase
{

    private String referenceString;
    private final ImportSettings defaultImportSettings = new ImportSettings();
    private final ExportSettings defaultExportSettings = new ExportSettings();


    public StringHandlerTest()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,colC,colD\n");
        sb.append("valA,valB,valC,valD\n");
        sb.append("valA2,valB2,valC2,valD2\n");

        referenceString = sb.toString();
    }

    public void testRead()
    throws Exception
    {
        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(defaultImportSettings, referenceString);

        // Header test
        assertEquals("[colA, colB, colC, colD]", csvDataStore.getHeader().toString());

        // Rows test
        assertEquals("{colA=valA, colB=valB, colC=valC, colD=valD}", csvDataStore.getRow(0).toString());
        assertEquals("{colA=valA2, colB=valB2, colC=valC2, colD=valD2}", csvDataStore.getRow(1).toString());

        // Col test
        assertEquals("[valA, valA2]", csvDataStore.getCol("colA").toString());
        assertEquals("[valC, valC2]", csvDataStore.getCol("colC").toString());

        // Cols test
        List<CsvRow> rows = csvDataStore.getCols(0, 2);
        assertEquals(2, rows.size());
        assertEquals("{colA=valA, colC=valC}", rows.get(0).toString());
        assertEquals("{colA=valA2, colC=valC2}", rows.get(1).toString());

        rows = csvDataStore.getCols("colB", "colC");
        assertEquals(2, rows.size());
        assertEquals("{colB=valB, colC=valC}", rows.get(0).toString());
        assertEquals("{colB=valB2, colC=valC2}", rows.get(1).toString());
    }

    public void testReadWOHeader()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();
        importSettings.setHeader(false);

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, referenceString);

        assertEquals(3, csvDataStore.getRowCount());
        assertEquals("[colA, colB, colC, colD]", csvDataStore.getRow(0).toString());
        assertEquals("[valA, valB, valC, valD]", csvDataStore.getRow(1).toString());
        assertEquals("[valA2, valB2, valC2, valD2]", csvDataStore.getRow(2).toString());

        assertEquals("[colA, valA, valA2]", csvDataStore.getCol(0).toString());
        assertEquals("[colB, valB, valB2]", csvDataStore.getCol(1).toString());
        assertEquals("[colC, valC, valC2]", csvDataStore.getCol(2).toString());
        assertEquals("[colD, valD, valD2]", csvDataStore.getCol(3).toString());
    }

    public void testDupeHeader()
    throws Exception
    {
        final CsvDataStore csvDataStore = new CsvDataStore();
        csvDataStore.setAllowDupesInHeader(true);

        csvDataStore.setHeader("colA", "colB", "colC", "colB");
        csvDataStore.addRow("valA", "valB", "valC", "valD");
        csvDataStore.addRow("valA2", "valB2", "valC2", "valD2");

        assertEquals("[colA, colB, colC, colB]", csvDataStore.getHeader().toString());

        assertEquals("[valA, valA2]", csvDataStore.getCol(0).toString());
        assertEquals("[valB, valB2]", csvDataStore.getCol(1).toString());
        assertEquals("[valC, valC2]", csvDataStore.getCol(2).toString());
        assertEquals("[valD, valD2]", csvDataStore.getCol(3).toString());

        assertEquals("[valA, valA2]", csvDataStore.getCol("colA").toString());
        assertEquals("[valB, valB2]", csvDataStore.getCol("colB").toString());
        assertEquals("[valC, valC2]", csvDataStore.getCol("colC").toString());
    }

    public void testReadFile()
    throws Exception
    {
        final CsvDataStore csvDataStore = CsvFileHandler.importCsv(defaultImportSettings, new File(this.getClass().getResource("/test1.csv").getFile()));

        // Header test
        assertEquals("[colA, colB, colC, colD]", csvDataStore.getHeader().toString());

        // Rows test
        assertEquals("{colA=valA, colB=valB, colC=valC, colD=valD}", csvDataStore.getRow(0).toString());
        assertEquals("{colA=valA2, colB=valB2, colC=valC2, colD=valD2}", csvDataStore.getRow(1).toString());

        // Col test
        assertEquals("[valA, valA2]", csvDataStore.getCol("colA").toString());
        assertEquals("[valC, valC2]", csvDataStore.getCol("colC").toString());
    }

    public void testWriteString()
    throws Exception
    {
        final CsvDataStore csvDataStore = CsvFileHandler.importCsv(defaultImportSettings, new File(this.getClass().getResource("/test1.csv").getFile()));

        assertEquals(referenceString, CsvStringHandler.exportCsv(defaultExportSettings, csvDataStore));
    }

    public void testCreate()
    throws Exception
    {
        final CsvDataStore csvDataStore = new CsvDataStore();

        csvDataStore.setHeader("colA", "colB", "colC", "colD");
        csvDataStore.addRow("valA", "valB", "valC", "valD");
        csvDataStore.addRow("valA2", "valB2", "valC2", "valD2");

        assertEquals(referenceString, CsvStringHandler.exportCsv(defaultExportSettings, csvDataStore));
    }

    public void testCreateExcel()
    throws Exception
    {
        final CsvDataStore csvDataStore = CsvFileHandler.importCsv(defaultImportSettings, new File(this.getClass().getResource("/test1.csv").getFile()));

        final File file = new File(this.getClass().getResource("/").getFile(), "test1.xls");
        ExcelHandler.exportExcel(defaultExportSettings, csvDataStore, file);
    }

    public void testReadExcel()
    throws Exception
    {
        final CsvDataStore csvDataStore = ExcelHandler.importExcel(defaultImportSettings, new File(this.getClass().getResource("/test1.xls").getFile()));

        assertEquals(referenceString, CsvStringHandler.exportCsv(defaultExportSettings, csvDataStore));
    }

    public void testQuoting()
    throws Exception
    {
        final ExportSettings exportSettings = new ExportSettings();
        exportSettings.setEncloseStrategy(EncloseStrategy.ALWAYS);

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(defaultImportSettings, referenceString);

        final StringBuilder sb = new StringBuilder();

        sb.append("\"colA\",\"colB\",\"colC\",\"colD\"\n");
        sb.append("\"valA\",\"valB\",\"valC\",\"valD\"\n");
        sb.append("\"valA2\",\"valB2\",\"valC2\",\"valD2\"\n");

        assertEquals(sb.toString(), CsvStringHandler.exportCsv(exportSettings, csvDataStore));
    }

    public void testQuoting2()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();

        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,\"colC,colC2\",colD\n");
        sb.append("valA,valB,valC,\"valD,valDD\"\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals(Arrays.asList("colA", "colB", "colC,colC2", "colD"), csvDataStore.getHeader());
        assertEquals(Arrays.asList("valA", "valB", "valC", "valD,valDD"), csvDataStore.getRow(0).getList());

        assertEquals(sb.toString(), CsvStringHandler.exportCsv(defaultExportSettings, csvDataStore));
    }

    public void testQuoting3()
    throws Exception
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,\"colC,colC2\",colD\n");
        sb.append("valA,valB,valC,\"\"\"valD\"\",\"\"valDD\"\"\"\n");
        sb.append("valA2,\"val,\"\"B2\"\"\",valC2,valD2\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(defaultImportSettings, sb.toString());

        assertEquals(Arrays.asList("colA", "colB", "colC,colC2", "colD"), csvDataStore.getHeader());
        assertEquals(Arrays.asList("valA", "valB", "valC", "\"valD\",\"valDD\""), csvDataStore.getRow(0).getList());
        assertEquals(Arrays.asList("valA2", "val,\"B2\"", "valC2", "valD2"), csvDataStore.getRow(1).getList());

        assertEquals(sb.toString(), CsvStringHandler.exportCsv(defaultExportSettings, csvDataStore));
    }

    public void testNegativeFilter()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();
        importSettings.setLineFilter(new HashLineFilter());

        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,colC,colD\n");
        sb.append("#valA,valB,valC,valD\n");
        sb.append("valA2,valB2,valC2,valD2\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals(1, csvDataStore.getRowCount());
        assertEquals(Arrays.asList("colA", "colB", "colC", "colD"), csvDataStore.getHeader());
        assertEquals(Arrays.asList("valA2", "valB2", "valC2", "valD2"), csvDataStore.getRow(0).getList());
    }

    public void testPositiveFilter()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();
        importSettings.setLineFilter(new PatternLineFilter("^valA.*$"));

        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,colC,colD\n");
        sb.append("valA,valB,valC,valD\n");
        sb.append("vala2,valB2,valC2,valD2\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals(1, csvDataStore.getRowCount());
        assertEquals(Arrays.asList("colA", "colB", "colC", "colD"), csvDataStore.getHeader());
        assertEquals(Arrays.asList("colA", "colB", "colC", "colD"), csvDataStore.getHeader());
    }

    public void testStartLine()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();
        importSettings.setStartLine(1);

        final StringBuilder sb = new StringBuilder();

        sb.append("A,B,C,D\n");
        sb.append("colA,colB,colC,colD\n");
        sb.append("valA,valB,valC,valD\n");
        sb.append("valA2,valB2,valC2,valD2\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals(2, csvDataStore.getRowCount());
        assertEquals("{colA=valA, colB=valB, colC=valC, colD=valD}", csvDataStore.getRow(0).toString());
        assertEquals("{colA=valA2, colB=valB2, colC=valC2, colD=valD2}", csvDataStore.getRow(1).toString());
    }

    public void testMultiLine()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();

        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,colC,\"colD\ncolD2\"\n");
        sb.append("valA,valB,valC,\"valD\nvalDD\"\n");
        sb.append("valA2,valB2,valC2,valD2\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals("[colA, colB, colC, colD\ncolD2]", csvDataStore.getHeader().toString());
    }

    public void testEOLStyleMac()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();

        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,colC,\"colD\rcolD2\"\r");
        sb.append("valA,valB,valC,\"valD\rvalDD\"\r");
        sb.append("valA2,valB2,valC2,valD2\r");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals("[colA, colB, colC, colD\rcolD2]", csvDataStore.getHeader().toString());
    }

    public void testEOLStyleWin()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();

        final StringBuilder sb = new StringBuilder();

        sb.append("colA,colB,colC,\"colD\r\ncolD2\"\r\n");
        sb.append("valA,valB,valC,\"valD\r\nvalDD\"\r\n");
        sb.append("valA2,valB2,valC2,valD2\r\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals("[colA, colB, colC, colD\r\ncolD2]", csvDataStore.getHeader().toString());
    }

    public void testEscape()
    throws Exception
    {
        final ExportSettings exportSettings = new ExportSettings();
        exportSettings.setEncloseStrategy(EncloseStrategy.NEVER);
        exportSettings.setEscaping(true);

        final CsvDataStore csvDataStore = new CsvDataStore();

        csvDataStore.setHeader("colA", "colB", "colC", "colD");
        csvDataStore.addRow("valA", "valB,valB2,valB3", "valC", "valD");

        final String export = CsvStringHandler.exportCsv(exportSettings, csvDataStore);

        final StringBuilder sb = new StringBuilder();
        sb.append("colA,colB,colC,colD\n");
        sb.append("valA,valB\\,valB2\\,valB3,valC,valD\n");

        assertEquals(sb.toString(), export);
    }

    public void testHeaderDetection()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();
        importSettings.setHeader(false);
        importSettings.setCsvHeaderDetector(new CsvHeaderDetector() {
            public boolean isHeader(List<String> header)
            {
                return header.contains("colA");
            }
        });

        final StringBuilder sb = new StringBuilder();
        sb.append("colA,colB,colC,colD\n");
        sb.append("valA,valB,valC,valD\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals("[colA, colB, colC, colD]", csvDataStore.getHeader().toString());
    }

    public void testHeaderDetection2()
    throws Exception
    {
        final ImportSettings importSettings = new ImportSettings();
        importSettings.setHeader(false);
        importSettings.setCsvHeaderDetector(new CsvHeaderDetector() {
            public boolean isHeader(List<String> header)
            {
                return false;
            }
        });

        final StringBuilder sb = new StringBuilder();
        sb.append("colA,colB,colC,colD\n");
        sb.append("valA,valB,valC,valD\n");

        final CsvDataStore csvDataStore = CsvStringHandler.importCsv(importSettings, sb.toString());

        assertEquals("[]", csvDataStore.getHeader().toString());
    }

}
