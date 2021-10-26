import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessListTest {

    @org.junit.jupiter.api.Test
    void getList() {
        ProcessList n = new ProcessList();
        assertTrue(n.getList() instanceof List);
    }
}