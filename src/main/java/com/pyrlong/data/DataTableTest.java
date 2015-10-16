/**
 * 
 * DataTableTest.java Create on Jul 31, 2012 9:07:01 PM
 * 
 * @author James Cheung
 * @version 1.0
 *          Copyright (c) 2012 Pyrlong,Inc. All Rights Reserved.
 */
package com.pyrlong.data;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * @author James Cheung
 * 
 */
public class DataTableTest {

    DataTable localTable;

    @Before
    public void setUp() throws Exception {
        localTable = DataTableTestTool.createTestTable();
        DataTableTestTool.fillTable(localTable);
    }

    /**
     * Test method for {@link com.pyrlong.data.DataTable#getTotalCount()}.
     * 
     * @throws Exception
     */
    @Test
    public final void testGetTotalCount() throws Exception {
        Assert.assertEquals(100, localTable.getTotalCount());
        DataTableTestTool.fillTable(localTable);
        Assert.assertEquals(200, localTable.getTotalCount());
    }

    /**
     * Test method for {@link com.pyrlong.data.DataTable#getRows()}.
     * 
     * @throws Exception
     */
    @Test
    public final void testGetRows() throws Exception {
        DataRowCollection rows = localTable.getRows();
        Assert.assertEquals(rows.size(), localTable.getTotalCount());
    }

    /**
     * Test method for {@link com.pyrlong.data.DataTable#getColumns()}.
     * 
     * @throws Exception
     */
    @Test
    public final void testGetColumns() throws Exception {
        DataColumnCollection cols = localTable.getColumns();
        Assert.assertEquals(cols.size(), 100);
    }

    /**
     * Test method for
     * {@link com.pyrlong.data.DataTable#getValue(int, java.lang.String)}.
     */
    @Test
    public final void testGetValueIntString() {
        Object actual = localTable.getValue(0, "COL0");
        Assert.assertEquals("0_0", actual);
    }

    /**
     * Test method for {@link com.pyrlong.data.DataTable#getValue(int, int)}.
     */
    @Test
    public final void testGetValueIntInt() {
        Object actual = localTable.getValue(0, 0);
        Assert.assertEquals("0_0", actual);
    }

    /**
     * Test method for {@link com.pyrlong.data.DataTable#newRow()}.
     * 
     * @throws Exception
     */
    @Test
    public final void testNewRow() throws Exception {
        DataRow newRow = localTable.newRow();
        Assert.assertEquals(newRow.getTable(), localTable);
        Assert.assertEquals(newRow.getColumns(), localTable.getColumns());
        Assert.assertEquals(newRow.getRowIndex(), 101);
    }

    /**
     * Test method for
     * {@link com.pyrlong.data.DataTable#setValue(int, int, java.lang.Object)}.
     */
    @Test
    public final void testSetValueIntIntObject() {
        Object actual = localTable.getValue(0, 0);
        Assert.assertEquals("0_0", actual);
        localTable.setValue(0, 0, "100");
        Assert.assertEquals("100", localTable.getValue(0, 0));
    }

    /**
     * Test method for
     * {@link com.pyrlong.data.DataTable#setValue(int, java.lang.String, java.lang.Object)}
     * .
     */
    @Test
    public final void testSetValueIntStringObject() {
        localTable.setValue(0, "COL0", "100");
        Assert.assertEquals("100", localTable.getValue(0, 0));
    }

    /**
     * Test method for
     * {@link com.pyrlong.data.DataTable#select(java.lang.String)}.
     * 
     * @throws Exception
     */
    @Test
    public final void testSelectString() throws Exception {
        // 测试数据表的选择过滤方法
        DataTable table = new DataTable(); // 新建一个数据表对象，不带构造函数参数
        DataTableTestTool.fillTable(table, "col_");// 填充数据
        DataTableTestTool.printTable(table);
        DataTable rows = table.select("col_0> 2 && col_1<10");
        Assert.assertNotNull(rows);
        DataTableTestTool.printTable(rows);
        Assert.assertEquals(2, rows.getTotalCount());
        // 测试查询指定列
        DataTable filterResult = table.select("col_1>2 && col_2<10",
                "col_1,col_2,col_3,col_4".split(","), true);
        DataTableTestTool.printTable(filterResult);
    }

    /**
     * Test method for
     * {@link com.pyrlong.data.DataTable#structToXml(java.lang.String)}.
     * 
     * @throws Exception
     */
//    @Test
//    public final void testStructToXml() throws Exception {
//        DataTable table = DataTableTestTool.createTestTable();
//        table.structToXml(table.getTableName() + ".table");
//        DataTable newTable = DataTable.fromXml(table.getTableName() + ".table");
//        Assert.assertEquals(table.getColumns().size(), newTable.getColumns()
//                .size());
//        for (DataColumn col : table.getColumns()) {
//            Assert.assertEquals(col.getCaptionName(), newTable.getColumns()
//                    .get(col.getColumnIndex()).getCaptionName());
//            Assert.assertEquals(col.getColumnName(),
//                    newTable.getColumns().get(col.getColumnIndex())
//                            .getColumnName());
//            Assert.assertEquals(col.getDataType(),
//                    newTable.getColumns().get(col.getColumnIndex())
//                            .getDataType());
//        }
//        FileOper.delFile(table.getTableName() + ".table");
//    }

}
