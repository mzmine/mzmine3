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

package io.github.mzmine.datamodel;

/**
 * Represents the type of MS scan.
 *
 * See also K. K. Murray, R. K. Boyd, M. N. Eberlin, G. J. Langley, L. Li and Y. Naito, Definitions
 * of Terms Relating to Mass Spectrometry (IUPAC Recommendations 2013) Pure Appl. Chem. 2013, 85,
 * 1515-1609.
 */
public enum MsScanType {

  /**
   * Full MS scan.
   */
  FULLMS,

  /**
   * Fragmentation (tandem MS, or MS/MS) scan.
   */
  MSMS,

  /**
   * Single ion monitoring (SIM) scan. This scan isolates a narrow range of m/z values before
   * scanning, thus increasing the sensitivity in this range.
   */
  SIM,

  /**
   * Multiple Reaction Monitoring (MRM) or Selected Reaction Monitoring (SRM) scan.
   */
  MRM_SRM,

  /**
   * Data Independent Acquisition (DIA) scan.
   */
  DIA,

  /**
   * Unknown MS scan type.
   */
  UNKNOWN;
}
