package org.goobi.production.flow.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.faces.model.SelectItem;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProcessManager.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class SearchResultHelperTest {

    private SearchResultHelper helper;
    private List<SearchColumn> columnList;

    @Before
    public void setUp() throws Exception {

        SelectItem column1 = new SelectItem("Titel", "prozesse.Titel");
        column1.setValue("prozesse.title");
        SelectItem column2 = new SelectItem("ProzesseID", "prozesse.ProzesseID");
        column2.setValue("prozesse.ProzesseID");

        helper = new SearchResultHelper(Arrays.asList(column1, column2));
        SearchColumn col = new SearchColumn(1);
        col.setValue("prozesse.title");
        SearchColumn col2 = new SearchColumn(2);
        col2.setValue("prozesse.ProzesseID");
        columnList = Arrays.asList(col, col2);
    }

    @Test
    public void testGetResult() {
        PowerMock.mockStatic(ProcessManager.class);
        Object[] row1 = new Object[] { "1", "Test 1" };
        Object[] row2 = new Object[] { "2", "Test 2" };
        List lst = new ArrayList();
        lst.add(row1);
        lst.add(row2);
        EasyMock.expect(ProcessManager.runSQL(EasyMock.anyString()))
                .andReturn(lst)
                .anyTimes();
        PowerMock.replayAll();

        XSSFWorkbook wb = helper.getResult(columnList, "", "{process.propertyname}", true, true);

        assertNotNull(wb);
        assertEquals("Search results", wb.getSheetName(0));

        PowerMock.verifyAll();
    }

}
