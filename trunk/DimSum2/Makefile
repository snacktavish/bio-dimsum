CXXFLAGS =	-m64 -O3 -g -Wall -Wstrict-aliasing -fno-rtti -I/opt/local/include -Ilibxsd -I/usr/local/cuda/include
LDFLAGS =	-lxerces-c -lcudart -lcuda
OBJS =		src/DimSum.o src/Node.o src/XYFunction.o src/Pfunction.o src/DispersalFunctions.o src/ConfigFile.o\
			src/Config.o src/Random.o src/BMPImage.o src/OutputFunction.o src/VisualOutput.o src/TreeOutput.o\
			src/LocationsOutput.o src/Timer.o src/BMPOutput.o
TARGET =	DimSum

$(TARGET):	$(OBJS)
	/usr/local/cuda/bin/nvcc -m64 -arch sm_21 -o src/kernel.o -c src/kernel.cu 
	$(CXX) -o $(TARGET) $(OBJS) src/kernel.o -L/usr/local/cuda/lib -L/opt/local/lib $(LDFLAGS)

all:	$(TARGET)

clean:
	rm -f src/kernel.o $(OBJS) $(TARGET)
