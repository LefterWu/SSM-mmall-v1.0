package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 订单控制器
 * @author: wuleshen
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    /**
     * 支付方法
     * @param session
     * @param orderNo
     * @param request
     * @return 带有map的响应，里面存放了外部订单号和二维码url
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(orderNo, user.getId(), path);
    }

    /**
     * 支付宝回调方法
     * @param request
     * @return
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        logger.debug("**********************支付宝回调开始**********************");
        // 得到并遍历回调传来的参数
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        // 将前台的参数转换为"xxx"->"aaa,bbb,ccc"的格式存入params中
        for (Map.Entry<String, String[]> entry: requestParams.entrySet()) {
            String name = entry.getKey();
            String values[] = entry.getValue();
            StringBuilder valStr = new StringBuilder();
            for (int i = 0; i < values.length; i ++) {
                if ( i != values.length - 1) {
                    valStr.append(values[i]).append(",");
                } else {
                    valStr.append(values[i]);
                }
            }
            params.put(name, valStr.toString());
        }
        // 日志打印回调信息，包括签名，支付状态，所有参数
        logger.info("alipay_callback, sign:{}, trade_status:{}, params:{}", params.get("sign"), params.get("trade_status"), params.toString());

        // 重要！ 异步返回结果的验签，验证回调是否为支付宝发的，并且避免重复通知

        // 在通知返回参数列表中，需要除去sign、sign_type两个参数，而sign已经在#rsaCheckV2方法中除去了
        params.remove("sign_type");
        try {
            // 使用RSA的验签方法，通过签名字符串、签名参数（经过base64解码）及支付宝公钥验证签名
            boolean rsaCheckV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            // 验签失败
            if ( !rsaCheckV2 ) {
                return ServerResponse.createByErrorMessage("验签失败，检测到非法请求");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常");
        }

        /*
        商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，并判断total_amount是否确实为该订单的实际金额（即商户
        订单创建时的金额），同时需要校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，
        一个商户可能有多个seller_id/seller_email），上述有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
        在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
        在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
         */
        ServerResponse serverResponse = iOrderService.alipayCallback(params);
        if (serverResponse.isSuccuess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }

        return Const.AlipayCallback.RESPONSE_FAILED;

    }

    /**
     * 查询订单支付状态
     * @param session
     * @param orderNo
     * @return 付款成功，返回响应信息为true；付款失败，返回响应信息为false
     */
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (serverResponse.isSuccuess()) {
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }

}
