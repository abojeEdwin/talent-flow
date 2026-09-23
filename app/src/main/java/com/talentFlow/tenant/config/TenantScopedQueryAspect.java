package com.talentFlow.tenant.config;

import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Replaces the former Hibernate {@code @Filter(tenantFilter)} for MongoDB.
 * Every query executed through the tenant-scoped {@code MongoTemplate} picks up
 * the current organization automatically when a tenant context is present.
 * Super-admin flows (no tenant context) remain globally scoped, matching the
 * previous behaviour.
 */
@Aspect
@Component
public class TenantScopedQueryAspect {

    @Pointcut("execution(* org.springframework.data.mongodb.core.MongoTemplate.find(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.findOne(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.findAll(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.aggregate(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.count(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.exists(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.remove(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.delete(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.findAndRemove(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.findAndModify(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.updateFirst(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.updateMulti(..)) || "
            + "execution(* org.springframework.data.mongodb.core.MongoTemplate.upsert(..))")
    public void tenantQueryExecution() {
    }

    @Around("tenantQueryExecution()")
    public Object scopeQueryByTenant(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Query query = null;
        Class<?> entityType = null;
        for (Object arg : args) {
            if (arg instanceof Query candidateQuery && query == null) {
                query = candidateQuery;
            } else if (arg instanceof Class<?> candidateType) {
                entityType = candidateType;
            }
        }

        if (query != null && entityType != null && TenantAware.class.isAssignableFrom(entityType)) {
            UUID tenantId = TenantContextHolder.getTenantId();
            if (tenantId != null) {
                query.addCriteria(Criteria.where("organization.$id").is(tenantId));
            }
        }
        return joinPoint.proceed(args);
    }
}