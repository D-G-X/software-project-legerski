package de.hft.licensing.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LicensingLoggerFactory {

    private static final Map<String, Logger> CACHE = new ConcurrentHashMap<>();

    private LicensingLoggerFactory() {}

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    private static Logger getLogger(String name) {
        return CACHE.computeIfAbsent(name, LicensingLoggerFactory::createProxy);
    }

    private static Logger createProxy(String name) {
        Logger delegate = LoggerFactory.getLogger(name);

        return (Logger) Proxy.newProxyInstance(
                LicensingLoggerFactory.class.getClassLoader(),
                new Class<?>[]{Logger.class},
                (proxy, method, args) -> {
                    if (args != null && args.length > 0) {
                        String m = method.getName();
                        String prefix = switch (m) {
                            case "info"  -> "[INFO] ";
                            case "warn"  -> "[WARN] ";
                            case "error" -> "[ERROR] ";
                            case "debug" -> "[DEBUG] ";
                            case "trace" -> "[TRACE] ";
                            default -> null;
                        };

                        if (prefix != null) {
                            int msgIdx = (args[0] instanceof Marker) ? 1 : 0;
                            if (msgIdx < args.length && args[msgIdx] instanceof String s) {
                                if (!s.startsWith(prefix)) {
                                    Object[] newArgs = args.clone();
                                    newArgs[msgIdx] = prefix + s;
                                    return method.invoke(delegate, newArgs);
                                }
                            }
                        }
                    }
                    return method.invoke(delegate, args);
                }
        );
    }
}