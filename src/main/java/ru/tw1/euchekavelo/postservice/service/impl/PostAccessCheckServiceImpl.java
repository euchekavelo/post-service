package ru.tw1.euchekavelo.postservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tw1.euchekavelo.postservice.model.Post;
import ru.tw1.euchekavelo.postservice.service.AuthorizationService;
import ru.tw1.euchekavelo.postservice.service.EntityAccessCheckService;

@Service
@RequiredArgsConstructor
public class PostAccessCheckServiceImpl implements EntityAccessCheckService<Post> {

    private final AuthorizationService authorizationService;

    @Override
    public void checkEntityAccess(Post entity) {
        authorizationService.checkAccess(entity.getUserId());
    }
}
