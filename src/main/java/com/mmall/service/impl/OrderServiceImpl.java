package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeWithHBServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderItemMapper;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.PayInfoMapper;
import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.PayInfo;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @Description: 订单服务
 * @author: wuleshen
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    // 将AlipayTradeService设置为静态成员变量，不用反复new
    private static AlipayTradeServiceImpl tradeService;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    // 用于存放外部订单号和生成的二维码url
    private Map<String, String> resultMap = new HashMap<>();

    /**
     * 支付
     * @param orderNo 订单号
     * @param userId 用户id
     * @param path 二维码上传路径 String path = request.getSession().getServletContext().getRealPath("upload");
     * @return
     */
    @Override
    public ServerResponse pay(Long orderNo, Integer userId, String path) {

        //查询用户订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        logger.debug("order: {}", order);

        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));


        return trade_precreate(order, path);

    }

    /**
     * 回调校验商户数据的正确性
     * @param params 回调参数
     * @return
     */
    @Override
    public ServerResponse alipayCallback(Map<String, String> params) {

        // 支付宝外部订单号，也就是商城订单号
        Long orderNo = Long.valueOf(params.get("out_trade_no"));
        logger.debug("out_trade_no: {}", orderNo.toString());
        // 支付宝交易号
        String tradeNo = params.get("trade_no");
        // 交易状态
        String tradeStatus = params.get("trade_status");
        logger.debug("trade_status: {}", tradeStatus);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("不是该商城订单，忽略回调");
        }
        // 判断交易状态
        // 交易状态为已付款，已发货，订单完成，订单关闭时，为重复调用
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        // 交易成功，记录付款时间，更新订单状态为已付款
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();

    }

    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("查询错误，订单不存在");
        }
        // 查询支付状态
        // 只要是已付款之后的状态，都认为是付款成功
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


    /**
     * 支付宝生成二维码代码, 修改自com.alipay.demo.trade.Main支付宝官方demo
     * @param order 用户订单
     * @param path 二维码上传路径
     */
    private ServerResponse trade_precreate(Order order, String path) {
        // 1. 设置支付相关参数
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = String.valueOf(order.getOrderNo());

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("mmall扫码支付,订单号: ").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        // 获取用户订单详情
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(),order.getUserId());
        logger.debug("orderItemList: {}" , orderItemList);
        for (OrderItem orderItem : orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100)).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 2. 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        // 3. 初始化tradeService
        tradeService = init_tradeService();

        // 4. 当面付预下单
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        // 5. 根据下单返回结果进行后续处理
        switch (result.getTradeStatus()) {

            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                // 5.1 预下单成功，生成二维码到指定路径
                trade_precreate_success(result, path);
                logger.debug("resultMap: {}", resultMap);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }

    /**
     * 预下单成功，生成二维码到本地
     * @param result
     * @param path
     */
    private void trade_precreate_success(AlipayF2FPrecreateResult result, String path) {
        // 简单打印应答
        AlipayTradePrecreateResponse response = result.getResponse();
        dumpResponse(response);

        // 本地生成二维码存放文件夹
        File folder = new File(path);
        if (!folder.exists()) {
            folder.setWritable(true);
            folder.mkdirs();
        }
        /*
            生成二维码流程
            1. 根据外部订单号（outTradeNo）生成二维码存放路径（qrPath）
            2. 生成二维码到qrPath
         */
        String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
        String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());

        // 生成二维码图片到本地指定路径
        File imageFile = ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
        try {
            // 将本地路径的二维码图片上传到ftp服务器
            FTPUtil.uploadFile(Lists.newArrayList(imageFile));
        } catch (IOException e) {
            logger.error("上传二维码异常", e);
        }

        logger.info("qrPath:" + qrPath);
        // 生成二维码的ftp Url
        String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + imageFile.getName();
        resultMap.put("qrUrl", qrUrl);
    }

    private AlipayTradeServiceImpl init_tradeService() {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        return tradeService;
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

}
