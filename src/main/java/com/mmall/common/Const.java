package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description: 存放一些常量
 * @author: wuleshen
 */
public class Const {
    // 当前用户
    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    // 支付方式
    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付");

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        // 获得对应编码的描述
        public static PaymentTypeEnum codeOf(int code) {
            for (PaymentTypeEnum paymentType: values()) {
                if (paymentType.getCode() == code) {
                    return paymentType;
                }
            }
            throw new RuntimeException("未找到对应的支付状态");
        }
    }

    // 用户角色
    public interface Role {
        int ROLE_CUSTOMER = 0;      //普通用户
        int ROLE_ADMIN = 1;         //管理员
    }

    // 销售状态
    public interface SALE_STATUS {
        int ON_SALE = 1;    //在售
        int SOLD_OUT = 2;   //售完或下架
        int DELETE = 3;     //删除
    }

    // 排序依据
    public interface ORDER_BY {
        Set<String> PRICE_ORDER = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Cart {
        int CHECKED = 1;    //购物车选中状态
        int UN_CHECKED = 0; //购物车未选中状态
        String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";   //限制数量失败，即库存不够
        String SUFFICIENT_STOCK = "SUFFICIENT_STOCK"; //限制数量成功，即库存充足
    }

    // 交易状态
    public enum OrderStatusEnum{
        CANCELED(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已付款"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("未找到对应的交易状态");
        }
    }

    // 支付宝回调
    public interface  AlipayCallback {
        // 交易创建，等待买家付款
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        // 交易支付成功
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        // 回调成功
        String RESPONSE_SUCCESS = "success";
        // 回调失败
        String RESPONSE_FAILED = "failed";
    }

    // 支付平台
    public enum PayPlatformEnum{
        ALIPAY(1, "支付宝"),
        WECHATPAY(2, "微信");

        PayPlatformEnum(int code, String value){
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
