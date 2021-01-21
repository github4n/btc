package com.example.btc.services.ws.hb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.btc.services.ws.handler.WssMarketReqHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
@PropertySource("classpath:url.properties")
@Service
public class Hbprice {
    @Value("${hburl}")
    private String hburl;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public float getHbprice(String reqparam) throws MalformedURLException, URISyntaxException, InterruptedException {
       long startTime=System.currentTimeMillis();
         AtomicReference<Float> price = new AtomicReference<>((float) 0);
        //reqparam="market.btcusdt.trade.detail";
        WssMarketReqHandle wssMarketReqHandle = new WssMarketReqHandle(hburl, response -> {
            //logger.info("火币接收原始数据{}",response);
            long endTime=System.currentTimeMillis();
            logger.info("火币数据加载完成,用时{}",(endTime-startTime)+"ms");
            JSONObject jsall=JSON.parseObject(response);
            String temp=jsall.get("data").toString();
            temp=temp.replaceAll("},","}#");
            List<String> listdata=new ArrayList<String>();

            listdata= Arrays.asList(temp.substring(1, temp.length() - 1).split("#"));
            JSONObject jsdata=JSONObject.parseObject( listdata.get(0));
             price.set(jsdata.getFloatValue("price"));
            //logger.info(String.valueOf(price.get()));

            // logger.info("请求 KLine 数据用户收到的原始数据:{}", response);
            /// MarketKLineReqResponse marketKLineReqResponse = JSON.parseObject(response, MarketKLineReqResponse.class);
            // logger.info("请求 KLine 数据解析之后的数据为:{}", JSON.toJSON(marketKLineReqResponse));
        });
        Map<String, Object> param = new HashMap<>();
        param.put("req", reqparam);
        wssMarketReqHandle.doReq(JSON.toJSONString(param));
        Thread.sleep(500);
        return price.get();
    }
}
