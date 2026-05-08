package org.goobi.production.flow.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.faces.model.SelectItem;

@ExtendWith(MockitoExtension.class)
public class SearchResultHelperTest {

    private SearchResultHelper helper;
    private List<SearchColumn> columnList;

    @BeforeEach
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testGetResult() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            Object[] row1 = new Object[] { "1", "Test 1" };
            Object[] row2 = new Object[] { "2", "Test 2" };
            List lst = new ArrayList();
            lst.add(row1);
            lst.add(row2);
            mockedProcessManager.when(() -> ProcessManager.runSQL(Mockito.anyString())).thenReturn(lst);

            XSSFWorkbook wb = helper.getResult(columnList, "", "{process.propertyname}", true, true);

            assertNotNull(wb);
            assertEquals("Search results", wb.getSheetName(0));
        }
    }

}
