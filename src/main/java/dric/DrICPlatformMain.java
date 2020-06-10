package dric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dric.grpc.PBDrICPlatformServant;
import dric.proto.EndPoint;
import dric.store.PipeLineMonitor;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import utils.UsageHelp;
import utils.Utilities;
import utils.func.FOption;
import utils.jdbc.JdbcProcessor;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class DrICPlatformMain implements Runnable {
	private static final Logger s_logger = LoggerFactory.getLogger(DrICPlatformMain.class);
	private static final int DEFAULT_DRIC_PORT = 10703;
	private static final String DEFAULT_CONFIG_FNAME = "dric.platform.yaml";
	
	@Spec private CommandSpec m_spec;
	@Mixin private UsageHelp m_help;
	
	@Option(names={"--config"}, paramLabel="path", description={"platform configuration file"})
	private File m_configFile;
	
	private File m_homeDir;
	@Option(names={"--home"}, paramLabel="path", description={"DrICPlatform Home Directory"})
	public void setHome(String path) throws IOException {
		m_homeDir = new File(path).getCanonicalFile();
	}
	
	@Option(names={"-f", "--format"}, description={"format DrICPlatform database"})
	private boolean m_format = false;
	
	@Option(names={"-u", "--update"}, description={"update pipeline's result"})
	private boolean m_update = false;
	
	@Option(names={"-v"}, description={"verbose"})
	private boolean m_verbose = false;
	
	private volatile PipeLineMonitor m_topicMonitor;
	
	public static final void main(String... args) throws Exception {
		DrICPlatformMain cmd = new DrICPlatformMain();
		CommandLine.run(cmd, System.out, System.err, Help.Ansi.OFF, args);
	}
	
	@Override
	public void run() {
		try  {
			configureLog4j();
			
			File configFile = FOption.ofNullable(m_configFile)
									.getOrElse(() -> new File(getHomeDir(), DEFAULT_CONFIG_FNAME));
			if ( m_verbose ) {
				System.out.println("use config.file=" + configFile);
			}
			DrICPlatformConfig config = DrICPlatformConfig.from(configFile);
			DrICPlatform platform = new DrICPlatform(config);

			if ( m_format ) {
				JdbcProcessor jdbc = ConfigUtils.getJdbcProcessor(config.getJdbcEndPoint());
				try ( Connection conn = jdbc.connect() ) {
					if ( m_verbose ) {
						System.out.println("format database");
					}
					
					PipeLineMonitor.format(conn);
				}
			}
			if ( m_update ) {
				m_topicMonitor = new PipeLineMonitor(platform);
				m_topicMonitor.subscribe();
			}
			
	    	Runtime.getRuntime().addShutdownHook(new Thread() {
	    		public void run() {
	    			if ( m_topicMonitor != null ) {
	    				m_topicMonitor.unsubscribe();
	    				m_topicMonitor = null;
	    			}
	    		}
	    	});
			
			EndPoint endPoint = platform.getServiceEndPoint("platform");
			int port = endPoint.getPort();
			if ( port < 0 ) {
				port = DEFAULT_DRIC_PORT;
			}
			
			Server server = createServer(platform, port);
			server.start();

			System.out.printf("started: DrICPlatform[host=%s, port=%d]%n", endPoint.getHost(), port);

			server.awaitTermination();
		}
		catch ( Throwable e ) {
			System.err.printf("failed: %s%n%n", e);
			m_spec.commandLine().usage(System.out, Ansi.OFF);
		}
		finally {
			if ( m_topicMonitor != null ) {
				m_topicMonitor.unsubscribe();
				m_topicMonitor = null;
			}
		}
	}
	
	private File configureLog4j() throws IOException {
		File propsFile = new File(getHomeDir(), "log4j.properties");
		if ( m_verbose ) {
			System.out.println("use log4.properties=" + propsFile);
		}
		
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
	
	private File getHomeDir() {
		File homeDir = m_homeDir;
		if ( homeDir == null ) {
			homeDir = FOption.ofNullable(System.getenv("DRIC_HOME"))
								.map(File::new)
								.getOrElse(Utilities::getCurrentWorkingDir);
		}
		
		return homeDir;
	}
}
