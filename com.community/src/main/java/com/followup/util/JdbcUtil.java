package com.followup.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JdbcUtil {

    @Autowired
    private DataSource dataSource;

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, params);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("JDBC更新操作异常，SQL：{}", sql, e);
            throw new RuntimeException("数据库操作异常", e);
        }
    }

    public Long insertAndGetId(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(pstmt, params);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        } catch (SQLException e) {
            log.error("JDBC新增操作异常，SQL：{}", sql, e);
            throw new RuntimeException("数据库操作异常", e);
        }
    }

    public <T> T queryOne(Class<T> clazz, String sql, Object... params) {
        List<T> list = queryList(clazz, sql, params);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T> List<T> queryList(Class<T> clazz, String sql, Object... params) {
        List<T> resultList = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    T entity = clazz.newInstance();
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        // 核心修复1：跳过静态字段、final字段、以及serialVersionUID
                        if (Modifier.isStatic(field.getModifiers())
                                || Modifier.isFinal(field.getModifiers())
                                || "serialVersionUID".equals(field.getName())) {
                            continue;
                        }

                        field.setAccessible(true);
                        String fieldName = camelToUnderline(field.getName());

                        // 核心修复2：先判断结果集里是否有这个列，没有就跳过，避免报错
                        if (!hasColumn(rs, fieldName)) {
                            continue;
                        }

                        Object value = getResultSetValue(rs, fieldName, field.getType());
                        field.set(entity, value);
                    }
                    resultList.add(entity);
                }
            }
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            log.error("JDBC查询操作异常，SQL：{}", sql, e);
            throw new RuntimeException("数据库查询异常", e);
        }
        return resultList;
    }

    public Long queryCount(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            log.error("JDBC计数查询异常，SQL：{}", sql, e);
            throw new RuntimeException("数据库查询异常", e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    private String camelToUnderline(String camelName) {
        if (camelName == null || camelName.isEmpty()) {
            return camelName;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelName.length(); i++) {
            char c = camelName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 新增辅助方法：判断结果集里是否包含指定列名
     */
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if (columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    private Object getResultSetValue(ResultSet rs, String columnName, Class<?> fieldType) throws SQLException {
        if (fieldType == String.class) {
            return rs.getString(columnName);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return rs.getInt(columnName);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return rs.getLong(columnName);
        } else if (fieldType == LocalDateTime.class) {
            Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp == null ? null : timestamp.toLocalDateTime();
        } else if (fieldType == LocalDate.class) {
            Date date = rs.getDate(columnName);
            return date == null ? null : date.toLocalDate();
        } else {
            return rs.getObject(columnName);
        }
    }
}