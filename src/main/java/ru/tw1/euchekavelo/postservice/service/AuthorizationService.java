package ru.tw1.euchekavelo.postservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tw1.euchekavelo.postservice.exception.ResourceAccessDeniedException;
import ru.tw1.euchekavelo.postservice.security.util.UserDetailsContextUtil;

import java.util.UUID;

import static ru.tw1.euchekavelo.postservice.exception.enums.ExceptionMessage.RESOURCE_ACCESS_DENIED_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final UserDetailsContextUtil userDetailsContextUtil;

    public void checkAccess(UUID resourceOwnerId) {
        if (!isResourceOwner(resourceOwnerId) && !hasAdminRole()) {
            log.error(RESOURCE_ACCESS_DENIED_EXCEPTION_MESSAGE.getExceptionMessage());
            throw new ResourceAccessDeniedException(RESOURCE_ACCESS_DENIED_EXCEPTION_MESSAGE.getExceptionMessage());
        }
    }

    private boolean isResourceOwner(UUID resourceOwnerId) {
        return userDetailsContextUtil.getUserId().equals(resourceOwnerId);
    }

    private boolean hasAdminRole() {
        return userDetailsContextUtil.getRoles().stream()
                .anyMatch(roleName -> roleName.equalsIgnoreCase("posts_admin"));
    }
}
