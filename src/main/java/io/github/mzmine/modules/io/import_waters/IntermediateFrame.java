/*
 * Copyright 2006-2021 The MZmine Development Team
 * This file is part of MZmine.
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package io.github.mzmine.modules.io.import_waters;

import MassLynxSDK.MassLynxIonMode;
import MassLynxSDK.MassLynxRawScanReader;
import MassLynxSDK.MasslynxRawException;
import MassLynxSDK.Scan;
import com.google.common.collect.Range;
import io.github.mzmine.datamodel.MassSpectrumType;
import io.github.mzmine.datamodel.MobilityType;
import io.github.mzmine.datamodel.PolarityType;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.impl.SimpleFrame;
import net.csibio.aird.util.ArrayUtil;

public class IntermediateFrame extends IntermediateScan{
  private int driftscancount;

  public int getDriftscancount() {
    return driftscancount;
  }

  public IntermediateFrame(RawDataFile newMZmineFile, boolean iscontinuum, int mslevel,
      MassLynxIonMode ionmode, Range<Double> MZRange, int function_number, float retentionTime,int driftscancount) {
    super(newMZmineFile, iscontinuum, mslevel, ionmode, MZRange, function_number, retentionTime,driftscancount);
    this.driftscancount=driftscancount;
  }

  public SimpleFrame toframe(int numscan, MassLynxRawScanReader rawscanreader)
      throws MasslynxRawException {
    //scan Value
    Scan scan = rawscanreader.ReadScan(this.getFunction_number(),this.driftscancount);

    PolarityType polarity = PolarityType.UNKNOWN;

    MassSpectrumType spectrumType=this.isIscontinuum()?MassSpectrumType.PROFILE:MassSpectrumType.CENTROIDED;

    polarity= this.getIonmode()==MassLynxIonMode.ES_POS? PolarityType.POSITIVE:PolarityType.NEGATIVE;

    SimpleFrame simpleframe=new SimpleFrame(this.getNewMZmineFile(),numscan,this.getMslevel()
        ,this.getRetentionTime(),ArrayUtil.fromFloatToDouble(scan.GetMasses()),ArrayUtil.fromFloatToDouble(scan.GetIntensities()),spectrumType,polarity,"",
        this.getMZRange(), MobilityType.TRAVELING_WAVE,null,0f);

    return simpleframe;
  }

}
