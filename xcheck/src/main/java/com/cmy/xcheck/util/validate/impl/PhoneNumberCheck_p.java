package com.cmy.xcheck.util.validate.impl;

import com.cmy.xcheck.support.XResult;
import com.cmy.xcheck.util.validate.ValidateMethod;
import com.cmy.xcheck.util.validate.ValidateParam;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码
 * @Author chenjw
 * @Date 2016年12月26日
 */
@Component
public class PhoneNumberCheck_p implements ValidateMethod {
    private static final Pattern Tel_Pattern = Pattern.compile("^\\d{11}$");

    @Override
    public XResult validate(ValidateParam validateParam) {
        // 默认校验11位手机号码,后期可根据参数加入其它校验方法
        Matcher matcher = Tel_Pattern.matcher(validateParam.getMainFieldVal());
        if (matcher.matches()) {
            return XResult.success();
        }
        return XResult.failure(validateParam.getMainFieldName() + "手机号码格式不正确");
    }

    @Override
    public String getMethodAttr() {
        return "p";
    }
}