/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */


public class DispersalFunctionC {
  public static void setRandArray(double[] _randArray) {
    DispersalFunctionCJNI.setRandArray(_randArray);
  }

  public static void setArrays(float[] _sb_DATA, int[] _sb_META, int[] _sb_SIZE, float[] _hb_DATA, int[] _hb_META, int[] _hb_SIZE) {
    DispersalFunctionCJNI.setArrays(_sb_DATA, _sb_META, _sb_SIZE, _hb_DATA, _hb_META, _hb_SIZE);
  }

  public static void migrateLoop(double[] children, int[] rm, double[] d, double[] paramd, int[] parami) {
    DispersalFunctionCJNI.migrateLoop(children, rm, d, paramd, parami);
  }

}
