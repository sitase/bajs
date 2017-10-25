package se.tardell.simon.bajs.sample;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
public class HelloController {

    @ModelAttribute
    public void disableXSSProtection(HttpServletResponse response) {
        response.setHeader("X-XSS-Protection", "0");
    }
    @RequestMapping(value = "/hello")
    @ResponseBody
    public String hello(@RequestParam(name = "name") String name) {
       return  "<html><body><h1>Hello "+name+"</h1></body></html>";
    }

}