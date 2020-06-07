package dric.grpc;

import com.google.protobuf.StringValue;

import dric.DrICPlatform;
import dric.proto.DrICPlatformGrpc.DrICPlatformImplBase;
import dric.proto.EndPoint;
import io.grpc.Status;
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
	
	@Override
    public void getServiceEndPoint(StringValue req, StreamObserver<EndPoint> out) {
		try {
			EndPoint ep = m_platform.getServiceEndPoint(req.getValue());
			out.onNext(ep);
		}
		catch ( IllegalArgumentException e ) {
			out.onError(Status.NOT_FOUND
							.withDescription("service name: " + req.getValue())
							.asException());
		}
		catch ( Exception e ) {
			out.onError(Status.INTERNAL.withCause(e).asException());
		}
		finally {
			out.onCompleted();
		}
    }
}
