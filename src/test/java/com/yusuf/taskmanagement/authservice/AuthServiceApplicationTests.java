package com.yusuf.taskmanagement.authservice;

import com.mongodb.client.MongoClient;
import com.yusuf.taskmanagement.authservice.repository.UserRepository;
import com.yusuf.taskmanagement.authservice.security.JwtService;
import com.yusuf.taskmanagement.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
class AuthServiceApplicationTests {

    @Autowired
    private ApplicationContext context;

    // Isolate persistence while keeping the real application and security context.
    @MockitoBean(enforceOverride = true)
    private UserRepository userRepository;

    // Replacing the template prevents automatic index creation during startup.
    @MockitoBean(enforceOverride = true)
    private MongoTemplate mongoTemplate;

    // Boot's GridFS factory otherwise reads the converter from MongoTemplate.
    @MockitoBean(enforceOverride = true)
    private GridFsTemplate gridFsTemplate;

    // Replacing only the repository/template would still start a network client.
    @MockitoBean(enforceOverride = true)
    private MongoClient mongoClient;

    @Test
    void contextLoads() {
        assertThat(context.getBean(AuthService.class)).isNotNull();
        assertThat(context.getBean(JwtService.class)).isNotNull();
        assertThat(context.getBeansOfType(MongoClient.class).values())
                .isNotEmpty()
                .allSatisfy(client -> assertThat(mockingDetails(client).isMock()).isTrue());
        verifyNoInteractions(userRepository, mongoTemplate, gridFsTemplate, mongoClient);
    }

}
