package cn.huangkanglin.activiti5;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        Map<String, String> requestParamMap = new HashMap<>();

        //   requestParamMap.put("orderType",);
        String string = requestParamMap.get("orderType");
        // 本人代办
        if (string == null || string.equals("") || string.equals("1")) {
            System.out.println("11111111111");
        } else if (string.equals("2")) {
            System.out.println("222222222222");
        }

    }
}
