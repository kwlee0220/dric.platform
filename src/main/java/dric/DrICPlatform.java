package dric;

import dric.proto.ServiceEndPoint;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatform {
	private final DrICPlatformConfig m_conf;
	
	public DrICPlatform(DrICPlatformConfig conf) {
		m_conf = conf;
	}
	
	public ServiceEndPoint getDataStoreEndPoint() {
		return m_conf.getDataStoreConfig().getServiceEndPoint();
	}
	
	public ServiceEndPoint getVideoServerEndPoint() {
		return m_conf.getVideoServerConfig().getServiceEndPoint();
	}
	
	public ServiceEndPoint getTopicServerEndPoint() {
		return m_conf.getVideoServerConfig().getServiceEndPoint();
	}
}
