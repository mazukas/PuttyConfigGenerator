package gov.sbs.devops.puttygenerator;

import gov.sbs.devops.puttygenerator.domain.ServerMetadata;
import gov.sbs.devops.puttygenerator.filezilla.FileZilla3;
import gov.sbs.devops.puttygenerator.filezilla.FileZilla3.Servers;
import gov.sbs.devops.puttygenerator.filezilla.FileZilla3.Servers.Server;
import gov.sbs.devops.puttygenerator.mtputty.MTPutty;
import gov.sbs.devops.puttygenerator.mtputty.MTPutty.Servers.Putty;
import gov.sbs.devops.puttygenerator.mtputty.MTPutty.Servers.Putty.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class FileGenerator {

	public static void main(String[] args) {
		
		System.out.println("User Name : " + args[0]);
		System.out.println("AWS Servers File Location : " + args[1]);
		System.out.println("GCS Servers File Location : " + args[2]);
		System.out.println("Filezilla File Output Location : " + args[3]);
		System.out.println("MTPutty File Output Location : " + args[4]);
		
		try {
			Properties props = new Properties();
			try {
				props.load(FileGenerator.class.getResourceAsStream("/config.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			FileGenerator fg = new FileGenerator(	args[1],
													args[2],
													args[3],
													args[4],
													args[0]);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public FileGenerator(String awsServersBucketUrl, String gcsServersBucketUrl, String filezillaConfigLocation, String mtPuttyConfigLocation, String loginName) throws IOException, JAXBException {
		List<ServerMetadata> smList = readFileFromAmazon(awsServersBucketUrl);
		smList.addAll(readFileFromAmazon(gcsServersBucketUrl));
		
		generateFilezillaConfigFile(smList, filezillaConfigLocation, loginName);
		generateMTPuttyConfigFile(smList, mtPuttyConfigLocation, loginName);
	}
	
	private List<ServerMetadata> readFileFromAmazon(String bucketUrl) throws IOException {
		List<ServerMetadata> smList = new ArrayList<ServerMetadata>();
		
		URL website = new URL(bucketUrl);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                    connection.getInputStream()));

        String inputLine;

        //Expect this format "ServerUrl1;Desc1;Port1;Some desc1"
        while ((inputLine = in.readLine()) != null)  {
            String[] parts = inputLine.split(";");
            ServerMetadata sm = new ServerMetadata(parts[0], parts[1], new Integer(parts[2]), parts[3]);
            smList.add(sm);
        }

        in.close();

        return smList;
	}
	
	private List<ServerMetadata> readFileFromFileSystem(String path) throws IOException {
		List<ServerMetadata> smList = new ArrayList<ServerMetadata>();
		
		for (String line : Files.readAllLines(Paths.get(path), Charset.defaultCharset())) {
            String[] parts = line.split(";");
            ServerMetadata sm = new ServerMetadata(parts[0], parts[1], new Integer(parts[2]), parts[3]);
            smList.add(sm);
		}
	
		
		return smList;
	}
	
	private void generateFilezillaConfigFile(List<ServerMetadata> smList, String fileOutputPath, String loginName) throws JAXBException, FileNotFoundException {
		FileZilla3 fileZilla = new FileZilla3();
		fileZilla.setServers(new Servers());
		for (ServerMetadata sm : smList) {
			Server server = new Server();
			server.setHost(sm.getHost());
			server.setName(sm.getName());
			server.setPort(sm.getPort());
			server.setComments(sm.getComments());
			server.setUser(loginName);
			
			//This is a hack because the <Server> element wants to use mixed 
			//content and I really don't want to deal with that right now
			server.setValueHost(sm.getHost());
			
			fileZilla.getServers().getServer().add(server);
		}
		
		StringWriter sw = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(FileZilla3.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

		jaxbMarshaller.marshal(fileZilla, sw);
		
		String xml = sw.toString();
		xml = xml.replaceAll("<ValueHost>", "").replaceAll("</ValueHost>", "");
		
		PrintWriter out = new PrintWriter(fileOutputPath);
		out.print(xml);
		out.close();
	}
	
	private void generateMTPuttyConfigFile(List<ServerMetadata> smList, String fileOutputPath, String loginName) throws JAXBException, FileNotFoundException {
		/*
		MTPutty mtputty = new MTPutty();
		mtputty.setVersion(1.0);
		mtputty.setGlobals(new Globals());
		mtputty.getGlobals().setPuttyLocation("D:\\tools\\PuTTY\\PUTTY.EXE");
		
		mtputty.getGlobals().setGUI(new GUI());
		mtputty.getGlobals().getGUI().setPos(new Pos());
		mtputty.getGlobals().getGUI().getPos().setLeft(0);
		mtputty.getGlobals().getGUI().getPos().setTop(0);
		mtputty.getGlobals().getGUI().getPos().setRight(1900);
		mtputty.getGlobals().getGUI().getPos().setBottom(1150);
		mtputty.getGlobals().getGUI().getPos().setMaximized(0);
		mtputty.getGlobals().getGUI().setServersBox(new ServersBox());
		mtputty.getGlobals().getGUI().getServersBox().setVisible(1);
		mtputty.getGlobals().getGUI().getServersBox().setWidth(240);
		mtputty.getGlobals().getGUI().getServersBox().setHeight(1050);
		mtputty.getGlobals().getGUI().getServersBox().setDock("Left");
		mtputty.getGlobals().getGUI().getServersBox().setAutohide(0);
		mtputty.getGlobals().getGUI().setToolbar(new Toolbar());
		mtputty.getGlobals().getGUI().getToolbar().setVisible(1);
		mtputty.getGlobals().getGUI().setMainMenu(new MainMenu());
		mtputty.getGlobals().getGUI().getMainMenu().setVisible(1);
		mtputty.getGlobals().getGUI().setStartPage(new StartPage());
		mtputty.getGlobals().getGUI().getStartPage().setHideOnTab(0);
		
		mtputty.getGlobals().setGeneral(new General());
		mtputty.getGlobals().getGeneral().setTabCaption(0);
		mtputty.getGlobals().getGeneral().setNormalTermination(0);
		mtputty.getGlobals().getGeneral().setAbnormalTermination(0);
		mtputty.getGlobals().getGeneral().setQuitNoConfirm(1);
		mtputty.getGlobals().getGeneral().setCloseButtonOnTabs(1);
		
		mtputty.setInternals(new Internals());
		mtputty.getInternals().setPutty(null);
		mtputty.setRecents(new Recents());
		mtputty.getRecents().setPuttyCon(null);
		
		mtputty.setHotkeys(new Hotkeys());
		mtputty.getHotkeys().setNextTab(0);
		mtputty.getHotkeys().setPrevTab(0);
		mtputty.getHotkeys().setAppSwitch(16576);
		mtputty.getHotkeys().setAcPuttyLocations(0);
		mtputty.getHotkeys().setAcSettings(0);
		mtputty.getHotkeys().setAcHideServers(16450);
		mtputty.getHotkeys().setAcHideMenu(16461);
		mtputty.getHotkeys().setAcHideToolbar(16468);
		mtputty.getHotkeys().setAcAddServer(0);
		mtputty.getHotkeys().setAcAddGroup(0);
		mtputty.getHotkeys().setAcRemove(0);
		mtputty.getHotkeys().setAcDetach(0);
		mtputty.getHotkeys().setAcConnect(0);
		mtputty.getHotkeys().setAcConnectTo(16459);
		mtputty.getHotkeys().setAcTreeProps(0);
		mtputty.getHotkeys().setAcSendScript(0);
		mtputty.getHotkeys().setAcAttach(0);
		mtputty.getHotkeys().setAcRenameTab(0);
		mtputty.getHotkeys().setAcImportTree(0);
		mtputty.getHotkeys().setAcExportTree(0);
		mtputty.getHotkeys().setAcHotkeyMap(0);
		mtputty.getHotkeys().setAcDuplicate(0);
		*/
		
		//Because unlike filezilla which breaks up its configuration, MTPutty dumps everything 
		//into one file.  Because of this we must read the file in, clean out the current 
		//list of servers, and re-populate the list.
        JAXBContext jc = JAXBContext.newInstance(MTPutty.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File xml = new File(fileOutputPath);
        MTPutty mtputty = (MTPutty) unmarshaller.unmarshal(xml);
		
        if (null == mtputty.getServers()) {
        	mtputty.setServers(new gov.sbs.devops.puttygenerator.mtputty.MTPutty.Servers());
        }
        
        mtputty.getServers().setPutty(new Putty());
		for (ServerMetadata sm : smList) {
			Node server = new Node();
			server.setType(1);
			server.setPort(sm.getPort());
			server.setSavedSession("Default Settings");
			server.setDisplayName(sm.getName());
			server.setServerName(sm.getHost());
			server.setPuttyConType(4);
			server.setUserName(loginName);
			server.setPasswordDelay(0);
			server.setScriptDelay(0);
			server.setCLParams(sm.getHost() + " -ssh -P " + sm.getPort() + " -l "+ loginName);
			
			mtputty.getServers().getPutty().getNode().add(server);
		}
		
		StringWriter sw = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(MTPutty.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		jaxbMarshaller.marshal(mtputty, sw);

		PrintWriter out = new PrintWriter(fileOutputPath);
		out.print(sw.toString());
		out.close();
	}

}
