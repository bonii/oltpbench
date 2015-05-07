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

    // Ships
    public static final int NUM_SHIPS = 1000;
    public static final int VISIBLE_RANGE = 250000;
    
    // Solar systems
    public static final long AU = 149600000000L; // According to wolfram
    public static final long MIN_SYSTEM_SIZE = 10 * AU;// TODO * AU;
    public static final long MAX_SYSTEM_SIZE = 100 * AU;// TODO * AU;
    public static final int MIN_SECURITY = -10;
    public static final int MAX_SECURITY = 10;
    public static final int NUM_SOLAR_SYSTEMS = 1;
    
    // Classes
    public static final int NUM_CLASSES = 5;
    public static final int MIN_REACHABILITY = 100;
    public static final int MAX_REACHABILITY = 1000;
    public static final int MIN_HEALTH = 1000;
    public static final int MAX_HEALTH = 100000;
    public static final int MIN_FITTINGS = 1;
    public static final int MAX_FITTINGS = 10;
    // TODO random names maybe?
    public static final String[] classes = new String[] {
        "Missile boat", "Torpedo boat", "Minesweeper", 
        "Medium landing craft", "Landing Ship Tank" };
    
    // Fittings
    public static final int NUM_FITTING = 50;
    public static final int NUM_FITTING_TYPES = 2;
    public static final int FITTING_TYPE_OFFENSIVE = 0;
    public static final int FITTING_TYPE_DEFENSIVE = 1;
    public static final int MAX_FITTING_VALUE = 1000;

    // Runtime variables
    public static final int MAX_MOVE = 110000; 
    
    // Table names
    public static final String TABLENAME_SHIPS = "ships";
    public static final String TABLENAME_CLASSES = "classes";
    public static final String TABLENAME_SOLARSYSTEMS = "solar_systems";
    public static final String TABLENAME_FITTING = "fitting";
    public static final String TABLENAME_FITTINGS = "fittings";

}
