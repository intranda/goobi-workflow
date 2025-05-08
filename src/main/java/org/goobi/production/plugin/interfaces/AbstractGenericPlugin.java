package org.goobi.production.plugin.interfaces;

import de.sub.goobi.helper.Helper;
import lombok.extern.slf4j.Slf4j;

import jakarta.faces.application.Application;
import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import java.io.IOException;

@Slf4j
public abstract class AbstractGenericPlugin implements IGenericPlugin {
    private UIComponent modal;

    public static void success(String message) {
        log.debug(message);
        Helper.setMeldung(message);
    }

    public static void warn(String message) {
        log.warn(message);
        Helper.setFehlerMeldung(message);
    }

    public static void error(String message) {
        log.error(message);
        Helper.setFehlerMeldung(message);
    }

    private UIComponent generateModalComponent() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        FaceletContext faceletContext = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);

        UIComponent parent = FacesContext.getCurrentInstance().getApplication().createComponent(HtmlPanelGroup.COMPONENT_TYPE);
        parent.setId("pluginModal" + getId());

        Resource componentResource = context.getApplication().getResourceHandler().createResource("barcodeModal.xhtml", "plugins");
        UIComponent composite = application.createComponent(context, componentResource);
        composite.setId("pluginModal" + getId());

        // This basically creates <composite:implementation>.
        UIComponent implementation = application.createComponent(UIPanel.COMPONENT_TYPE);
        implementation.setRendererType("javax.faces.Group");
        composite.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, implementation);

        parent.getChildren().add(composite);
        parent.pushComponentToEL(context, composite); // This makes #{cc} available.
        try {
            faceletContext.includeFacelet(implementation, componentResource.getURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        parent.popComponentFromEL(context);

        //add some attributes...
        parent.getAttributes().put("plugin", this); // this might not be required..
        composite.getAttributes().put("plugin", this);

        return parent;
    }

    public void setModal(UIComponent c) {

    }

    public synchronized UIComponent getModal() {
        if (this.modal == null && this.getModalPath() != null) {
            this.modal = generateModalComponent();
        }
        return this.modal;
    }
}
