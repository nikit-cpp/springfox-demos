package springfoxdemo.staticdocs.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Created by nikita on 01.11.16.
 */
@RestController
public class TestController {

    @RequestMapping(path = "/fail", method = RequestMethod.POST)
    public Map<String, String> fail(@RequestParam Map<String, String> requestParameters) {
        return requestParameters;
    }
}
