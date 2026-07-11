package ru.netology.cloudservice.security;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Простая чёрная книга токенов: при logout добавляем токен сюда,
 * фильтр потом отвергает заблокированные токены.
 * Для production логичнее хранить blacklist в Redis с TTL = время жизни токена.
 */
@Component
public class TokenBlacklist {

    private final Set<String> revoked = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            revoked.add(token);
        }
    }

    public boolean isRevoked(String token) {
        return revoked.contains(token);
    }
}
