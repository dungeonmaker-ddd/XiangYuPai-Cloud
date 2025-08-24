package com.xypai.gateway.config;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

import java.util.Random;

/**
 * 验证码文本生成器 - 数学运算
 *
 * 生成简单的数学运算表达式作为验证码
 * 支持加法、减法、乘法运算
 *
 * @author xypai
 */
public class KaptchaTextCreator extends DefaultTextCreator {

    private static final String[] NUMS = "0,1,2,3,4,5,6,7,8,9,10".split(",");
    private static final String[] OPERATORS = {"+", "-", "*"};
    
    @Override
    public String getText() {
        Random random = new Random();

        int x = random.nextInt(10);
        int y = random.nextInt(10);
        String operator = OPERATORS[random.nextInt(OPERATORS.length)];

        int result;
        String expression;

        switch (operator) {
            case "+":
                result = x + y;
                expression = x + "+" + y + "=?";
                break;
            case "-":
                // 确保结果为正数
                if (x < y) {
                    int temp = x;
                    x = y;
                    y = temp;
                }
                result = x - y;
                expression = x + "-" + y + "=?";
                break;
            case "*":
                // 使用较小的数字进行乘法，避免结果过大
                x = random.nextInt(6) + 1; // 1-6
                y = random.nextInt(6) + 1; // 1-6
                result = x * y;
                expression = x + "×" + y + "=?";
                break;
            default:
                result = x + y;
                expression = x + "+" + y + "=?";
        }

        // 返回格式: 表达式@结果
        return expression + "@" + result;
    }
}