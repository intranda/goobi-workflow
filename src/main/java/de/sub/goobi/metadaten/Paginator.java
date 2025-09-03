/**
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 *
 * Copyright 2011, Center for Retrospective Digitization, Göttingen (GDZ),
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.goobi.pagination.IntegerSequence;
import org.goobi.pagination.RomanNumberSequence;

import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.RomanNumeral;

/**
 * Sets new labels to a given set of pages.
 */
@Log4j2
public class Paginator {

    public enum Mode {
        PAGES,
        COLUMNS,
        FOLIATION,
        RECTOVERSO_FOLIATION,
        RECTOVERSO,
        DOUBLE_PAGES
    }

    public enum Type {
        ARABIC,
        ROMAN,
        UNCOUNTED,
        FREETEXT
    }

    public enum Scope {
        FROMFIRST,
        SELECTED
    }

    private int[] selectedPages;
    @Getter
    private Metadatum[] pagesToPaginate;
    private Mode paginationMode = Paginator.Mode.PAGES;
    private Scope paginationScope = Paginator.Scope.FROMFIRST;
    private String paginationStartValue = "uncounted";
    private Type paginationType = Paginator.Type.UNCOUNTED;
    private boolean fictitiousPagination = false;
    @Getter
    @Setter
    private String prefix = null;
    @Getter
    @Setter
    private String suffix = null;
    private String doublePageDiscriminator = "-";

    /**
     * Perform pagination.
     * 
     * @throws IllegalArgumentException Thrown if invalid config parameters have been set.
     */
    @SuppressWarnings("rawtypes")
    public void run() throws IllegalArgumentException {
        assertSelectionIsNotNull();
        assertValidPaginationStartValue();
        List sequence = createPaginationSequence();
        if (StringUtils.isNotBlank(prefix) || StringUtils.isNotBlank(suffix)) {
            sequence = addPrefixAndSuffixToSequence(sequence);
        }
        applyPaginationSequence(sequence);
    }

    @SuppressWarnings("rawtypes")
    private void applyPaginationSequence(List sequence) {
        if (paginationScope == Scope.SELECTED) {
            applyToSelected(sequence);
        } else if (paginationScope == Scope.FROMFIRST) {
            applyFromFirstSelected(sequence);
        }
    }

    private void assertSelectionIsNotNull() {
        if (selectedPages == null || selectedPages.length == 0) {
            throw new IllegalArgumentException("No pages selected for pagination.");
        }
    }

    private void assertValidPaginationStartValue() {
        // arabic numbers
        if (paginationType == Paginator.Type.ARABIC) {
            try {
                Integer.parseInt(paginationStartValue);
            } catch (NumberFormatException e) {
                Helper.setFehlerMeldung(Helper.getTranslation("NoRomanNumber", paginationStartValue));
            }
        }
        // roman numbers
        if (paginationType == Paginator.Type.ROMAN) {
            RomanNumeral roman = new RomanNumeral();
            try {
                roman.setValue(paginationStartValue.toUpperCase());
            } catch (NumberFormatException e) {
                Helper.setFehlerMeldung(Helper.getTranslation("NoRomanNumber", paginationStartValue));
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private List createPaginationSequence() {
        int increment = determineIncrementFromPaginationMode();
        int start = determinePaginationBaseValue();

        int end = 0;
        if (Mode.DOUBLE_PAGES.equals(paginationMode) || Paginator.Mode.RECTOVERSO_FOLIATION.equals(paginationMode)) {
            end = determinePaginationEndValue(2, start) + 1;
            doublePageDiscriminator = "-";
        } else {
            end = determinePaginationEndValue(increment, start);
        }
        List sequence = determineSequenceFromPaginationType(increment, start, end);

        if (fictitiousPagination) {
            sequence = addSquareBracketsToEachInSequence(sequence);
        }

        if ((paginationMode == Paginator.Mode.PAGES) || (paginationMode == Paginator.Mode.COLUMNS)) {
            return sequence;
        }

        if (Mode.DOUBLE_PAGES.equals(paginationMode)) {
            if (Type.UNCOUNTED.equals(paginationType) || Type.FREETEXT.equals(paginationType)) {
                sequence = cloneEachInSequence(sequence);
            }
            return scrunchSequence(sequence);
        }

        sequence = cloneEachInSequence(sequence);

        if (paginationType == Paginator.Type.UNCOUNTED || paginationType == Paginator.Type.FREETEXT) {
            return sequence;
        }

        if ((paginationMode == Paginator.Mode.RECTOVERSO) || (paginationMode == Paginator.Mode.RECTOVERSO_FOLIATION)) {
            sequence = addAlternatingRectoVersoSuffixToEachInSequence(sequence);
        }

        if (paginationMode == Paginator.Mode.RECTOVERSO_FOLIATION) {
            sequence.remove(0);
            sequence = scrunchSequence(sequence);
        }

        return sequence;
    }

    @SuppressWarnings("rawtypes")
    private List addPrefixAndSuffixToSequence(List sequence) {
        List<Object> newSequence = new ArrayList<>(sequence.size());
        for (Object o : sequence) {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(prefix)) {
                sb.append(prefix);
            }
            sb.append(o.toString());
            if (StringUtils.isNotBlank(suffix)) {
                sb.append(suffix);
            }
            newSequence.add(sb.toString());
        }
        return newSequence;
    }

    @SuppressWarnings("rawtypes")
    private List addSquareBracketsToEachInSequence(List sequence) {
        List<Object> fictitiousSequence = new ArrayList<>(sequence.size());
        for (Object o : sequence) {
            String newLabel = o.toString();
            fictitiousSequence.add("[" + newLabel + "]");
        }
        return fictitiousSequence;
    }

    @SuppressWarnings("rawtypes")
    private List addAlternatingRectoVersoSuffixToEachInSequence(List sequence) {
        List<Object> rectoversoSequence = new ArrayList<>(sequence.size());
        boolean toggle = false;
        for (Object o : sequence) {
            String label = o.toString();
            toggle = !toggle;
            rectoversoSequence.add(label + (toggle ? "r" : "v"));
        }
        return rectoversoSequence;
    }

    @SuppressWarnings("rawtypes")
    private List scrunchSequence(List sequence) {
        List<Object> scrunchedSequence = new ArrayList<>((sequence.size() / 2));
        String prev = "";
        boolean scrunch = false;
        for (Object o : sequence) {
            if (scrunch) {
                scrunchedSequence.add(prev + doublePageDiscriminator + o.toString());
            } else {
                prev = o.toString();
            }
            scrunch = !scrunch;
        }
        return scrunchedSequence;
    }

    @SuppressWarnings("rawtypes")
    private List cloneEachInSequence(List sequence) {
        List<Object> foliationSequence = new ArrayList<>(sequence.size() * 2);
        for (Object o : sequence) {
            foliationSequence.add(o);
            foliationSequence.add(o);
        }
        return foliationSequence;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List determineSequenceFromPaginationType(int increment, int start, int end) {
        List sequence = null;

        switch (paginationType) {
            case UNCOUNTED:
                sequence = new ArrayList(1);
                sequence.add("uncounted");
                break;
            case FREETEXT:
                sequence = new ArrayList(1);
                sequence.add(paginationStartValue);
                break;
            case ROMAN:
                sequence = new RomanNumberSequence(start, end, increment);
                // if start value is lower case, convert all to lower case
                if (paginationStartValue.toLowerCase().equals(paginationStartValue)) {
                    List sequenceSmall = new ArrayList<String>();
                    for (Object rno : sequence) {
                        rno = ((String) rno).toLowerCase();
                        sequenceSmall.add(((String) rno).toLowerCase());
                    }
                    sequence = sequenceSmall;
                }
                break;
            case ARABIC:
                sequence = new IntegerSequence(start, end, increment);
                break;
            default:
                sequence = new IntegerSequence(start, end, increment);
        }
        return sequence;
    }

    private int determineIncrementFromPaginationMode() {
        int increment = 1;
        if (paginationMode == Paginator.Mode.COLUMNS) {
            increment = 2;
        }
        return increment;
    }

    private int determinePaginationEndValue(int increment, int start) {

        int numSelectedPages = selectedPages.length;
        if (paginationScope == Paginator.Scope.FROMFIRST) {
            int first = selectedPages[0];
            numSelectedPages = pagesToPaginate.length - first;
        }
        return start + (numSelectedPages * increment);
    }

    @SuppressWarnings("rawtypes")
    private void applyFromFirstSelected(List sequence) {
        int first = selectedPages[0] - 1;
        Iterator seqit = sequence.iterator();
        for (int pageNum = first; pageNum < pagesToPaginate.length; pageNum++) {
            if (!seqit.hasNext()) {
                seqit = sequence.iterator();
            }
            pagesToPaginate[pageNum].setWert(String.valueOf(seqit.next()));
        }
    }

    @SuppressWarnings("rawtypes")
    private void applyToSelected(List sequence) {
        Iterator seqit = sequence.iterator();
        for (int num : selectedPages) {
            if (!seqit.hasNext()) {
                seqit = sequence.iterator();
            }
            pagesToPaginate[num - 1].setWert(String.valueOf(seqit.next()));
        }
    }

    private int determinePaginationBaseValue() {

        int paginationBaseValue = 1;

        if (paginationType == Paginator.Type.ARABIC) {
            paginationBaseValue = Integer.parseInt(paginationStartValue);
        } else if (paginationType == Paginator.Type.ROMAN) {
            RomanNumeral r = new RomanNumeral();
            try {
                r.setValue(paginationStartValue.toUpperCase());
                paginationBaseValue = r.intValue();
            } catch (NumberFormatException exception) {
                log.warn(exception);
            }
        }
        return paginationBaseValue;
    }

    /**
     * Give a list of page numbers to select pages to actually paginate.
     * 
     * @param selectedPages Array numbers, each pointing to a given page set via <code>setPagesToPaginate</code>
     * 
     * @return This object for fluent interfacing.
     */
    public Paginator setPageSelection(int[] selectedPages) {
        this.selectedPages = selectedPages;
        return this;
    }

    /**
     * Give page objects to apply new page labels on.
     * 
     * @param newPaginated Array of page objects.
     * @return This object for fluent interfacing.
     */
    public Paginator setPagesToPaginate(Metadatum[] newPaginated) {
        this.pagesToPaginate = newPaginated;
        return this;
    }

    /**
     * Set pagination mode.
     * 
     * @param paginationMode Mode of counting pages.
     * @return This object for fluent interfacing.
     */
    public Paginator setPaginationMode(Mode paginationMode) {
        this.paginationMode = paginationMode;
        return this;
    }

    /**
     * Set scope of pagination.
     * 
     * @param paginationScope Set which pages from a selection get labeled.
     * @return This object for fluent interfacing.
     */
    public Paginator setPaginationScope(Scope paginationScope) {
        this.paginationScope = paginationScope;
        return this;
    }

    /**
     * Set start value of pagination. Counting up starts here depending on the pagination mode set.
     * 
     * @param paginationStartValue May contain arabic or roman number.
     * @return This object for fluent interfacing.
     */
    public Paginator setPaginationStartValue(String paginationStartValue) {
        this.paginationStartValue = paginationStartValue;
        return this;
    }

    /**
     * Determine weather arabic or roman numbers should be used when counting.
     * 
     * @param paginationType Set style of pagination numbers.
     * @return This object for fluent interfacing.
     */
    public Paginator setPaginationType(Type paginationType) {
        this.paginationType = paginationType;
        return this;
    }

    /**
     * Enable or disable fictitious pagination using square bracktes around numbers.
     * 
     * @param b True, fictitious pagination. False, regular pagination.
     * @return This object for fluent interfacing.
     */
    public Paginator setFictitious(boolean b) {
        this.fictitiousPagination = b;
        return this;
    }
}
