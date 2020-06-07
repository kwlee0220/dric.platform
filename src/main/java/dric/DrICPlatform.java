package dric;

import dric.proto.EndPoint;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatform {
	private final DrICPlatformConfig m_conf;
	
	public DrICPlatform(DrICPlatformConfig conf) {
		m_conf = conf;
	}
	
	public EndPoint getServiceEndPoint(String name) {
		return m_conf.getServiceEndPoint(name);
	}
}
