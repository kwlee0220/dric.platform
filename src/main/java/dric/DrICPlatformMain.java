package dric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dric.grpc.PBDrICPlatformServant;
import dric.proto.ServiceEndPoint;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import utils.NetUtils;
import utils.UsageHelp;
import utils.func.FOption;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatformMain implements Runnable {
	private static final Logger s_logger = LoggerFactory.getLogger(DrICPlatformMain.class);
	private static final int DEFAULT_DRIC_PORT = 10703;
	
	@Spec private CommandSpec m_spec;
	@Mixin private UsageHelp m_help;
	
	@Option(names={"--config"}, paramLabel="path", description={"platform configuration file"})
	private File m_configFile = new File("dric_platform.yaml");
	
	@Option(names={"-v"}, description={"verbose"})
	private boolean m_verbose = false;
	
	public static final void main(String... args) throws Exception {
		configureLog4j();

		DrICPlatformMain cmd = new DrICPlatformMain();
		CommandLine.run(cmd, System.out, System.err, Help.Ansi.OFF, args);
	}
	
	@Override
	public void run() {
		try  {
			DrICPlatformConfig config = DrICPlatformConfig.from(m_configFile);
			DrICPlatform platform = new DrICPlatform(config);
			
			ServiceEndPoint endPoint = config.getServiceEndPoint();
			
			int port = endPoint.getPort();
			if ( port < 0 ) {
				port = DEFAULT_DRIC_PORT;
			}
			
			Server server = createServer(platform, port);
			server.start();

			String host = NetUtils.getLocalHostAddress();
			System.out.printf("started: DrICPlatform[host=%s, port=%d]%n", host, port);

			server.awaitTermination();
		}
		catch ( Throwable e ) {
			System.err.printf("failed: %s%n%n", e);
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
		finally {
		}
	}
	
	public static File getLog4jPropertiesFile() {
		String homeDir = FOption.ofNullable(System.getenv("DRIC_HOME"))
								.getOrElse(() -> System.getProperty("user.dir"));
		return new File(homeDir, "log4j.properties");
	}
	
	public static File configureLog4j() throws IOException {
		File propsFile = getLog4jPropertiesFile();
		
		Properties props = new Properties();
		try ( InputStream is = new FileInputStream(propsFile) ) {
			props.load(is);
		}
		PropertyConfigurator.configure(props);
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("use log4j.properties from {}", propsFile);
		}
		
		return propsFile;
	}
	
	private Server createServer(DrICPlatform dric, int port) {
		PBDrICPlatformServant platform = new PBDrICPlatformServant(dric);
		
		Server nettyServer = NettyServerBuilder.forPort(port)
												.addService(platform)
												.build();
		return nettyServer;
	}
}
