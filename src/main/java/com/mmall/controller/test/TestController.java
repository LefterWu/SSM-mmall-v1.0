package com.mmall.controller.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description: 测试内网穿透
 * @author: wuleshen
 */
@Controller
@RequestMapping("/test/")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @RequestMapping("test.do")
    public String test(String str) {
        logger.info("test info");
        logger.debug("test debug");
        logger.warn("test warn");
        return "test value: " + str;
    }
}
