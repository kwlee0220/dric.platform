package dric.grpc;

import com.google.protobuf.StringValue;

import dric.DrICPlatform;
import dric.proto.DrICPlatformGrpc.DrICPlatformImplBase;
import dric.proto.EndPoint;
import dric.proto.EndPointResponse;
import io.grpc.stub.StreamObserver;
import proto.ErrorValue;
import proto.ErrorValue.Code;
import utils.grpc.PBUtils;

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
    public void getServiceEndPoint(StringValue req, StreamObserver<EndPointResponse> out) {
		try {
			EndPoint ep = m_platform.getServiceEndPoint(req.getValue());
			EndPointResponse resp = EndPointResponse.newBuilder().setEndPoint(ep).build();
			out.onNext(resp);
		}
		catch ( IllegalArgumentException e ) {
			ErrorValue error = PBUtils.ERROR(Code.INVALID_ARGUMENT, "service name: " + req.getValue());
			out.onNext(EndPointResponse.newBuilder().setError(error).build());
		}
		catch ( Exception e ) {
			ErrorValue error = PBUtils.ERROR(e);
			out.onNext(EndPointResponse.newBuilder().setError(error).build());
		}
		finally {
			out.onCompleted();
		}
    }
}
