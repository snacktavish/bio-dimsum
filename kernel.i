 /* kernel.i */
 %include "arrays_java.i"
%apply int[] {int*};
%apply long[] {long*};
%apply long long[] {long long*};
%apply float[] {float*};
%apply double[] {double*};
 %module kernel
 %{
 /* Put header files here or function declarations like below */
extern void setArrays(float* sb_DATA, int sbx, int sby, float* hb_DATA, int hbx, int hby);
extern void migrateCPU(double* children, int* rm, double* d, double* paramd, long long* parami, int offset);
 %}
extern void setArrays(float* sb_DATA, int sbx, int sby, float* hb_DATA, int hbx, int hby);
extern void migrateCPU(double* children, int* rm, double* d, double* paramd, long long* parami, int offset);
