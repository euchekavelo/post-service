package ru.tw1.euchekavelo.postservice.service;

public interface EntityAccessCheckService<T> {

    void checkEntityAccess(T entity);
}
