package test.java;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import main.java.db.LogRepository;
import main.java.db.LogSchema;
import main.java.service.LogService;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @Mock
    private LogRepository logRepository;
    @InjectMocks
    private LogService logService;
    private LogSchema testLog;

    @BeforeEach
    void setup(){
        testLog = new LogSchema();
        testLog.setUserId("1234abcd");
        testLog.setUserLog("User has logged in \n User has logged out");
    }

    @Test
    @DisplayName("Test to create a user log")
    void createLogTest(){
        when(logRepository.save(testLog)).thenReturn(testLog);

        assertEquals(testLog, logService.createLog(testLog), "The two created logs should match");
        verify(logRepository, times(1)).save(testLog);

    }

    @Test
    @DisplayName("Test to get a list of user logs")
    void getAllLogsTest(){
        LogSchema secondLog = new LogSchema();
        secondLog.setUserId("abcdef1234");
        secondLog.setUserLog("User logged in \n User booked an appointment \n user logged out");
        ArrayList<LogSchema> userLogs = new ArrayList<>();
        userLogs.add(secondLog);
        userLogs.add(testLog);

        when(logRepository.findAll()).thenReturn(userLogs);

        assertEquals(userLogs, logService.getAllLogs(), "The two list of user logs should match");
        verify(logRepository, times(1)).findAll();
    }

    
    @Test
    @DisplayName("Test to create a user log")
    void getLogByIdTest(){
        testLog.setId("65ffe2345tgf2345");
        when(logRepository.findById(testLog.getId())).thenReturn(Optional.of(testLog));

        assertEquals(logService.getLog(testLog), Optional.of(testLog), "The two logs should be equal in the test");

        verify(logRepository, times(1)).findById(testLog.getId());

    }


}
