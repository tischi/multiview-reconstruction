package net.preibisch.distribution.headless;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import mpicbg.spim.data.SpimDataException;
import net.preibisch.mvrecon.fiji.plugin.fusion.FusionGUI;

public class Clustering {

	public static void main(String[] args) throws SpimDataException, IOException, JSchException, SftpException {
		run("/Users/Marwan/Desktop/grid-3d-stitched-h5/dataset.xml");
	}

	public static void run(String input_path) throws SpimDataException, IOException, JSchException, SftpException {
		run(input_path, "output.n5");
	}

	public static void run(String input_path, String output_name)
			throws SpimDataException, IOException, JSchException, SftpException {
	}

	public static void run(FusionGUI gui) {
		System.out.println("Start clustering.. ");

		if (gui.getNonRigidParameters().isActive())
			ImplimentedTasks.NonRegid(gui);
		else
			ImplimentedTasks.fusion(gui);
	}
}
