import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.makjan.sulgilddara.SulgilddaraApplication;
import com.makjan.sulgilddara.board.scheduler.TemporaryBoardFileCleanupScheduler;

@SpringBootTest(classes = SulgilddaraApplication.class)
class TemporaryBoardFileCleanupSchedulerTest {

    @Autowired
    private TemporaryBoardFileCleanupScheduler scheduler;

    @Test
    void cleanupExpiredTempFilesTest() {
    	scheduler.cleanupExpiredTempFiles();			
    }
}