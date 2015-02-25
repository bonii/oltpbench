/***************************************************************************
 *  Copyright (C) 2012 by H-Store Project                                  *
 *  Brown University                                                       *
 *  Massachusetts Institute of Technology                                  *
 *  Yale University                                                        *
 *                                                                         *
 *  Original By: VoltDB Inc.											   *
 *  Ported By:  Justin A. DeBrabant (http://www.cs.brown.edu/~debrabant/)  *                                                                      *
 *                                                                         *
 *  Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the        *
 *  "Software"), to deal in the Software without restriction, including    *
 *  without limitation the rights to use, copy, modify, merge, publish,    *
 *  distribute, sublicense, and/or sell copies of the Software, and to     *
 *  permit persons to whom the Software is furnished to do so, subject to  *
 *  the following conditions:                                              *
 *                                                                         *
 *  The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.        *
 *                                                                         *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF     *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. *
 *  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR      *
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  *
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR  *
 *  OTHER DEALINGS IN THE SOFTWARE.                                        *
 ***************************************************************************/

package com.oltpbenchmark.benchmarks.galaxy;

/**
 * A class containing all the constant values used by the benchmark
 */
public abstract class GalaxyConstants {

  public static final int NUM_CLASSES = 5;
  public static final int NUM_SOLARSYSTEMS = 6;
  public static final int MAX_MOVE = 110000;
  public static final int NUM_SHIPS = 1000;

  public static final String TABLENAME_SHIPS = "ships";
  public static final String TABLENAME_CLASSES = "classes";
  public static final String TABLENAME_SOLARSYSTEMS = "solarsystems";

  // Initialize some common constants and variables
  public static final String CLASSES_CSV = "Missile boat, Torpedo boat, Minesweeper," +
      "Medium landing craft, Landing Ship Tank";


  // Ship classes data
  public static final String[] classes = new String[] {
    "Missile boat", "Torpedo boat", "Minesweeper", "Medium landing craft", "Landing Ship Tank" };

  public static final int[] reachability = {200, 450, 900, 3000, 1200};

  // Solarsystem size data
  public static final int[] x_max = {100000, 20000, 35000, 10000, 88000, 50000};
  public static final int[] y_max = {100000, 15000, 15000, 80000, 20000, 40000};

}
