package pac.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

	@Autowired
    private SimpMessagingTemplate template;

	@Autowired
	private MQTTController mqttController;
	
	@Autowired
	public void init() {
		mqttController.subscribe("hello/temp", "javaclientTemp-sub", template);
		System.out.println("Subscribed in topic hello/temp...");
		mqttController.subscribe("hello/led", "javaclientLed-sub", template);
		System.out.println("Subscribed in topic hello/led...");
	}

	
    @MessageMapping("/send/message")
    public void onReceivedMesage(String message) {
        this.template.convertAndSend("/chat",  new SimpleDateFormat("HH:mm:ss").format(new Date())+"- "+message);
    }
    
    @MessageMapping("/send/led")
    public void onReceivedLed(String message) {
    	if("ligar".equalsIgnoreCase(message)) {
    		mqttController.changeLed(1);
    		System.out.println("Led ligado");
    	}
    	if("desligar".equalsIgnoreCase(message)) {
    		mqttController.changeLed(0);
    		System.out.println("Led desligado");
    	}
    	if("iniciar".equalsIgnoreCase(message)) {
    		mqttController.changeLed(-1);
    		System.out.println("Buscando status led");
    	}
    }
    
}
