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
import dric.proto.JdbcEndPoint;
import utils.stream.FStream;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatformConfig {
	private final Map<String,EndPoint> m_endPoints = Maps.newHashMap();
	private final JdbcEndPoint m_jdbc;
	
	private DrICPlatformConfig(Map<String,EndPoint> endPoints, JdbcEndPoint jdbc) {
		m_endPoints.putAll(endPoints);
		m_jdbc = jdbc;
	}
	
	public static DrICPlatformConfig from(File configFile) throws FileNotFoundException, IOException {
		Yaml yaml = new Yaml();
		try ( FileReader reader = new FileReader(configFile) ) {
			@SuppressWarnings("unchecked")
			Map<String,Object> props = (Map<String,Object>)yaml.load(reader);
			return DrICPlatformConfig.from(props);
		}
	}
	
	public static DrICPlatformConfig from(Map<String,Object> config) {
		Map<String,EndPoint> endPoints = Maps.newHashMap();

		Map<String,Object> seps = ConfigUtils.getSubConfig(config, "service_end_points");
		FStream.from(seps)
				.forEach(kv -> endPoints.put(kv.key(), ConfigUtils.parseEndPoint(seps, kv.key())));
		JdbcEndPoint jdbc = ConfigUtils.parseJdbcEndPoint(config, "jdbc");
		
		return new DrICPlatformConfig(endPoints, jdbc);
	}
	
	public JdbcEndPoint getJdbcEndPoint() {
		return m_jdbc;
	}
	
	public EndPoint getServiceEndPoint(String serviceName) {
		EndPoint ep = m_endPoints.get(serviceName);
		if ( ep == null ) {
			throw new IllegalArgumentException("unregistered service: " + serviceName);
		}
		
		return ep;
	}
}
