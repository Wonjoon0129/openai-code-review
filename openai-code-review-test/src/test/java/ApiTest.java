import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Test
    public void test() {
        System.out.println(Integer.parseInt("aaaaa1"));
        System.out.println(Integer.parseInt("aaaaa2"));
        System.out.println(Integer.parseInt("aaaaa3、4"));
        System.out.println(Integer.parseInt("aaaaa5"));

    }

}
