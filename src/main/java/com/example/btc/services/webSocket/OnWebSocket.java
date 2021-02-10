package com.example.btc.services.webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.btc.baseDao.UrlPara;
import com.example.btc.services.CustomMultiThreadingService.CustomMultiThreadingService;
import com.example.btc.services.http.bian.biAn;
import com.example.btc.services.http.bter.bter;
import com.example.btc.services.http.mocha.mocha;
import com.example.btc.services.http.ok.OkPrice;
import com.example.btc.services.ws.handler.*;
import com.example.btc.services.ws.hb.Hbprice;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unbescape.css.CssIdentifierEscapeLevel;

import javax.annotation.PostConstruct;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiringInspection"})
@Slf4j
@Service
@ServerEndpoint("/test/{name}")//("/websocket/{name}")
public class OnWebSocket {
    private static Hbprice hb;
    private  static UrlPara urlPara;
    private  static  OkPrice okPrice;
    private  static bter mbter;
    private static mocha mmocha;
    private static List<String> reqparams=new ArrayList<>();
    @Autowired
    public void setRepository(Hbprice hb) {
        OnWebSocket.hb = hb;
    }
    @Autowired
    public void setUrlPara(UrlPara urlPara){OnWebSocket.urlPara=urlPara;
        reqparams=urlPara.getHbpara();
    }
    @Autowired
    public  void  setOkPrice(OkPrice okPrice){OnWebSocket.okPrice=okPrice;};
    @Autowired
    public  void  setBter(bter mbter){OnWebSocket.mbter=mbter;}
    @Autowired
    public void setMocha(mocha mmocha){OnWebSocket.mmocha=mmocha;}

    private Logger logger = LoggerFactory.getLogger(OnWebSocket.class);
    String hburl="wss://api.huobiasia.vip/ws";
    private  String okurl="wss://real.coinall.ltd:8443/ws/v3";
    private  String bturl="wss://ws.gateio.ws/v3/";//"wss://webws.gateio.live/v3/?v=647320";//wss://ws.gateio.ws/v3/
    private  String bnurl="wss://stream.yshyqxx.com/stream";
    private  String mcurl="wss://contract.mxc.me/ws";
    private  String zburl="wss://api.zb.center/websocket/";
    private  String bsurl="wss://api.aex.zone/wsv3";
    //库币前端socket地址
    private  String kburl="wss://push-socketio.kucoin.cc/socket.io/?token=2neAiuYvAU5cbMXpmsXD5OJlewXCKryg8dSpDCgag8ZwbZpn3uIHi6siD_s132wYwoXOiOG0Q0GtkO7lJxgkJLlUcTB0YEwtDLb3Wn3QP70Gg-Y4I5btzKqRyqznCVt1whuoNZhpWWEsP1jUbPa-MVk_m4uS-u53.UQNVzzBGHR3T5IjiT0lrWA%3D%3D&format=json&acceptUserMessage=false&connectId=connect_welcome&EIO=3&transport=websocket";

   // List<String> reqparams=urlPara.getHbpara();
    /**
     *  与某个客户端的连接对话，需要通过它来给客户端发送消息
     */
    private Session session;

    /**
     * 标识当前连接客户端的用户名
     */
    public String name;

    /**
     *  用于存所有的连接服务的客户端，这个对象存储是安全的
     */
    private static ConcurrentHashMap<String, OnWebSocket> webSocketSet = new ConcurrentHashMap<>();


    @OnOpen
    public void OnOpen(Session session, @PathParam(value = "name") String name) throws InterruptedException, URISyntaxException, MalformedURLException {
        this.session = session;
        UUID uuid = UUID.randomUUID();
        this.name = name+uuid;
        // name是用来表示唯一客户端，如果需要指定发送，需要指定发送通过name来区分
        webSocketSet.put(this.name,this);
        log.info("[WebSocket] 连接成功，当前连接人数为：={}",webSocketSet.size());
        String type=name.substring(0,2);
        socketdo(type);
    }

    void socketdo(String type)  {
        try {
            switch (type) {
                case "hb":
                    WssMarketHandle wssMarketHandle = new WssMarketHandle(hburl);
                    wssMarketHandle.sub(reqparams, response -> {
                        if(this.session.isOpen()) {
                            AppointSending(name, response.toString());
                        }
                    });
                    Thread.sleep(60000);
                    break;
                case "ok":
                    OkWssMarketHandle okwssMarketHandle = new OkWssMarketHandle(okurl);
                    okwssMarketHandle.sub(reqparams, response -> {
                        if(this.session.isOpen()) {
                            AppointSending(name, response.toString());
                        }
                    });
                    Thread.sleep(60000);
                    break;
                case "bt":
                        BtWssMarketHandle btWssMarketHandle=new BtWssMarketHandle(bturl);
                        btWssMarketHandle.sub(reqparams,response ->
                        {
                            if(this.session.isOpen()) {
                                AppointSending(name, response.toString());
                            }
                        });
                        Thread.sleep(60000);
                    break;
                case "bn":
                    BnWssMarketHandle bnWssMarketHandle=new BnWssMarketHandle(bnurl);
                    bnWssMarketHandle.sub(reqparams,response ->{
                        if(this.session.isOpen()) {
                            AppointSending(name, response.toString());
                        }
                    });
                    Thread.sleep(60000);
                    break;
                case "mc":
                    McWssMarketHandle mcWssMarketHandle=new McWssMarketHandle(mcurl);
                    mcWssMarketHandle.sub(reqparams,response->{
                        //logger.info(response.toString());
                        if(this.session.isOpen()) {
                            AppointSending(name, response.toString());
                        }
                    });
                    Thread.sleep(60000);
                    break;
                case "zb":
                    ZbWssMarketHandle zbWssMarketHandle=new ZbWssMarketHandle(zburl);
                    zbWssMarketHandle.sub(reqparams,response->{
                        //logger.info(response.toString());
                        if(this.session.isOpen()) {
                            AppointSending(name, response.toString());
                        }
                    });
                    Thread.sleep(60000);
                case "bs":
                    BsWssMarketHandle bsWssMarketHandle=new BsWssMarketHandle(bsurl);
                    bsWssMarketHandle.sub(reqparams,response->{
                        if(this.session.isOpen()) {
                            AppointSending(name, response.toString());
                        }
                    });
                    break;
                case "kb":
                    KbWssMarketHandle kbWssMarketHandle=new KbWssMarketHandle(kburl);
                    kbWssMarketHandle.sub(reqparams,response->{
                        if(this.session.isOpen()) {
                            AppointSending(name, response.toString());
                        }
                    });
                    break;
                default:
                    break;
            }
        }
        catch (InterruptedException| URISyntaxException e)
        {
            logger.info("尝试给前端发送消息失败！！！{}",e);
        }
    }
    @OnClose
    public void OnClose() throws InterruptedException, URISyntaxException {
        webSocketSet.remove(this.name);
        String type=name.substring(0,2);
        switch (type)
        {
            case"hb":
                //wssMarketHandle.close();
                break;
            case "ok":
                //OkwssMarketHandle.close();
                break;

        }

        log.info("[WebSocket] 退出成功，当前连接人数为：={}",webSocketSet.size());
    }

    @OnMessage
    public void OnMessage(String message) throws InterruptedException {
        log.info("[WebSocket] 收到消息：{}",message);
      //接收心跳
        String type=name.substring(0,2);
        switch (type)
        {
            case"hb":
                AppointSending(name,"pong");
                break;
            case"ok":
                AppointSending(name,"pong");
                break;
            case"bt":
                AppointSending(name,"pong");
                break;
        }


       // String channeltype=name.substring(0,2);
        //JSONObject jspara=JSONObject.parseObject(message);
        //判断是否需要指定发送，具体规则自定义

//        if(message.indexOf("TOUSER") == 0){
//            String name = message.substring(message.indexOf("TOUSER")+6,message.indexOf(";"));
//            AppointSending(name,message.substring(message.indexOf(";")+1,message.length()));
//        }else{
//            //GroupSending(message);
//            GroupSending(String.valueOf(i));
//        }


    }

    /**
     * 群发
     * @param message
     */
    public void GroupSending(String message){
        for (String name : webSocketSet.keySet()){
            try {
                webSocketSet.get(name).session.getBasicRemote().sendText(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定发送
     * @param name
     * @param message
     */
    public void AppointSending(String name,String message) throws InterruptedException {
        // if (this.session.isOpen())
        synchronized(this.session) {
            try {
                //Thread.sleep(1000);
                webSocketSet.get(name).session.getBasicRemote().sendText(message);
            } catch (IllegalStateException | IOException e) {
                // e.printStackTrace();
                logger.info("发送报错");
            }
        }


        // webSocketSet.get(name).session.getAsyncRemote().sendText(message);
    }
}