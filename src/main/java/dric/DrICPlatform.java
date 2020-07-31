package dric;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import dric.proto.EndPoint;
import dric.store.TopicException;
import dric.type.CameraFrame;
import io.grpc.StatusRuntimeException;
import marmot.dataset.DataSet;
import marmot.dataset.DataSetExistsException;
import marmot.dataset.DataSetInfo;
import marmot.dataset.DataSetServer;
import marmot.dataset.DataSetType;
import marmot.remote.client.GrpcMarmotRuntimeProxy;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatform {
	public static final String TOPIC_BBOX_TRACKS = "dric/bbox_tracks";
	
	private final DrICPlatformConfig m_conf;
	
	public DrICPlatform(DrICPlatformConfig conf) {
		m_conf = conf;
		
		EndPoint topicServerEp = getServiceEndPoint("topic_server");
		EndPoint ep = getServiceEndPoint("marmot_server");
		try ( GrpcMarmotRuntimeProxy marmot = GrpcMarmotRuntimeProxy.connect(ep.getHost(), ep.getPort()) ) {
			DataSetServer dsServer = marmot.getDataSetServer();
			DataSetInfo cameraFrames = new DataSetInfo("topics/" + CameraFrame.getTopicName(),
														DataSetType.MQTT, CameraFrame.RECORD_SCHEMA);
			DataSet ds = dsServer.createDataSet(cameraFrames, false);
			DataSetInfo info = ds.getDataSetInfo();
			info.setParameter(topicServerEp.getHost() + ":" + topicServerEp.getPort() + ":" + CameraFrame.getTopicName());
			dsServer.updateDataSet(info);
		}
		catch ( DataSetExistsException expected ) { }
		catch ( StatusRuntimeException e ) {
			throw new IllegalStateException("fails to connect to marmot-server: end-point=" + ep);
		}
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
