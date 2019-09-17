package net.preibisch.distribution.algorithm.task;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;
import net.preibisch.distribution.algorithm.controllers.items.ViewIdMD;
import net.preibisch.distribution.io.GsonIO;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.XmlIoSpimData2;

public class FusionParams implements Params {
	private String xml;
	private List<? extends ViewIdMD> viewIds;
	private Interval bb;
	private double downsampling;

	public void toJson(String path)  {
		if (Double.isNaN(this.downsampling))
			this.downsampling = 0;
		System.out.println(this.toString());
		GsonIO.toJson(this, path);

	}

	public FusionParams(String xml, List<? extends ViewId> viewIds, Interval bb, double downsampling) {
		super();
		this.xml = xml;
		this.viewIds = viewsID(viewIds);
		this.bb = bb;
		this.downsampling = downsampling;
	}

	private List<? extends ViewIdMD> viewsID(List<? extends ViewId> viewIds) {
		List<ViewIdMD> ids = new ArrayList<ViewIdMD>();
		for(ViewId id: viewIds)
			ids.add(new ViewIdMD(id));
		return ids;
	}

	public FusionParams fromJson(String path) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Gson gson = new Gson();

		FusionParams params = gson.fromJson(new FileReader(path), FusionParams.class);
		if (params.getDownsampling() == 0)
			params.setDownsampling(Double.NaN);
		return params;
	}
	
	public SpimData2 getSpim() throws SpimDataException {
		return new XmlIoSpimData2( "" ).load(xml);
	}
	
	@Override
	public String toString() {
		return "xml: "+xml+" | views: "+viewIds.size()+" | bb: "+bb+" | downsampling: "+downsampling;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public Interval getBb() {
		return bb;
	}

	public void setBb(Interval bb) {
		this.bb = bb;
	}

	public double getDownsampling() {
		return downsampling;
	}

	public void setDownsampling(double downsampling) {
		this.downsampling = downsampling;
	}

}
