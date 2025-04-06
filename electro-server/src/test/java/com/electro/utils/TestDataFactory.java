package com.electro.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.Collection;

public class TestDataFactory {
    // ObjectMapper to convert objects to JSON and vice versa
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static ObjectMapper objectMapperLogger() {
        return objectMapper
                .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static <T> T createExpectedData(String classPathResource, String rootName, Class<T> targetClass) {
        try {
            var jsonFile = new ClassPathResource(classPathResource).getFile();
            var root = objectMapper.readTree(jsonFile);
            return objectMapper.treeToValue(root.get(rootName), targetClass);
        } catch (IOException e) {
            throw new RuntimeException("Error loading test data", e);
        }
    }

    public static Authentication createMockAuthentication(String username) {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) {
            }

            @Override
            public String getName() {
                return username;
            }
        };
    }
}