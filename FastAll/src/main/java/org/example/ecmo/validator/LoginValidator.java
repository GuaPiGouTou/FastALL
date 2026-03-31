package org.example.ecmo.validator;

import org.example.ecmo.context.LoginContext;

public interface LoginValidator {
    
    boolean validate(LoginContext context);
    
    int getOrder();
}
