package uk.gov.digital.ho.hocs.application.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.MessageContext;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;

import java.time.LocalDateTime;

@Component
@Aspect
public class MessageHandlerContextAspect {

    private final MessageContext messageContext;

    private final MessageLogService messageLogService;

    public MessageHandlerContextAspect(MessageContext messageContext,
                                       MessageLogService messageLogService) {
        this.messageContext = messageContext;
        this.messageLogService = messageLogService;
    }

    @Pointcut("execution(* uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler.handleMessage(..))")
    public void messageReceivedPointcut(){
    }

    @Around("messageReceivedPointcut()")
    public void aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        var getArguments = joinPoint.getArgs();
        var message = (Message) getArguments[0];

        messageContext.initialiseContext(message.id());
        messageLogService.updateProcessedTime(message.id(), LocalDateTime.now());
        joinPoint.proceed();
        messageContext.clearContext();
    }

}
