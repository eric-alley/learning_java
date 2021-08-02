package io.kimmking.cache.pushsub;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xjm
 * @version 1.0
 * @date 2021-07-20 15:51
 */
@Service
public class PushServiceImpl implements PushService {
    @Autowired
    private RedisTemplate redisTemplate;



    @Override
    public void pushMsgToOne(String userId, String msg){
        ConcurrentHashMap<String, Channel> userChannelMap = NettyConfig.getUserChannelMap();
        Channel channel = userChannelMap.get(userId);
        if(!Objects.isNull(channel)){
            // 如果该用户的客户端是与本服务器建立的channel,直接推送消息
            channel.writeAndFlush(new TextWebSocketFrame(msg));
        }else {
            // 发布，给其他服务器消费
            NettyPushMessageBody pushMessageBody = new NettyPushMessageBody();
            pushMessageBody.setUserId(userId);
            pushMessageBody.setMessage(msg);
            redisTemplate.convertAndSend(BaseConstant.PUSH_MESSAGE_TO_ONE,pushMessageBody);
        }

    }
    @Override
    public void pushMsgToAll(String msg){
        // 发布，给其他服务器消费
        redisTemplate.convertAndSend(BaseConstant.PUSH_MESSAGE_TO_ALL,msg);
//        NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame(msg));
    }
}