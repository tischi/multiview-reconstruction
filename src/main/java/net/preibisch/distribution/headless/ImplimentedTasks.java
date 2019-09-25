package net.preibisch.distribution.headless;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.Interval;
import net.preibisch.distribution.algorithm.task.FusionParams;
import net.preibisch.distribution.algorithm.task.TaskFile;
import net.preibisch.distribution.tools.Tools;
import net.preibisch.mvrecon.fiji.plugin.fusion.FusionGUI;

public class ImplimentedTasks {

	public static void NonRegid(FusionGUI fusion) {
		String taskPath = TaskFile.get(TaskFile.NON_RIGID);
		
	}

	public static void fusion(FusionGUI gui) {

		String taskPath = TaskFile.get(TaskFile.FUSION);
		
		String xml = Tools.getXML(gui.getSpimData().getBasePath().getAbsolutePath()).getAbsolutePath();
		System.out.println("XML: "+xml);
		double downsampling = gui.getDownsampling();
		System.out.println("downsampling: "+downsampling);
		Interval interval = gui.getBoundingBox();
		System.out.println("interval: "+interval.toString());
		FusionParams params = new FusionParams(xml, gui.getFusionGroups(), interval, downsampling);
		
		
		try {
			ClusterWorkflow.run(params, taskPath);
		} catch (IOException | JSchException | SftpException | SpimDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

//			params.toJson(testFile);
	
	}

}
