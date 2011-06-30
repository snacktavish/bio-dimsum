 /* DispersalFunctionC.i */
 %include "arrays_java.i"
%apply int[] {int*};
%apply float[] {float*};
%apply double[] {double*};
 %module DispersalFunctionC
 %{
 /* Put header files here or function declarations like below */
extern void setRandArray(double* _randArray);
extern void setArrays(float* _sb_DATA,int* _sb_META,int* _sb_SIZE,float* _hb_DATA,int* _hb_META,int* _hb_SIZE);
 extern void migrateLoop(double* children, int *rm, double *d, double* paramd, int* parami);
 %}
 extern void setRandArray(double* _randArray);
extern void setArrays(float* _sb_DATA,int* _sb_META,int* _sb_SIZE,float* _hb_DATA,int* _hb_META,int* _hb_SIZE);
 extern void migrateLoop(double* children, int *rm, double *d, double* paramd, int* parami);