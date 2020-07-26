package dric.grpc;

import dric.DrICPlatform;
import dric.proto.DrICPlatformGrpc.DrICPlatformImplBase;
import dric.proto.EndPoint;
import dric.proto.EndPointResponse;
import io.grpc.stub.StreamObserver;
import marmot.proto.ErrorProto;
import marmot.proto.ErrorProto.Code;
import marmot.proto.StringProto;
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
    public void getServiceEndPoint(StringProto req, StreamObserver<EndPointResponse> out) {
		try {
			EndPoint ep = m_platform.getServiceEndPoint(req.getValue());
			EndPointResponse resp = EndPointResponse.newBuilder().setEndPoint(ep).build();
			out.onNext(resp);
		}
		catch ( IllegalArgumentException e ) {
			ErrorProto error = PBUtils.ERROR(Code.INVALID_ARGUMENT, "service name: " + req.getValue());
			out.onNext(EndPointResponse.newBuilder().setError(error).build());
		}
		catch ( Exception e ) {
			ErrorProto error = PBUtils.ERROR(e);
			out.onNext(EndPointResponse.newBuilder().setError(error).build());
		}
		finally {
			out.onCompleted();
		}
    }
}
