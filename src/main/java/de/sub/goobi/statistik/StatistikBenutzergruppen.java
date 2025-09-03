package de.sub.goobi.statistik;

import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

public final class StatistikBenutzergruppen {

    private StatistikBenutzergruppen() {
        // hide implicit public constructor
    }

    public static Dataset getDiagramm(List<Process> inProzesse) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Process proz : inProzesse) {
            Step step = proz.getAktuellerSchritt();
            /* wenn wirklich ein aktueller Schritt zurückgegeben wurde */
            if (step != null) {
                /* von dem Schritt alle verantwortlichen Benutzergruppen ermitteln und im Diagramm erfassen */
                for (Usergroup group : step.getBenutzergruppenList()) {
                    if (dataset.getIndex(group.getTitel()) != -1) {
                        dataset.setValue(group.getTitel(), dataset.getValue(group.getTitel()).intValue() + 1d);
                    } else {
                        dataset.setValue(group.getTitel(), 1);
                    }
                }

            }
        }
        return dataset;
    }

}
