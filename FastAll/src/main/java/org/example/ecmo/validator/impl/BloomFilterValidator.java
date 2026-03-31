package org.example.ecmo.validator.impl;

import org.example.ecmo.context.LoginContext;
import org.example.ecmo.validator.LoginValidator;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BloomFilterValidator implements LoginValidator {

    @Autowired
    private RBloomFilter<String> userBloomFilter;

    @Override
    public boolean validate(LoginContext context) {
        if (!userBloomFilter.contains(context.getUsername())) {
            context.setErrorMessage("用户名不存在");
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
