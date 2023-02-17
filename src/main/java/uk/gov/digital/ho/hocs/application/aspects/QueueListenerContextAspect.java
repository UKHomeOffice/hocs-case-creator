package uk.gov.digital.ho.hocs.application.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.ClientContext;

@Component
@Aspect
public class QueueListenerContextAspect {

    private final ClientContext clientContext;
    private final String user;
    private final String group;
    private final String team;

    public QueueListenerContextAspect(ClientContext clientContext,
                               @Value("${case.creator.identity.user}") String user,
                               @Value("${case.creator.identity.group}") String group,
                               @Value("${case.creator.identity.team}") String team) {
        this.clientContext = clientContext;
        this.user = user;
        this.group = group;
        this.team = team;
    }

    @Pointcut("execution(* uk.gov.digital.ho.hocs.queue.common.QueueListener.onMessageReceived(..))")
    public void messageReceivedPointcut(){
    }

    @Around("messageReceivedPointcut()")
    public void aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        var getArguments = joinPoint.getArgs();
        var messageId = (String) getArguments[1];
        clientContext.setContext(user, group, team, messageId);
        joinPoint.proceed();
    }

}
