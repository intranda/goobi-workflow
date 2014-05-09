package de.sub.goobi.helper;

import javax.faces.context.FacesContext;

public class FacesContextHelper {
    private static FacesContext context;

    public static FacesContext getCurrentFacesContext() {
        return context != null ? context : FacesContext.getCurrentInstance();
    }

    public static void setFacesContext(FacesContext facesContext) {
        context = facesContext;
    }

    public static void reset() {
        context = null;
    }
}
