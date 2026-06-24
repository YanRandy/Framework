package randy.framework.servlet;

import randy.framework.annotation.Controller;
import randy.framework.annotation.UrlMapping;

@Controller
public class BallsController {

    @UrlMapping(value = "/fling",method = "POST")
    public void fling() {
        int a = 1;
        System.out.println("Flinging technique + " + a);
    }
}