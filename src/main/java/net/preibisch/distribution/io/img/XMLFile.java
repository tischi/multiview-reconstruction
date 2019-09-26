package net.preibisch.distribution.io.img;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOException;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.distribution.algorithm.controllers.items.DataExtension;
import net.preibisch.distribution.tools.Tools;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.XmlIoSpimData2;
import net.preibisch.mvrecon.fiji.spimdata.boundingbox.BoundingBox;
import net.preibisch.mvrecon.process.boundingbox.BoundingBoxBigDataViewer;
import net.preibisch.mvrecon.process.boundingbox.BoundingBoxEstimation;
import net.preibisch.mvrecon.process.boundingbox.BoundingBoxMaximal;
import net.preibisch.mvrecon.process.fusion.FusionTools;

public class XMLFile extends ImgFile {
	private final static String HDF5_FILE = "dataset.h5";

	private List<List<ViewId>> viewIds;
	private Interval bb;
	private SpimData2 spimData;
	private double downsampling;
	private List<File> relatedFiles;
	// private T dataType;

	public List<File> getRelatedFiles() {
		return relatedFiles;
	}

	public SpimData2 spimData() {
		return spimData;
	}

	public List<List<ViewId>> viewIds() {
		return viewIds;
	}

	public Interval bb() {
		return bb;
	}
	@Override
	public long[] getDimensions(int downsampling) {
		return new BoundingBox(bb).getDimensions(downsampling);
	}

	public static XMLFile XMLFile(String path) throws SpimDataException, IOException {
		System.out.println("XML default loader with only path .");
		return XMLFile(path, false);
	}

	public static XMLFile XMLFile(String path, boolean useBDV) throws SpimDataException, IOException {
		return XMLFile(path, Double.NaN, useBDV);
	}

	public static XMLFile XMLFile(String path, double downsampling, boolean useBDV)
			throws SpimDataException, IOException {
		File f = new File(path);
		if (!f.exists())
			throw new IOException("File not found! " + path);

		if (!DataExtension.XML.equals(DataExtension.fromURI(path)))
			throw new IIOException("Invalide input! " + path);
		System.out.println("File found! ");

		SpimData2 spimdata = new XmlIoSpimData2("").load(path);

		List<File> relatedFiles = initRelatedFiles(f);
		final List<ViewId> viewIds = new ArrayList<ViewId>();
		viewIds.addAll(spimdata.getSequenceDescription().getViewDescriptions().values());
		BoundingBox bbx = estimateBoundingBox(spimdata, viewIds, useBDV);
		int down;
		if (Double.isNaN(downsampling))
			down = 1;
		else
			down = (int) downsampling;
		return new XMLFile(path, bbx, spimdata, down, list(viewIds), relatedFiles);

	}

	public static XMLFile XMLFile(String path, Interval bb, double downsampling) throws IOException, SpimDataException {
		final List<ViewId> viewIds = new ArrayList<ViewId>();
		SpimData2 spimdata = new XmlIoSpimData2("").load(path);
		viewIds.addAll(spimdata.getSequenceDescription().getViewDescriptions().values());
		return XMLFile(path, bb,  downsampling, viewIds);
	}
	
	public static XMLFile XMLFile(String path, Interval bb, double downsampling, List<ViewId> viewIds) throws IOException, SpimDataException {

		System.out.println("XML specific info loader ");
		File f = new File(path);
		if (!f.exists())
			throw new IOException("File not found! " + path);

		if (!DataExtension.XML.equals(DataExtension.fromURI(path)))
			throw new IIOException("Invalide input! " + path);
		System.out.println("File found! ");

		SpimData2 spimdata = new XmlIoSpimData2("").load(path);

		List<File> relatedFiles = initRelatedFiles(f);

		return new XMLFile(path, bb, spimdata, downsampling,  list(viewIds), relatedFiles);
	}


//	public XMLFile(String path, BoundingBox bb, SpimData2 spimdata, int downsampling, List<ViewId> viewIds,
//			List<File> relatedFiles) {
//		this(path,bb,spimdata,downsampling,list(viewIds),relatedFiles);
//	}
	
	private static List<List<ViewId>> list(List<ViewId> viewIds) {
		List<List<ViewId>> l = new ArrayList<>();
		l.add(viewIds);
		return l;
	}

	public XMLFile(String path, Interval bb, SpimData2 spimdata, double downsampling, List<List<ViewId>> viewIds,
			List<File> relatedFiles) {
		super(path);

		this.viewIds = viewIds;
		this.bb = bb;
		this.downsampling = downsampling;
		this.spimData = spimdata;
		this.relatedFiles = relatedFiles;
		
		this.dims = new BoundingBox(bb).getDimensions((int)downsampling);

		// dataType = Util.getTypeFromInterval(fuse());
		System.out.println(toString());
	}



	public static List<File> initRelatedFiles(File f) throws IOException {
		List<File> files = new ArrayList<File>();
		File hdfFile = new File(f.getParent(), HDF5_FILE);
		if (!hdfFile.exists())
			throw new IOException("HDF5 file not exist: " + hdfFile.getAbsolutePath());
		files.add(hdfFile);
		return files;
	}

	@Override
	public String toString() {
		String related = "\n Related files: \n";
		for (File f : relatedFiles)
			related += f.getAbsolutePath() + "\n";

		return super.toString() + related;
	}

	private static BoundingBox estimateBoundingBox(SpimData2 spimdata, List<ViewId> viewIds, boolean useBDV) {
		BoundingBoxEstimation estimation;
		if (useBDV)
			estimation = new BoundingBoxBigDataViewer(spimdata, viewIds);
		else
			estimation = new BoundingBoxMaximal(viewIds, spimdata);

		return estimation.estimate("Full Bounding Box");
	}

	@Override
	public RandomAccessibleInterval<FloatType> fuse(int i) throws IOException {
		return FusionTools.fuseVirtual(spimData, viewIds.get(i), bb, downsampling).getA();
	}

	@Override
	public RandomAccessibleInterval<FloatType> fuse(Interval bb,int i) throws IOException {
		return FusionTools.fuseVirtual(spimData, viewIds.get(i), bb, downsampling).getA();
	}

	public static File fromStitchFolder(String inputPath) {
		return Tools.getXML(inputPath);
	}
	@Override
	public RandomAccessibleInterval<FloatType> fuse() throws IOException {
		return fuse(0);
	}

}