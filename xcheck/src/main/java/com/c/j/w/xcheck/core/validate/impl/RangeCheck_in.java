package com.c.j.w.xcheck.core.validate.impl;

import com.c.j.w.xcheck.core.validate.AbstractValidateMethod;
import com.c.j.w.xcheck.core.validate.ValidateParam;
import com.c.j.w.xcheck.core.XResult;
import org.springframework.stereotype.Component;

/**
 * 参数数值范围校验
 * @Author chenjw
 * @Date 2016年12月08日
 */
@Component("in")
public class RangeCheck_in extends AbstractValidateMethod {

    @Override
    public XResult validate(ValidateParam validateParam) {
        String argValues = validateParam.getArgumentsVal();
        String[] split = argValues.split(",");
        for (String e : split) {
            if (validateParam.getMainFieldVal().equals(e)) {
                return XResult.success();
            }
        }
        int lastIndex = argValues.lastIndexOf(",");

        String argComment = lastIndex == -1
                ? argValues
                : argValues.substring(0, lastIndex) + "或" + argValues.substring(lastIndex+1, argValues.length());
        return XResult.failure(getFieldAlias(validateParam) + "必须为" + argComment.replaceAll(",", "、"));
    }

}
