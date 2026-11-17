package com.uxinterfacely.experienceletter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds the DataSource and tolerates a URL pasted without the "jdbc:" prefix,
 * e.g. Aiven's Service URI:  mysql://user:pass@host:port/db?ssl-mode=REQUIRED
 * (that raw form caused: "Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl").
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String rawUrl,
            @Value("${spring.datasource.username:}") String username,
            @Value("${spring.datasource.password:}") String password) {

        String url = rawUrl == null ? "" : rawUrl.trim();
        String user = username;
        String pass = password;

        if (url.startsWith("jdbc:")) {
            // already fine
        } else if (url.startsWith("mysql://")) {
            String rest = url.substring("mysql://".length());
            int at = rest.lastIndexOf('@');
            if (at >= 0) {
                String userInfo = rest.substring(0, at);
                rest = rest.substring(at + 1);
                int colon = userInfo.indexOf(':');
                String u = colon >= 0 ? userInfo.substring(0, colon) : userInfo;
                String p = colon >= 0 ? userInfo.substring(colon + 1) : "";
                if (user == null || user.isBlank() || "root".equals(user)) user = decode(u);
                if (pass == null || pass.isBlank()) pass = decode(p);
            }
            url = "jdbc:mysql://" + rest;
        } else {
            throw new IllegalStateException(
                    "Invalid database URL. It must start with 'jdbc:mysql://' (or 'mysql://').");
        }

        // Connector/J property name is sslMode (Aiven's URI uses ssl-mode)
        url = url.replace("ssl-mode=", "sslMode=");

        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(30000);
        cfg.setPoolName("HikariPool-1");
        return new HikariDataSource(cfg);
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
