package com.example.pitchboxd.support;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;

    // String 리스트 대신 TableInfo 리스트를 상태로 가집니다.
    private List<TableInfo> tableInfos;

    @Override
    public void afterPropertiesSet() {
        tableInfos = entityManager.getMetamodel().getEntities().stream()
                .map(this::createTableInfo)
                .collect(Collectors.toList());
    }

    private TableInfo createTableInfo(EntityType<?> entity) {
        String tableName = getTableName(entity);
        String pkColumnName = getPkColumnName(entity);
        return new TableInfo(tableName, pkColumnName);
    }

    private String getTableName(EntityType<?> entity) {
        Table tableAnnotation = entity.getJavaType().getAnnotation(Table.class);
        if (tableAnnotation != null && tableAnnotation.name() != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        // 기본 테이블명 반환 (Spring Naming Strategy에 따라 캐멀케이스를 스네이크케이스로 변환하는 로직이 추가로 필요할 수 있습니다)
        return convertCamelToSnake(entity.getName());
    }

    private String getPkColumnName(EntityType<?> entity) {
        // 1. 엔티티의 식별자(ID) 속성을 가져옵니다.
        SingularAttribute<?, ?> idAttribute = entity.getId(entity.getIdType().getJavaType());

        // 2. 리플렉션을 통해 실제 Java 필드(Member)에 접근합니다.
        Member member = idAttribute.getJavaMember();
        if (member instanceof Field field) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            // 3. @Column(name="...")이 명시되어 있다면 그 값을 최우선으로 사용합니다.
            if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
                return columnAnnotation.name();
            }
            // 4. 명시된 어노테이션이 없다면 필드명을 스네이크케이스로 변환하여 반환합니다. (예: matchId -> match_id)
            return convertCamelToSnake(field.getName());
        }

        return "ID"; // 최후의 Fallback
    }

    // 엔티티명/필드명(CamelCase)을 DB 컬럼명(snake_case)으로 변환하는 유틸리티 메서드
    private String convertCamelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    @Transactional
    public void clean() {
        entityManager.clear();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        for (TableInfo table : tableInfos) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + table.name()).executeUpdate();
            // 동적으로 추출한 PK 컬럼명을 사용하여 시퀀스를 초기화합니다.
            entityManager.createNativeQuery(
                    "ALTER TABLE " + table.name() + " ALTER COLUMN " + table.pkColumn() + " RESTART WITH 1"
            ).executeUpdate();
        }

        truncateInfrastructureTableSafely("SPRING_SESSION_ATTRIBUTES");
        truncateInfrastructureTableSafely("SPRING_SESSION");

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    // 새롭게 추가한 방어적 삭제 로직
    private void truncateInfrastructureTableSafely(String tableName) {
        // H2, MySQL 등의 표준 시스템 뷰인 INFORMATION_SCHEMA를 조회하여 테이블 존재 여부 확인
        Number count = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = :tableName"
        ).setParameter("tableName", tableName.toUpperCase()).getSingleResult();

        if (count != null && count.intValue() > 0) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
    }

    private record TableInfo(String name, String pkColumn) {
    }
}
