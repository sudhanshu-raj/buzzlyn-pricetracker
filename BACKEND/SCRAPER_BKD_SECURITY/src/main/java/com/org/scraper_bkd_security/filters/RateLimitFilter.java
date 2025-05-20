//package com.org.scraper_bkd_security.filters;
//
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.BucketConfiguration;
//import io.github.bucket4j.ConsumptionProbe;
//import io.github.bucket4j.distributed.proxy.ProxyManager;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Supplier;
//
//@Component
//@Order(1)
//public class RateLimitFilter implements Filter {
//
//    private final Logger LOG = LoggerFactory.getLogger(getClass());
//
//    @Autowired
//    Supplier<BucketConfiguration> bucketConfiguration;
//
//    @Autowired
//    ProxyManager<String> proxyManager;
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, ServletException, IOException {
//        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
//        String key = httpRequest.getRemoteAddr();
//        Bucket bucket = proxyManager.builder().build(key, bucketConfiguration);
//
//        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
//        LOG.debug(">>>>>>>>remainingTokens: {}", probe.getRemainingTokens());
//        if (probe.isConsumed()) {
//            filterChain.doFilter(servletRequest, servletResponse);
//        } else {
//            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
//            httpResponse.setContentType("text/plain");
//            httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
//            httpResponse.setStatus(429);
//            httpResponse.getWriter().append("Too many requests");
//        }
//    }
//
//}
