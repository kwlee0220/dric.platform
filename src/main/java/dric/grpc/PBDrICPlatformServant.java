package dric.grpc;

import com.google.protobuf.Empty;

import dric.DrICPlatform;
import dric.proto.DrICPlatformGrpc.DrICPlatformImplBase;
import dric.proto.ServiceEndPoint;
import dric.proto.ServiceEndPointResponse;
import io.grpc.stub.StreamObserver;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class PBDrICPlatformServant extends DrICPlatformImplBase {
	private final DrICPlatform m_platform;
	
	public PBDrICPlatformServant(DrICPlatform platform) {
		m_platform = platform;
	}
	
    public void getVideoServerEndPoint(Empty req, StreamObserver<ServiceEndPointResponse> out) {
		try {
			ServiceEndPoint sep = m_platform.getVideoServerEndPoint();
			out.onNext(ServiceEndPointResponse.newBuilder()
											.setEndPoint(sep)
											.build());
		}
		catch ( Exception e ) {
			out.onNext(ServiceEndPointResponse.newBuilder()
											.setError(PBUtils.toErrorProto(e))
											.build());
		}
		finally {
			out.onCompleted();
		}
    }
	
    public void getDataStoreEndPoint(Empty req, StreamObserver<ServiceEndPointResponse> out) {
		try {
			ServiceEndPoint sep = m_platform.getDataStoreEndPoint();
			out.onNext(ServiceEndPointResponse.newBuilder()
											.setEndPoint(sep)
											.build());
		}
		catch ( Exception e ) {
			out.onNext(ServiceEndPointResponse.newBuilder()
											.setError(PBUtils.toErrorProto(e))
											.build());
		}
		finally {
			out.onCompleted();
		}
    }
}
