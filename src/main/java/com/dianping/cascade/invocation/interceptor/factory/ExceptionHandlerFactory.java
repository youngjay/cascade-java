package com.dianping.cascade.invocation.interceptor.factory;

import com.dianping.cascade.*;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/3/16.
 */
public class ExceptionHandlerFactory implements InvocationInterceptorFactory {
    public static String CASCADE_ERROR = "[Cascade Error] ";


    @Override
    public InvocationInterceptor create(Method method, Object target, MethodParametersResolver methodParametersResolver) {
        return new InvocationInterceptor() {
            @Override
            public Object invoke(InvocationHandler invocationHandler, Field field, ContextParams contextParams) {
                try {
                    return invocationHandler.invoke(field, contextParams);
                } catch (Throwable ex) {
                    Throwable cause = getCause(ex);

                    if (cause instanceof BusinessException) {
                        return String.format(CASCADE_ERROR + cause.getMessage());
                    }

                    String msg = cause.getMessage();

                    if (msg == null) {
                        msg = cause.getClass().getSimpleName();
                    }

                    return String.format(CASCADE_ERROR + "[%s.%s] %s", field.getType(), field.getCategory(), msg);
                }
            }

            private Throwable getCause(Throwable outer) {
                Throwable inner = outer.getCause();
                return inner == null ? outer : getCause(inner);
            }
        };
    }
}
