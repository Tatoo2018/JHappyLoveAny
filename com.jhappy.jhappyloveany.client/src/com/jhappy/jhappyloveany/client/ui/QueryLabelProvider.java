package com.jhappy.jhappyloveany.client.ui;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class QueryLabelProvider extends LabelProvider implements ITableLabelProvider {
    @Override
    public String getColumnText(Object element, int columnIndex) {
        QueryModel model = (QueryModel) element;
        switch (columnIndex) {
            case 0: return model.getType();        // "xml" or "properties"
            case 1: return model.getFilepath();    // ".*\.xml$"
            case 2: return model.getXpath();       // "//@id"
            case 3: return model.getDescription();
            default: return "";
        }
    }
    @Override public Image getColumnImage(Object element, int columnIndex) { return null; }
}