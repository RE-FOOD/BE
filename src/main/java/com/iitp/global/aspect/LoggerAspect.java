package com.iitp.global.aspect;

import com.iitp.global.util.client.ClientIPAddressUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Slf4j
public class LoggerAspect {
    @Pointcut("execution(* com.iitp..*Controller.*(..)) || execution(* com.iitp..*GlobalExceptionHandler.*(..))")
    // 이런 패턴이 실행될 경우 수행
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object logRequest(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = requestAttributes.getRequest();
        String controllerName = proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();
        // 요청 IP,
        log.info("{}: {} {} PARAM={}",
                ClientIPAddressUtil.getClientIP(request),
                request.getMethod(),
                request.getRequestURI(),
                extractRequestParameters(request)
        );
        return result;
    }

    private static JSONObject extractRequestParameters(HttpServletRequest request) {   // request로부터 param 추출, JSONObject로 변환
        JSONObject jsonObject = new JSONObject();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            String replaceParam = param.replaceAll("\\.", "-");
            jsonObject.put(replaceParam, request.getParameter(param));
        }
        return jsonObject;
    }
}
