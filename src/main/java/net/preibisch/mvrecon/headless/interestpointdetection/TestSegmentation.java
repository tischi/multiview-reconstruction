/*-
 * #%L
 * Software for the reconstruction of multi-view microscopic acquisitions
 * like Selective Plane Illumination Microscopy (SPIM) Data.
 * %%
 * Copyright (C) 2012 - 2017 Multiview Reconstruction developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package net.preibisch.mvrecon.headless.interestpointdetection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mpicbg.spim.data.sequence.ViewDescription;
import mpicbg.spim.data.sequence.ViewId;
import net.preibisch.legacy.io.IOFunctions;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPoint;
import net.preibisch.mvrecon.process.interestpointdetection.InterestPointTools;
import net.preibisch.mvrecon.process.interestpointdetection.methods.dog.DoG;
import net.preibisch.mvrecon.process.interestpointdetection.methods.dog.DoGParameters;
import net.preibisch.mvrecon.process.interestpointdetection.methods.dom.DoM;
import net.preibisch.mvrecon.process.interestpointdetection.methods.dom.DoMParameters;
import net.preibisch.simulation.imgloader.SimulatedBeadsImgLoader;

public class TestSegmentation
{
	public static void testDoG( SpimData2 spimData )
	{
		DoGParameters dog = new DoGParameters();

		dog.imgloader = spimData.getSequenceDescription().getImgLoader();
		dog.toProcess = new ArrayList< ViewDescription >();
		dog.toProcess.addAll( spimData.getSequenceDescription().getViewDescriptions().values() );

		// filter not present ViewIds
		final List< ViewDescription > removed = SpimData2.filterMissingViews( spimData, dog.toProcess );
		IOFunctions.println( new Date( System.currentTimeMillis() ) + ": Removed " +  removed.size() + " views because they are not present." );

		dog.downsampleXY = 4;
		dog.downsampleZ = 2;
		dog.sigma = 1.1;

		//dog.deviceList = spim.headless.cuda.CUDADevice.getSeparableCudaList( "lib/libSeparableConvolutionCUDALib.so" );
		//dog.cuda = spim.headless.cuda.CUDADevice.separableConvolution;

		//		DoG.findInterestPoints( dog );
		// TODO: make cuda headless
		//dog.deviceList = spim.headless.cuda.CUDADevice.getSeparableCudaList( "lib/libSeparableConvolutionCUDALib.so" );
		//dog.cuda = spim.headless.cuda.CUDADevice.separableConvolution;

		final HashMap< ViewId, List< InterestPoint > > points = DoG.findInterestPoints( dog );

		InterestPointTools.addInterestPoints( spimData, "beads", points, "DoG, sigma=1.4, downsample=2" );
	}

	public static void testDoM( final SpimData2 spimData )
	{
		DoMParameters dom = new DoMParameters();
		
		dom.imgloader = spimData.getSequenceDescription().getImgLoader();
		dom.toProcess = new ArrayList< ViewDescription >();
		dom.toProcess.addAll( spimData.getSequenceDescription().getViewDescriptions().values() );

		// filter not present ViewIds
		final List< ViewDescription > removed = SpimData2.filterMissingViews( spimData, dom.toProcess );
		IOFunctions.println( new Date( System.currentTimeMillis() ) + ": Removed " +  removed.size() + " views because they are not present." );

		dom.downsampleXY = 2;
		dom.radius1 = 2;
		
		final HashMap< ViewId, List< InterestPoint > > points = DoM.findInterestPoints( dom );
		
		InterestPointTools.addInterestPoints( spimData, "beads", points, "DoM, sigma=2, downsample=2" );
	}

	public static void main( String[] args )
	{
		// generate 4 views with 1000 corresponding beads, single timepoint
		SpimData2 spimData = SpimData2.convert( SimulatedBeadsImgLoader.spimdataExample() );

		testDoG( spimData );
	}

}
