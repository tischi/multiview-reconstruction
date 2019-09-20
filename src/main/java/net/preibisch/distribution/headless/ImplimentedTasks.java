package net.preibisch.distribution.headless;

import java.util.ArrayList;
import java.util.List;

import mpicbg.spim.data.sequence.ViewId;
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
		List<ViewId> viewIds = new ArrayList<>(gui.getFusionGroups().get(0).getViews());
		System.out.println("ViewsIds: "+viewIds.size());
		FusionParams params = new FusionParams(xml, viewIds, interval, downsampling);

			params.toJson(testFile);
	
	}

}
