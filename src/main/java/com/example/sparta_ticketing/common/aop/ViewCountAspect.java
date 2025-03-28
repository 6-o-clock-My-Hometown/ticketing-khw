package com.example.sparta_ticketing.common.aop;

import com.example.sparta_ticketing.common.redis.ViewCountService;
import com.example.sparta_ticketing.domain.auth.entity.AuthUser;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ViewCountAspect {

    private final ViewCountService viewCountService;

    @Around("@annotation(com.example.sparta_ticketing.common.aop.ViewCount) && args(showId,authUser)")
    public Object countView(ProceedingJoinPoint joinPoint, Long showId, AuthUser authUser) throws Throwable {
        Object result = joinPoint.proceed();
        viewCountService.increaseViewCount(showId, authUser.getId());
        return result;
    }
}