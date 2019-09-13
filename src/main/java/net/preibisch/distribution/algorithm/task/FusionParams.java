package net.preibisch.distribution.algorithm.task;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;

public class FusionParams implements Params {
	private String xml;
	private List< ? extends ViewId > viewIds;
	private Interval bb;
	private double downsampling;
	
//	spimData = new XmlIoSpimData2( "" ).load( "/Users/spreibi/Documents/Microscopy/SPIM/HisYFP-SPIM/dataset.xml" );
	
	
	public void toJson(String path) throws JsonIOException, IOException {
		Gson gson = new Gson();

		gson.toJson(this, new FileWriter(path));

	}

	public FusionParams(String xml, List<? extends ViewId> viewIds, Interval bb, double downsampling) {
		super();
		this.xml = xml;
		this.viewIds = viewIds;
		this.bb = bb;
		this.downsampling = downsampling;
	}

	public FusionParams fromJson(String path) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Gson gson = new Gson();

		FusionParams params = gson.fromJson(new FileReader(path), FusionParams.class);
		return params;
	}
}
