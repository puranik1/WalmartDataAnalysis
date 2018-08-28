package analytics.controller;

import analytics.service.RegisterStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class provides method for REST GET call for injestion of messages
 */
@RestController
public class AnalyticsController {

    @Autowired
    RegisterStatus registerStatus;

    @RequestMapping("/injestmessages")
    public void injestMessages(){

        registerStatus.processMessages();

    }

}
