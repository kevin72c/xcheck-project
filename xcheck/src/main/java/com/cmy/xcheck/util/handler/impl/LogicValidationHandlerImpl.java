package com.cmy.xcheck.util.handler.impl;

import com.cmy.xcheck.exception.ExpressionDefineException;
import com.cmy.xcheck.support.XBean;
import com.cmy.xcheck.support.XResult;
import com.cmy.xcheck.util.StringUtil;
import com.cmy.xcheck.util.XHelper;
import com.cmy.xcheck.util.handler.ValidationHandler;
import com.cmy.xcheck.util.item.XCheckItem;
import com.cmy.xcheck.util.item.impl.XCheckItemLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogicValidationHandlerImpl implements ValidationHandler {

    @Autowired
    private XHelper xHelper;
    private static final ScriptEngine JS_ENGINE = new ScriptEngineManager().getEngineByName("javascript");
    private static final Pattern Date_Format_Pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s?(\\d{1,2}:\\d{1,2}(:\\d{1,3})?)?");
    private static final DateFormat Date_Format = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat Date_Time_Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

    @Override
    public XResult validate(XBean xBean, XCheckItem checkItem, Map<String, String[]> requestParams) {
        XCheckItemLogic checkItemLogic = (XCheckItemLogic) checkItem;
        String[] leftValues = requestParams.get(checkItemLogic.getLeftField());
        String[] rightValues = requestParams.get(checkItemLogic.getRightField());
        if (leftValues == null) {
            return XResult.failure(checkItemLogic.getLeftField() + "不能为空");
        }
        if (rightValues == null) {
            return XResult.failure(checkItemLogic.getRightField() + "不能为空");
        }
        if (leftValues.length != rightValues.length) {
            return XResult.failure(checkItemLogic.getLeftField() + "或" + checkItemLogic.getRightField() + "填写不正确");
        }
        return compareEachFiled(leftValues, rightValues, xBean, checkItemLogic);
    }

    public static void main(String[] args) {
        Matcher matcher = Date_Format_Pattern.matcher("2012-12-12");
        System.out.println(matcher.matches());
    }
    private String getValue(String field, String fieldValue) {
        if (StringUtil.isDecimal(field)) {
            return field;
        } else {
            if (isDateFormat(fieldValue)) {
                return toLongDate(fieldValue);
            } else {
                return fieldValue;
            }
        }
    }
    private boolean isDateFormat(String str) {
        return Date_Format_Pattern.matcher(str).matches();
    }

    /**
     * 转长整形字符日期
     */
    private String toLongDate(String date){
        try {
            if (date.length() == 10) {
                return String.valueOf(Date_Format.parse(date).getTime());
            } else {
                return String.valueOf(Date_Time_Format.parse(date).getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private XResult compareEachFiled(String[] leftValues, String[] rightValues, XBean xBean, XCheckItemLogic checkItemLogic) {
        for (int i = 0; i < leftValues.length; i++) {
            String finalLeftVal = getValue(checkItemLogic.getLeftField(), leftValues[i]);
            if (StringUtil.isEmpty(finalLeftVal)) {
                return XResult.failure(checkItemLogic.getLeftField() + "不能为空");
            }
            String finalRightVal = getValue(checkItemLogic.getRightField(), rightValues[i]);
            if (StringUtil.isEmpty(finalRightVal)) {
                return XResult.failure(checkItemLogic.getRightField() + "不能为空");
            }

            // 比较公式
            String formula =  finalLeftVal + checkItemLogic.getComparisonOperator() + finalRightVal;
            try {
                boolean valid =  (Boolean) JS_ENGINE.eval(formula);
                if (valid) {
                    continue;
                } else {
                    String message = xHelper.getAlias(checkItemLogic.getLeftField(), xBean.getFieldAlias())
                            + "必须" + xHelper.getPropertity(checkItemLogic.getComparisonOperator())
                            + xHelper.getAlias(checkItemLogic.getRightField(), xBean.getFieldAlias());
                    return XResult.failure(message);
                }
            } catch (ScriptException e) {
                throw new ExpressionDefineException("比较表达式有误 公式：" + formula, e);
            }
        }
        return XResult.success();
    }

//    public String buildLogicErrorMessage(XBean xBean, XCheckItemLogic xCheckItem) {
//        String left = getFiledAlias(xCheckItem.getLeftField(), xBean.getFieldAlias());
//        String right = getFiledAlias(xCheckItem.getRightField(), xBean.getFieldAlias());
//        return left + getProperty(xCheckItem.getComparisonOperator()) + right;
//    }
}
