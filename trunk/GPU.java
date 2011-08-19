import static jcuda.driver.CUaddress_mode.CU_TR_ADDRESS_MODE_CLAMP;
import static jcuda.driver.CUarray_format.CU_AD_FORMAT_FLOAT;
import static jcuda.driver.CUfilter_mode.CU_TR_FILTER_MODE_POINT;
import static jcuda.driver.JCudaDriver.CU_TRSA_OVERRIDE_FORMAT;
import static jcuda.driver.JCudaDriver.cuArrayCreate;
import static jcuda.driver.JCudaDriver.cuMemFree;
import static jcuda.driver.JCudaDriver.cuMemcpy2D;
import static jcuda.driver.JCudaDriver.cuModuleGetTexRef;
import static jcuda.driver.JCudaDriver.cuTexRefSetAddressMode;
import static jcuda.driver.JCudaDriver.cuTexRefSetArray;
import static jcuda.driver.JCudaDriver.cuTexRefSetFilterMode;
import static jcuda.driver.JCudaDriver.cuTexRefSetFormat;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUDA_ARRAY_DESCRIPTOR;
import jcuda.driver.CUDA_MEMCPY2D;
import jcuda.driver.CUarray;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmemorytype;
import jcuda.driver.CUmodule;
import jcuda.driver.CUtexref;
import jcuda.driver.JCudaDriver;


public class GPU {
	CUmodule module;
	CUfunction function;
	int block_size =256;
	CUarray sb_DEV;
	CUarray hb_DEV;
	
	
	public GPU() {	
		JCudaDriver.setExceptionsEnabled(true);

        JCudaDriver.cuInit(0);
        CUcontext context = new CUcontext();
        CUdevice device = new CUdevice();
        JCudaDriver.cuDeviceGet(device, 0);
        JCudaDriver.cuCtxCreate(context, 0, device);
        module = new CUmodule();
        JCudaDriver.cuModuleLoad(module, "kernel.cubin");
        function = new CUfunction();
        JCudaDriver.cuModuleGetFunction(function, module, "migrateGPU");
	}

	public void cpBorders2GPU(XYFunction soft, XYFunction hard) {
		sb_DEV=cp2gpu(soft.getF(),"softborderDATA");
		hb_DEV=cp2gpu(hard.getF(),"hardborderDATA");
	}
	
	
	@SuppressWarnings("deprecation")
	public void migrate(double[] children, int[] rm, double[] d, double[] paramd, long[] parami) {
        CUdeviceptr childrenDevice = new CUdeviceptr();
        JCudaDriver.cuMemAlloc(childrenDevice, Sizeof.DOUBLE*children.length);
        JCudaDriver.cuMemcpyHtoD(childrenDevice, Pointer.to(children), Sizeof.DOUBLE*children.length);
        
        CUdeviceptr rmDevice = new CUdeviceptr();
        JCudaDriver.cuMemAlloc(rmDevice, Sizeof.INT*rm.length);
        JCudaDriver.cuMemcpyHtoD(rmDevice, Pointer.to(rm), Sizeof.INT*rm.length);
        
        CUdeviceptr dDevice = new CUdeviceptr();
        JCudaDriver.cuMemAlloc(dDevice, Sizeof.DOUBLE*d.length);
        JCudaDriver.cuMemcpyHtoD(dDevice, Pointer.to(d), Sizeof.DOUBLE*d.length);
        
        CUdeviceptr paramdDevice = new CUdeviceptr();
        JCudaDriver.cuMemAlloc(paramdDevice, Sizeof.DOUBLE*paramd.length);
        JCudaDriver.cuMemcpyHtoD(paramdDevice, Pointer.to(paramd), Sizeof.DOUBLE*paramd.length);
        
        CUdeviceptr paramiDevice = new CUdeviceptr();
        JCudaDriver.cuMemAlloc(paramiDevice, Sizeof.LONG*parami.length);
        JCudaDriver.cuMemcpyHtoD(paramiDevice, Pointer.to(parami), Sizeof.LONG*parami.length);
        

        int numThread = (int)Math.ceil((double)rm.length/(double)block_size);

        
        int offset = 0;
        JCudaDriver.cuParamSetSize(function, Sizeof.POINTER*5+Sizeof.INT);
        JCudaDriver.cuParamSetv(function, offset, Pointer.to(childrenDevice), Sizeof.POINTER);
        offset += Sizeof.POINTER;
        JCudaDriver.cuParamSetv(function, offset, Pointer.to(rmDevice), Sizeof.POINTER);
        offset += Sizeof.POINTER;
        JCudaDriver.cuParamSetv(function, offset, Pointer.to(dDevice), Sizeof.POINTER);
        offset += Sizeof.POINTER;
        JCudaDriver.cuParamSetv(function, offset, Pointer.to(paramdDevice), Sizeof.POINTER);
        offset += Sizeof.POINTER;
        JCudaDriver.cuParamSetv(function, offset, Pointer.to(paramiDevice), Sizeof.POINTER);
        offset += Sizeof.POINTER;
        JCudaDriver.cuFuncSetBlockShape(function, block_size, 1, 1);
        
    	JCudaDriver.cuParamSeti(function, offset, 0);
    	JCudaDriver.cuLaunchGrid(function, numThread,1);
    	JCudaDriver.cuCtxSynchronize();

        JCudaDriver.cuMemcpyDtoH(Pointer.to(children),childrenDevice,Sizeof.DOUBLE*children.length);
        cuMemFree(paramiDevice);
        cuMemFree(paramdDevice);
        cuMemFree(dDevice);
        cuMemFree(rmDevice);
        cuMemFree(childrenDevice);   
}
	

	public  void updateGPU(CUarray array, FloatArray2D src ){
		int sizeX = src.size()[0];
		int sizeY = src.size()[1];
		CUDA_MEMCPY2D copyHD = new CUDA_MEMCPY2D();
        copyHD.srcMemoryType = CUmemorytype.CU_MEMORYTYPE_HOST;
        copyHD.srcHost = Pointer.to(src._data);
        copyHD.srcPitch = sizeX * Sizeof.FLOAT;
        copyHD.dstMemoryType = CUmemorytype.CU_MEMORYTYPE_ARRAY;
        copyHD.dstArray = array;
        copyHD.WidthInBytes = sizeX * Sizeof.FLOAT;
        copyHD.Height = sizeY;
        cuMemcpy2D(copyHD);
	}
	
	
	private CUarray cp2gpu(FloatArray2D src, String texName) {
		
		int sizeX = src.size()[0];
		int sizeY = src.size()[1];
		CUarray array = new CUarray();
        CUDA_ARRAY_DESCRIPTOR ad = new CUDA_ARRAY_DESCRIPTOR();
        ad.Format = CU_AD_FORMAT_FLOAT;
        ad.Width = sizeX;
        ad.Height = sizeY;
        ad.NumChannels = 1;
        cuArrayCreate(array, ad);

        CUDA_MEMCPY2D copyHD = new CUDA_MEMCPY2D();
        copyHD.srcMemoryType = CUmemorytype.CU_MEMORYTYPE_HOST;
        copyHD.srcHost = Pointer.to(src._data);
        copyHD.srcPitch = sizeX * Sizeof.FLOAT;
        copyHD.dstMemoryType = CUmemorytype.CU_MEMORYTYPE_ARRAY;
        copyHD.dstArray = array;
        copyHD.WidthInBytes = sizeX * Sizeof.FLOAT;
        copyHD.Height = sizeY;
        cuMemcpy2D(copyHD);

        CUtexref texref = new CUtexref();
        cuModuleGetTexRef(texref, module, texName);
        cuTexRefSetFilterMode(texref, CU_TR_FILTER_MODE_POINT);
        cuTexRefSetAddressMode(texref, 0, CU_TR_ADDRESS_MODE_CLAMP);
        cuTexRefSetAddressMode(texref, 1, CU_TR_ADDRESS_MODE_CLAMP);
        //cuTexRefSetFlags(texref, CU_TRSF_NORMALIZED_COORDINATES);
        cuTexRefSetFormat(texref, CU_AD_FORMAT_FLOAT, 1);
        cuTexRefSetArray(texref, array, CU_TRSA_OVERRIDE_FORMAT);
        return array;
	}
}
