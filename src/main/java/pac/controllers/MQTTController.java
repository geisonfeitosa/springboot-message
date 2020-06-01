package pac.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MQTTController {

	private ObjectMapper mapper = new ObjectMapper();
	private String topic = "hello/led";
	private int qos = 2;
	private String broker = "tcp://iot.plug.farm:1883";
	private MemoryPersistence persistence = new MemoryPersistence();
	
	private String getJsonLed(int v) throws JsonProcessingException {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("sensor", "clock");
		data.put("number", v);
		data.put("time", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
		String json = mapper.writeValueAsString(data);
		return json;
	}

	public void subscribe(String topic, String user, SimpMessagingTemplate template) {
		try {
			MqttClient sampleClient = new MqttClient(broker, user, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			IMqttMessageListener listener = (String topicListener, MqttMessage messageListener) -> {
				String json = new String(messageListener.getPayload()); 
				System.out.println(json);
				template.convertAndSend("/chat",  json);
			};
			sampleClient.subscribe(topic, listener);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void changeLed(int v) {
		try {
			MqttClient sampleClient = new MqttClient(broker, "javaclient-pub", persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
			String content = getJsonLed(v);
			System.out.println("Publishing message: " + content);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			sampleClient.publish(topic, message);
			System.out.println("Message published");
			sampleClient.disconnect();
			System.out.println("Disconnected");
		} catch (JsonProcessingException | MqttException e) {
			e.printStackTrace();
		}
	}
	
}
