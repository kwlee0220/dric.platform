package dric.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import dric.ConfigUtils;
import dric.DrICPlatform;
import dric.proto.ObjectBBoxTrackProto;
import dric.type.BoundingBox;
import dric.type.ObjectBBoxTrack;
import utils.func.Try;
import utils.jdbc.JdbcProcessor;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class PipeLineMonitor implements MqttCallback {
	private static final Logger s_logger = LoggerFactory.getLogger(PipeLineMonitor.class);
	
	private final DrICPlatform m_platform;
	private final JdbcProcessor m_jdbc;
	private final String[] m_topicNames;
	private volatile MqttClient m_client;
	
	public PipeLineMonitor(DrICPlatform platform) {
		m_platform = platform;
		m_jdbc = ConfigUtils.getJdbcProcessor(platform.getConfig().getJdbcEndPoint());
		m_topicNames = new String[] { DrICPlatform.TOPIC_BBOX_TRACKS };
	}
	
	public static void format(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		Try.run(() -> stmt.executeUpdate("drop table bbox_tracks"));
		stmt.executeUpdate(SQL_CREATE_BBOX_TRACKS);
	}

	public void subscribe() {
		try {
			m_client = m_platform.getMqttClient("dric_store");
			m_client.setCallback(this);
			m_client.connect();
			
			if ( s_logger.isInfoEnabled() ) {
				s_logger.info("starting to subscribe topics: " + Arrays.toString(m_topicNames));
			}
			m_client.subscribe(m_topicNames);
		}
		catch ( Exception e ) {
			throw new TopicException("" + e);
		}
	}
	
	public void unsubscribe() {
		if ( m_client != null ) {
			MqttClient client = m_client;
			m_client = null;

			if ( s_logger.isInfoEnabled() ) {
				s_logger.info("closing PipeLineMonitor: topics=" + Arrays.toString(m_topicNames));
			}
			try {
				client.unsubscribe(m_topicNames);
				client.disconnect();
			}
			catch ( Exception e ) {
				throw new TopicException("" + e);
			}
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		if ( topic.startsWith("dric/bbox_tracks") ) {
			appendBBoxTrack(message);
		}
	}

	@Override public void connectionLost(Throwable cause) { }
	@Override public void deliveryComplete(IMqttDeliveryToken token) { }
	
	private void appendBBoxTrack(MqttMessage mqttMsg) throws InvalidProtocolBufferException, SQLException {
		ObjectBBoxTrackProto proto = ObjectBBoxTrackProto.parseFrom(mqttMsg.getPayload());
		ObjectBBoxTrack track = ObjectBBoxTrack.fromProto(proto);
		
		String sql = "insert into bbox_tracks values (?,?,?,?,?,?,?,?)";
		try {
			m_jdbc.executeUpdate(sql, pstmt -> {
				pstmt.setString(1, track.cameraId());
				pstmt.setString(2, track.luid());
				
				BoundingBox bbox = track.bbox();
				pstmt.setDouble(3, bbox.tl().x());
				pstmt.setDouble(4, bbox.tl().y());
				pstmt.setDouble(5, bbox.br().x());
				pstmt.setDouble(6, bbox.br().y());
				
				pstmt.setFloat(7, track.heading());
				pstmt.setLong(8, track.ts());
			});
		}
		catch ( ExecutionException neverHappens ) { }
	}

	private static final String SQL_CREATE_BBOX_TRACKS
		= "create table bbox_tracks ("
		+ 	"camera_id varchar not null,"
		+ 	"luid varchar not null,"
		+ 	"tl_x double precision not null,"
		+ 	"tl_y double precision not null,"
		+ 	"br_x double precision not null,"
		+ 	"br_y double precision not null,"
		+ 	"heading real not null,"
		+ 	"ts bigint not null,"
		+ 	"primary key (camera_id, luid, ts)"
		+ ")";
}
