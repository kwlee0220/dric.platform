package dric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.Maps;

import dric.proto.EndPoint;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatformConfig {
	private final Map<String,EndPoint> m_endPoints = Maps.newHashMap();
	
	private DrICPlatformConfig(Map<String,EndPoint> endPoints) {
		m_endPoints.putAll(endPoints);
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
	public static DrICPlatformConfig from(Map<String,Object> config) {
		Map<String,EndPoint> endPoints = Maps.newHashMap();

		Map<String,Object> seps = ConfigUtils.getSubConfig(config, "service_end_points");
		for ( String key: Arrays.asList("platform", "data_store", "topic_server", "video_server") ) {
			endPoints.put(key, ConfigUtils.parseEndPoint(seps, key));
		}
		
		return new DrICPlatformConfig(endPoints);
	}
	
	public EndPoint getServiceEndPoint(String serviceName) {
		EndPoint ep = m_endPoints.get(serviceName);
		if ( ep == null ) {
			throw new IllegalArgumentException("unregistered service: " + serviceName);
		}
		
		return ep;
	}
}
