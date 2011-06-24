 /* DispersalFunctionC.i */
 %include "arrays_java.i"
%apply int[] {int*};
%apply float[] {float*};
%apply double[] {double*};
 %module DispersalFunctionC
 %{
 /* Put header files here or function declarations like below */
extern void setArrays(double* _randArray,float* _sb_DATA,int* _sb_META,int* _sb_SIZE,float* _hb_DATA,int* _hb_META,int* _hb_SIZE);
 extern void migrate(double* node, double d, int* rm, int* parami, double* paramv, int id);
 %}
 
extern void setArrays(double* _randArray,float* _sb_DATA,int* _sb_META,int* _sb_SIZE,float* _hb_DATA,int* _hb_META,int* _hb_SIZE);
 extern void migrate(double* node, double d, int* rm, int* parami, double* paramv, int id);