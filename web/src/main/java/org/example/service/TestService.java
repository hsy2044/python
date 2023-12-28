package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TestService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void testConnection() {
        try {
            String sql = "SELECT TOP 1 * FROM TEST"; // 替换为您的表名
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            System.out.println("查询结果: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
