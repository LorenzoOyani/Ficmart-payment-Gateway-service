package com.org.infrastructure.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;

import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Component
public class ChaosBankFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ChaosBankFilter.class);

    private final ObjectMapper objectMapper;



    @Override
    protected void doFilterInternal(@NonNull jakarta.servlet.http.HttpServletRequest request, @NonNull jakarta.servlet.http.HttpServletResponse response,@NonNull jakarta.servlet.FilterChain filterChain) throws IOException, ServletException {

//        MultiValueMap<String, String> v = new LinkedMultiValueMap<>();

        int delays = ThreadLocalRandom.current().nextInt(100, 2001);
        try{
            Thread.sleep(delays);
        }catch(InterruptedException e){
            logger.error("error: {}", e.getMessage());
        }

        /// simulate 5% randomness
        if(ThreadLocalRandom.current().nextInt(100) < 5){
            response.setStatus(500);
            response.setHeader("Content-Type", "application/json");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "message",  "", "d"));

            return;
        }

        filterChain.doFilter(request, response);
    }

}
