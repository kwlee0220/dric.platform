package dric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import dric.proto.ServiceEndPoint;
import utils.func.FOption;
import utils.func.Funcs;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatformConfig {
	private final ServiceEndPoint m_endPoint;
	private final DataStoreConfig m_dataStoreConfig;
	private final TopicServerConfig m_topicServerConfig;
	private final VideoServerConfig m_videoServerConfig;
	
	private DrICPlatformConfig(ServiceEndPoint endPoint, DataStoreConfig dsConfig,
								TopicServerConfig tsConfig, VideoServerConfig vsConfig) {
		m_endPoint = endPoint;
		m_dataStoreConfig = dsConfig;
		m_topicServerConfig = tsConfig;
		m_videoServerConfig = vsConfig;
	}
	
	public static DrICPlatformConfig from(File configFile) throws FileNotFoundException, IOException {
		Yaml yaml = new Yaml();
		try ( FileReader reader = new FileReader(configFile) ) {
			@SuppressWarnings("unchecked")
			Map<String,Object> props = (Map<String,Object>)yaml.load(reader);
			return DrICPlatformConfig.from(props);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static DrICPlatformConfig from(Map<String,Object> props) {
		ServiceEndPoint sep = parseEndPoint((Map<String,Object>)props.get("service_end_point"));
		DataStoreConfig dsConf = DataStoreConfig.from((Map<String,Object>)props.get("data_store"));
		TopicServerConfig tsConf = TopicServerConfig.from((Map<String,Object>)props.get("topic_server"));
		VideoServerConfig vsConf = VideoServerConfig.from((Map<String,Object>)props.get("video_server"));
		
		return new DrICPlatformConfig(sep, dsConf, tsConf, vsConf);
	}
	
	public ServiceEndPoint getServiceEndPoint() {
		return m_endPoint;
	}
	
	public DataStoreConfig getDataStoreConfig() {
		return m_dataStoreConfig;
	}
	
	public TopicServerConfig getTopicServerConfig() {
		return m_topicServerConfig;
	}
	
	public VideoServerConfig getVideoServerConfig() {
		return m_videoServerConfig;
	}
	
	public static class DataStoreConfig {
		private final ServiceEndPoint m_endPoint;
		
		private DataStoreConfig(ServiceEndPoint sep) {
			m_endPoint = sep;
		}
		
		public static DataStoreConfig from(Map<String,Object> props) {
			return new DataStoreConfig(parseEndPoint(props));
		}
		
		public ServiceEndPoint getServiceEndPoint() {
			return m_endPoint;
		}
	}
	
	public static class VideoServerConfig {
		private final ServiceEndPoint m_endPoint;
		
		private VideoServerConfig(ServiceEndPoint sep) {
			m_endPoint = sep;
		}
		
		public static VideoServerConfig from(Map<String,Object> props) {
			return new VideoServerConfig(parseEndPoint(props));
		}
		
		public ServiceEndPoint getServiceEndPoint() {
			return m_endPoint;
		}
	}
	
	public static class TopicServerConfig {
		private final ServiceEndPoint m_endPoint;
		
		private TopicServerConfig(ServiceEndPoint sep) {
			m_endPoint = sep;
		}
		
		public static TopicServerConfig from(Map<String,Object> props) {
			return new TopicServerConfig(parseEndPoint(props));
		}
		
		public ServiceEndPoint getServiceEndPoint() {
			return m_endPoint;
		}
	}
	
	private static ServiceEndPoint parseEndPoint(Map<String,Object> props) {
		String host = Funcs.applyIfNotNull(props.get("host"), Object::toString, "localhost"); 
		int port = FOption.ofNullable(props.get("port"))
							.map(Object::toString)
							.map(Integer::parseInt)
							.getOrElse(-1);
		
		return ServiceEndPoint.newBuilder().setHost(host).setPort(port).build();
	}
}
