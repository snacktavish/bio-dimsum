/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */


public class kernel {
  public static void setArrays(float[] sb_DATA, int sbx, int sby, float[] hb_DATA, int hbx, int hby) {
    kernelJNI.setArrays(sb_DATA, sbx, sby, hb_DATA, hbx, hby);
  }

  public static void migrateCPU(double[] children, int[] rm, double[] d, double[] paramd, long[] parami, int offset) {
    kernelJNI.migrateCPU(children, rm, d, paramd, parami, offset);
  }

}