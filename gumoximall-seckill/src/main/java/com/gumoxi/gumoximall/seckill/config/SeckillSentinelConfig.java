package com.gumoxi.gumoximall.seckill.config;

import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@Configuration
public class SeckillSentinelConfig {

//    public SeckillSentinelConfig(){
//        WebFluxCallbackManager.setBlockHandler(new BlockRequestHandler() {
//            @Override
//            public Mono<org.springframework.web.reactive.function.server.ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
//                return new Mono<ServerResponse>() {
//                    @Override
//                    public void subscribe(CoreSubscriber<? super ServerResponse> coreSubscriber) {
//                    }
//                };
//            }
//        });
//    }
}
