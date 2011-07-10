import static jcuda.driver.CUaddress_mode.CU_TR_ADDRESS_MODE_CLAMP;
import static jcuda.driver.CUarray_format.CU_AD_FORMAT_FLOAT;
import static jcuda.driver.CUarray_format.CU_AD_FORMAT_SIGNED_INT32;
import static jcuda.driver.CUfilter_mode.CU_TR_FILTER_MODE_POINT;
import static jcuda.driver.JCudaDriver.CU_TRSA_OVERRIDE_FORMAT;
import static jcuda.driver.JCudaDriver.cuArray3DCreate;
import static jcuda.driver.JCudaDriver.cuArrayCreate;
import static jcuda.driver.JCudaDriver.cuArrayDestroy;
import static jcuda.driver.JCudaDriver.cuMemFree;
import static jcuda.driver.JCudaDriver.cuMemcpy2D;
import static jcuda.driver.JCudaDriver.cuMemcpy3D;
import static jcuda.driver.JCudaDriver.cuMemcpyHtoA;
import static jcuda.driver.JCudaDriver.cuModuleGetTexRef;
import static jcuda.driver.JCudaDriver.cuTexRefSetAddressMode;
import static jcuda.driver.JCudaDriver.cuTexRefSetArray;
import static jcuda.driver.JCudaDriver.cuTexRefSetFilterMode;
import static jcuda.driver.JCudaDriver.cuTexRefSetFormat;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUDA_ARRAY3D_DESCRIPTOR;
import jcuda.driver.CUDA_ARRAY_DESCRIPTOR;
import jcuda.driver.CUDA_MEMCPY2D;
import jcuda.driver.CUDA_MEMCPY3D;
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
	int block_size =4;
	
	
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
	
	public CUarray cpRandArray2GPU(float[] rand) {
		System.out.println("CP RAND START");
		CUarray tmp =  cp2gpu(rand,"randArray");
		System.out.println("CP RAND FINISH");
		return tmp;
	}
	
	public void rmRandArray(CUarray array) {
		cuArrayDestroy(array);
	}
	
	public  void updaterand(CUarray array, float[] src ){
		Pointer pInput = Pointer.to(src);
        cuMemcpyHtoA(array, 0, pInput, src.length * Sizeof.FLOAT);
	}
	
	public void cpBorders2GPU(XYFunction soft, XYFunction hard) {
		System.out.println("CP BORDER START");
		cp2gpu(soft._f,"softborderDATA");
		cp2gpu(hard._f,"hardborderDATA");
		cp2gpu(soft._size_gen,"softborderMETA");
		cp2gpu(hard._size_gen,"hardborderMETA");
		System.out.println("CP BORDER FINISH");
		//cp2gpu(soft._f.size(), "softborderSIZE");
		//cp2gpu(hard._f.size(), "hardborderSIZE");
	}
	
	
	public void migrate(double[] children, int[] rm, double[] d, double[] paramd, int[] parami) {
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
        JCudaDriver.cuMemAlloc(paramiDevice, Sizeof.INT*parami.length);
        JCudaDriver.cuMemcpyHtoD(paramiDevice, Pointer.to(parami), Sizeof.INT*parami.length);
        
        int offset = 0;
        JCudaDriver.cuParamSetSize(function, Sizeof.POINTER*5);
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
        JCudaDriver.cuLaunchGrid(function, rm.length/block_size,1);
        JCudaDriver.cuCtxSynchronize();
        
        JCudaDriver.cuMemcpyDtoH(Pointer.to(children),childrenDevice,Sizeof.DOUBLE*children.length);
        cuMemFree(paramiDevice);
        cuMemFree(paramdDevice);
        cuMemFree(dDevice);
        cuMemFree(rmDevice);
        cuMemFree(childrenDevice);   
}
	
	
	private CUarray cp2gpu(int[] src, String texName) {
		int sizeX = src.length;
		CUarray array = new CUarray();
        CUDA_ARRAY_DESCRIPTOR ad = new CUDA_ARRAY_DESCRIPTOR();
        ad.Format = CU_AD_FORMAT_SIGNED_INT32;
        ad.Width = sizeX;
        ad.Height = 1;
        ad.NumChannels = 1;
        cuArrayCreate(array, ad);

        Pointer pInput = Pointer.to(src);
        cuMemcpyHtoA(array, 0, pInput, sizeX * Sizeof.INT);

        CUtexref texref = new CUtexref();
        cuModuleGetTexRef(texref, module, texName);
        cuTexRefSetFilterMode(texref, CU_TR_FILTER_MODE_POINT);
        cuTexRefSetAddressMode(texref, 0, CU_TR_ADDRESS_MODE_CLAMP);
        //cuTexRefSetFlags(texref, CU_TRSF_NORMALIZED_COORDINATES);
        cuTexRefSetFormat(texref, CU_AD_FORMAT_SIGNED_INT32, 1);
        cuTexRefSetArray(texref, array, CU_TRSA_OVERRIDE_FORMAT);
        return array;
	}
	
	private CUarray cp2gpu(float[] src, String texName) {
		System.out.println(src.length);
		int sizeX = src.length;
		CUarray array = new CUarray();
        CUDA_ARRAY_DESCRIPTOR ad = new CUDA_ARRAY_DESCRIPTOR();
        ad.Format = CU_AD_FORMAT_FLOAT;
        ad.Width = sizeX;
        ad.Height = 1;
        ad.NumChannels = 1;
        cuArrayCreate(array, ad);

        Pointer pInput = Pointer.to(src);
        cuMemcpyHtoA(array, 0, pInput, sizeX * Sizeof.FLOAT);

        CUtexref texref = new CUtexref();
        cuModuleGetTexRef(texref, module, texName);
        cuTexRefSetFilterMode(texref, CU_TR_FILTER_MODE_POINT);
        cuTexRefSetAddressMode(texref, 0, CU_TR_ADDRESS_MODE_CLAMP);
        //cuTexRefSetFlags(texref, CU_TRSF_NORMALIZED_COORDINATES);
        cuTexRefSetFormat(texref, CU_AD_FORMAT_FLOAT, 1);
        cuTexRefSetArray(texref, array, CU_TRSA_OVERRIDE_FORMAT);
        return array;
	}

	private void cp2gpu(FloatArray3D src, String texName) {
		int sizeX = src.size()[0];
		int sizeY = src.size()[1];
		int sizeZ = src.size()[2];
		
		CUarray array = new CUarray();
        CUDA_ARRAY3D_DESCRIPTOR ad = new CUDA_ARRAY3D_DESCRIPTOR();
        ad.Format = CU_AD_FORMAT_FLOAT;
        ad.Width = sizeX;
        ad.Height = sizeY;
        ad.Depth = sizeZ;
        ad.NumChannels = 1;
        cuArray3DCreate(array, ad);

        CUDA_MEMCPY3D copy = new CUDA_MEMCPY3D();
        copy.srcMemoryType = CUmemorytype.CU_MEMORYTYPE_HOST;
        copy.srcHost = Pointer.to(src._data);
        copy.srcPitch = sizeX * Sizeof.FLOAT;
        copy.srcHeight = sizeY;
        copy.dstMemoryType = CUmemorytype.CU_MEMORYTYPE_ARRAY;
        copy.dstArray = array;
        copy.dstHeight = sizeX;
        copy.WidthInBytes = sizeX * Sizeof.FLOAT;
        copy.Height = sizeY;
        copy.Depth = sizeZ;
        cuMemcpy3D(copy);

        CUtexref texref = new CUtexref();
        cuModuleGetTexRef(texref, module, texName);
        cuTexRefSetFilterMode(texref, CU_TR_FILTER_MODE_POINT);
        cuTexRefSetAddressMode(texref, 0, CU_TR_ADDRESS_MODE_CLAMP);
        cuTexRefSetAddressMode(texref, 1, CU_TR_ADDRESS_MODE_CLAMP);
        cuTexRefSetAddressMode(texref, 2, CU_TR_ADDRESS_MODE_CLAMP);
        //cuTexRefSetFlags(texref, CU_TRSF_NORMALIZED_COORDINATES);
        cuTexRefSetFormat(texref, CU_AD_FORMAT_FLOAT, 1);
        cuTexRefSetArray(texref, array, CU_TRSA_OVERRIDE_FORMAT);
	}
	
	
	private void cp2gpu(IntArray2D src, String texName) {
		int sizeX = src.size()[0];
		int sizeY = src.size()[1];
		CUarray array = new CUarray();
        CUDA_ARRAY_DESCRIPTOR ad = new CUDA_ARRAY_DESCRIPTOR();
        ad.Format = CU_AD_FORMAT_SIGNED_INT32;
        ad.Width = sizeX;
        ad.Height = sizeY;
        ad.NumChannels = 1;
        cuArrayCreate(array, ad);

        CUDA_MEMCPY2D copyHD = new CUDA_MEMCPY2D();
        copyHD.srcMemoryType = CUmemorytype.CU_MEMORYTYPE_HOST;
        copyHD.srcHost = Pointer.to(src._data);
        copyHD.srcPitch = sizeX * Sizeof.INT;
        copyHD.dstMemoryType = CUmemorytype.CU_MEMORYTYPE_ARRAY;
        copyHD.dstArray = array;
        copyHD.WidthInBytes = sizeX * Sizeof.INT;
        copyHD.Height = sizeY;
        cuMemcpy2D(copyHD);

        CUtexref texref = new CUtexref();
        cuModuleGetTexRef(texref, module, texName);
        cuTexRefSetFilterMode(texref, CU_TR_FILTER_MODE_POINT);
        cuTexRefSetAddressMode(texref, 0, CU_TR_ADDRESS_MODE_CLAMP);
        cuTexRefSetAddressMode(texref, 1, CU_TR_ADDRESS_MODE_CLAMP);
        //cuTexRefSetFlags(texref, CU_TRSF_NORMALIZED_COORDINATES);
        cuTexRefSetFormat(texref, CU_AD_FORMAT_SIGNED_INT32, 1);
        cuTexRefSetArray(texref, array, CU_TRSA_OVERRIDE_FORMAT);
	}
	


}
