#include "math.h"
#define SMALLEST_FLOAT 1.175494351E-38
extern "C"
__global__ void transMatrixCalc(int n, double* ad, double* bd, double* ed, double* cd,
									double bl, double catRate, double apRate, int catNum) {
    __shared__ double as[32][32];
    __shared__ double bs[32][32];
	__shared__ double es[32];

    int tx = threadIdx.x;
    int ty = threadIdx.y;
    
    int x = (blockIdx.x * blockDim.x) + tx;
    int y = (blockIdx.y * blockDim.y) + ty;
        
    double v = 0.0;
    
    int yn = y * n;
    int s = (n + 31) / 32;
	//int wholeBlocks = n/32;
    for(int m=0; m<s; m++) {
		
		int m32 = m * 32;

		as[ty][tx] = ad[yn + (m32 + tx)];
		bs[ty][tx] = bd[(m32 + ty) * n + x];
		es[tx] = ed[m32 + tx];

        __syncthreads();

		//skratiti broj iteracija
		for(int i=0; i+m32<n && i<32; i++) {
			double eigenExponent = exp(bl * catRate * apRate * es[i]);
			v += as[ty][i]* eigenExponent * bs[i][tx];
		}

        __syncthreads();
    }
    if(x < n && y < n) cd[catNum*n*n + yn + x] = v;    
}