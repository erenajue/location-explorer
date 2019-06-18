package lu.hitec.dispng.locationexplorer;

import lu.hitec.dispng.locationexplorer.repository.LocationExplorerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LocationExplorerApplicationTests {

    @Autowired
    private LocationExplorerRepository repository;

    @Test
    public void contextLoads() {
    }

}
