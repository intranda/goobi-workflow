package org.goobi.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import de.sub.goobi.helper.Helper;

@FacesValidator("org.goobi.validator.TimeValidator")
public class TimeValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String data = value.toString();
        int time = new Integer(data).intValue();
        if (time != 0) {
            if (time < 10 || time > 30) {
                FacesMessage msg = new FacesMessage(Helper.getTranslation("metadataSaveTimeError"), Helper.getTranslation("metadataSaveTimeError"));
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(msg);
            }
        }

    }

}
