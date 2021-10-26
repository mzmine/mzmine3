/*
 * Copyright 2006-2021 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package io.github.mzmine.datamodel.impl;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.Frame;
import io.github.mzmine.datamodel.IMSRawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.datamodel.msms.ActivationMethod;
import io.github.mzmine.datamodel.msms.MsMsInfo;
import io.github.mzmine.datamodel.msms.PasefMsMsInfo;
import io.github.mzmine.util.ParsingUtils;
import java.util.Objects;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jetbrains.annotations.Nullable;

/**
 * @author https://github.com/SteffenHeu
 * @see PasefMsMsInfo
 */
public class PasefMsMsInfoImpl implements PasefMsMsInfo {

  public static final String XML_TYPE_NAME = "pasefmsmsinfo";

  private final double precursorMz;
  private final Range<Integer> spectrumNumberRange;
  private final Float collisionEnergy;
  private final Integer precursorCharge;
  private Frame parentFrame;
  private Frame fragmentFrame;

  public PasefMsMsInfoImpl(double precursorMz, Range<Integer> spectrumNumberRange,
      @Nullable Float collisionEnergy, @Nullable Integer precursorCharge,
      @Nullable Frame parentScan, @Nullable Frame fragmentFrameNumber) {

    if (parentScan != null && parentScan.getMSLevel() != 1) {
      throw new IllegalArgumentException("Parent frame is not of ms level 1");
    }
    if (fragmentFrameNumber != null && fragmentFrameNumber.getMSLevel() < 2) {
      throw new IllegalArgumentException("Fragment frame is not of ms level >= 2");
    }

    this.precursorMz = precursorMz;
    this.spectrumNumberRange = spectrumNumberRange;
    this.collisionEnergy = collisionEnergy;
    this.precursorCharge = precursorCharge;
    this.parentFrame = parentScan;
    this.fragmentFrame = fragmentFrameNumber;
  }

  @Override
  public @Nullable Scan getParentScan() {
    return getParentFrame();
  }

  @Override
  public @Nullable Scan getMsMsScan() {
    return getMsMsFrame();
  }

  @Override
  public Frame getMsMsFrame() {
    return fragmentFrame;
  }

  @Override
  public Frame getParentFrame() {
    return parentFrame;
  }

  @Override
  public double getIsolationMz() {
    return precursorMz;
  }

  @Override
  public Range<Integer> getSpectrumNumberRange() {
    return spectrumNumberRange;
  }

  @Override
  public Float getActivationEnergy() {
    return collisionEnergy;
  }

  @Override
  public int getMsLevel() {
    return 2;
  }

  @Override
  public @Nullable ActivationMethod getActivationMethod() {
    return ActivationMethod.CID;
  }

  @Override
  public Integer getPrecursorCharge() {
    return precursorCharge;
  }

  @Override
  public boolean setMsMsScan(Scan scan) {
    if (!(scan instanceof Frame frame)) {
      return false;
    }
    if (fragmentFrame != null && !fragmentFrame.equals(scan)) {
      return false;
    }
    fragmentFrame = frame;
    return true;
  }

  @Override
  public String toString() {
    return "m/z " + precursorMz + " - Scans " + spectrumNumberRange.toString();
  }

  /**
   * Appends a new element for an {@link PasefMsMsInfoImpl} at the current position. Start and close
   * tag for this {@link PasefMsMsInfoImpl} are created in this method.
   *
   * @param writer The writer to use.
   */
  @Override
  public void writeToXML(XMLStreamWriter writer) throws XMLStreamException {
    writer.writeStartElement(XML_ELEMENT);
    writer.writeAttribute(XML_TYPE_ATTRIBUTE, XML_TYPE_NAME);
    writer.writeAttribute(XML_PRECURSOR_MZ_ATTR, String.valueOf(getIsolationMz()));

    if (getPrecursorCharge() != null) {
      writer.writeAttribute(XML_PRECURSOR_CHARGE_ATTR, String.valueOf(getPrecursorCharge()));
    }

    // todo nullable
    if (fragmentFrame != null) {
      writer.writeAttribute(XML_FRAGMENT_SCAN_ATTR,
          String.valueOf(fragmentFrame.getDataFile().getScans().indexOf(fragmentFrame)));
    }

    if (parentFrame != null) {
      writer.writeAttribute(XML_PARENT_SCAN_ATTR,
          String.valueOf(getParentFrame().getDataFile().getScans().indexOf(getParentFrame())));
    }

    if (getActivationEnergy() != null) {
      writer.writeAttribute(XML_ACTIVATION_ENERGY_ATTR, String.valueOf(this.getActivationEnergy()));
    }

    writer.writeAttribute(XML_SPECTRUM_NUMBER_RANGE_ATTR,
        ParsingUtils.rangeToString((Range) getSpectrumNumberRange()));
    writer.writeEndElement();
  }

  /**
   * @param reader A reader at an {@link PasefMsMsInfoImpl} element.
   * @return A loaded {@link PasefMsMsInfoImpl}.
   */
  public static PasefMsMsInfoImpl loadFromXML(XMLStreamReader reader, IMSRawDataFile file) {
    if (!reader.isStartElement() && reader.getAttributeValue(null, XML_TYPE_ATTRIBUTE)
        .equals(XML_TYPE_NAME)) {
      throw new IllegalStateException("Wrong msms info type.");
    }

    final double precursorMz = Double.parseDouble(
        reader.getAttributeValue(null, XML_PRECURSOR_MZ_ATTR));
    final Integer precursorCharge =
        reader.getAttributeValue(null, XML_PRECURSOR_CHARGE_ATTR) != null ? Integer.parseInt(
            reader.getAttributeValue(null, XML_PRECURSOR_CHARGE_ATTR)) : null;

    final Integer frameIndex =
        reader.getAttributeValue(null, XML_FRAGMENT_SCAN_ATTR) != null ? Integer.parseInt(
            reader.getAttributeValue(null, XML_FRAGMENT_SCAN_ATTR)) : null;

    final Integer parentFrameIndex =
        reader.getAttributeValue(null, XML_PARENT_SCAN_ATTR) != null ? Integer.parseInt(
            reader.getAttributeValue(null, XML_PARENT_SCAN_ATTR)) : null;

    final Float collisionEnergy =
        reader.getAttributeValue(null, XML_ACTIVATION_ENERGY_ATTR) != null ? Float.parseFloat(
            reader.getAttributeValue(null, XML_ACTIVATION_ENERGY_ATTR)) : null;

    Range<Integer> spectrumRange = ParsingUtils.parseIntegerRange(
        reader.getAttributeValue(null, XML_SPECTRUM_NUMBER_RANGE_ATTR));

    return new PasefMsMsInfoImpl(precursorMz, spectrumRange, collisionEnergy, precursorCharge,
        parentFrameIndex != null ? file.getFrame(parentFrameIndex) : null,
        frameIndex != null ? file.getFrame(frameIndex) : null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PasefMsMsInfoImpl)) {
      return false;
    }
    PasefMsMsInfoImpl that = (PasefMsMsInfoImpl) o;
    return Objects.equals(precursorMz, that.precursorMz) && Objects.equals(getSpectrumNumberRange(),
        that.getSpectrumNumberRange()) && Objects.equals(this.getActivationEnergy(),
        that.getActivationEnergy()) && Objects.equals(getPrecursorCharge(),
        that.getPrecursorCharge()) && Objects.equals(getParentFrame(), that.getParentFrame())
        && Objects.equals(fragmentFrame, that.fragmentFrame);
  }

  @Override
  public int hashCode() {
    return Objects.hash(precursorMz, getSpectrumNumberRange(), this.getActivationEnergy(),
        getPrecursorCharge(), getParentFrame(), fragmentFrame);
  }

  @Override
  public MsMsInfo createCopy() {
    return new PasefMsMsInfoImpl(precursorMz, spectrumNumberRange, collisionEnergy, precursorCharge,
        parentFrame, null);
  }
}
