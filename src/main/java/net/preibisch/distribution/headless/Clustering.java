package net.preibisch.distribution.headless;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.ViewDescription;
import mpicbg.spim.data.sequence.ViewId;
import mpicbg.spim.io.IOFunctions;
import net.imglib2.Interval;
import net.imglib2.util.Util;
import net.preibisch.distribution.algorithm.blockmanager.BlockConfig;
import net.preibisch.distribution.algorithm.blockmanager.block.BasicBlockInfo;
import net.preibisch.distribution.algorithm.clustering.ClusterFile;
import net.preibisch.distribution.algorithm.clustering.jsch.SCPManager;
import net.preibisch.distribution.algorithm.clustering.kafka.KafkaMessageManager;
import net.preibisch.distribution.algorithm.clustering.scripting.BatchScriptFile;
import net.preibisch.distribution.algorithm.clustering.scripting.ClusterScript;
import net.preibisch.distribution.algorithm.clustering.scripting.TaskType;
import net.preibisch.distribution.algorithm.controllers.items.BlocksMetaData;
import net.preibisch.distribution.algorithm.controllers.items.Job;
import net.preibisch.distribution.algorithm.controllers.items.server.Login;
import net.preibisch.distribution.algorithm.controllers.logmanager.MyLogger;
import net.preibisch.distribution.algorithm.controllers.metadata.MetadataGenerator;
import net.preibisch.distribution.algorithm.task.FusionParams;
import net.preibisch.distribution.gui.PreviewUI;
import net.preibisch.distribution.io.GsonIO;
import net.preibisch.distribution.io.img.XMLFile;
import net.preibisch.distribution.io.img.n5.N5File;
import net.preibisch.distribution.tools.Tools;
import net.preibisch.mvrecon.fiji.plugin.fusion.FusionGUI;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.constellation.grouping.Group;

public class Clustering {
	private static final String BATCH_NAME = "_submit.cmd";
	private static final String TASK_SHELL_NAME = "_task.sh";

	public static void main(String[] args) throws SpimDataException, IOException, JSchException, SftpException {
		run("/Users/Marwan/Desktop/grid-3d-stitched-h5/dataset.xml");
	}

	public static void run(String input_path) throws SpimDataException, IOException, JSchException, SftpException {
		run(input_path, "output.n5");
	}

	public static void run(String input_path, String output_name)
			throws SpimDataException, IOException, JSchException, SftpException {
	}

	public static void run(FusionGUI fusion) {
		System.out.println("Start clustering.. ");
		String testFile = "/Users/Marwan/Desktop/Task/test.json";
		if (fusion.getNonRigidParameters().isActive())
			// taskPath = TaskFile.get(TaskFile.NON_RIGID);
			return;
		else
		// taskPath = TaskFile.get(TaskFile.FUSION);

		{
			String xml = Tools.getXML(fusion.getSpimData().getBasePath().getAbsolutePath()).getAbsolutePath();
			System.out.println("XML: "+xml);
			double downsampling = fusion.getDownsampling();
			System.out.println("downsampling: "+downsampling);
			Interval interval = fusion.getBoundingBox();
			System.out.println("interval: "+interval.toString());
			List<ViewId> viewIds = new ArrayList<>(fusion.getFusionGroups().get(0).getViews());
			System.out.println("ViewsIds: "+viewIds.size());
			FusionParams params = new FusionParams(xml, viewIds, interval, downsampling);

				params.toJson(testFile);
		}
		return;

//		try {
//			IOFunctions.println("CLUSTER!!!");
//			Interval interval = fusion.getBoundingBox();
//			SpimData2 spimdata = fusion.getSpimData();
//			double downsampling = fusion.getDownsampling();
//			final List<Group<ViewDescription>> groups = fusion.getFusionGroups();
//
//			String taskPath = "";
//
//			if (fusion.getNonRigidParameters().isActive())
//				taskPath = TaskFile.get(TaskFile.NON_RIGID);
//			else
//				taskPath = TaskFile.get(TaskFile.FUSION);
//
//			File taskFile = new File(taskPath);
//			run(spimdata, interval, downsampling, groups, taskFile);
//
//			//
//		} catch (IOException | JSchException | SftpException e) {
//
//			e.printStackTrace();
//		}
	}

	private static void run(SpimData2 spimdata, Interval interval, double downsampling,
			List<Group<ViewDescription>> groups, File taskFile) throws IOException, JSchException, SftpException {

		File xml = XMLFile.fromStitchFolder(spimdata.getBasePath().getAbsolutePath());
		String inputPath = xml.getAbsolutePath();

		System.out.println("file: " + inputPath);

		int down;
		if (Double.isNaN(downsampling))
			down = 1;
		else
			down = (int) downsampling;
		// new ImageJ();

		MyLogger.initLogger();
		new Job();
		List<File> relatedFiles = XMLFile.initRelatedFiles(new File(inputPath));
		BoundingBox bb = new BoundingBox(interval);
		List<ViewId> viewIds = new ArrayList<>(groups.get(0).getViews());
		XMLFile inputFile = new XMLFile(inputPath, bb, spimdata, down, viewIds, relatedFiles);

		PreviewUI ui = new PreviewUI(inputFile, groups.size());

		Login.login();

		ClusterFile clusterFolderName = new ClusterFile(Login.getServer().getPath(), Job.getId());

		// SCPManager.createClusterFolder(clusterFolderName);

		inputFile.getRelatedFiles().add(taskFile);
		SCPManager.sendInput(inputFile, clusterFolderName);

		for (Group<ViewDescription> group : groups) {
			IOFunctions.println("group " + group);
			viewIds = new ArrayList<>(group.getViews());

			int i = groups.indexOf(group);
			String output_name = i + "_output.n5";
			MyLogger.log.info("inputPath: " + spimdata.getBasePath() + "\ninterval: " + interval.toString()
					+ "\n spimdata: " + spimdata.getBasePath().getName() + "\n downsampling: " + downsampling
					+ "\n viewIds: " + viewIds.toString() + "\n output_name: " + output_name);

			N5File outputFile = new N5File(Job.file(output_name).getAbsolutePath(), inputFile.getDims(),
					BlockConfig.BLOCK_UNIT);
			System.out.println("Blocks: " + Util.printCoordinates(outputFile.getBlocksize()));

			Map<Integer, BasicBlockInfo> blocksInfo = MetadataGenerator.generateBlocks(inputFile.bb(),
					outputFile.getBlocksize());
			BlocksMetaData md = new BlocksMetaData(Job.getId(), viewIds, blocksInfo,
					Util.int2long(outputFile.getBlocksize()), BlockConfig.BLOCK_UNIT, bb.getDimensions(down),
					blocksInfo.size(), down);
			String metadataFileName = i + "_metadata.json";
			File metadataFile = Job.file(metadataFileName);
			MyLogger.log.info(md.toString());
			Job.setTotalbBlocks(md.getTotal());
			// md.toJson(metadataFile);
			GsonIO.toJson(md, metadataFile);

			// Generate script

			String taskScriptName = i + TASK_SHELL_NAME;
			File scriptFile = Job.file(taskScriptName);
			File metadataCluster = clusterFolderName.subfile(metadataFile);
			File inputCluster = clusterFolderName.subfile(inputFile);
			File clusterOutput = clusterFolderName.subfile(outputFile);

			ClusterScript.generateTaskScript(scriptFile, taskFile.getName(), metadataCluster.getPath(),
					inputCluster.getPath(), clusterOutput.getPath());

			// Task to prepare N5
			String prepareScriptName = i + TaskType.file(TaskType.PREPARE);
			File prepareShell = Job.file(prepareScriptName);
			ClusterScript.generateTaskScript(TaskType.PREPARE, prepareShell, taskFile.getName(),
					metadataCluster.getPath(), inputCluster.getPath(), clusterOutput.getPath(), "");

			// Generate batch

			String batchScriptName = i + BATCH_NAME;
			File batchScriptFile = Job.file(batchScriptName);
			BatchScriptFile.generate(batchScriptFile, clusterFolderName.getPath(), md.getTotal(), i, prepareScriptName,
					taskScriptName); // md.getTotal()

			// send all
			List<File> toSend = new ArrayList<>();
			toSend.add(metadataFile);
			toSend.add(batchScriptFile);
			toSend.add(scriptFile);
			toSend.add(prepareShell);

			SCPManager.send(toSend, clusterFolderName);

			// Run
			SCPManager.startBatch(clusterFolderName.subfile(batchScriptFile));

		}
		new KafkaMessageManager(Job.getId(), groups.size());

	}
}
