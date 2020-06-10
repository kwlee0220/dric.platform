package dric;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import dric.proto.EndPoint;
import dric.store.TopicException;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatform {
	public static final String TOPIC_BBOX_TRACKS = "dric/bbox_tracks";
	
	private final DrICPlatformConfig m_conf;
	
	public DrICPlatform(DrICPlatformConfig conf) {
		m_conf = conf;
	}
	
	public DrICPlatformConfig getConfig() {
		return m_conf;
	}
	
	public EndPoint getServiceEndPoint(String name) {
		return m_conf.getServiceEndPoint(name);
	}
	
	public MqttClient getMqttClient(String id) {
		try {
			EndPoint ep = getServiceEndPoint("topic_server");
			String brokerUrl = String.format("tcp://%s:%d", ep.getHost(),  ep.getPort());
			MqttClient client = new MqttClient(brokerUrl, id);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			
			return client;
		}
		catch ( MqttException e ) {
			throw new TopicException("" + e);
		}
	}
}
