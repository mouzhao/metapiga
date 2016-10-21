#include "math.h"
#define SMALLEST_FLOAT 1.175494351E-38
#define MAX_ELEMENTS_PER_BLOCK 2048
#define NUM_BANKS 32
#define LOG_NUM_BANKS 5

#ifdef ZERO_BANK_CONFLICTS
#define CONFLICT_FREE_OFFSET(n)\
		((n) >> NUM_BANKS + (n) >> (2 * LOG_NUM_BANKS))
#else
#define CONFLICT_FREE_OFFSET(n)((n) >> LOG_NUM_BANKS)
#endif

extern "C"
__global__ void reduceStates(int numCategories, int numStates, int numSitesWithPadding, int numSites,
                    double pInv, double* equiFreq,
                    float* sequence, double* ufScaling){
    

    //among-site rate heterogenity category index
    int categoryIdx = blockIdx.y;
	
    // sequence site index
    int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	// site index within current block
	int siteBlockIdx = threadIdx.x; 
    // index of the ancestral state at the current node
    int stateIdx = threadIdx.y;



	__shared__ float partialLikelihoods[1024];
	__shared__ double sharedDoubleBuffer[1024];
	
	// Prefetching partial likelihoods
	if(siteIdx < numSitesWithPadding){
		partialLikelihoods[threadIdx.x + blockDim.x * threadIdx.y] = sequence[siteIdx + stateIdx*numSitesWithPadding + categoryIdx*numStates*numSitesWithPadding];
	}
	
	
	if(siteIdx < numSites){	
		sharedDoubleBuffer[stateIdx * blockDim.x + siteBlockIdx] = partialLikelihoods[stateIdx * blockDim.x + siteBlockIdx] * equiFreq[stateIdx]
											* ((1.0 - pInv)/(double)(numCategories)) * ufScaling[categoryIdx * numSitesWithPadding + siteIdx];
	}
    
	__syncthreads();
	
	// Reduce states here
	if(stateIdx == 0 && siteIdx < numSites){
		double cellLikelihoodAccumulator = 0;
		for(int st = 0; st < numStates; st++){
			cellLikelihoodAccumulator += sharedDoubleBuffer[st * blockDim.x + siteBlockIdx];
		}
		ufScaling[categoryIdx * numSitesWithPadding + siteIdx] = cellLikelihoodAccumulator;
	}
}

extern "C"
__global__ void reduceCategories(int numCategories, int numSites, int numSitesWithPadding, double pInv, double* ufScaling, double* invSites, double* weights){
	int categoryIdx = threadIdx.y;
	int siteIdx = threadIdx.x + blockDim.x * blockIdx.x;
	int siteBlockIdx = threadIdx.x;
	
	__shared__ double sharedDoubleBuffer[1024];
	
	if(siteIdx < numSites){
		sharedDoubleBuffer[categoryIdx * blockDim.x + siteBlockIdx] = ufScaling[categoryIdx * numSitesWithPadding + siteIdx];
	}
	
	__syncthreads();
	
	if(categoryIdx == 0 && siteIdx < numSites){
		for(int cat = 1; cat < numCategories; cat++){
			sharedDoubleBuffer[siteBlockIdx] += sharedDoubleBuffer[cat * blockDim.x + siteBlockIdx];
		}
		double siteLikelihoodInv = invSites[siteIdx] * pInv;
		ufScaling[siteIdx] = log(sharedDoubleBuffer[siteBlockIdx] + siteLikelihoodInv) * weights[siteIdx];
	}
	
}

extern "C"
__global__ void reduceSites(double* g_odata, double* g_idata, int n, double* debug){
	
	__shared__ double temp[2115];
	int thid = threadIdx.x;
	int offset = 1;
	
	int ai = thid;  
	int bi = thid + (MAX_ELEMENTS_PER_BLOCK/2);  
	int bankOffsetA = CONFLICT_FREE_OFFSET(ai);  
	int bankOffsetB = CONFLICT_FREE_OFFSET(bi);

	//Copy data from global memory to shared memory and apply
	// padding for sizes that are not exponents of 2
	
	int blockOffset = MAX_ELEMENTS_PER_BLOCK * blockIdx.x;
	
	if((blockOffset + ai) < n){
		temp[ai + bankOffsetA] = g_idata[blockOffset + ai];
	}else{
		temp[ai + bankOffsetA] = 0;
	}
	
	if((blockOffset + bi) < n){
		temp[bi + bankOffsetB] = g_idata[blockOffset + bi];
	}else{
		temp[bi + bankOffsetB] = 0;
	}
	
	for(int d = MAX_ELEMENTS_PER_BLOCK >> 1; d > 0; d >>= 1){
		__syncthreads();
		if(thid < d){
			int ai = offset * (2 * thid + 1) - 1;
			int bi = offset * (2 * thid + 2) - 1;
			ai += CONFLICT_FREE_OFFSET(ai);
			bi += CONFLICT_FREE_OFFSET(bi);
			
			temp[bi] += temp[ai];
		}
		offset *= 2;
	}
	
	if(thid == 0){
		g_idata[blockIdx.x] = temp[MAX_ELEMENTS_PER_BLOCK - 1 + CONFLICT_FREE_OFFSET(MAX_ELEMENTS_PER_BLOCK - 1)];
	}	
	__syncthreads();
	if(thid == 0 && blockIdx.x == 0 && gridDim.x == 1) g_odata[0] = g_idata[0];
}