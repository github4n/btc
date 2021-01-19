package com.example.btc.services.http.mocha;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Service
public class mocha {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public float getMcPrice(URL url) throws IOException {
        long startTime=System.currentTimeMillis();
        float price=0;
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept-Encoding","gzip, deflate");
        urlConnection.setRequestProperty("Content-type","application/x-www-form-urlencoded");
        InputStream in = urlConnection.getInputStream();
        GZIPInputStream gZipS=new GZIPInputStream(in);
        InputStreamReader res = new InputStreamReader(gZipS,"GBK");
        BufferedReader reader=new BufferedReader(res);
        String line;
        List<String> charinfo=new ArrayList<String>();
        while ((line = reader.readLine()) != null) {

            charinfo.add(line);
        }
        //System.out.println(charinfo.toString());
        long endTime=System.currentTimeMillis();
        logger.info("抹茶数据加载完成用时{}----------->",(endTime-startTime)+"ms");
        //第一组数据为最新交易数据
        JSONObject js = JSON.parseObject(charinfo.get(0));
        String data=js.getString("data");
        data=data.replaceAll("},","}#");
        List<String>datalist= Arrays.asList(data.split("#"));
        //此处头有一个[
        JSONObject datajs=JSONObject.parseObject(datalist.get(0).substring(1));
        price=datajs.getFloat("trade_price");
        return price;
    }
}